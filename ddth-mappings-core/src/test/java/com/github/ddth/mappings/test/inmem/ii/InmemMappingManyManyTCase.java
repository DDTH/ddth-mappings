package com.github.ddth.mappings.test.inmem.ii;

import com.github.ddth.mappings.IMappingDao;
import com.github.ddth.mappings.inmem.InmemMappingManyManyDao;
import com.github.ddth.mappings.test.BaseMappingManyManyTCase;

import junit.framework.Test;
import junit.framework.TestSuite;

public class InmemMappingManyManyTCase extends BaseMappingManyManyTCase<Integer, Integer> {

    public static Test suite() {
        return new TestSuite(InmemMappingManyManyTCase.class);
    }

    @Override
    protected IMappingDao<Integer, Integer> initDaoInstance() {
        return new InmemMappingManyManyDao<>(Integer.class, Integer.class).init();
    }

    @Override
    protected void destroyDaoInstance(IMappingDao<Integer, Integer> mappingsDao) {
        ((InmemMappingManyManyDao<?, ?>) mappingsDao).destroy();
    }

    @Override
    protected Integer genObject(int index) {
        return Integer.valueOf(index);
    }

    @Override
    protected Integer genTarget(int index) {
        return Integer.valueOf(index);
    }

}
