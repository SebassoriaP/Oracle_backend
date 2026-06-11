-- =====================================
-- CLEAN START
-- =====================================

BEGIN EXECUTE IMMEDIATE 'DROP TABLE overdue_report CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE issue_log CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE timelog CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE issues CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE feature CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE sprint CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE project CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE project_member CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE users CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE role CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE admin_user CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE kpi_snapshot CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE issue_log_seq'; EXCEPTION WHEN OTHERS THEN NULL; END;
/

-- =====================================
-- TABLES
-- =====================================

CREATE TABLE role (
    id NUMBER PRIMARY KEY,
    name VARCHAR2(50) UNIQUE
);

CREATE TABLE users (
    id NUMBER PRIMARY KEY,
    name VARCHAR2(100),
    email VARCHAR2(100) UNIQUE,
    password_hash VARCHAR2(200),
    work_mode VARCHAR2(50),
    role_id NUMBER,
    manager_id NUMBER,
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP,
    status VARCHAR2(20),
    chat_id VARCHAR2(100),

    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES role(id),
    CONSTRAINT fk_user_manager FOREIGN KEY (manager_id) REFERENCES users(id)
);

CREATE TABLE admin_user (
    id NUMBER PRIMARY KEY,
    email VARCHAR2(100) UNIQUE,
    password_hash VARCHAR2(200),
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP,
    last_login TIMESTAMP,
    is_super_admin NUMBER(1)
);

CREATE TABLE project (
    id NUMBER PRIMARY KEY,
    name VARCHAR2(100),
    description VARCHAR2(300),
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP,
    status VARCHAR2(20)
);

CREATE TABLE sprint (
    id NUMBER PRIMARY KEY,
    name VARCHAR2(100),
    start_date DATE,
    end_date DATE,
    goal VARCHAR2(300),
    status VARCHAR2(20),
    project_id NUMBER,

    CONSTRAINT fk_sprint_project FOREIGN KEY (project_id) REFERENCES project(id)
);

CREATE TABLE feature (
    id NUMBER PRIMARY KEY,
    title VARCHAR2(200),
    description VARCHAR2(300),
    sprint_id NUMBER,
    priority VARCHAR2(20),
    status VARCHAR2(20),

    CONSTRAINT fk_feature_sprint FOREIGN KEY (sprint_id) REFERENCES sprint(id)
);

