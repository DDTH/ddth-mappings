package com.github.ddth.mappings;

import java.util.Collection;
import java.util.Map;

import com.github.ddth.mappings.utils.MappingsUtils;

/**
 * Mappings API.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public interface IMappingDao<O, T> {

    public final static String STATS_KEY_TOTAL_ITEMS = "total-items";
    public final static String STATS_KEY_TOTAL_OBJS = "total-objs";
    public final static String STATS_KEY_TOTAL_TARGETS = "total-targets";

    /**
     * Map an object to target.
     *
     * @param namespace
     * @param obj
     * @param target
     * @return the existing object that mapped to the target (if any)
     */
    public MappingsUtils.DaoResult map(String namespace, O obj, T target);

    /**
     * Unmap an object from target.
     *
     * @param namespace
     * @param obj
     * @param target
     * @return the existing object that is mapping to the target (if any)
     */
    public MappingsUtils.DaoResult unmap(String namespace, O obj, T target);

    /**
     * Get all mappings (i.e targets) for an object.
     *
     * @param namespace
     * @param obj
     * @return
     */
    public Collection<MappingBo<O, T>> getMappingsForObject(String namespace, O obj);

    /**
     * Get all mappings (i.e objects) for a target.
     *
     * @param namespace
     * @param target
     * @return
     */
    public Collection<MappingBo<O, T>> getMappingsForTarget(String namespace, T target);

    /**
     * Get mappings stats.
     *
     * @param namespace
     * @return
     */
    public Map<String, Long> getStats(String namespace);
}
