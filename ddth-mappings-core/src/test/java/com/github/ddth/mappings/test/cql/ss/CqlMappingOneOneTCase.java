package com.github.ddth.mappings.test.cql.ss;

import com.github.ddth.mappings.IMappingDao;
import com.github.ddth.mappings.cql.CqlDelegator;
import com.github.ddth.mappings.cql.CqlMappingOneOneDao;
import com.github.ddth.mappings.test.BaseMappingOneOneTCase;
import com.github.ddth.mappings.test.cql.CqlTestUtils;

import junit.framework.Test;
import junit.framework.TestSuite;

public class CqlMappingOneOneTCase extends BaseMappingOneOneTCase<String, String> {

    public static Test suite() {
        return new TestSuite(CqlMappingOneOneTCase.class);
    }

    @Override
    protected IMappingDao<String, String> initDaoInstance() throws Exception {
        String hostAndPort = System.getProperty("cassandra.hostAndPort", "localhost:9042");
        String user = System.getProperty("cassandra.user", "");
        String password = System.getProperty("cassandra.pwd", "");
        String keyspace = System.getProperty("cassandra.keyspace", "test");

        CqlDelegator cqlDelegator = new CqlDelegator();
        cqlDelegator.setHostsAndPorts(hostAndPort);
        cqlDelegator.setUsername(user);
        cqlDelegator.setPassword(password);
        cqlDelegator.setKeyspace(keyspace);
        cqlDelegator.setTableStats("ss_mappings_stats");
        cqlDelegator.init();

        CqlTestUtils.loadAndRunCqlScript(cqlDelegator.getSession(), "/test_initscript_ss.cql.sql");

        CqlMappingOneOneDao<String, String> mappingsDao = new CqlMappingOneOneDao<>(String.class,
                String.class);
        mappingsDao.setCqlDelegator(cqlDelegator).setTableData("ss_mapoo_data").init();
        return mappingsDao;
    }

    @Override
    protected void destroyDaoInstance(IMappingDao<String, String> mappingsDao) {
        ((CqlMappingOneOneDao<?, ?>) mappingsDao).destroy();
    }

    @Override
    protected String genObject(int index) {
        return "object-" + index;
    }

    @Override
    protected String genTarget(int index) {
        return "target-" + index;
    }
}
