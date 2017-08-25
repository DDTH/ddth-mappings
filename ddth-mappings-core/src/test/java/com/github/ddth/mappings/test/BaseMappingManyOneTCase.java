package com.github.ddth.mappings.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

public abstract class BaseMappingManyOneTCase<O, T> extends BaseMappingTCase<O, T> {

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
     * {namespace -> {target -> [object]}}
     */
    private LoadingCache<String, LoadingCache<T, Set<MappingBo<O, T>>>> storageTargetObjs = CacheBuilder
            .newBuilder().build(new CacheLoader<String, LoadingCache<T, Set<MappingBo<O, T>>>>() {
                @Override
                public LoadingCache<T, Set<MappingBo<O, T>>> load(String key) throws Exception {
                    return CacheBuilder.newBuilder()
                            .build(new CacheLoader<T, Set<MappingBo<O, T>>>() {
                                @Override
                                public Set<MappingBo<O, T>> load(T key) throws Exception {
                                    return new HashSet<>();
                                }
                            });
                }
            });

    @Before
    public void setUp() throws Exception {
        mappingsDao = initDaoInstance();
        storageObjTarget.invalidateAll();
        storageTargetObjs.invalidateAll();
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
            storageTargetObjs.get(namespace).get(existingOT.getTarget()).remove(existingOT);
        }
        storageTargetObjs.get(namespace).get(target).add(bo);
        return daoResult;
    }

    private MappingsUtils.DaoResult doUnmap(IMappingDao<O, T> dao, String namespace, O object,
            T target) throws Exception {
        MappingBo<O, T> bo = MappingBo.newInstance(namespace, object, target);
        MappingsUtils.DaoResult daoResult = dao.unmap(namespace, object, target);
        storageObjTarget.get(namespace).remove(object, bo);
        storageTargetObjs.get(namespace).get(target).remove(bo);
        return daoResult;
    }

    @Test
    public void testEmpty() throws Exception {
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
             * - existing mapping: []
             */
            MappingsUtils.DaoResult daoResult = doMap(mappingsDao, NAMESPACE, object1, target1);
            assertEquals(DaoOperationStatus.SUCCESSFUL, daoResult.getStatus());
            assertEquals(0, daoResult.getOutput().size());

            /**
             * - 1 total objects, 1 total targets
             * 
             * - object1 -> [target1], target1 -> [object1]
             * 
             * - object2 -> [], target2 -> []
             */
            assertTotalItems(1, 1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object1), object1);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target1), target1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object2), object2);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target2), target2);

            MappingBo<O, T> o1t1 = MappingBo.newInstance(NAMESPACE, object1, target1);
            assertTargetForObject(o1t1, object1);
            assertObjectsForTarget(Arrays.asList(o1t1), target1);
            assertTargetForObject(null, object2);
            assertObjectsForTarget(Arrays.asList(), target2);
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
             * - 1 total objects, 1 total targets
             * 
             * - object1 -> [target1], target1 -> [object1]
             * 
             * - object2 -> [], target2 -> []
             */
            assertTotalItems(1, 1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object1), object1);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target1), target1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object2), object2);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target2), target2);

            MappingBo<O, T> o1t1 = MappingBo.newInstance(NAMESPACE, object1, target1);
            assertTargetForObject(o1t1, object1);
            assertObjectsForTarget(Arrays.asList(o1t1), target1);
            assertTargetForObject(null, object2);
            assertObjectsForTarget(Arrays.asList(), target2);
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
             * - 1 total objects, 1 total targets
             * 
             * - object1 -> [target1], target1 -> [object1]
             * 
             * - object2 -> [], target2 -> []
             */
            assertTotalItems(1, 1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object1), object1);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target1), target1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object2), object2);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target2), target2);

            MappingBo<O, T> o1t1 = MappingBo.newInstance(NAMESPACE, object1, target1);
            assertTargetForObject(o1t1, object1);
            assertObjectsForTarget(Arrays.asList(o1t1), target1);
            assertTargetForObject(null, object2);
            assertObjectsForTarget(Arrays.asList(), target2);
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
             * - 1 total objects, 1 total targets
             * 
             * - object1 -> [target1], target1 -> [object1]
             * 
             * - object2 -> [], target2 -> []
             */
            assertTotalItems(1, 1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object1), object1);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target1), target1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object2), object2);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target2), target2);

            MappingBo<O, T> o1t1 = MappingBo.newInstance(NAMESPACE, object1, target1);
            assertTargetForObject(o1t1, object1);
            assertObjectsForTarget(Arrays.asList(o1t1), target1);
            assertTargetForObject(null, object2);
            assertObjectsForTarget(Arrays.asList(), target2);
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
             * - 0 total objects, 0 total targets
             * 
             * - object1 -> [], target1 -> []
             * 
             * - object2 -> [], target2 -> []
             */
            assertTotalItems(0, 0);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object1), object1);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target1), target1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object2), object2);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target2), target2);

            assertTargetForObject(null, object1);
            assertObjectsForTarget(Arrays.asList(), target1);
            assertTargetForObject(null, object2);
            assertObjectsForTarget(Arrays.asList(), target2);
        }
    }

    @Test
    public void testMapUnmapOneObjMultiTargets() throws Exception {
        O object1 = genObject(1);
        T target1 = genTarget(1);
        O object2 = genObject(2);
        T target2 = genTarget(2);
        O object3 = genObject(3);
        T target3 = genTarget(3);

        {
            /**
             * Map object1 <-> target1: new mapping
             * 
             * - existing mapping: []
             */
            MappingsUtils.DaoResult daoResult = doMap(mappingsDao, NAMESPACE, object1, target1);
            assertEquals(DaoOperationStatus.SUCCESSFUL, daoResult.getStatus());
            assertEquals(0, daoResult.getOutput().size());

            /**
             * - 1 total objects, 1 total targets
             * 
             * - object1 -> [target1], target1 -> [object1]
             * 
             * - object2 -> [], target2 -> []
             * 
             * - object3 -> [], target3 -> []
             */
            assertTotalItems(1, 1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object1), object1);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target1), target1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object2), object2);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target2), target2);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object3), object3);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target3), target3);

            MappingBo<O, T> o1t1 = MappingBo.newInstance(NAMESPACE, object1, target1);
            assertTargetForObject(o1t1, object1);
            assertObjectsForTarget(Arrays.asList(o1t1), target1);
            assertTargetForObject(null, object2);
            assertObjectsForTarget(Arrays.asList(), target2);
            assertTargetForObject(null, object3);
            assertObjectsForTarget(Arrays.asList(), target3);
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
             * - 1 total objects, 1 total targets
             * 
             * - object1 -> [target2], target1 -> []
             * 
             * - object2 -> [], target2 -> [object1]
             * 
             * - object3 -> [], target3 -> []
             */
            assertTotalItems(1, 1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object1), object1);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target1), target1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object2), object2);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target2), target2);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object3), object3);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target3), target3);

            MappingBo<O, T> o1t2 = MappingBo.newInstance(NAMESPACE, object1, target2);
            assertTargetForObject(o1t2, object1);
            assertObjectsForTarget(Arrays.asList(), target1);
            assertTargetForObject(null, object2);
            assertObjectsForTarget(Arrays.asList(o1t2), target2);
            assertTargetForObject(null, object3);
            assertObjectsForTarget(Arrays.asList(), target3);
        }

        {
            /**
             * Unmap object1 <-> target3: not exists
             * 
             * - existing mapping: [target2]
             */
            MappingsUtils.DaoResult daoResult = doUnmap(mappingsDao, NAMESPACE, object1, target3);
            assertEquals(DaoOperationStatus.NOT_FOUND, daoResult.getStatus());
            assertEquals(1, daoResult.getOutput().size());
            assertMapping(daoResult.getSingleOutput(), NAMESPACE, object1, target2);

            /**
             * - 1 total objects, 1 total targets
             * 
             * - object1 -> [target2], target1 -> []
             * 
             * - object2 -> [], target2 -> [object1]
             * 
             * - object3 -> [], target3 -> []
             */
            assertTotalItems(1, 1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object1), object1);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target1), target1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object2), object2);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target2), target2);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object3), object3);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target3), target3);

            MappingBo<O, T> o1t2 = MappingBo.newInstance(NAMESPACE, object1, target2);
            assertTargetForObject(o1t2, object1);
            assertObjectsForTarget(Arrays.asList(), target1);
            assertTargetForObject(null, object2);
            assertObjectsForTarget(Arrays.asList(o1t2), target2);
            assertTargetForObject(null, object3);
            assertObjectsForTarget(Arrays.asList(), target3);
        }

        {
            /**
             * Unmap object1 <-> target1: not exists
             * 
             * - existing mapping: [target2]
             */
            MappingsUtils.DaoResult daoResult = doUnmap(mappingsDao, NAMESPACE, object1, target1);
            assertEquals(DaoOperationStatus.NOT_FOUND, daoResult.getStatus());
            assertEquals(1, daoResult.getOutput().size());
            assertMapping(daoResult.getSingleOutput(), NAMESPACE, object1, target2);

            /**
             * - 1 total objects, 1 total targets
             * 
             * - object1 -> [target2], target1 -> []
             * 
             * - object2 -> [], target2 -> [object1]
             * 
             * - object3 -> [], target3 -> []
             */
            assertTotalItems(1, 1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object1), object1);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target1), target1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object2), object2);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target2), target2);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object3), object3);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target3), target3);

            MappingBo<O, T> o1t2 = MappingBo.newInstance(NAMESPACE, object1, target2);
            assertTargetForObject(o1t2, object1);
            assertObjectsForTarget(Arrays.asList(), target1);
            assertTargetForObject(null, object2);
            assertObjectsForTarget(Arrays.asList(o1t2), target2);
            assertTargetForObject(null, object3);
            assertObjectsForTarget(Arrays.asList(), target3);
        }

        {
            /**
             * Unmap object1 <-> target2: existing
             * 
             * - existing mapping: [target2]
             */
            MappingsUtils.DaoResult daoResult = doUnmap(mappingsDao, NAMESPACE, object1, target2);
            assertEquals(DaoOperationStatus.SUCCESSFUL, daoResult.getStatus());
            assertEquals(1, daoResult.getOutput().size());
            assertMapping(daoResult.getSingleOutput(), NAMESPACE, object1, target2);

            /**
             * - 0 total objects, 0 total targets
             * 
             * - object1 -> [], target1 -> []
             * 
             * - object2 -> [], target2 -> []
             * 
             * - object3 -> [], target3 -> []
             */
            assertTotalItems(0, 0);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object1), object1);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target1), target1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object2), object2);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target2), target2);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object3), object3);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target3), target3);

            assertTargetForObject(null, object1);
            assertObjectsForTarget(Arrays.asList(), target1);
            assertTargetForObject(null, object2);
            assertObjectsForTarget(Arrays.asList(), target2);
            assertTargetForObject(null, object3);
            assertObjectsForTarget(Arrays.asList(), target3);
        }
    }

    @Test
    public void testMapUnmapMultiObjsOneTargets() throws Exception {
        O object1 = genObject(1);
        T target1 = genTarget(1);
        O object2 = genObject(2);
        T target2 = genTarget(2);
        O object3 = genObject(3);
        T target3 = genTarget(3);

        {
            /**
             * Map object1 <-> target1: new mapping
             * 
             * - existing mapping: []
             */
            MappingsUtils.DaoResult daoResult = doMap(mappingsDao, NAMESPACE, object1, target1);
            assertEquals(DaoOperationStatus.SUCCESSFUL, daoResult.getStatus());
            assertEquals(0, daoResult.getOutput().size());

            /**
             * - 1 total objects, 1 total targets
             * 
             * - object1 -> [target1], target1 -> [object1]
             * 
             * - object2 -> [], target2 -> []
             * 
             * - object3 -> [], target3 -> []
             */
            assertTotalItems(1, 1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object1), object1);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target1), target1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object2), object2);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target2), target2);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object3), object3);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target3), target3);

            MappingBo<O, T> o1t1 = MappingBo.newInstance(NAMESPACE, object1, target1);
            assertTargetForObject(o1t1, object1);
            assertObjectsForTarget(Arrays.asList(o1t1), target1);
            assertTargetForObject(null, object2);
            assertObjectsForTarget(Arrays.asList(), target2);
            assertTargetForObject(null, object3);
            assertObjectsForTarget(Arrays.asList(), target3);
        }

        {
            /**
             * Map object2 <-> target1: new mapping
             * 
             * - existing mapping: []
             */
            MappingsUtils.DaoResult daoResult = doMap(mappingsDao, NAMESPACE, object2, target1);
            assertEquals(DaoOperationStatus.SUCCESSFUL, daoResult.getStatus());
            assertEquals(0, daoResult.getOutput().size());

            /**
             * - 2 total objects, 1 total targets
             * 
             * - object1 -> [target1], target1 -> [object1, object2]
             * 
             * - object2 -> [target1], target2 -> []
             * 
             * - object3 -> [], target3 -> []
             */
            assertTotalItems(2, 1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object1), object1);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target1), target1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object2), object2);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target2), target2);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object3), object3);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target3), target3);

            MappingBo<O, T> o1t1 = MappingBo.newInstance(NAMESPACE, object1, target1);
            MappingBo<O, T> o2t1 = MappingBo.newInstance(NAMESPACE, object2, target1);
            assertTargetForObject(o1t1, object1);
            assertObjectsForTarget(Arrays.asList(o1t1, o2t1), target1);
            assertTargetForObject(o2t1, object2);
            assertObjectsForTarget(Arrays.asList(), target2);
            assertTargetForObject(null, object3);
            assertObjectsForTarget(Arrays.asList(), target3);
        }

        {
            /**
             * Unmap object3 <-> target1: not exists
             * 
             * - existing mapping: []
             */
            MappingsUtils.DaoResult daoResult = doUnmap(mappingsDao, NAMESPACE, object3, target1);
            assertEquals(DaoOperationStatus.NOT_FOUND, daoResult.getStatus());
            assertEquals(0, daoResult.getOutput().size());

            /**
             * - 2 total objects, 1 total targets
             * 
             * - object1 -> [target1], target1 -> [object1, object2]
             * 
             * - object2 -> [target1], target2 -> []
             * 
             * - object3 -> [], target3 -> []
             */
            assertTotalItems(2, 1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object1), object1);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target1), target1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object2), object2);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target2), target2);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object3), object3);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target3), target3);

            MappingBo<O, T> o1t1 = MappingBo.newInstance(NAMESPACE, object1, target1);
            MappingBo<O, T> o2t1 = MappingBo.newInstance(NAMESPACE, object2, target1);
            assertTargetForObject(o1t1, object1);
            assertObjectsForTarget(Arrays.asList(o1t1, o2t1), target1);
            assertTargetForObject(o2t1, object2);
            assertObjectsForTarget(Arrays.asList(), target2);
            assertTargetForObject(null, object3);
            assertObjectsForTarget(Arrays.asList(), target3);
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
             * - 1 total objects, 1 total targets
             * 
             * - object1 -> [], target1 -> [object2]
             * 
             * - object2 -> [target1], target2 -> []
             * 
             * - object3 -> [], target3 -> []
             */
            assertTotalItems(1, 1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object1), object1);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target1), target1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object2), object2);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target2), target2);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object3), object3);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target3), target3);

            MappingBo<O, T> o2t1 = MappingBo.newInstance(NAMESPACE, object2, target1);
            assertTargetForObject(null, object1);
            assertObjectsForTarget(Arrays.asList(o2t1), target1);
            assertTargetForObject(o2t1, object2);
            assertObjectsForTarget(Arrays.asList(), target2);
            assertTargetForObject(null, object3);
            assertObjectsForTarget(Arrays.asList(), target3);
        }

        {
            /**
             * Unmap object2 <-> target1: existing
             * 
             * - existing mapping: [target1]
             */
            MappingsUtils.DaoResult daoResult = doUnmap(mappingsDao, NAMESPACE, object2, target1);
            assertEquals(DaoOperationStatus.SUCCESSFUL, daoResult.getStatus());
            assertEquals(1, daoResult.getOutput().size());
            assertMapping(daoResult.getSingleOutput(), NAMESPACE, object2, target1);

            /**
             * - 0 total objects, 0 total targets
             * 
             * - object1 -> [], target1 -> []
             * 
             * - object2 -> [], target2 -> []
             * 
             * - object3 -> [], target3 -> []
             */
            assertTotalItems(0, 0);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object1), object1);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target1), target1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object2), object2);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target2), target2);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object3), object3);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target3), target3);

            assertTargetForObject(null, object1);
            assertObjectsForTarget(Arrays.asList(), target1);
            assertTargetForObject(null, object2);
            assertObjectsForTarget(Arrays.asList(), target2);
            assertTargetForObject(null, object3);
            assertObjectsForTarget(Arrays.asList(), target3);
        }
    }

    @Test
    public void testMapMulti() throws Exception {
        O object1 = genObject(1);
        T target1 = genTarget(1);
        O object2 = genObject(2);
        T target2 = genTarget(2);

        {
            /**
             * Map object1 <-> target1: new mapping
             * 
             * - existing mapping: []
             */
            MappingsUtils.DaoResult daoResult = doMap(mappingsDao, NAMESPACE, object1, target1);
            assertEquals(DaoOperationStatus.SUCCESSFUL, daoResult.getStatus());
            assertEquals(0, daoResult.getOutput().size());

            /**
             * 1 total objects, 1 total targets
             * 
             * - object1 -> [target1], target1 -> [object1]
             * 
             * - object2 -> [], target2 -> []
             */
            assertTotalItems(1, 1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object1), object1);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target1), target1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object2), object2);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target2), target2);

            MappingBo<O, T> o1t1 = MappingBo.newInstance(NAMESPACE, object1, target1);
            assertTargetForObject(o1t1, object1);
            assertObjectsForTarget(Arrays.asList(o1t1), target1);
            assertTargetForObject(null, object2);
            assertObjectsForTarget(Arrays.asList(), target2);
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
             * 1 total objects, 1 total targets
             * 
             * - object1 -> [target2], target1 -> []
             * 
             * - object2 -> [], target2 -> [object1]
             */
            assertTotalItems(1, 1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object1), object1);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target1), target1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object2), object2);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target2), target2);

            MappingBo<O, T> o1t2 = MappingBo.newInstance(NAMESPACE, object1, target2);
            assertTargetForObject(o1t2, object1);
            assertObjectsForTarget(Arrays.asList(), target1);
            assertTargetForObject(null, object2);
            assertObjectsForTarget(Arrays.asList(o1t2), target2);
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
         * 1 total objects, 1 total targets
         * 
         * - object -> [target], target -> [object]
         */
        assertTotalItems(1, 1);
        assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object), object);
        assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target), target);

        MappingBo<O, T> ot = MappingBo.newInstance(NAMESPACE, object, target);
        assertTargetForObject(ot, object);
        assertObjectsForTarget(Arrays.asList(ot), target);
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
             * i total objects, i total targets
             * 
             * - object -> [target], target -> [object]
             */
            assertTotalItems(i, i);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object), object);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target), target);

            MappingBo<O, T> ot = MappingBo.newInstance(NAMESPACE, object, target);
            assertTargetForObject(ot, object);
            assertObjectsForTarget(Arrays.asList(ot), target);
        }
    }

    @Test
    public void testMapStatsGetMultiTargets() throws Exception {
        for (int i = 0; i < 10; i++) {
            O object = genObject(0);
            T target = genTarget(i);

            /**
             * Map object <-> target<i>: existing mapping
             * 
             * - existing mapping [target<i-1>]
             */
            MappingsUtils.DaoResult daoResult = doMap(mappingsDao, NAMESPACE, object, target);
            assertEquals(DaoOperationStatus.SUCCESSFUL, daoResult.getStatus());
            assertEquals(i == 0 ? 0 : 1, daoResult.getOutput().size());

            /**
             * 1 total objects, 1 total targets
             * 
             * - object<i> -> [target], target -> [object<i>]
             */
            assertTotalItems(1, 1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object), object);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target), target);

            MappingBo<O, T> ot = MappingBo.newInstance(NAMESPACE, object, target);
            assertTargetForObject(ot, object);
            assertObjectsForTarget(Arrays.asList(ot), target);
        }
    }

    @Test
    public void testMapStatsGetMultiObjects() throws Exception {
        Collection<MappingBo<O, T>> mappings = new HashSet<>();
        for (int i = 1; i < 10; i++) {
            O object = genObject(i);
            T target = genTarget(0);

            /**
             * Map object<i> <-> target: new mapping
             * 
             * - existing mapping: []
             */
            MappingsUtils.DaoResult daoResult = doMap(mappingsDao, NAMESPACE, object, target);
            assertEquals(DaoOperationStatus.SUCCESSFUL, daoResult.getStatus());
            assertEquals(0, daoResult.getOutput().size());

            /**
             * 1 total objects, i total targets
             * 
             * - object<i> -> [target], target -> [object<1>,object<2>...]
             */
            assertTotalItems(i, 1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object), object);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target), target);

            MappingBo<O, T> oit = MappingBo.newInstance(NAMESPACE, object, target);
            mappings.add(oit);
            assertTargetForObject(oit, object);
            assertObjectsForTarget(mappings, target);
        }
    }

    @Test
    public void testMapStatsGetSameObjTargetMulti() throws Exception {
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
         * 1 total objects, 1 total targets
         * 
         * - object -> [target], target -> [object]
         */
        assertTotalItems(1, 1);
        assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object), object);
        assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target), target);

        MappingBo<O, T> ot = MappingBo.newInstance(NAMESPACE, object, target);
        assertTargetForObject(ot, object);
        assertObjectsForTarget(Arrays.asList(ot), target);

        for (int i = 1; i < 10; i++) {
            /**
             * Map object <-> target: existing mapping
             * 
             * - existing mapping: [target]
             */
            daoResult = doMap(mappingsDao, NAMESPACE, object, target);
            assertEquals(DaoOperationStatus.SUCCESSFUL, daoResult.getStatus());
            assertEquals(1, daoResult.getOutput().size());
            assertMapping(daoResult.getSingleOutput(), NAMESPACE, object, target);

            /**
             * 1 total objects, 1 total targets
             * 
             * - object -> [target], target -> [object]
             */
            assertTotalItems(1, 1);
            assertTargetForObject(storageObjTarget.get(NAMESPACE).get(object), object);
            assertObjectsForTarget(storageTargetObjs.get(NAMESPACE).get(target), target);

            assertTargetForObject(ot, object);
            assertObjectsForTarget(Arrays.asList(ot), target);
        }
    }
}
