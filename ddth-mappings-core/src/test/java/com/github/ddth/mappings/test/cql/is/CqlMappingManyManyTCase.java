package com.github.ddth.mappings.test.cql.is;

import com.github.ddth.mappings.IMappingDao;
import com.github.ddth.mappings.cql.CqlDelegator;
import com.github.ddth.mappings.cql.CqlMappingManyManyDao;
import com.github.ddth.mappings.test.BaseMappingManyManyTCase;
import com.github.ddth.mappings.test.cql.CqlTestUtils;

import junit.framework.Test;
import junit.framework.TestSuite;

public class CqlMappingManyManyTCase extends BaseMappingManyManyTCase<Integer, String> {

    public static Test suite() {
        return new TestSuite(CqlMappingManyManyTCase.class);
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

        CqlMappingManyManyDao<Integer, String> mappingsDao = new CqlMappingManyManyDao<>(
                Integer.class, String.class);
        mappingsDao.setCqlDelegator(cqlDelegator).setTableData("is_mapmm_data").init();
        return mappingsDao;
    }

    @Override
    protected void destroyDaoInstance(IMappingDao<Integer, String> mappingsDao) {
        ((CqlMappingManyManyDao<?, ?>) mappingsDao).destroy();
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
