[![Build Status](https://travis-ci.org/DDTH/ddth-mappings.svg?branch=master)](https://travis-ci.org/DDTH/ddth-mappings)

# ddth-mappings

DDTH's one-to-one, many-to-one, many-to-many mappings library.

By Thanh Ba Nguyen (btnguyen2k (at) gmail.com).

Project home: [https://github.com/DDTH/ddth-mappings](https://github.com/DDTH/ddth-mappings)


## License

See LICENSE.txt for details. Copyright (c) 2017 Thanh Ba Nguyen.

Third party libraries are distributed under their own licenses.


## Installation

Latest release version: `0.1.0`. See [RELEASE-NOTES.md](RELEASE-NOTES.md).

Maven dependency: if only a sub-set of `ddth-mappings` functionality is used, choose the corresponding
dependency artifact(s) to reduce the number of unused jar files.

*ddth-mappings-core*: mapping API & in-memory storage, all other dependencies are *optional*.

```xml
<dependency>
	<groupId>com.github.ddth</groupId>
	<artifactId>ddth-mappings-core</artifactId>
	<version>0.1.0</version>
</dependency>
```

*ddth-mappings-cql*: include all *ddth-mappings-core* and C*QL dependencies (for Cassandra storage).

```xml
<dependency>
    <groupId>com.github.ddth</groupId>
    <artifactId>ddth-mappings-cql</artifactId>
    <version>0.1.0</version>
    <type>pom</type>
</dependency>
```

## Introduction

This library provides API to map from an object to a target (`object -> target`) and vice versa (`target -> object`).

## Mapping types

`ddth-mappings` supports 3 mapping types:

- One-One (OO) mapping: `user-name <-> user-account` is an example of OO mapping.
- Many-One (MO) mapping: `phone-number <-> user-account` is an example of MO mapping.
- Many-Many (MM) mapping: `student <-> class` is an example of MM mapping.

## Storage types

`ddth-mappings` supports 2 mapping types:

- In-memory: store mapping data in memory, suitable for testing or small temporary data.
- Cassandra: store mapping data in Apache Cassandra (accessed via C*QL), suitable for big and long-lasting data.

## Data types

Currently `ddth-mappings` support only integers and strings as data types (both `object` and `target`).

- Integer types: `byte`, `short`, `int`, `long`
- `String`

## Namespace

Mappings are grouped into namespaces. Each namespace has a unique name throughout the storage.

## Usage

Obtain an instance of `IMappingDao`: in-memory mapping DAOs

```java
// in-memory one-one mapping dao: mapping String with String
IMappingDao<String, String> daoOO = new InmemMappingOneOneDao<>(String.class, String.class).init();

// in-memory many-one mapping dao: mapping Integer with String
IMappingDao<Integer, String> daoMO = new InmemMappingManyOneDao<>(Integer.class, String.class).init();

// in-memory many-many mapping dao: mapping Long with Integer
IMappingDao<Long, Integer> daoMM = new InmemMappingManyManyDao<>(Long.class, Integer.class).init();
```

Obtain an instance of `IMappingDao`: C*QL/Cassandra mapping DAOs:

```java
/* first, build a CqlDelegator instance */
CqlDelegator cqlDelegator = new CqlDelegator();
cqlDelegator.setHostsAndPorts("localhost:9042");
cqlDelegator.setUsername("cassandra");
cqlDelegator.setPassword("secretpassword");
cqlDelegator.setKeyspace("ddth-mappings");
cqlDelegator.setTableStats("mappings_stats");
cqlDelegator.init();

/* then, build the C*QL dao instance */

// C*QL one-one mapping dao: mapping String with String
// OO mappings are stored in one single table
IMappingDao<String, String> daoOO = new CqlMappingOneOneDao<>(String.class, String.class)
    .setCqlDelegator(cqlDelegator)
    .setTableData("mapoo_data")
    .init();

// C*QL many-one mapping dao: mapping Integer with String
// MO mappings are stored in 2 column families/tables:
// - 1 CF/table for object -> target mappings
// - 1 CF/table for target -> object mappings
IMappingDao<Integer, String> daoMO = new CqlMappingManyOneDao<>(Integer.class, String.class)
    .setCqlDelegator(cqlDelegator)
    .setTableObjTarget("mapmo_objtarget")
    .setTableTargetObj("mapmo_targetobj")
    .init();

// C*QL one-one mapping dao: mapping Long with Integer
// MM mappings are stored in one single table
IMappingDao<Long, Integer> daoMM = new CqlMappingOneOneDao<>(Long.class, Integer.class)
    .setCqlDelegator(cqlDelegator)
    .setTableData("mapmm_data")
    .init();
```

Map `object -> target` (mapping `target -> object` will be automatically handled):

```java
String NAMESPACE = "my-namespace";

String obj = "username";
Stirng target = "userid";
MappingsUtils.DaoResult result = daoOO.map(NAMESPACE, obj, target);

int obj = 1234;
String target = "abc"
MappingsUtils.DaoResult result = daoMO.map(NAMESPACE, obj, target);

long obj = 5678L;
int target = 123;
MappingsUtils.DaoResult result = daoMM.map(NAMESPACE, obj, target);
```

Unmap an existing mapping:

```java
MappingsUtils.DaoResult result = daoOO.unmap(NAMESPACE, "username", "userid");

MappingsUtils.DaoResult result = daoMO.unmap(NAMESPACE, 1234, "abc");

MappingsUtils.DaoResult result = daoMM.unmap(NAMESPACE, 5678L, 123);
```

Get all targets that an object is mapped to:

```java
Collection<MappingBo<String, String>> mappings = daoOO.getMappingsForObject(NAMESPACE, "username");

Collection<MappingBo<Integer, String>> mappings = daoMO.getMappingsForObject(NAMESPACE, 1234);

Collection<MappingBo<Long, Integer>> mappings = daoMM.getMappingsForObject(NAMESPACE, 5678L);
```

Get all objects that a target is mapped to:

```java
Collection<MappingBo<String, String>> mappings = daoOO.getMappingsForTarget(NAMESPACE, "userid");

Collection<MappingBo<Integer, String>> mappings = daoMO.getMappingsForTarget(NAMESPACE, "abc");

Collection<MappingBo<Long, Integer>> mappings = daoMM.getMappingsForTarget(NAMESPACE, 123);
```

Count number of items:

```java
Map<String, Long> stats = dao.getStats(NAMESPACE);

Long numItems = stats.get(IMappingDao.STATS_KEY_TOTAL_ITEMS);

Long numObjects = stats.get(IMappingDao.STATS_KEY_TOTAL_OBJS);

Long numTargets = stats.get(IMappingDao.STATS_KEY_TOTAL_TARGETS);
```

## Schema for C*QL storage

```sql
CREATE TABLE mappings_stats (
    m_mapping               VARCHAR,
    m_namespace             VARCHAR,
    m_key                   VARCHAR,
    m_value                 COUNTER,
    PRIMARY KEY (m_mapping, m_namespace, m_key)
) WITH COMPACT STORAGE;

-- column family to store 1-1 mappings
CREATE TABLE mapoo_data (
    m_namespace             VARCHAR,
    m_type                  VARCHAR,
    m_key                   VARCHAR,
    m_data                  BLOB,
    PRIMARY KEY (m_namespace, m_type, m_key)
) WITH COMPACT STORAGE;

-- column family to store n-n mappings
CREATE TABLE mapmm_data (
    m_namespace             VARCHAR,
    m_type                  VARCHAR,
    m_key                   VARCHAR,
    m_value                 VARCHAR,
    m_data                  BLOB,
    PRIMARY KEY (m_namespace, m_type, m_key, m_value)
) WITH COMPACT STORAGE;

-- n-1: column family to store object -> target mappings
CREATE TABLE mapmo_objtarget (
    m_namespace             VARCHAR,
    m_object                VARCHAR,
    m_data                  BLOB,
    PRIMARY KEY (m_namespace, m_object)
) WITH COMPACT STORAGE;

-- n-1: column family to store target -> object mappings
CREATE TABLE mapmo_targetobj (
    m_namespace             VARCHAR,
    m_target                VARCHAR,
    m_object                VARCHAR,
    m_data                  BLOB,
    PRIMARY KEY (m_namespace, m_target, m_object)
) WITH COMPACT STORAGE;
```
