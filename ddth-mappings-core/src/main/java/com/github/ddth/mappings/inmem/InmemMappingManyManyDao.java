package com.github.ddth.mappings.inmem;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.github.ddth.dao.utils.DaoResult.DaoOperationStatus;
import com.github.ddth.mappings.AbstractMappingManyManyDao;
import com.github.ddth.mappings.MappingBo;
import com.github.ddth.mappings.utils.MappingsUtils;
import com.github.ddth.mappings.utils.MappingsUtils.DaoResult;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * In-memory n-n mapping.
 *
 * <p>
 * In-memory mappings for testing or small temporary data.
 * </p>
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class InmemMappingManyManyDao<O, T> extends AbstractMappingManyManyDao<O, T> {

    public InmemMappingManyManyDao(Class<O> objectClass, Class<T> targetClass) {
        super(objectClass, targetClass);
    }

    /*
     * {namespace -> {object -> [target]}}
     */
    private LoadingCache<String, LoadingCache<O, Set<MappingBo<O, T>>>> storageObjTargets = CacheBuilder
            .newBuilder().build(new CacheLoader<String, LoadingCache<O, Set<MappingBo<O, T>>>>() {
                @Override
                public LoadingCache<O, Set<MappingBo<O, T>>> load(String key) throws Exception {
                    return CacheBuilder.newBuilder()
                            .build(new CacheLoader<O, Set<MappingBo<O, T>>>() {
                                @Override
                                public Set<MappingBo<O, T>> load(O key) throws Exception {
                                    return new HashSet<>();
                                }
                            });
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Long> getStats(String namespace) {
        return new HashMap<String, Long>() {
            private static final long serialVersionUID = 1L;
            {
                try {
                    AtomicLong numObjs = new AtomicLong();
                    AtomicLong numTargets = new AtomicLong();
                    storageObjTargets.get(namespace).asMap()
                            .forEach((key, value) -> numObjs.addAndGet(value.size() > 0 ? 1 : 0));
                    storageTargetObjs.get(namespace).asMap().forEach(
                            (key, value) -> numTargets.addAndGet(value.size() > 0 ? 1 : 0));
                    put(STATS_KEY_TOTAL_OBJS, numObjs.get());
                    put(STATS_KEY_TOTAL_TARGETS, numTargets.get());
                    put(STATS_KEY_TOTAL_ITEMS, numObjs.get() + numTargets.get());
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
    protected Collection<MappingBo<O, T>> storageGetMappingsObjTargets(String namespace, O obj) {
        try {
            return new HashSet<>(storageObjTargets.get(namespace).get(obj));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<MappingBo<O, T>> storageGetMappingsTargetObjs(String namespace, T target) {
        try {
            return new HashSet<>(storageTargetObjs.get(namespace).get(target));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DaoResult storageMap(MappingBo<O, T> mappingToAdd,
            Collection<MappingBo<O, T>> existingOT, Collection<MappingBo<O, T>> existingTO) {
        final String namespace = mappingToAdd.getNamespace();
        final O obj = mappingToAdd.getObject();
        final T target = mappingToAdd.getTarget();
        try {
            if (storageObjTargets.get(namespace).get(obj).add(mappingToAdd)) {
            }
            storageTargetObjs.get(namespace).get(target).add(mappingToAdd);
            return new MappingsUtils.DaoResult(DaoOperationStatus.SUCCESSFUL);
        } catch (Exception e) {
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DaoResult storageUnmap(MappingBo<O, T> mappingToRemove,
            Collection<MappingBo<O, T>> existingOT, Collection<MappingBo<O, T>> existingTO) {
        final String namespace = mappingToRemove.getNamespace();
        final O obj = mappingToRemove.getObject();
        final T target = mappingToRemove.getTarget();
        try {
            if (storageObjTargets.get(namespace).get(obj).remove(mappingToRemove)) {
            }
            storageTargetObjs.get(namespace).get(target).remove(mappingToRemove);
            return new MappingsUtils.DaoResult(DaoOperationStatus.SUCCESSFUL);
        } catch (Exception e) {
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
        }
    }

}
