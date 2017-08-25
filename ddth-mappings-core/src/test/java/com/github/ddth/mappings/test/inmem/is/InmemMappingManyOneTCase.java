package com.github.ddth.mappings.test.inmem.is;

import com.github.ddth.mappings.IMappingDao;
import com.github.ddth.mappings.inmem.InmemMappingManyOneDao;
import com.github.ddth.mappings.test.BaseMappingManyOneTCase;

import junit.framework.Test;
import junit.framework.TestSuite;

public class InmemMappingManyOneTCase extends BaseMappingManyOneTCase<Integer, String> {

    public static Test suite() {
        return new TestSuite(InmemMappingManyOneTCase.class);
    }

    @Override
    protected IMappingDao<Integer, String> initDaoInstance() {
        return new InmemMappingManyOneDao<>(Integer.class, String.class).init();
    }

    @Override
    protected void destroyDaoInstance(IMappingDao<Integer, String> mappingsDao) {
        ((InmemMappingManyOneDao<?, ?>) mappingsDao).destroy();
    }

    @Override
    protected Integer genObject(int index) {
        return Integer.valueOf(index);
    }

    @Override
    protected String genTarget(int index) {
        return "target-" + index;
    }

}
