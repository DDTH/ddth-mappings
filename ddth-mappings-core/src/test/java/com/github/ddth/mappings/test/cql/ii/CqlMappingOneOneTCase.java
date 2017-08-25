package com.github.ddth.mappings.test.cql.ii;

import com.github.ddth.mappings.IMappingDao;
import com.github.ddth.mappings.cql.CqlDelegator;
import com.github.ddth.mappings.cql.CqlMappingOneOneDao;
import com.github.ddth.mappings.test.BaseMappingOneOneTCase;
import com.github.ddth.mappings.test.cql.CqlTestUtils;

import junit.framework.Test;
import junit.framework.TestSuite;

public class CqlMappingOneOneTCase extends BaseMappingOneOneTCase<Integer, Integer> {

    public static Test suite() {
        return new TestSuite(CqlMappingOneOneTCase.class);
    }

    @Override
    protected IMappingDao<Integer, Integer> initDaoInstance() throws Exception {
        String hostAndPort = System.getProperty("cassandra.hostAndPort", "localhost:9042");
        String user = System.getProperty("cassandra.user", "");
        String password = System.getProperty("cassandra.pwd", "");
        String keyspace = System.getProperty("cassandra.keyspace", "test");

        CqlDelegator cqlDelegator = new CqlDelegator();
        cqlDelegator.setHostsAndPorts(hostAndPort);
        cqlDelegator.setUsername(user);
        cqlDelegator.setPassword(password);
        cqlDelegator.setKeyspace(keyspace);
        cqlDelegator.setTableStats("ii_mappings_stats");
        cqlDelegator.init();

        CqlTestUtils.loadAndRunCqlScript(cqlDelegator.getSession(), "/test_initscript_ii.cql.sql");

        CqlMappingOneOneDao<Integer, Integer> mappingsDao = new CqlMappingOneOneDao<>(Integer.class,
                Integer.class);
        mappingsDao.setCqlDelegator(cqlDelegator).setTableData("ii_mapoo_data").init();
        return mappingsDao;
    }

    @Override
    protected void destroyDaoInstance(IMappingDao<Integer, Integer> mappingsDao) {
        ((CqlMappingOneOneDao<?, ?>) mappingsDao).destroy();
    }

    @Override
    protected Integer genObject(int index) {
        return index;
    }

    @Override
    protected Integer genTarget(int index) {
        return index;
    }
}
