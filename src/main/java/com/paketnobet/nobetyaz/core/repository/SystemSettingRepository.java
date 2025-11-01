package com.paketnobet.nobetyaz.core.repository;

import com.paketnobet.nobetyaz.core.model.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> {
}
