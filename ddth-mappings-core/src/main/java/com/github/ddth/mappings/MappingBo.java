package com.github.ddth.mappings;

import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.github.ddth.dao.BaseDataJsonFieldBo;

/**
 * Mapping {@code object <--> target}.
 *
 * <p>
 * Fields:
 * <ol>
 * <li>{@link #ATTR_NAMESPACE}: namespace, so that multiple mappings can share a
 * same storage.</li>
 * <li>{@link #ATTR_OBJECT}: mapping object.</li>
 * <li>{@link #ATTR_TARGET}: mapping target.</li>
 * <li>{@link #ATTR_TIMESTAMP}: timestamp when the mapping is created/updated
 * (depends implementation).</li>
 * <li>{@link #ATTR_INFO}: extra info (JSON encoding)</li>
 * </ol>
 * </p>
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class MappingBo<O, T> extends BaseDataJsonFieldBo {
    @SuppressWarnings("rawtypes")
    public final static MappingBo[] EMPTY_ARRAY = new MappingBo[0];

    public static <O, T> MappingBo<O, T> newInstance(String ns, O obj, T target) {
        return newInstance(ns, obj, target, new Date(), null);
    }

    public static <O, T> MappingBo<O, T> newInstance(String ns, O obj, T target, String info) {
        return newInstance(ns, obj, target, new Date(), info);
    }

    public static <O, T> MappingBo<O, T> newInstance(String ns, O obj, T target, long timestamp) {
        return newInstance(ns, obj, target, new Date(timestamp), null);
    }

    public static <O, T> MappingBo<O, T> newInstance(String ns, O obj, T target, Date timestamp) {
        return newInstance(ns, obj, target, timestamp, null);
    }

    public static <O, T> MappingBo<O, T> newInstance(String ns, O obj, T target, long timestamp,
            String info) {
        return newInstance(ns, obj, target, new Date(timestamp), info);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <O, T> MappingBo<O, T> newInstance(String ns, O obj, T target, Date timestamp,
            String info) {
        MappingBo<O, T> bo = new MappingBo(obj.getClass(), target.getClass());
        bo.setNamespace(ns).setObject(obj).setTarget(target).setTimestamp(timestamp).setInfo(info);
        return bo;
    }

    protected final static String ATTR_NAMESPACE = "ns";
    protected final static String ATTR_OBJECT = "obj";
    protected final static String ATTR_TARGET = "target";
    protected final static String ATTR_TIMESTAMP = "t";

    protected Class<O> objectClass;
    protected Class<T> targetClass;

    public MappingBo(Class<O> objectClass, Class<T> targetClass) {
        this.objectClass = objectClass;
        this.targetClass = targetClass;
        // Class<?> clazz = getClass();
        // Type type = clazz.getGenericSuperclass();
        // while (type != null) {
        // if (type instanceof ParameterizedType) {
        // ParameterizedType parameterizedType = (ParameterizedType) type;
        // this.objectClass = (Class<O>)
        // parameterizedType.getActualTypeArguments()[0];
        // this.targetClass = (Class<T>)
        // parameterizedType.getActualTypeArguments()[1];
        // break;
        // } else {
        // clazz = clazz.getSuperclass();
        // type = clazz != null ? clazz.getGenericSuperclass() : null;
        // }
        // }
    }

    public Class<O> getObjectClass() {
        return objectClass;
    }

    public Class<T> getTargetClass() {
        return targetClass;
    }

    public String getNamespace() {
        return getAttribute(ATTR_NAMESPACE, String.class);
    }

    public MappingBo<O, T> setNamespace(String value) {
        setAttribute(ATTR_NAMESPACE, value != null ? value.trim().toLowerCase() : null);
        return this;
    }

    public O getObject() {
        return getAttribute(ATTR_OBJECT, objectClass);
    }

    public MappingBo<O, T> setObject(O value) {
        setAttribute(ATTR_OBJECT, value);
        return this;
    }

    public T getTarget() {
        return getAttribute(ATTR_TARGET, targetClass);
    }

    public MappingBo<O, T> setTarget(T value) {
        setAttribute(ATTR_TARGET, value);
        return this;
    }

    public Date getTimestamp() {
        return getAttribute(ATTR_TIMESTAMP, Date.class);
    }

    public long getTimestampAsLong() {
        Date value = getTimestamp();
        return value != null ? value.getTime() : 0;
    }

    public MappingBo<O, T> setTimestamp(Date value) {
        setAttribute(ATTR_TIMESTAMP, value);
        return this;
    }

    public MappingBo<O, T> setTimestamp(long value) {
        setAttribute(ATTR_TIMESTAMP, value);
        return this;
    }

    public String getInfo() {
        return getData();
    }

    public MappingBo<O, T> setInfo(String value) {
        setData(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        HashCodeBuilder hcb = new HashCodeBuilder(19, 81);
        hcb.append(getNamespace()).append(getObject()).append(getTarget());
        return hcb.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof MappingBo) {
            MappingBo<O, T> other = (MappingBo<O, T>) obj;
            EqualsBuilder eq = new EqualsBuilder();
            eq.append(getNamespace(), other.getNamespace()).append(getObject(), other.getObject())
                    .append(getTarget(), other.getTarget());
            return eq.isEquals();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        tsb.append("ns", getNamespace()).append("obj", getObject()).append("target", getTarget());
        return tsb.toString();
    }

}
