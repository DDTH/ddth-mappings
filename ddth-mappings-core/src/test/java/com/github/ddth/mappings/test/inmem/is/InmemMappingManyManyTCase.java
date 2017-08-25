package com.github.ddth.mappings.test.inmem.is;

import com.github.ddth.mappings.IMappingDao;
import com.github.ddth.mappings.inmem.InmemMappingManyManyDao;
import com.github.ddth.mappings.test.BaseMappingManyManyTCase;

import junit.framework.Test;
import junit.framework.TestSuite;

public class InmemMappingManyManyTCase extends BaseMappingManyManyTCase<Integer, String> {

    public static Test suite() {
        return new TestSuite(InmemMappingManyManyTCase.class);
    }

    @Override
    protected IMappingDao<Integer, String> initDaoInstance() {
        return new InmemMappingManyManyDao<>(Integer.class, String.class).init();
    }

    @Override
    protected void destroyDaoInstance(IMappingDao<Integer, String> mappingsDao) {
        ((InmemMappingManyManyDao<?, ?>) mappingsDao).destroy();
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
