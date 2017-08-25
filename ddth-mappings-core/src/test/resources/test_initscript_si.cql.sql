-- Sample C* schema for string-string mappings
-- (the .sql ext is just for syntax highlighting)

-- table to store mapping stats
DROP TABLE IF EXISTS si_mappings_stats;
CREATE TABLE si_mappings_stats (
    m_mapping               VARCHAR,
    m_namespace             VARCHAR,
    m_key                   VARCHAR,
    m_value                 COUNTER,
    PRIMARY KEY (m_mapping, m_namespace, m_key)
) WITH COMPACT STORAGE;

-- One table to store 1-1 mappings
DROP TABLE IF EXISTS si_mapoo_data;
CREATE TABLE si_mapoo_data (
    m_namespace             VARCHAR,
    m_type                  VARCHAR,
    m_key                   VARCHAR,
    m_data                  BLOB,
    PRIMARY KEY (m_namespace, m_type, m_key)
) WITH COMPACT STORAGE;

-- One table to store n-n mappings
DROP TABLE IF EXISTS si_mapmm_data;
CREATE TABLE si_mapmm_data (
    m_namespace             VARCHAR,
    m_type                  VARCHAR,
    m_key                   VARCHAR,
    m_value                 VARCHAR,
    m_data                  BLOB,
    PRIMARY KEY (m_namespace, m_type, m_key, m_value)
) WITH COMPACT STORAGE;

-- Tables to store n-1 mappings: map object -> target
DROP TABLE IF EXISTS si_mapmo_objtarget;
CREATE TABLE si_mapmo_objtarget (
    m_namespace             VARCHAR,
    m_object                VARCHAR,
    m_data                  BLOB,
    PRIMARY KEY (m_namespace, m_object)
) WITH COMPACT STORAGE;

-- Tables to store n-1 mappings: map target -> object
DROP TABLE IF EXISTS si_mapmo_targetobj;
CREATE TABLE si_mapmo_targetobj (
    m_namespace             VARCHAR,
    m_target                VARCHAR,
    m_object                VARCHAR,
    m_data                  BLOB,
    PRIMARY KEY (m_namespace, m_target, m_object)
) WITH COMPACT STORAGE;
