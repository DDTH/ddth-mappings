package com.github.ddth.mappings.test.inmem.ii;

import com.github.ddth.mappings.IMappingDao;
import com.github.ddth.mappings.inmem.InmemMappingOneOneDao;
import com.github.ddth.mappings.test.BaseMappingOneOneTCase;

import junit.framework.Test;
import junit.framework.TestSuite;

public class InmemMappingOneOneTCase extends BaseMappingOneOneTCase<Integer, Integer> {

    public static Test suite() {
        return new TestSuite(InmemMappingOneOneTCase.class);
    }

    @Override
    protected IMappingDao<Integer, Integer> initDaoInstance() {
        return new InmemMappingOneOneDao<>(Integer.class, Integer.class).init();
    }

    @Override
    protected void destroyDaoInstance(IMappingDao<Integer, Integer> mappingsDao) {
        ((InmemMappingOneOneDao<?, ?>) mappingsDao).destroy();
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
