package com.github.ddth.mappings;

import java.util.Date;

/**
 * Long-String mapping.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 * @see MappingBo
 */
public class MappingLSBo extends MappingBo<Long, String> {
    public final static MappingLSBo[] EMPTY_ARRAY = new MappingLSBo[0];

    public static MappingLSBo newInstance(String ns, Long obj, String target) {
        return newInstance(ns, obj, target, new Date(), null);
    }

    public static MappingLSBo newInstance(String ns, Long obj, String target, String info) {
        return newInstance(ns, obj, target, new Date(), info);
    }

    public static MappingLSBo newInstance(String ns, Long obj, String target, long timestamp) {
        return newInstance(ns, obj, target, new Date(timestamp), null);
    }

    public static MappingLSBo newInstance(String ns, Long obj, String target, Date timestamp) {
        return newInstance(ns, obj, target, timestamp, null);
    }

    public static MappingLSBo newInstance(String ns, Long obj, String target, long timestamp,
            String info) {
        return newInstance(ns, obj, target, new Date(timestamp), info);
    }

    public static MappingLSBo newInstance(String ns, Long obj, String target, Date timestamp,
            String info) {
        MappingLSBo bo = new MappingLSBo();
        bo.setNamespace(ns).setObject(obj).setTarget(target).setTimestamp(timestamp).setInfo(info);
        return bo;
    }

    public MappingLSBo() {
        super(Long.class, String.class);
    }
}
