package com.github.ddth.mappings.test.inmem.ss;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({ InmemMappingManyManyTCase.class, InmemMappingManyOneTCase.class,
        InmemMappingOneOneTCase.class })

public class MySuiteTest {
}
