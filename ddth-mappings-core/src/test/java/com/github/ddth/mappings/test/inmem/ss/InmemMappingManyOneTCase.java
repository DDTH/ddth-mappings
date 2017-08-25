package com.github.ddth.mappings.test.inmem.ss;

import com.github.ddth.mappings.IMappingDao;
import com.github.ddth.mappings.inmem.InmemMappingManyOneDao;
import com.github.ddth.mappings.test.BaseMappingManyOneTCase;

import junit.framework.Test;
import junit.framework.TestSuite;

public class InmemMappingManyOneTCase extends BaseMappingManyOneTCase<String, String> {

    public static Test suite() {
        return new TestSuite(InmemMappingManyOneTCase.class);
    }

    @Override
    protected IMappingDao<String, String> initDaoInstance() {
        return new InmemMappingManyOneDao<>(String.class, String.class).init();
    }

    @Override
    protected void destroyDaoInstance(IMappingDao<String, String> mappingsDao) {
        ((InmemMappingManyOneDao<?, ?>) mappingsDao).destroy();
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
