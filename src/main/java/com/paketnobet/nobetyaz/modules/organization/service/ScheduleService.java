package com.paketnobet.nobetyaz.modules.organization.service;

import com.paketnobet.nobetyaz.core.exception.ResourceNotFoundException;
import com.paketnobet.nobetyaz.core.exception.RuleViolationException;
import com.paketnobet.nobetyaz.modules.organization.dto.ScheduledShiftResponse;
import com.paketnobet.nobetyaz.modules.organization.model.entity.*;
import com.paketnobet.nobetyaz.modules.organization.model.enums.EDayType;
import com.paketnobet.nobetyaz.modules.organization.model.enums.EShiftStatus;
import com.paketnobet.nobetyaz.modules.organization.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private final ScheduledShiftRepository scheduledShiftRepository;
    private final ShiftRequirementRepository shiftRequirementRepository;
    private final MemberRepository memberRepository;
    private final ScheduleValidatorService scheduleValidator;
    private final HolidayRepository holidayRepository;

    @Transactional
    public void generateScheduleForMonth(int year, int month) {
        log.info("generateScheduleForMonth (Yeni Tatil Mantığı) çağrıldı: Yıl={}, Ay={}", year, month);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        Map<LocalDate, EDayType> holidayMap = holidayRepository.findByHolidayDateBetween(startDate, endDate)
                .stream()
                .collect(Collectors.toMap(Holiday::getHolidayDate, Holiday::getHolidayType));
        log.info("{} ayı için {} adet tatil/özel gün kaydı bulundu.", yearMonth, holidayMap.size());

        List<ShiftRequirement> allRequirements = shiftRequirementRepository.findAll();
        if (allRequirements.isEmpty()) {
            log.warn("Hiç nöbet gereksinimi tanımlanmadığı için nöbet oluşturulamıyor!");
            throw new RuleViolationException(
                    "Boş nöbet oluşturulamadı! Lütfen önce en az bir 'Nöbet Gereksinimi' tanımlayın."
            );
        }
        log.info("Toplam {} adet nöbet gereksinimi bulundu.", allRequirements.size());

        List<ScheduledShift> shiftsToCreate = new ArrayList<>();
        List<ScheduledShift> shiftsToDelete = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            final LocalDate currentDate = date;

            EDayType currentDayType;
            if (holidayMap.containsKey(currentDate)) {
                currentDayType = holidayMap.get(currentDate); // Örn: PUBLIC_HOLIDAY
            } else {
                DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
                if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                    currentDayType = EDayType.WEEKEND;
                } else {
                    currentDayType = EDayType.WEEKDAY;
                }
            }
            // log.info("Tarih: {} -> Gün Tipi: {}", currentDate, currentDayType); // Detaylı loglama için

            List<ShiftRequirement> applicableRequirements = allRequirements.stream()
                    .filter(req -> req.getLocation().isActive())
                    .filter(req -> {
                        EDayType applyOn = req.getApplyOn();
                        return applyOn == currentDayType || applyOn == EDayType.ALL_DAYS;
                    })
                    .toList();

            if (applicableRequirements.isEmpty() && currentDate.isBefore(LocalDate.now().plusDays(1))) {
                log.warn("Tarih: {} (Tip: {}) için UYGUN GEREKSİNİM KURALI BULUNAMADI.", currentDate, currentDayType);
                continue;
            }

            applicableRequirements.forEach(req -> {
                int requiredCount = req.getRequiredMemberCount();
                Qualification reqQualification = req.getQualification();
                String qualName = reqQualification != null ? reqQualification.getName() : "Herhangi Bir Personel";

                List<ScheduledShift> existingShifts;
                if (reqQualification != null) {
                    existingShifts = scheduledShiftRepository
                            .findByLocationIdAndShiftTemplateIdAndShiftDateAndRequiredQualificationId(
                                    req.getLocation().getId(),
                                    req.getShiftTemplate().getId(),
                                    currentDate,
                                    reqQualification.getId()
                            );
                } else {
                    existingShifts = scheduledShiftRepository
                            .findByLocationIdAndShiftTemplateIdAndShiftDateAndRequiredQualificationIdIsNull(
                                    req.getLocation().getId(),
                                    req.getShiftTemplate().getId(),
                                    currentDate
                            );
                }
                int existingCount = existingShifts.size();
                int delta = requiredCount - existingCount;

                if (delta > 0) {
                    log.info("Tarih: {}, Gereksinim: {} ({} kişi). Mevcut: {}. EKLENİYOR: {}",
                            currentDate, qualName, requiredCount, existingCount, delta);
                    for (int i = 0; i < delta; i++) {
                        shiftsToCreate.add(ScheduledShift.builder()
                                .location(req.getLocation())
                                .shiftTemplate(req.getShiftTemplate())
                                .member(null)
                                .requiredQualification(reqQualification)
                                .shiftDate(currentDate)
                                .startDatetime(currentDate.atTime(req.getShiftTemplate().getStartTime()).atZone(ZoneId.systemDefault()).toInstant())
                                .endDatetime(currentDate.atTime(req.getShiftTemplate().getEndTime()).atZone(ZoneId.systemDefault()).toInstant())
                                .status(EShiftStatus.OPEN)
                                .applyOn(req.getApplyOn())
                                .build());
                    }
                } else if (delta < 0) {
                    int countToDelete = Math.abs(delta);
                    log.info("Tarih: {}, Gereksinim: {} ({} kişi). Mevcut: {}. SİLİNECEK: {}",
                            currentDate, qualName, requiredCount, existingCount, countToDelete);
                    List<ScheduledShift> emptyShiftsToDelete = existingShifts.stream()
                            .filter(shift -> shift.getMember() == null)
                            .limit(countToDelete)
                            .toList();
                    shiftsToDelete.addAll(emptyShiftsToDelete);
                } else {
                    log.info("Tarih: {}, Gereksinim: {} ({} kişi). Mevcut: {}. Sayı tamam.",
                            currentDate, qualName, requiredCount, existingCount);
                }
            });
        }

        if (!shiftsToDelete.isEmpty()) {
            scheduledShiftRepository.deleteAllInBatch(shiftsToDelete);
            log.info("{} adet fazla boş nöbet başarıyla veritabanından silindi.", shiftsToDelete.size());
        }
        if (!shiftsToCreate.isEmpty()) {
            scheduledShiftRepository.saveAll(shiftsToCreate);
            log.info("{} adet yeni boş nöbet başarıyla veritabanına kaydedildi.", shiftsToCreate.size());
        }
        if (shiftsToCreate.isEmpty() && shiftsToDelete.isEmpty()) {
            log.info("Veritabanında herhangi bir değişiklik yapılmadı (Çizelge güncel).");
        }
    }

    public List<ScheduledShiftResponse> findByPeriod(LocalDate startDate, LocalDate endDate) {
        List<ScheduledShift> shifts = scheduledShiftRepository.findByShiftDateBetween(startDate, endDate);
        return shifts.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public ScheduledShiftResponse assignMember(UUID shiftId, UUID memberId) {
        ScheduledShift shiftToAssign = scheduledShiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled Shift not found with id: " + shiftId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));

        if (shiftToAssign.getMember() != null) {
            throw new IllegalStateException("This shift is already assigned to another member.");
        }

        scheduleValidator.validateAssignment(member, shiftToAssign);

        shiftToAssign.setMember(member);
        shiftToAssign.setStatus(EShiftStatus.CONFIRMED);
        Optional<Holiday> holidayOpt = holidayRepository.findByHolidayDate(shiftToAssign.getShiftDate());
        if (holidayOpt.isPresent()) {
            EDayType type = holidayOpt.get().getHolidayType();
            if (type == EDayType.RELIGIOUS_HOLIDAY || type == EDayType.PUBLIC_HOLIDAY) {
                log.info("Manuel atama bir bayram gününe yapıldı. Personel {} (ID: {}) hafızası güncelleniyor.",
                        member.getFirstName(), member.getId());
                member.setLastWorkedReligiousHolidayDate(shiftToAssign.getShiftDate());
                memberRepository.save(member);
            }
        }

        scheduledShiftRepository.save(shiftToAssign);
        return toResponse(shiftToAssign);
    }

    @Transactional(readOnly = true)
    public ScheduledShiftResponse findShiftById(UUID shiftId) {
        ScheduledShift shift = scheduledShiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled Shift not found with id: " + shiftId));
        return toResponse(shift);
    }

    @Transactional(readOnly = true)
    public ByteArrayInputStream exportScheduleToExcel(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy - EEEE", new java.util.Locale("tr", "TR"));

        List<ScheduledShift> shifts = scheduledShiftRepository.findByShiftDateBetweenOrderByShiftDateAscShiftTemplateStartTimeAsc(startDate, endDate);
        log.info("{} ayı için {} nöbet kaydı Excel (Gruplanmış Liste) için çekildi.", yearMonth, shifts.size());

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {

            Sheet sheet = workbook.createSheet(yearMonth.toString() + " Çizelgesi");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Font dayHeaderFont = workbook.createFont();
            dayHeaderFont.setBold(true);
            dayHeaderFont.setFontHeightInPoints((short) 12);
            CellStyle dayHeaderCellStyle = workbook.createCellStyle();
            dayHeaderCellStyle.setFont(dayHeaderFont);
            dayHeaderCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            dayHeaderCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Font weekendDayHeaderFont = workbook.createFont();
            weekendDayHeaderFont.setBold(true);
            weekendDayHeaderFont.setFontHeightInPoints((short) 12);
            weekendDayHeaderFont.setColor(IndexedColors.DARK_RED.getIndex());
            CellStyle weekendDayHeaderCellStyle = workbook.createCellStyle();
            weekendDayHeaderCellStyle.setFont(weekendDayHeaderFont);
            weekendDayHeaderCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            weekendDayHeaderCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] headers = { "Tarih", "Lokasyon", "Nöbet Saati", "Personel", "Yetkinlik" };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIdx = 1;
            LocalDate currentDate = null;

            for (ScheduledShift shift : shifts) {
                if (currentDate == null || !currentDate.isEqual(shift.getShiftDate())) {
                    currentDate = shift.getShiftDate();

                    Row dayRow = sheet.createRow(rowIdx++);
                    Cell dayCell = dayRow.createCell(0);
                    dayCell.setCellValue(currentDate.format(dateFormatter));

                    sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, headers.length - 1));

                    DayOfWeek day = currentDate.getDayOfWeek();
                    if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                        dayCell.setCellStyle(weekendDayHeaderCellStyle);
                    } else {
                        dayCell.setCellStyle(dayHeaderCellStyle);
                    }
                }

                Row dataRow = sheet.createRow(rowIdx++);

                dataRow.createCell(0).setCellValue(" ");
                dataRow.createCell(1).setCellValue(shift.getLocation().getName());

                String timeStr = shift.getShiftTemplate().getStartTime().format(timeFormatter) + " - " +
                        shift.getShiftTemplate().getEndTime().format(timeFormatter);
                dataRow.createCell(2).setCellValue(timeStr);

                if (shift.getMember() != null) {
                    dataRow.createCell(3).setCellValue(shift.getMember().getFirstName() + " " + shift.getMember().getLastName());
                } else {
                    dataRow.createCell(3).setCellValue("(BOŞ)");
                }

                if (shift.getRequiredQualification() != null) {
                    dataRow.createCell(4).setCellValue(shift.getRequiredQualification().getName());
                } else {
                    dataRow.createCell(4).setCellValue("-");
                }
            }
            sheet.setColumnWidth(0, 30 * 256);
            sheet.setColumnWidth(1, 30 * 256);
            sheet.setColumnWidth(2, 18 * 256);
            sheet.setColumnWidth(3, 30 * 256);
            sheet.setColumnWidth(4, 25 * 256);
            workbook.write(out);
            log.info("Excel (Güne Göre Gruplanmış Liste) dosyası başarıyla oluşturuldu.");
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            log.error("Excel (Grup Liste) dışa aktarma hatası!", e);
            throw new RuntimeException("Excel dosyası oluşturulurken hata oluştu: ".concat(e.getMessage()));
        }
    }

    ScheduledShiftResponse toResponse(ScheduledShift shift) {
        ScheduledShiftResponse.MemberInfo memberInfo = null;
        if (shift.getMember() != null) {
            Member member = shift.getMember();

            UUID memberUserId = null;
            if (member.getUser() != null) {
                memberUserId = member.getUser().getId();
            }

            memberInfo = new ScheduledShiftResponse.MemberInfo(
                    member.getId(),
                    member.getFirstName(),
                    member.getLastName(),
                    memberUserId
            );
        }

        ScheduledShiftResponse.QualificationInfo requiredQualInfo = null;
        if (shift.getRequiredQualification() != null) {
            requiredQualInfo = new ScheduledShiftResponse.QualificationInfo(
                    shift.getRequiredQualification().getId(),
                    shift.getRequiredQualification().getName()
            );
        }

        return new ScheduledShiftResponse(
                shift.getId(),
                shift.getShiftDate(),
                shift.getShiftTemplate().getStartTime(),
                shift.getShiftTemplate().getEndTime(),
                shift.getStatus().name(),
                new ScheduledShiftResponse.LocationInfo(shift.getLocation().getId(), shift.getLocation().getName()),
                memberInfo,
                new ScheduledShiftResponse.ShiftTemplateInfo(shift.getShiftTemplate().getId(), shift.getShiftTemplate().getName()),
                requiredQualInfo
        );
    }
}