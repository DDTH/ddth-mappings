package com.github.ddth.mappings;

import java.util.Date;

/**
 * String-Integer mapping.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 * @see MappingBo
 */
public class MappingSIBo extends MappingBo<String, Integer> {
    public final static MappingSIBo[] EMPTY_ARRAY = new MappingSIBo[0];

    public static MappingSIBo newInstance(String ns, String obj, Integer target) {
        return newInstance(ns, obj, target, new Date(), null);
    }

    public static MappingSIBo newInstance(String ns, String obj, Integer target, String info) {
        return newInstance(ns, obj, target, new Date(), info);
    }

    public static MappingSIBo newInstance(String ns, String obj, Integer target, long timestamp) {
        return newInstance(ns, obj, target, new Date(timestamp), null);
    }

    public static MappingSIBo newInstance(String ns, String obj, Integer target, Date timestamp) {
        return newInstance(ns, obj, target, timestamp, null);
    }

    public static MappingSIBo newInstance(String ns, String obj, Integer target, long timestamp,
            String info) {
        return newInstance(ns, obj, target, new Date(timestamp), info);
    }

    public static MappingSIBo newInstance(String ns, String obj, Integer target, Date timestamp,
            String info) {
        MappingSIBo bo = new MappingSIBo();
        bo.setNamespace(ns).setObject(obj).setTarget(target).setTimestamp(timestamp).setInfo(info);
        return bo;
    }

    public MappingSIBo() {
        super(String.class, Integer.class);
    }

}
