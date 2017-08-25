package com.github.ddth.mappings.test.inmem.si;

import com.github.ddth.mappings.IMappingDao;
import com.github.ddth.mappings.inmem.InmemMappingManyManyDao;
import com.github.ddth.mappings.test.BaseMappingManyManyTCase;

import junit.framework.Test;
import junit.framework.TestSuite;

public class InmemMappingManyManyTCase extends BaseMappingManyManyTCase<String, Integer> {

    public static Test suite() {
        return new TestSuite(InmemMappingManyManyTCase.class);
    }

    @Override
    protected IMappingDao<String, Integer> initDaoInstance() {
        return new InmemMappingManyManyDao<>(String.class, Integer.class).init();
    }

    @Override
    protected void destroyDaoInstance(IMappingDao<String, Integer> mappingsDao) {
        ((InmemMappingManyManyDao<?, ?>) mappingsDao).destroy();
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
