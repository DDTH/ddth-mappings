package com.github.ddth.mappings.test.inmem.si;

import com.github.ddth.mappings.IMappingDao;
import com.github.ddth.mappings.inmem.InmemMappingManyOneDao;
import com.github.ddth.mappings.test.BaseMappingManyOneTCase;

import junit.framework.Test;
import junit.framework.TestSuite;

public class InmemMappingManyOneTCase extends BaseMappingManyOneTCase<String, Integer> {

    public static Test suite() {
        return new TestSuite(InmemMappingManyOneTCase.class);
    }

    @Override
    protected IMappingDao<String, Integer> initDaoInstance() {
        return new InmemMappingManyOneDao<>(String.class, Integer.class).init();
    }

    @Override
    protected void destroyDaoInstance(IMappingDao<String, Integer> mappingsDao) {
        ((InmemMappingManyOneDao<?, ?>) mappingsDao).destroy();
    }

    @Override
    protected String genObject(int index) {
        return "object-" + index;
    }

    @Override
    protected Integer genTarget(int index) {
        return Integer.valueOf(index);
    }

}
