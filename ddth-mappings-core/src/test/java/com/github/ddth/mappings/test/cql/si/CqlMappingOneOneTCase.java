package com.github.ddth.mappings.test.cql.si;

import com.github.ddth.mappings.IMappingDao;
import com.github.ddth.mappings.cql.CqlDelegator;
import com.github.ddth.mappings.cql.CqlMappingOneOneDao;
import com.github.ddth.mappings.test.BaseMappingOneOneTCase;
import com.github.ddth.mappings.test.cql.CqlTestUtils;

import junit.framework.Test;
import junit.framework.TestSuite;

public class CqlMappingOneOneTCase extends BaseMappingOneOneTCase<String, Integer> {

    public static Test suite() {
        return new TestSuite(CqlMappingOneOneTCase.class);
    }

    @Override
    protected IMappingDao<String, Integer> initDaoInstance() throws Exception {
        String hostAndPort = System.getProperty("cassandra.hostAndPort", "localhost:9042");
        String user = System.getProperty("cassandra.user", "");
        String password = System.getProperty("cassandra.pwd", "");
        String keyspace = System.getProperty("cassandra.keyspace", "test");

        CqlDelegator cqlDelegator = new CqlDelegator();
        cqlDelegator.setHostsAndPorts(hostAndPort);
        cqlDelegator.setUsername(user);
        cqlDelegator.setPassword(password);
        cqlDelegator.setKeyspace(keyspace);
        cqlDelegator.setTableStats("si_mappings_stats");
        cqlDelegator.init();

        CqlTestUtils.loadAndRunCqlScript(cqlDelegator.getSession(), "/test_initscript_si.cql.sql");

        CqlMappingOneOneDao<String, Integer> mappingsDao = new CqlMappingOneOneDao<>(String.class,
                Integer.class);
        mappingsDao.setCqlDelegator(cqlDelegator).setTableData("si_mapoo_data").init();
        return mappingsDao;
    }

    @Override
    protected void destroyDaoInstance(IMappingDao<String, Integer> mappingsDao) {
        ((CqlMappingOneOneDao<?, ?>) mappingsDao).destroy();
    }

    @Override
    protected String genObject(int index) {
        return "object-" + index;
    }

    @Override
    protected Integer genTarget(int index) {
        return index;
    }
}
