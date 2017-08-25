package com.github.ddth.mappings;

import java.util.Collection;
import java.util.Collections;

import com.github.ddth.dao.utils.CacheInvalidationReason;
import com.github.ddth.dao.utils.DaoResult.DaoOperationStatus;
import com.github.ddth.mappings.utils.MappingsUtils;

/**
 * Abstract implementation of 1-1 mapping DAO.
 * 
 * <p>
 * 1-1 mappings: object can be mapped to only one target and vice versa.
 * </p>
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public abstract class AbstractMappingOneOneDao<O, T> extends AbstractMappingDao<O, T> {

    public AbstractMappingOneOneDao(Class<O> objectClass, Class<T> targetClass) {
        super(objectClass, targetClass);
    }

    protected void invalidate(MappingBo<O, T> bo, CacheInvalidationReason cir) {
        if (bo != null) {
            final String cacheName = getCacheName();
            switch (cir) {
            case CREATE:
            case UPDATE:
                putToCache(cacheName, cacheKeyObjTarget(bo), bo);
                putToCache(cacheName, cacheKeyTargetObj(bo), bo);
                break;
            case DELETE:
                removeFromCache(cacheName, cacheKeyObjTarget(bo));
                removeFromCache(cacheName, cacheKeyTargetObj(bo));
                break;
            }
        }
    }

    /**
     * Get existing mapping {@code object -> target}.
     *
     * @param namespace
     * @param obj
     * @return
     */
    @SuppressWarnings("unchecked")
    protected MappingBo<O, T> getMappingObjTarget(String namespace, O obj) {
        final String cacheName = getCacheName();
        final String cacheKey = cacheKeyObjTarget(namespace, obj);
        MappingBo<O, T> bo = getFromCache(cacheName, cacheKey, MappingBo.class);
        if (bo == null) {
            bo = storageGetMappingObjTarget(namespace, obj);
            putToCache(cacheName, cacheKey, bo);
        }
        return bo;
    }

    /**
     * Get existing mapping {@code target -> object}.
     *
     * @param namespace
     * @param target
     * @return
     */
    @SuppressWarnings("unchecked")
    protected MappingBo<O, T> getMappingTargetObj(String namespace, T target) {
        final String cacheName = getCacheName();
        final String cacheKey = cacheKeyTargetObj(namespace, target);
        MappingBo<O, T> bo = getFromCache(cacheName, cacheKey, MappingBo.class);
        if (bo == null) {
            bo = storageGetMappingTargetObj(namespace, target);
            putToCache(cacheName, cacheKey, bo);
        }
        return bo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MappingsUtils.DaoResult map(String namespace, O obj, T target) {
        MappingBo<O, T> mappingToAdd = createMappingBo(namespace, obj, target);
        MappingBo<O, T> existingOT = getMappingObjTarget(namespace, obj);
        MappingsUtils.DaoResult mapResult = null;
        if (existingOT == null || !target.equals(existingOT.getTarget())) {
            mapResult = storageMap(mappingToAdd, existingOT);
            if (mapResult.matchStatus(DaoOperationStatus.SUCCESSFUL,
                    DaoOperationStatus.DUPLICATED_KEY)) {
                invalidate(existingOT, CacheInvalidationReason.DELETE);
                invalidate(mapResult.getSingleOutput(), CacheInvalidationReason.CREATE);
            }
        }
        return new MappingsUtils.DaoResult(
                mapResult == null ? DaoOperationStatus.SUCCESSFUL : mapResult.getStatus(),
                existingOT == null ? null : Collections.singleton(existingOT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MappingsUtils.DaoResult unmap(String namespace, O obj, T target) {
        MappingBo<O, T> mappingToRemove = createMappingBo(namespace, obj, target);
        MappingsUtils.DaoResult unmapResult = null;
        MappingBo<O, T> existingOT = getMappingObjTarget(namespace, obj);
        if (existingOT != null && target.equals(existingOT.getTarget())) {
            unmapResult = storageUnmap(mappingToRemove);
            if (unmapResult.matchStatus(DaoOperationStatus.SUCCESSFUL)) {
                invalidate(existingOT, CacheInvalidationReason.DELETE);
            }
        }
        return new MappingsUtils.DaoResult(
                unmapResult == null ? DaoOperationStatus.NOT_FOUND : unmapResult.getStatus(),
                existingOT == null ? null : Collections.singleton(existingOT));
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public Collection<MappingBo<O, T>> getMappingsForObject(String namespace, O obj) {
        MappingBo<O, T> existing = getMappingObjTarget(namespace, obj);
        return existing != null ? Collections.singleton(existing) : Collections.EMPTY_SET;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public Collection<MappingBo<O, T>> getMappingsForTarget(String namespace, T target) {
        MappingBo<O, T> existing = getMappingTargetObj(namespace, target);
        return existing != null ? Collections.singleton(existing) : Collections.EMPTY_SET;
    }

    /**
     * Get existing mapping {@code object -> target} from storage. Sub-class
     * will implement this method.
     *
     * @param namespace
     * @param obj
     * @return
     */
    protected abstract MappingBo<O, T> storageGetMappingObjTarget(String namespace, O obj);

    /**
     * Get existing mapping {@code target -> object} from storage. Sub-class
     * will implement this method.
     *
     * @param namespace
     * @param target
     * @return
     */
    protected abstract MappingBo<O, T> storageGetMappingTargetObj(String namespace, T target);

    /**
     * Save mapping {@code object <-> target} to storage. Sub-class will
     * implement this method.
     *
     * @param mappingToAdd
     * @param existingOT
     * @return
     */
    protected abstract MappingsUtils.DaoResult storageMap(MappingBo<O, T> mappingToAdd,
            MappingBo<O, T> existingOT);

    /**
     * Remove mapping {@code object <-> target} from storage. Sub-class will
     * implement this method.
     *
     * @param mappingToRemove
     * @return
     */
    protected abstract MappingsUtils.DaoResult storageUnmap(MappingBo<O, T> mappingToRemove);
}
