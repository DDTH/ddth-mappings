package com.github.ddth.mappings;

import com.github.ddth.dao.BaseDao;
import com.github.ddth.dao.utils.CacheInvalidationReason;

/**
 * Abstract implementation of mapping DAO.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public abstract class AbstractMappingDao<O, T> extends BaseDao implements IMappingDao<O, T> {

    private String cacheName;

    protected Class<O> objectClass;
    protected Class<T> targetClass;

    public AbstractMappingDao(Class<O> objectClass, Class<T> targetClass) {
        this.objectClass = objectClass;
        this.targetClass = targetClass;
    }

    public Class<O> getObjectClass() {
        return objectClass;
    }

    public Class<T> getTargetClass() {
        return targetClass;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    protected String cacheKeyObjTarget(String namespace, O obj) {
        return "O-" + namespace + "-" + obj;
    }

    protected String cacheKeyTargetObj(String namespace, T target) {
        return "T-" + namespace + "-" + target;
    }

    protected String cacheKeyObjTarget(MappingBo<O, T> bo) {
        return cacheKeyObjTarget(bo.getNamespace(), bo.getObject());
    }

    protected String cacheKeyTargetObj(MappingBo<O, T> bo) {
        return cacheKeyTargetObj(bo.getNamespace(), bo.getTarget());
    }

    protected abstract void invalidate(MappingBo<O, T> bo, CacheInvalidationReason cir);

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractMappingDao<O, T> init() {
        super.init();
        return this;
    }

    /**
     * Create a new mapping. Sub-class may override this method to customize its
     * business logic.
     * 
     * @param namespace
     * @param obj
     * @param target
     * @return
     */
    protected MappingBo<O, T> createMappingBo(String namespace, O obj, T target) {
        MappingBo<O, T> bo = MappingBo.newInstance(namespace, obj, target);
        return bo;
    }

    /**
     * Serialize mapping object.
     * 
     * <p>
     * Note: this method does nothing, simply returns the object as-is.
     * Sub-class may override this method to implement its own logic.
     * </p>
     * 
     * @param obj
     * @return
     */
    protected Object serializeObject(O obj) {
        return obj;
    }

    /**
     * Counterpart of {@link #serializeObject(O)}.
     * 
     * <p>
     * Note: this method does nothing, simply returns the object as-is.
     * Sub-class may override this method to implement its own logic.
     * </p>
     * 
     * @param obj
     * @return
     */
    @SuppressWarnings("unchecked")
    protected O deserializeObject(Object obj) {
        return (O) obj;
    }

    /**
     * Serialize mapping target .
     * 
     * <p>
     * Note: this method does nothing, simply returns the target as-is.
     * Sub-class may override this method to implement its own logic.
     * </p>
     * 
     * @param target
     * @return
     */
    protected Object serializeTarget(T target) {
        return target;
    }

    /**
     * Counterpart of {@link #serializeTarget(T)}.
     * 
     * <p>
     * Note: this method does nothing, simply returns the target as-is.
     * Sub-class may override this method to implement its own logic.
     * </p>
     * 
     * @param target
     * @return
     */
    @SuppressWarnings("unchecked")
    protected T deserializeTarget(Object target) {
        return (T) target;
    }

}
