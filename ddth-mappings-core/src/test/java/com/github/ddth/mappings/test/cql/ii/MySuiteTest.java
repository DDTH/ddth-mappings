package com.github.ddth.mappings.test.cql.ii;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({ CqlMappingManyManyTCase.class, CqlMappingManyOneTCase.class,
        CqlMappingOneOneTCase.class })

public class MySuiteTest {
}
