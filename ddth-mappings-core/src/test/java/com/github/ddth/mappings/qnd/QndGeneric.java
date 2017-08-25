package com.github.ddth.mappings.qnd;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class QndGeneric {

    public static class MyGeneric<T> {
        private Class<T> typeClass;

        @SuppressWarnings("unchecked")
        public MyGeneric() {
            Class<?> clazz = getClass();
            Type type = clazz.getGenericSuperclass();
            while (type != null) {
                if (type instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) type;
                    Type _temp = parameterizedType.getActualTypeArguments()[0];
                    System.out.println(_temp.getTypeName());
                    this.typeClass = (Class<T>) _temp;
                    break;
                } else {
                    clazz = clazz.getSuperclass();
                    type = clazz != null ? clazz.getGenericSuperclass() : null;
                }
            }
        }

        public Class<T> getTypeClass() {
            return typeClass;
        }
    }

    public static class MyString extends MyGeneric<String> {
    }

    public static class MyOtherString extends MyString {
    }

    public static class MyOtherGeneric<T> extends MyGeneric<T> {
    }

    public static class MyStringX extends MyOtherGeneric<String> {
    }

    public static class MyOtherStringX extends MyStringX {
    }

    public static void main(String[] args) {
        System.out.println(new MyGeneric<String>().getTypeClass());
        System.out.println(new MyString().getTypeClass());
        System.out.println(new MyOtherString().getTypeClass());

        System.out.println(new MyOtherGeneric<String>().getTypeClass());
        System.out.println(new MyStringX().getTypeClass());
        System.out.println(new MyOtherStringX().getTypeClass());
    }

}
