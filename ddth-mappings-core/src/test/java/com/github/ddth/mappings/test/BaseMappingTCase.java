package com.github.ddth.mappings.test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.ddth.mappings.IMappingDao;
import com.github.ddth.mappings.MappingBo;

import junit.framework.TestCase;

public abstract class BaseMappingTCase<O, T> extends TestCase {

    protected final static String NAMESPACE = "namespace";
    protected IMappingDao<O, T> mappingsDao;

    protected abstract IMappingDao<O, T> initDaoInstance() throws Exception;

    protected abstract void destroyDaoInstance(IMappingDao<O, T> mappingsDao);

    protected void assertTotalItems(long expected) {
        Map<String, Long> stats = mappingsDao.getStats(NAMESPACE);
        if (expected > 0) {
            assertNotNull(stats.get(IMappingDao.STATS_KEY_TOTAL_ITEMS));
            assertEquals(expected, stats.get(IMappingDao.STATS_KEY_TOTAL_ITEMS).longValue());
        } else {
            assertTrue(stats.get(IMappingDao.STATS_KEY_TOTAL_ITEMS) == null
                    || expected == stats.get(IMappingDao.STATS_KEY_TOTAL_ITEMS).longValue());
        }
    }

    protected void assertTotalItems(long expectedObjs, long expectedTargets) {
        Map<String, Long> stats = mappingsDao.getStats(NAMESPACE);
        if (expectedObjs > 0) {
            assertNotNull(stats.get(IMappingDao.STATS_KEY_TOTAL_OBJS));
            assertEquals(expectedObjs, stats.get(IMappingDao.STATS_KEY_TOTAL_OBJS).longValue());
        } else {
            assertTrue(stats.get(IMappingDao.STATS_KEY_TOTAL_OBJS) == null
                    || 0 == stats.get(IMappingDao.STATS_KEY_TOTAL_OBJS).longValue());
        }
        if (expectedTargets > 0) {
            assertNotNull(stats.get(IMappingDao.STATS_KEY_TOTAL_TARGETS));
            assertEquals(expectedTargets,
                    stats.get(IMappingDao.STATS_KEY_TOTAL_TARGETS).longValue());
        } else {
            assertTrue(stats.get(IMappingDao.STATS_KEY_TOTAL_TARGETS) == null
                    || 0 == stats.get(IMappingDao.STATS_KEY_TOTAL_TARGETS).longValue());
        }
    }

    protected void assertTargetForObject(MappingBo<O, T> expected, O object) {
        Set<MappingBo<O, T>> targets = new HashSet<>();
        mappingsDao.getMappingsForObject(NAMESPACE, object).forEach(bo -> targets.add(bo));
        assertEquals(expected != null ? Collections.singleton(expected) : Collections.EMPTY_SET,
                targets);
    }

    protected void assertObjectForTarget(MappingBo<O, T> expected, T target) {
        Set<MappingBo<O, T>> objects = new HashSet<>();
        mappingsDao.getMappingsForTarget(NAMESPACE, target).forEach(bo -> objects.add(bo));
        assertEquals(expected != null ? Collections.singleton(expected) : Collections.EMPTY_SET,
                objects);
    }

    protected void assertObjectsForTarget(Collection<MappingBo<O, T>> expected, T target) {
        Set<MappingBo<O, T>> objects = new HashSet<>();
        mappingsDao.getMappingsForTarget(NAMESPACE, target).forEach(bo -> objects.add(bo));
        assertEquals(new HashSet<>(expected), objects);
    }

    protected void assertTargetsForObject(Collection<MappingBo<O, T>> expected, O object) {
        Set<MappingBo<O, T>> targets = new HashSet<>();
        mappingsDao.getMappingsForObject(NAMESPACE, object).forEach(bo -> targets.add(bo));
        assertEquals(new HashSet<>(expected), targets);
    }

    protected void assertMapping(MappingBo<O, T> bo, String expectedNamespace, O expectedObj,
            T expectedTarget) {
        assertNotNull(bo);
        assertEquals(expectedNamespace, bo.getNamespace());
        assertEquals(expectedObj, bo.getObject());
        assertEquals(expectedTarget, bo.getTarget());
    }

    protected abstract O genObject(int index);

    protected abstract T genTarget(int index);

}
