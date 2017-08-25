package com.github.ddth.mappings;

import java.util.Date;

/**
 * Integer-Integer mapping.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 * @see MappingBo
 */
public class MappingIIBo extends MappingBo<Integer, Integer> {

    public final static MappingIIBo[] EMPTY_ARRAY = new MappingIIBo[0];

    public static MappingIIBo newInstance(String ns, Integer obj, Integer target) {
        return newInstance(ns, obj, target, new Date(), null);
    }

    public static MappingIIBo newInstance(String ns, Integer obj, Integer target, String info) {
        return newInstance(ns, obj, target, new Date(), info);
    }

    public static MappingIIBo newInstance(String ns, Integer obj, Integer target, long timestamp) {
        return newInstance(ns, obj, target, new Date(timestamp), null);
    }

    public static MappingIIBo newInstance(String ns, Integer obj, Integer target, Date timestamp) {
        return newInstance(ns, obj, target, timestamp, null);
    }

    public static MappingIIBo newInstance(String ns, Integer obj, Integer target, long timestamp,
            String info) {
        return newInstance(ns, obj, target, new Date(timestamp), info);
    }

    public static MappingIIBo newInstance(String ns, Integer obj, Integer target, Date timestamp,
            String info) {
        MappingIIBo bo = new MappingIIBo();
        bo.setNamespace(ns).setObject(obj).setTarget(target).setTimestamp(timestamp).setInfo(info);
        return bo;
    }

    public MappingIIBo() {
        super(Integer.class, Integer.class);
    }

}
