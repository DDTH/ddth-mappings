package com.github.ddth.mappings.utils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.github.ddth.mappings.MappingBo;

/**
 * Utility class.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class MappingsUtils {

    public final static Charset UTF8 = Charset.forName("utf-8");

    public static class DaoResult extends com.github.ddth.dao.utils.DaoResult {

        public DaoResult(DaoOperationStatus status) {
            this(status, null);
        }

        public DaoResult(DaoOperationStatus status, Collection<MappingBo<?, ?>> output) {
            super(status, output != null ? new HashSet<MappingBo<?, ?>>(output) : new HashSet<>());
        }

        public boolean matchStatus(DaoOperationStatus... statusList) {
            for (DaoOperationStatus status : statusList) {
                if (status == getStatus()) {
                    return true;
                }
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public Collection<MappingBo<?, ?>> getOutput() {
            return (Collection<MappingBo<?, ?>>) super.getOutput();
        }

        @SuppressWarnings("unchecked")
        public <O, T> MappingBo<O, T> getSingleOutput() {
            return (MappingBo<O, T>) getOutput().stream().findAny().orElse(null);
        }
    }

    /**
     * Simple encoding: separator.
     */
    public final static char SE_SEPARATOR = 0x01;

    /**
     * Simple encoding: concatenate a list of strings.
     *
     * @param inputs
     * @return
     */
    public static String seConcatToString(String... inputs) {
        return inputs != null ? StringUtils.join(inputs, SE_SEPARATOR) : "";
    }

    /**
     * Simple encoding: concatenate a list of strings.
     *
     * @param inputs
     * @return
     */
    public static byte[] seConcatToBytes(String... inputs) {
        return seConcatToString(inputs).getBytes(UTF8);
    }

    /**
     * Simple encoding: concatenate a list of strings.
     *
     * @param inputs
     * @return
     */
    public static ByteBuffer seConcatToByteBuffer(String... inputs) {
        return ByteBuffer.wrap(seConcatToBytes(inputs));
    }

    /**
     * Simple encoding: split a string to array of strings that was concatenated
     * by {@link #seConcatToString(String...)}.
     *
     * @param input
     * @return
     */
    public static String[] seSplit(String input) {
        return input != null ? StringUtils.split(input, SE_SEPARATOR)
                : ArrayUtils.EMPTY_STRING_ARRAY;
    }

    /**
     * Simple encoding: split a byte array to array of strings that was
     * concatenated by {@link #seConcatToBytes(String...)}.
     *
     * @param input
     * @return
     */
    public static String[] seSplit(byte[] input) {
        return seSplit(input != null ? new String(input, UTF8) : null);
    }

    /**
     * Simple encoding: split a byte-buffer to array of strings that was encoded
     * by {@link #seConcatToByteBuffer(String...)}.
     *
     * @param input
     * @return
     */
    public static String[] seSplit(ByteBuffer input) {
        return seSplit(input != null ? input.array() : null);
    }
}
