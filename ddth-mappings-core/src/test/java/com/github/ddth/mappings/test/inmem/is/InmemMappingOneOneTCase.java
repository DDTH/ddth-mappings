package com.github.ddth.mappings.test.inmem.is;

import com.github.ddth.mappings.IMappingDao;
import com.github.ddth.mappings.inmem.InmemMappingOneOneDao;
import com.github.ddth.mappings.test.BaseMappingOneOneTCase;

import junit.framework.Test;
import junit.framework.TestSuite;

public class InmemMappingOneOneTCase extends BaseMappingOneOneTCase<Integer, String> {

    public static Test suite() {
        return new TestSuite(InmemMappingOneOneTCase.class);
    }

    @Override
    protected IMappingDao<Integer, String> initDaoInstance() {
        return new InmemMappingOneOneDao<>(Integer.class, String.class).init();
    }

    @Override
    protected void destroyDaoInstance(IMappingDao<Integer, String> mappingsDao) {
        ((InmemMappingOneOneDao<?, ?>) mappingsDao).destroy();
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
