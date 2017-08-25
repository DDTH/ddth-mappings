package com.github.ddth.mappings;

import java.util.Date;

/**
 * Integer-String mapping.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 * @see MappingBo
 */
public class MappingISBo extends MappingBo<Integer, String> {
    public final static MappingISBo[] EMPTY_ARRAY = new MappingISBo[0];

    public static MappingISBo newInstance(String ns, Integer obj, String target) {
        return newInstance(ns, obj, target, new Date(), null);
    }

    public static MappingISBo newInstance(String ns, Integer obj, String target, String info) {
        return newInstance(ns, obj, target, new Date(), info);
    }

    public static MappingISBo newInstance(String ns, Integer obj, String target, long timestamp) {
        return newInstance(ns, obj, target, new Date(timestamp), null);
    }

    public static MappingISBo newInstance(String ns, Integer obj, String target, Date timestamp) {
        return newInstance(ns, obj, target, timestamp, null);
    }

    public static MappingISBo newInstance(String ns, Integer obj, String target, long timestamp,
            String info) {
        return newInstance(ns, obj, target, new Date(timestamp), info);
    }

    public static MappingISBo newInstance(String ns, Integer obj, String target, Date timestamp,
            String info) {
        MappingISBo bo = new MappingISBo();
        bo.setNamespace(ns).setObject(obj).setTarget(target).setTimestamp(timestamp).setInfo(info);
        return bo;
    }

    public MappingISBo() {
        super(Integer.class, String.class);
    }
}
