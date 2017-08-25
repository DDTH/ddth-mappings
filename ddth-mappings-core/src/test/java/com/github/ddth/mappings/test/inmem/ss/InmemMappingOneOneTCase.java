package com.github.ddth.mappings.test.inmem.ss;

import com.github.ddth.mappings.IMappingDao;
import com.github.ddth.mappings.inmem.InmemMappingOneOneDao;
import com.github.ddth.mappings.test.BaseMappingOneOneTCase;

import junit.framework.Test;
import junit.framework.TestSuite;

public class InmemMappingOneOneTCase extends BaseMappingOneOneTCase<String, String> {

    public static Test suite() {
        return new TestSuite(InmemMappingOneOneTCase.class);
    }

    @Override
    protected IMappingDao<String, String> initDaoInstance() {
        return new InmemMappingOneOneDao<>(String.class, String.class);
    }

    @Override
    protected void destroyDaoInstance(IMappingDao<String, String> mappingsDao) {
        ((InmemMappingOneOneDao<?, ?>) mappingsDao).destroy();
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
