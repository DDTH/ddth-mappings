package com.github.ddth.mappings.test.inmem.ii;

import com.github.ddth.mappings.IMappingDao;
import com.github.ddth.mappings.inmem.InmemMappingManyOneDao;
import com.github.ddth.mappings.test.BaseMappingManyOneTCase;

import junit.framework.Test;
import junit.framework.TestSuite;

public class InmemMappingManyOneTCase extends BaseMappingManyOneTCase<Integer, Integer> {

    public static Test suite() {
        return new TestSuite(InmemMappingManyOneTCase.class);
    }

    @Override
    protected IMappingDao<Integer, Integer> initDaoInstance() {
        return new InmemMappingManyOneDao<>(Integer.class, Integer.class).init();
    }

    @Override
    protected void destroyDaoInstance(IMappingDao<Integer, Integer> mappingsDao) {
        ((InmemMappingManyOneDao<?, ?>) mappingsDao).destroy();
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
