CREATE TABLE shift_templates
(
    id                UUID PRIMARY KEY,
    name              VARCHAR(100)             NOT NULL,
    start_time        TIME                     NOT NULL, -- '08:00:00'
    end_time          TIME                     NOT NULL, -- '16:00:00'
    duration_in_hours DOUBLE PRECISION         NOT NULL, -- Nöbetin kaç saat sürdüğü
    is_night_shift    BOOLEAN DEFAULT false,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Bir personelin sahip olması gereken yetkinlikleri/sertifikaları tutar (Kıdemli, Sarı Alan Sorumlusu vb.)
CREATE TABLE qualifications
(
    id          UUID PRIMARY KEY,
    name        VARCHAR(100) UNIQUE      NOT NULL,
    description TEXT,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Personellerin hangi yetkinliklere sahip olduğunu belirten ara tablo (Many-to-Many)
CREATE TABLE member_qualifications
(
    member_id        UUID NOT NULL,
    qualification_id UUID NOT NULL,
    PRIMARY KEY (member_id, qualification_id),
    CONSTRAINT fk_member FOREIGN KEY (member_id) REFERENCES members (id) ON DELETE CASCADE,
    CONSTRAINT fk_qualification FOREIGN KEY (qualification_id) REFERENCES qualifications (id) ON DELETE CASCADE
);

-- Belirli bir lokasyondaki, belirli bir şablonun, hangi yetkinlikten kaç kişi gerektirdiğini tutar
CREATE TABLE shift_requirements
(
    id                    UUID PRIMARY KEY,
    location_id           UUID    NOT NULL,
    shift_template_id     UUID    NOT NULL,
    qualification_id      UUID, -- NULL olabilir, "herhangi bir personel" anlamına gelir
    required_member_count INTEGER NOT NULL,
    CONSTRAINT fk_location FOREIGN KEY (location_id) REFERENCES locations (id) ON DELETE CASCADE,
    CONSTRAINT fk_shift_template FOREIGN KEY (shift_template_id) REFERENCES shift_templates (id) ON DELETE CASCADE,
    CONSTRAINT fk_qualification FOREIGN KEY (qualification_id) REFERENCES qualifications (id) ON DELETE SET NULL
);

-- Planlanmış ve bir personele atanmış gerçek nöbetleri tutan ana tablo
CREATE TABLE scheduled_shifts
(
    id                UUID PRIMARY KEY,
    member_id         UUID                     NOT NULL,
    location_id       UUID                     NOT NULL,
    shift_template_id UUID                     NOT NULL,
    shift_date        DATE                     NOT NULL,
    start_datetime    TIMESTAMP WITH TIME ZONE NOT NULL,
    end_datetime      TIMESTAMP WITH TIME ZONE NOT NULL,
    status            VARCHAR(50)              NOT NULL, -- 'CONFIRMED', 'PENDING'
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_member FOREIGN KEY (member_id) REFERENCES members (id) ON DELETE CASCADE,
    CONSTRAINT fk_location FOREIGN KEY (location_id) REFERENCES locations (id) ON DELETE CASCADE,
    CONSTRAINT fk_shift_template FOREIGN KEY (shift_template_id) REFERENCES shift_templates (id) ON DELETE CASCADE
);

-- Personelin izin kayıtlarını tutar
CREATE TABLE leave_records
(
    id         UUID PRIMARY KEY,
    member_id  UUID                     NOT NULL,
    leave_type VARCHAR(50)              NOT NULL, -- 'ANNUAL_LEAVE', 'SICK_LEAVE'
    start_date DATE                     NOT NULL,
    end_date   DATE                     NOT NULL,
    reason     TEXT,
    status     VARCHAR(50)              NOT NULL, -- 'APPROVED', 'REQUESTED'
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_member FOREIGN KEY (member_id) REFERENCES members (id) ON DELETE CASCADE
);

-- Kurumun tanımlayacağı temel sayısal kuralları tutar
CREATE TABLE rule_configurations
(
    rule_key    VARCHAR(100) PRIMARY KEY,
    rule_value  VARCHAR(255)             NOT NULL,
    description TEXT,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL
);