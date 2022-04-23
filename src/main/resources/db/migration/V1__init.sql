CREATE TABLE clubs
(
    id               INT AUTO_INCREMENT NOT NULL,
    name             VARCHAR(255) NULL,
    address          VARCHAR(255) NULL,
    number_of_events INT NULL,
    CONSTRAINT pk_clubs PRIMARY KEY (id)
);

CREATE TABLE coaches
(
    id               INT AUTO_INCREMENT NOT NULL,
    first_name       VARCHAR(255) NULL,
    last_name        VARCHAR(255) NULL,
    year_of_birth    INT NULL,
    number_of_events INT NULL,
    CONSTRAINT pk_coaches PRIMARY KEY (id)
);

CREATE TABLE event_hours
(
    id INT AUTO_INCREMENT NOT NULL,
    _from time NULL,
    _to   time NULL,
    CONSTRAINT pk_event_hours PRIMARY KEY (id)
);

CREATE TABLE event_instance
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    title        VARCHAR(255) NULL,
    date         date NULL,
    time         time NULL,
    duration     BIGINT NULL,
    capacity     INT NULL,
    participants INT NULL,
    club_id      INT NULL,
    coach_id     INT NULL,
    CONSTRAINT pk_eventinstance PRIMARY KEY (id)
);

CREATE TABLE events
(
    id       INT AUTO_INCREMENT NOT NULL,
    title    VARCHAR(255) NULL,
    day      VARCHAR(255) NULL,
    time     time NULL,
    duration BIGINT NULL,
    club_id  INT NULL,
    coach_id INT NULL,
    CONSTRAINT pk_events PRIMARY KEY (id)
);

CREATE TABLE fill_level
(
    club_id        INT NOT NULL,
    fill_level_id  INT NOT NULL,
    fill_level_key INT NOT NULL,
    CONSTRAINT pk_fill_level PRIMARY KEY (club_id, fill_level_key)
);

CREATE TABLE opening_hours
(
    id INT AUTO_INCREMENT NOT NULL,
    _from time NULL,
    _to   time NULL,
    CONSTRAINT pk_opening_hours PRIMARY KEY (id)
);

CREATE TABLE schedules
(
    id       INT AUTO_INCREMENT NOT NULL,
    title    VARCHAR(255) NULL,
    day      VARCHAR(255) NULL,
    time     time NULL,
    duration BIGINT NULL,
    capacity INT NULL,
    club_id  INT NULL,
    coach_id INT NULL,
    CONSTRAINT pk_schedules PRIMARY KEY (id)
);

CREATE TABLE when_open
(
    club_id          INT NOT NULL,
    opening_hours_id INT NOT NULL,
    when_open_key    INT NOT NULL,
    CONSTRAINT pk_when_open PRIMARY KEY (club_id, when_open_key)
);

ALTER TABLE fill_level
    ADD CONSTRAINT uc_fill_level_fill_level UNIQUE (fill_level_id);

ALTER TABLE when_open
    ADD CONSTRAINT uc_when_open_opening_hours UNIQUE (opening_hours_id);

ALTER TABLE fill_level
    ADD CONSTRAINT fk_fillev_on_club FOREIGN KEY (club_id) REFERENCES clubs (id);

ALTER TABLE fill_level
    ADD CONSTRAINT fk_fillev_on_event_hours FOREIGN KEY (fill_level_id) REFERENCES event_hours (id);

ALTER TABLE when_open
    ADD CONSTRAINT fk_when_open_on_club FOREIGN KEY (club_id) REFERENCES clubs (id);

ALTER TABLE when_open
    ADD CONSTRAINT fk_when_open_on_opening_hours FOREIGN KEY (opening_hours_id) REFERENCES opening_hours (id);