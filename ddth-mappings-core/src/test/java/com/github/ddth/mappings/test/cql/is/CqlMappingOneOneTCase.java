package com.github.ddth.mappings.test.cql.is;

import com.github.ddth.mappings.IMappingDao;
import com.github.ddth.mappings.cql.CqlDelegator;
import com.github.ddth.mappings.cql.CqlMappingOneOneDao;
import com.github.ddth.mappings.test.BaseMappingOneOneTCase;
import com.github.ddth.mappings.test.cql.CqlTestUtils;

import junit.framework.Test;
import junit.framework.TestSuite;

public class CqlMappingOneOneTCase extends BaseMappingOneOneTCase<Integer, String> {

    public static Test suite() {
        return new TestSuite(CqlMappingOneOneTCase.class);
    }

    @Override
    protected IMappingDao<Integer, String> initDaoInstance() throws Exception {
        String hostAndPort = System.getProperty("cassandra.hostAndPort", "localhost:9042");
        String user = System.getProperty("cassandra.user", "");
        String password = System.getProperty("cassandra.pwd", "");
        String keyspace = System.getProperty("cassandra.keyspace", "test");

        CqlDelegator cqlDelegator = new CqlDelegator();
        cqlDelegator.setHostsAndPorts(hostAndPort);
        cqlDelegator.setUsername(user);
        cqlDelegator.setPassword(password);
        cqlDelegator.setKeyspace(keyspace);
        cqlDelegator.setTableStats("is_mappings_stats");
        cqlDelegator.init();

        CqlTestUtils.loadAndRunCqlScript(cqlDelegator.getSession(), "/test_initscript_is.cql.sql");

        CqlMappingOneOneDao<Integer, String> mappingsDao = new CqlMappingOneOneDao<>(Integer.class,
                String.class);
        mappingsDao.setCqlDelegator(cqlDelegator).setTableData("is_mapoo_data").init();
        return mappingsDao;
    }

    @Override
    protected void destroyDaoInstance(IMappingDao<Integer, String> mappingsDao) {
        ((CqlMappingOneOneDao<?, ?>) mappingsDao).destroy();
    }

    @Override
    protected Integer genObject(int index) {
        return index;
    }

    @Override
    protected String genTarget(int index) {
        return "target-" + index;
    }
}
