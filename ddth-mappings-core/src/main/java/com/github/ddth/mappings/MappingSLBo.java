package com.github.ddth.mappings;

import java.util.Date;

/**
 * String-Long mapping.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 * @see MappingBo
 */
public class MappingSLBo extends MappingBo<String, Long> {
    public final static MappingSLBo[] EMPTY_ARRAY = new MappingSLBo[0];

    public static MappingSLBo newInstance(String ns, String obj, Long target) {
        return newInstance(ns, obj, target, new Date(), null);
    }

    public static MappingSLBo newInstance(String ns, String obj, Long target, String info) {
        return newInstance(ns, obj, target, new Date(), info);
    }

    public static MappingSLBo newInstance(String ns, String obj, Long target, long timestamp) {
        return newInstance(ns, obj, target, new Date(timestamp), null);
    }

    public static MappingSLBo newInstance(String ns, String obj, Long target, Date timestamp) {
        return newInstance(ns, obj, target, timestamp, null);
    }

    public static MappingSLBo newInstance(String ns, String obj, Long target, long timestamp,
            String info) {
        return newInstance(ns, obj, target, new Date(timestamp), info);
    }

    public static MappingSLBo newInstance(String ns, String obj, Long target, Date timestamp,
            String info) {
        MappingSLBo bo = new MappingSLBo();
        bo.setNamespace(ns).setObject(obj).setTarget(target).setTimestamp(timestamp).setInfo(info);
        return bo;
    }

    public MappingSLBo() {
        super(String.class, Long.class);
    }

}
