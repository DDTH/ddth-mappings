package com.github.ddth.mappings.inmem;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.ddth.dao.utils.DaoResult.DaoOperationStatus;
import com.github.ddth.mappings.AbstractMappingOneOneDao;
import com.github.ddth.mappings.MappingBo;
import com.github.ddth.mappings.utils.MappingsUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * In-memory 1-1 mapping.
 *
 * <p>
 * In-memory mappings for testing or small temporary data.
 * </p>
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class InmemMappingOneOneDao<O, T> extends AbstractMappingOneOneDao<O, T> {

    public InmemMappingOneOneDao(Class<O> objectClass, Class<T> targetClass) {
        super(objectClass, targetClass);
    }

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

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Long> getStats(String namespace) {
        return new HashMap<String, Long>() {
            private static final long serialVersionUID = 1L;
            {
                try {
                    int value = storageObjTarget.get(namespace).size();
                    put(STATS_KEY_TOTAL_ITEMS, (long) value);
                    put(STATS_KEY_TOTAL_OBJS, (long) value);
                    put(STATS_KEY_TOTAL_TARGETS, (long) value);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected MappingBo<O, T> storageGetMappingObjTarget(String namespace, O obj) {
        try {
            return storageObjTarget.get(namespace).get(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected MappingBo<O, T> storageGetMappingTargetObj(String namespace, T target) {
        try {
            return storageTargetObj.get(namespace).get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected MappingsUtils.DaoResult storageMap(MappingBo<O, T> mappingToAdd,
            MappingBo<O, T> existingOT) {
        final String namespace = mappingToAdd.getNamespace();
        final O newObj = mappingToAdd.getObject();
        final T newTarget = mappingToAdd.getTarget();
        try {
            storageObjTarget.get(namespace).put(newObj, mappingToAdd);
            if (existingOT != null) {
                storageTargetObj.get(namespace).remove(existingOT.getTarget());
            }
            storageTargetObj.get(namespace).put(newTarget, mappingToAdd);
            return new MappingsUtils.DaoResult(DaoOperationStatus.SUCCESSFUL);
        } catch (Exception e) {
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected MappingsUtils.DaoResult storageUnmap(MappingBo<O, T> mappingToRemove) {
        final String namespace = mappingToRemove.getNamespace();
        final O obj = mappingToRemove.getObject();
        final T target = mappingToRemove.getTarget();
        try {
            storageObjTarget.get(namespace).remove(obj, mappingToRemove);
            storageTargetObj.get(namespace).remove(target, mappingToRemove);
            return new MappingsUtils.DaoResult(DaoOperationStatus.SUCCESSFUL);
        } catch (Exception e) {
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
        }
    }

}
