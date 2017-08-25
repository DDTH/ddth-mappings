package com.github.ddth.mappings;

import java.util.Date;

/**
 * Long-Long mapping.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 * @see MappingBo
 */
public class MappingLLBo extends MappingBo<Long, Long> {

    public final static MappingLLBo[] EMPTY_ARRAY = new MappingLLBo[0];

    public static MappingLLBo newInstance(String ns, Long obj, Long target) {
        return newInstance(ns, obj, target, new Date(), null);
    }

    public static MappingLLBo newInstance(String ns, Long obj, Long target, String info) {
        return newInstance(ns, obj, target, new Date(), info);
    }

    public static MappingLLBo newInstance(String ns, Long obj, Long target, long timestamp) {
        return newInstance(ns, obj, target, new Date(timestamp), null);
    }

    public static MappingLLBo newInstance(String ns, Long obj, Long target, Date timestamp) {
        return newInstance(ns, obj, target, timestamp, null);
    }

    public static MappingLLBo newInstance(String ns, Long obj, Long target, long timestamp,
            String info) {
        return newInstance(ns, obj, target, new Date(timestamp), info);
    }

    public static MappingLLBo newInstance(String ns, Long obj, Long target, Date timestamp,
            String info) {
        MappingLLBo bo = new MappingLLBo();
        bo.setNamespace(ns).setObject(obj).setTarget(target).setTimestamp(timestamp).setInfo(info);
        return bo;
    }

    public MappingLLBo() {
        super(Long.class, Long.class);
    }

}
