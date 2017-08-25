package com.github.ddth.mappings.test.cql.ss;

import com.github.ddth.mappings.IMappingDao;
import com.github.ddth.mappings.cql.CqlDelegator;
import com.github.ddth.mappings.cql.CqlMappingManyManyDao;
import com.github.ddth.mappings.test.BaseMappingManyManyTCase;
import com.github.ddth.mappings.test.cql.CqlTestUtils;

import junit.framework.Test;
import junit.framework.TestSuite;

public class CqlMappingManyManyTCase extends BaseMappingManyManyTCase<String, String> {

    public static Test suite() {
        return new TestSuite(CqlMappingManyManyTCase.class);
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

        CqlMappingManyManyDao<String, String> mappingsDao = new CqlMappingManyManyDao<>(
                String.class, String.class);
        mappingsDao.setCqlDelegator(cqlDelegator).setTableData("ss_mapmm_data").init();
        return mappingsDao;
    }

    @Override
    protected void destroyDaoInstance(IMappingDao<String, String> mappingsDao) {
        ((CqlMappingManyManyDao<?, ?>) mappingsDao).destroy();
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