CREATE TABLE issues (
    id NUMBER PRIMARY KEY,
    title VARCHAR2(200) NOT NULL,
    description VARCHAR2(500),
    type VARCHAR2(20) CHECK (type IN ('TASK','BUG','TRAINING')),
    status VARCHAR2(20) DEFAULT 'open'
        CHECK (status IN ('open','in_progress','closed')),
    estimated_hours NUMBER,
    actual_hours NUMBER,
    feature_id NUMBER,
    assigned_to NUMBER,
    is_visible NUMBER(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP,
    updated_at TIMESTAMP,
    due_date DATE,
    overdue_notified NUMBER(1) DEFAULT 0 NOT NULL,

    CONSTRAINT fk_issue_feature FOREIGN KEY (feature_id) REFERENCES feature(id),
    CONSTRAINT fk_issue_user FOREIGN KEY (assigned_to) REFERENCES users(id)
);

CREATE TABLE timelog (
    id NUMBER PRIMARY KEY,
    issue_id NUMBER,
    user_id NUMBER,
    hours_logged NUMBER,
    log_date DATE,

    CONSTRAINT fk_timelog_issue FOREIGN KEY (issue_id) REFERENCES issues(id),
    CONSTRAINT fk_timelog_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE kpi_snapshot (
    id NUMBER PRIMARY KEY,
    project_id NUMBER,
    sprint_id NUMBER,
    snapshot_date DATE,

    pct_tasks_updated NUMBER,
    pct_tasks_visible NUMBER,
    visible_in_progress_count NUMBER,

    pct_tasks_with_hours NUMBER,
    delayed_tasks NUMBER,
    completed_tasks_per_dev NUMBER,

    pct_tasks_completed NUMBER,
    tasks_per_sprint NUMBER,
    estimation_accuracy NUMBER,

    CONSTRAINT fk_kpi_project FOREIGN KEY (project_id) REFERENCES project(id),
    CONSTRAINT fk_kpi_sprint FOREIGN KEY (sprint_id) REFERENCES sprint(id)
);

CREATE TABLE project_member (
    id NUMBER PRIMARY KEY,
    project_id NUMBER,
    user_id NUMBER,
    role_in_project VARCHAR2(20),
    joined_at TIMESTAMP DEFAULT SYSTIMESTAMP,

    CONSTRAINT fk_pm_project FOREIGN KEY (project_id) REFERENCES project(id),
    CONSTRAINT fk_pm_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE issue_log (
    id NUMBER PRIMARY KEY,
    issue_id NUMBER,
    field_name VARCHAR2(50),
    old_value VARCHAR2(200),
    new_value VARCHAR2(200),
    changed_by NUMBER,
    changed_at TIMESTAMP DEFAULT SYSTIMESTAMP,

    CONSTRAINT fk_log_issue FOREIGN KEY (issue_id) REFERENCES issues(id),
    CONSTRAINT fk_log_user FOREIGN KEY (changed_by) REFERENCES users(id)
);

CREATE TABLE overdue_report (
    id NUMBER PRIMARY KEY,
    issue_id NUMBER NOT NULL,

    task_title VARCHAR2(200),
    developer_name VARCHAR2(120),

    due_date TIMESTAMP,
    submitted_at TIMESTAMP DEFAULT SYSTIMESTAMP,

    reason VARCHAR2(500),

    ai_summary VARCHAR2(1000),
    ai_category VARCHAR2(30),
    severity VARCHAR2(10),

    delay_days NUMBER,
    impact_level VARCHAR2(10),
    description VARCHAR2(1000),

    recommendation VARCHAR2(1000),

    CONSTRAINT fk_overdue_issue
        FOREIGN KEY (issue_id)
        REFERENCES issues(id)
);

-- =====================================
-- SEQUENCE
-- =====================================

CREATE SEQUENCE issue_log_seq START WITH 1 INCREMENT BY 1;

-- =====================================
-- TRIGGERS
-- =====================================

CREATE OR REPLACE TRIGGER trg_issues_before_update
BEFORE UPDATE ON issues
FOR EACH ROW
BEGIN
    :NEW.updated_at := SYSTIMESTAMP;
END;
/

CREATE OR REPLACE TRIGGER trg_issues_after_insert
AFTER INSERT ON issues
FOR EACH ROW
BEGIN
    INSERT INTO issue_log(id, issue_id, field_name, old_value, new_value, changed_by)
    VALUES (issue_log_seq.NEXTVAL, :NEW.id, 'created', NULL, 'created', :NEW.assigned_to);
END;
/

CREATE OR REPLACE TRIGGER trg_issues_after_update
AFTER UPDATE ON issues
FOR EACH ROW
BEGIN
    IF :OLD.status != :NEW.status THEN
        INSERT INTO issue_log(id, issue_id, field_name, old_value, new_value, changed_by)
        VALUES (issue_log_seq.NEXTVAL, :OLD.id, 'status', :OLD.status, :NEW.status, :NEW.assigned_to);
    END IF;

    IF :OLD.assigned_to != :NEW.assigned_to THEN
        INSERT INTO issue_log(id, issue_id, field_name, old_value, new_value, changed_by)
        VALUES (issue_log_seq.NEXTVAL, :OLD.id, 'assigned_to', :OLD.assigned_to, :NEW.assigned_to, :NEW.assigned_to);
    END IF;

    IF :OLD.actual_hours != :NEW.actual_hours THEN
        INSERT INTO issue_log(id, issue_id, field_name, old_value, new_value, changed_by)
        VALUES (issue_log_seq.NEXTVAL, :OLD.id, 'actual_hours', :OLD.actual_hours, :NEW.actual_hours, :NEW.assigned_to);
    END IF;
END;
/
