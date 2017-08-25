package com.github.ddth.mappings.test.cql.si;

import com.github.ddth.mappings.IMappingDao;
import com.github.ddth.mappings.cql.CqlDelegator;
import com.github.ddth.mappings.cql.CqlMappingManyManyDao;
import com.github.ddth.mappings.test.BaseMappingManyManyTCase;
import com.github.ddth.mappings.test.cql.CqlTestUtils;

import junit.framework.Test;
import junit.framework.TestSuite;

public class CqlMappingManyManyTCase extends BaseMappingManyManyTCase<String, Integer> {

    public static Test suite() {
        return new TestSuite(CqlMappingManyManyTCase.class);
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

        CqlMappingManyManyDao<String, Integer> mappingsDao = new CqlMappingManyManyDao<>(
                String.class, Integer.class);
        mappingsDao.setCqlDelegator(cqlDelegator).setTableData("si_mapmm_data").init();
        return mappingsDao;
    }

    @Override
    protected void destroyDaoInstance(IMappingDao<String, Integer> mappingsDao) {
        ((CqlMappingManyManyDao<?, ?>) mappingsDao).destroy();
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
