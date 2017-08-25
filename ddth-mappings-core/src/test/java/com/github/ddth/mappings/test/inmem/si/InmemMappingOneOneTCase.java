package com.github.ddth.mappings.test.inmem.si;

import com.github.ddth.mappings.IMappingDao;
import com.github.ddth.mappings.inmem.InmemMappingOneOneDao;
import com.github.ddth.mappings.test.BaseMappingOneOneTCase;

import junit.framework.Test;
import junit.framework.TestSuite;

public class InmemMappingOneOneTCase extends BaseMappingOneOneTCase<String, Integer> {

    public static Test suite() {
        return new TestSuite(InmemMappingOneOneTCase.class);
    }

    @Override
    protected IMappingDao<String, Integer> initDaoInstance() {
        return new InmemMappingOneOneDao<>(String.class, Integer.class).init();
    }

    @Override
    protected void destroyDaoInstance(IMappingDao<String, Integer> mappingsDao) {
        ((InmemMappingOneOneDao<?, ?>) mappingsDao).destroy();
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
