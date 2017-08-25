package com.github.ddth.mappings.test.inmem.ss;

import com.github.ddth.mappings.IMappingDao;
import com.github.ddth.mappings.inmem.InmemMappingManyManyDao;
import com.github.ddth.mappings.test.BaseMappingManyManyTCase;

import junit.framework.Test;
import junit.framework.TestSuite;

public class InmemMappingManyManyTCase extends BaseMappingManyManyTCase<String, String> {

    public static Test suite() {
        return new TestSuite(InmemMappingManyManyTCase.class);
    }

    @Override
    protected IMappingDao<String, String> initDaoInstance() {
        return new InmemMappingManyManyDao<>(String.class, String.class).init();
    }

    @Override
    protected void destroyDaoInstance(IMappingDao<String, String> mappingsDao) {
        ((InmemMappingManyManyDao<?, ?>) mappingsDao).destroy();
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
