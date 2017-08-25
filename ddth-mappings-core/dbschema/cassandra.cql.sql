-- Cassandra schema for mappings
-- (The .sql ext is just for syntax highlighting)

CREATE TABLE mappings_stats (
    m_mapping               VARCHAR,
    m_namespace             VARCHAR,
    m_key                   VARCHAR,
    m_value                 COUNTER,
    PRIMARY KEY (m_mapping, m_namespace, m_key)
) WITH COMPACT STORAGE;


-- ===== Tables to store 1-1 mappings
CREATE TABLE mapoo_data (
    m_namespace             VARCHAR,
    m_type                  VARCHAR,
    m_key                   VARCHAR,
    m_data                  BLOB,
    PRIMARY KEY (m_namespace, m_type, m_key)
) WITH COMPACT STORAGE;


-- ===== Tables to store n-n mappings
CREATE TABLE mapmm_data (
    m_namespace             VARCHAR,
    m_type                  VARCHAR,
    m_key                   VARCHAR,
    m_value                 VARCHAR,
    m_data                  BLOB,
    PRIMARY KEY (m_namespace, m_type, m_key, m_value)
) WITH COMPACT STORAGE;


-- ===== Tables to store n-1 mappings
-- Map: object -> target
CREATE TABLE mapmo_objtarget (
    m_namespace             VARCHAR,
    m_object                VARCHAR,
    m_data                  BLOB,
    PRIMARY KEY (m_namespace, m_object)
) WITH COMPACT STORAGE;

-- Map: target -> objects
CREATE TABLE mapmo_targetobj (
    m_namespace             VARCHAR,
    m_target                VARCHAR,
    m_object                VARCHAR,
    m_data                  BLOB,
    PRIMARY KEY (m_namespace, m_target, m_object)
) WITH COMPACT STORAGE;
