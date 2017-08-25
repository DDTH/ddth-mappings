package com.github.ddth.mappings;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.github.ddth.dao.utils.CacheInvalidationReason;
import com.github.ddth.dao.utils.DaoResult.DaoOperationStatus;
import com.github.ddth.mappings.utils.MappingsUtils;

/**
 * Abstract implementation of n-n mapping DAO.
 *
 * <p>
 * n-n mappings: one object can be mapped to multiple targets and vice versa.
 * </p>
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public abstract class AbstractMappingManyManyDao<O, T> extends AbstractMappingDao<O, T> {

    public AbstractMappingManyManyDao(Class<O> objectClass, Class<T> targetClass) {
        super(objectClass, targetClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void invalidate(MappingBo<O, T> bo, CacheInvalidationReason cir) {
        if (bo != null) {
            final String cacheName = getCacheName();
            removeFromCache(cacheName, cacheKeyObjTarget(bo));
            removeFromCache(cacheName, cacheKeyTargetObj(bo));
        }
    }

    /**
     * Get existing mappings {@code object -> targets}.
     *
     * <p>
     * This method returns an empty set if no mapping found.
     * </p>
     *
     * @param namespace
     * @param obj
     * @return
     */
    @SuppressWarnings("unchecked")
    protected Collection<MappingBo<O, T>> getMappingsObjTargets(String namespace, O obj) {
        final String cacheName = getCacheName();
        final String cacheKey = cacheKeyObjTarget(namespace, obj);
        Collection<MappingBo<O, T>> mappings = getFromCache(cacheName, cacheKey, Collection.class);
        if (mappings == null) {
            mappings = storageGetMappingsObjTargets(namespace, obj);
            putToCache(cacheName, cacheKey, mappings);
        }
        return mappings != null ? mappings : Collections.EMPTY_SET;
    }

    /**
     * Get existing mappings {@code target -> objects}.
     *
     * <p>
     * This method returns an empty set if no mapping found.
     * </p>
     *
     * @param namespace
     * @param target
     * @return
     */
    @SuppressWarnings("unchecked")
    protected Collection<MappingBo<O, T>> getMappingsTargetObjs(String namespace, T target) {
        final String cacheName = getCacheName();
        final String cacheKey = cacheKeyTargetObj(namespace, target);
        Collection<MappingBo<O, T>> mappings = getFromCache(cacheName, cacheKey, Collection.class);
        if (mappings == null) {
            mappings = storageGetMappingsTargetObjs(namespace, target);
            putToCache(cacheName, cacheKey, mappings);
        }
        return mappings != null ? mappings : Collections.EMPTY_SET;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MappingsUtils.DaoResult map(String namespace, O obj, T target) {
        MappingBo<O, T> mappingToAdd = createMappingBo(namespace, obj, target);
        Collection<MappingBo<O, T>> existingOT = getMappingsObjTargets(namespace, obj);
        Collection<MappingBo<O, T>> existingTO = getMappingsTargetObjs(namespace, target);
        MappingsUtils.DaoResult mapResult = null;
        if (existingOT == null || !existingOT.contains(mappingToAdd)) {
            mapResult = storageMap(mappingToAdd, existingOT, existingTO);
            if (mapResult.matchStatus(DaoOperationStatus.SUCCESSFUL,
                    DaoOperationStatus.DUPLICATED_KEY)) {
                invalidate(mappingToAdd, CacheInvalidationReason.DELETE);
            }
        }
        return new MappingsUtils.DaoResult(
                mapResult == null ? DaoOperationStatus.SUCCESSFUL : mapResult.getStatus(),
                new HashSet<>(existingOT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MappingsUtils.DaoResult unmap(String namespace, O obj, T target) {
        MappingBo<O, T> mappingToRemove = createMappingBo(namespace, obj, target);
        Collection<MappingBo<O, T>> existingOT = getMappingsObjTargets(namespace, obj);
        Collection<MappingBo<O, T>> existingTO = getMappingsTargetObjs(namespace, target);
        MappingsUtils.DaoResult unmapResult = null;
        if (existingOT != null && existingOT.contains(mappingToRemove)) {
            unmapResult = storageUnmap(mappingToRemove, existingOT, existingTO);
            if (unmapResult.matchStatus(DaoOperationStatus.SUCCESSFUL)) {
                invalidate(mappingToRemove, CacheInvalidationReason.DELETE);
            }
        }
        return new MappingsUtils.DaoResult(
                unmapResult == null ? DaoOperationStatus.NOT_FOUND : unmapResult.getStatus(),
                new HashSet<>(existingOT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<MappingBo<O, T>> getMappingsForObject(String namespace, O obj) {
        return getMappingsObjTargets(namespace, obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<MappingBo<O, T>> getMappingsForTarget(String namespace, T target) {
        return getMappingsTargetObjs(namespace, target);
    }

    /**
     * Get existing mappings {@code object -> targets} from storage. Sub-class
     * will implement this method.
     *
     * @param namespace
     * @param obj
     * @return
     */
    protected abstract Collection<MappingBo<O, T>> storageGetMappingsObjTargets(String namespace,
            O obj);

    /**
     * Get existing mappings {@code target -> objects} from storage. Sub-class
     * will implement this method.
     *
     * @param namespace
     * @param target
     * @return
     */
    protected abstract Collection<MappingBo<O, T>> storageGetMappingsTargetObjs(String namespace,
            T target);

    /**
     * Save mapping {@code object <-> target} to storage. Sub-class will
     * implement this method.
     *
     * @param mappingToAdd
     * @param existingOT
     * @param existingTO
     * @return
     */
    protected abstract MappingsUtils.DaoResult storageMap(MappingBo<O, T> mappingToAdd,
            Collection<MappingBo<O, T>> existingOT, Collection<MappingBo<O, T>> existingTO);

    /**
     * Remove mapping {@code object <-> target} from storage. Sub-class will
     * implement this method.
     *
     * @param mappingToRemove
     * @param existingOT
     * @param existingTO
     * @return
     */
    protected abstract MappingsUtils.DaoResult storageUnmap(MappingBo<O, T> mappingToRemove,
            Collection<MappingBo<O, T>> existingOT, Collection<MappingBo<O, T>> existingTO);
}
