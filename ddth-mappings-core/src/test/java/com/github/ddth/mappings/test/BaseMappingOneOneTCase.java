package com.github.ddth.mappings.test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.ddth.dao.utils.DaoResult.DaoOperationStatus;
import com.github.ddth.mappings.IMappingDao;
import com.github.ddth.mappings.MappingBo;
import com.github.ddth.mappings.utils.MappingsUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public abstract class BaseMappingOneOneTCase<O, T> extends BaseMappingTCase<O, T> {

    /*
     * {namespace -> {object -> target}}
     */
    private LoadingCache<String, Map<O, MappingBo<O, T>>> storageObjTarget = CacheBuilder
            .newBuilder().build(new CacheLoader<String, Map<O, MappingBo<O, T>>>() {
                @Override
                public Map<O, MappingBo<O, T>> load(String key) throws Exception {
                    return new ConcurrentHashMap<>();
                }
            });

    /*
     * {namespace -> {target -> object}}
     */
    private LoadingCache<String, Map<T, MappingBo<O, T>>> storageTargetObj = CacheBuilder
            .newBuilder().build(new CacheLoader<String, Map<T, MappingBo<O, T>>>() {
                @Override
                public Map<T, MappingBo<O, T>> load(String key) throws Exception {
                    return new ConcurrentHashMap<>();
                }
            });

    @Before
    public void setUp() throws Exception {
        mappingsDao = initDaoInstance();
        storageObjTarget.invalidateAll();
        storageTargetObj.invalidateAll();
    }

    @After
    public void tearDown() {
        if (mappingsDao != null) {
            destroyDaoInstance(mappingsDao);
        }
    }

    private MappingsUtils.DaoResult doMap(IMappingDao<O, T> dao, String namespace, O object,
            T target) throws Exception {
        MappingBo<O, T> existingOT = storageObjTarget.get(namespace).get(object);
        MappingBo<O, T> bo = MappingBo.newInstance(namespace, object, target);
        MappingsUtils.DaoResult daoResult = dao.map(namespace, object, target);
        storageObjTarget.get(namespace).put(object, bo);
        if (existingOT != null) {
            storageTargetObj.get(namespace).remove(existingOT.getTarget());
        }
        storageTargetObj.get(namespace).put(target, bo);
        return daoResult;
    }

    private MappingsUtils.DaoResult doUnmap(IMappingDao<O, T> dao, String namespace, O object,
            T target) throws Exception {
        MappingBo<O, T> bo = MappingBo.newInstance(namespace, object, target);
        MappingsUtils.DaoResult daoResult = dao.unmap(namespace, object, target);
        storageObjTarget.get(namespace).remove(object, bo);
        storageTargetObj.get(namespace).remove(target, bo);
        return daoResult;
    }

    @Test
    public void testEmpty() {
        assertTotalItems(0);
        assertTargetForObject(null, genObject(0));
        assertObjectForTarget(null, genTarget(0));
    }

    @Test
    public void testMapUnmap() throws Exception {
        O object1 = genObject(1);
        T target1 = genTarget(1);
        O object2 = genObject(2);
        T target2 = genTarget(2);

        {
            /**
             * Map object1 <-> target1: new mapping
             * 
             * - existing mapping: empty
             */
            MappingsUtils.DaoResult daoResult = doMap(mappingsDao, NAMESPACE, object1, target1);
            assertEquals(DaoOperationStatus.SUCCESSFUL, daoResult.getStatus());
            assertEquals(0, daoResult.getOutput().size());

            /**
             * - 1 total items
             * 
             * - object1 -> [target1], target1 -> [object1]
             * 
             * - object2 -> [], target2 -> []
             */
            assertTotalItems(1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object1), object1);
            assertObjectForTarget(storageTargetObj.get(NAMESPACE).get(target1), target1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object2), object2);
            assertObjectForTarget(storageTargetObj.get(NAMESPACE).get(target2), target2);

            MappingBo<O, T> o1t1 = MappingBo.newInstance(NAMESPACE, object1, target1);
            assertTargetForObject(o1t1, object1);
            assertObjectForTarget(o1t1, target1);
            assertTargetForObject(null, object2);
            assertObjectForTarget(null, target2);
        }

        {
            /**
             * Unmap object1 <-> target2: not exist
             * 
             * - existing mapping: [target1]
             */
            MappingsUtils.DaoResult daoResult = doUnmap(mappingsDao, NAMESPACE, object1, target2);
            assertEquals(DaoOperationStatus.NOT_FOUND, daoResult.getStatus());
            assertEquals(1, daoResult.getOutput().size());
            assertMapping(daoResult.getSingleOutput(), NAMESPACE, object1, target1);

            /**
             * - 1 total items
             * 
             * - object1 -> [target1], target1 -> [object1]
             * 
             * - object2 -> [], target2 -> []
             */
            assertTotalItems(1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object1), object1);
            assertObjectForTarget(storageTargetObj.get(NAMESPACE).get(target1), target1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object2), object2);
            assertObjectForTarget(storageTargetObj.get(NAMESPACE).get(target2), target2);

            MappingBo<O, T> o1t1 = MappingBo.newInstance(NAMESPACE, object1, target1);
            assertTargetForObject(o1t1, object1);
            assertObjectForTarget(o1t1, target1);
            assertTargetForObject(null, object2);
            assertObjectForTarget(null, target2);
        }

        {
            /**
             * Unmap object2 <-> target1: not exist
             * 
             * - existing mapping: []
             */
            MappingsUtils.DaoResult daoResult = doUnmap(mappingsDao, NAMESPACE, object2, target1);
            assertEquals(DaoOperationStatus.NOT_FOUND, daoResult.getStatus());
            assertEquals(0, daoResult.getOutput().size());

            /**
             * - 1 total items
             * 
             * - object1 -> [target1], target1 -> [object1]
             * 
             * - object2 -> [], target2 -> []
             */
            assertTotalItems(1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object1), object1);
            assertObjectForTarget(storageTargetObj.get(NAMESPACE).get(target1), target1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object2), object2);
            assertObjectForTarget(storageTargetObj.get(NAMESPACE).get(target2), target2);

            MappingBo<O, T> o1t1 = MappingBo.newInstance(NAMESPACE, object1, target1);
            assertTargetForObject(o1t1, object1);
            assertObjectForTarget(o1t1, target1);
            assertTargetForObject(null, object2);
            assertObjectForTarget(null, target2);
        }

        {
            /**
             * Unmap object2 <-> target2: not exist
             * 
             * - existing mapping: []
             */
            MappingsUtils.DaoResult daoResult = doUnmap(mappingsDao, NAMESPACE, object2, target2);
            assertEquals(DaoOperationStatus.NOT_FOUND, daoResult.getStatus());
            assertEquals(0, daoResult.getOutput().size());

            /**
             * - 1 total items
             * 
             * - object1 -> [target1], target1 -> [object1]
             * 
             * - object2 -> [], target2 -> []
             */
            assertTotalItems(1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object1), object1);
            assertObjectForTarget(storageTargetObj.get(NAMESPACE).get(target1), target1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object2), object2);
            assertObjectForTarget(storageTargetObj.get(NAMESPACE).get(target2), target2);

            MappingBo<O, T> o1t1 = MappingBo.newInstance(NAMESPACE, object1, target1);
            assertTargetForObject(o1t1, object1);
            assertObjectForTarget(o1t1, target1);
            assertTargetForObject(null, object2);
            assertObjectForTarget(null, target2);
        }

        {
            /**
             * Unmap object1 <-> target1: existing
             * 
             * - existing mapping: [target1]
             */
            MappingsUtils.DaoResult daoResult = doUnmap(mappingsDao, NAMESPACE, object1, target1);
            assertEquals(DaoOperationStatus.SUCCESSFUL, daoResult.getStatus());
            assertEquals(1, daoResult.getOutput().size());
            assertMapping(daoResult.getSingleOutput(), NAMESPACE, object1, target1);

            /**
             * - 0 total items
             * 
             * - object1 -> [], target1 -> []
             * 
             * - object2 -> [], target2 -> []
             */
            assertTotalItems(0);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object1), object1);
            assertObjectForTarget(storageTargetObj.get(NAMESPACE).get(target1), target1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object2), object2);
            assertObjectForTarget(storageTargetObj.get(NAMESPACE).get(target2), target2);

            assertTargetForObject(null, object1);
            assertObjectForTarget(null, target1);
            assertTargetForObject(null, object2);
            assertObjectForTarget(null, target2);
        }
    }

    @Test
    public void testMapRemap() throws Exception {
        O object1 = genObject(1);
        T target1 = genTarget(1);
        O object2 = genObject(2);
        T target2 = genTarget(2);

        {
            /**
             * Map object1 <-> target1: new mapping
             * 
             * - existing mapping: empty
             */
            MappingsUtils.DaoResult daoResult = doMap(mappingsDao, NAMESPACE, object1, target1);
            assertEquals(DaoOperationStatus.SUCCESSFUL, daoResult.getStatus());
            assertEquals(0, daoResult.getOutput().size());

            /**
             * - 1 total items
             * 
             * - object1 -> [target1], target1 -> [object1]
             * 
             * - object2 -> [], target2 -> []
             */
            assertTotalItems(1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object1), object1);
            assertObjectForTarget(storageTargetObj.get(NAMESPACE).get(target1), target1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object2), object2);
            assertObjectForTarget(storageTargetObj.get(NAMESPACE).get(target2), target2);

            MappingBo<O, T> o1t1 = MappingBo.newInstance(NAMESPACE, object1, target1);
            assertTargetForObject(o1t1, object1);
            assertObjectForTarget(o1t1, target1);
            assertTargetForObject(null, object2);
            assertObjectForTarget(null, target2);
        }

        {
            /**
             * Map object1 <-> target2: new mapping
             * 
             * - existing mapping: [target1]
             */
            MappingsUtils.DaoResult daoResult = doMap(mappingsDao, NAMESPACE, object1, target2);
            assertEquals(DaoOperationStatus.SUCCESSFUL, daoResult.getStatus());
            assertEquals(1, daoResult.getOutput().size());
            assertMapping(daoResult.getSingleOutput(), NAMESPACE, object1, target1);

            /**
             * - 1 total items
             * 
             * - object1 -> [target2], target1 -> []
             * 
             * - object2 -> [], target2 -> [object1]
             */
            assertTotalItems(1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object1), object1);
            assertObjectForTarget(storageTargetObj.get(NAMESPACE).get(target1), target1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object2), object2);
            assertObjectForTarget(storageTargetObj.get(NAMESPACE).get(target2), target2);

            MappingBo<O, T> o1t2 = MappingBo.newInstance(NAMESPACE, object1, target2);
            assertTargetForObject(o1t2, object1);
            assertObjectForTarget(null, target1);
            assertTargetForObject(null, object2);
            assertObjectForTarget(o1t2, target2);
        }
    }

    @Test
    public void testMapStatsGet() throws Exception {
        O object = genObject(0);
        T target = genTarget(0);

        /**
         * Map object <-> target: new mapping
         * 
         * - existing mapping: []
         */
        MappingsUtils.DaoResult daoResult = doMap(mappingsDao, NAMESPACE, object, target);
        assertEquals(DaoOperationStatus.SUCCESSFUL, daoResult.getStatus());
        assertEquals(0, daoResult.getOutput().size());

        /**
         * 1 total items:
         * 
         * - object -> [target], target -> [object]
         */
        assertTotalItems(1);
        assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object), object);
        assertObjectForTarget(storageTargetObj.get(NAMESPACE).get(target), target);

        MappingBo<O, T> ot = MappingBo.newInstance(NAMESPACE, object, target);
        assertTargetForObject(ot, object);
        assertObjectForTarget(ot, target);
    }

    @Test
    public void testMapStatsGetDifferent() throws Exception {
        for (int i = 1; i < 10; i++) {
            O object = genObject(i);
            T target = genTarget(i);
            /**
             * Map object <-> target: new mapping
             * 
             * - existing mapping: []
             */
            MappingsUtils.DaoResult daoResult = doMap(mappingsDao, NAMESPACE, object, target);
            assertEquals(DaoOperationStatus.SUCCESSFUL, daoResult.getStatus());
            assertEquals(0, daoResult.getOutput().size());

            /**
             * i total items
             * 
             * - object -> [target], target -> [object]
             */
            assertTotalItems(i);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object), object);
            assertObjectForTarget(storageTargetObj.get(NAMESPACE).get(target), target);

            MappingBo<O, T> ot = MappingBo.newInstance(NAMESPACE, object, target);
            assertTargetForObject(ot, object);
            assertObjectForTarget(ot, target);
        }
    }

    @Test
    public void testMapStatsGetSameObjTargetMultipleTimes() throws Exception {
        O object = genObject(0);
        T target = genTarget(0);
        MappingsUtils.DaoResult daoResult;

        /**
         * Map object <-> target: new mapping
         * 
         * - existing mapping: []
         */
        daoResult = doMap(mappingsDao, NAMESPACE, object, target);
        assertEquals(DaoOperationStatus.SUCCESSFUL, daoResult.getStatus());
        assertEquals(0, daoResult.getOutput().size());

        /**
         * 1 total items
         * 
         * - object -> [target], target -> [object]
         */
        assertTotalItems(1);
        assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object), object);
        assertObjectForTarget(storageTargetObj.get(NAMESPACE).get(target), target);

        MappingBo<O, T> ot = MappingBo.newInstance(NAMESPACE, object, target);
        assertTargetForObject(ot, object);
        assertObjectForTarget(ot, target);

        for (int i = 1; i < 10; i++) {
            /**
             * Map object <-> target: existing
             * 
             * - existing mapping: [target]
             */
            daoResult = doMap(mappingsDao, NAMESPACE, object, target);
            assertEquals(DaoOperationStatus.SUCCESSFUL, daoResult.getStatus());
            assertEquals(1, daoResult.getOutput().size());
            assertMapping(daoResult.getSingleOutput(), NAMESPACE, object, target);

            /**
             * 1 total items
             * 
             * - object -> [target], target -> [object]
             */
            assertTotalItems(1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object), object);
            assertObjectForTarget(storageTargetObj.get(NAMESPACE).get(target), target);

            assertTargetForObject(ot, object);
            assertObjectForTarget(ot, target);
        }
    }
}
