package com.github.ddth.mappings;

import java.util.Date;

/**
 * String-String mapping.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 * @see MappingBo
 */
public class MappingSSBo extends MappingBo<String, String> {
    public final static MappingSSBo[] EMPTY_ARRAY = new MappingSSBo[0];

    public static MappingSSBo newInstance(String ns, String obj, String target) {
        return newInstance(ns, obj, target, new Date(), null);
    }

    public static MappingSSBo newInstance(String ns, String obj, String target, String info) {
        return newInstance(ns, obj, target, new Date(), info);
    }

    public static MappingSSBo newInstance(String ns, String obj, String target, long timestamp) {
        return newInstance(ns, obj, target, new Date(timestamp), null);
    }

    public static MappingSSBo newInstance(String ns, String obj, String target, Date timestamp) {
        return newInstance(ns, obj, target, timestamp, null);
    }

    public static MappingSSBo newInstance(String ns, String obj, String target, long timestamp,
            String info) {
        return newInstance(ns, obj, target, new Date(timestamp), info);
    }

    public static MappingSSBo newInstance(String ns, String obj, String target, Date timestamp,
            String info) {
        MappingSSBo bo = new MappingSSBo();
        bo.setNamespace(ns).setObject(obj).setTarget(target).setTimestamp(timestamp).setInfo(info);
        return bo;
    }

    public MappingSSBo() {
        super(String.class, String.class);
    }

}
