package com.lenebf.android.buildtime;

import java.lang.reflect.Method;

public class BuildTime {

    public static long get(Object object) {
        try {
            String btClassName = object.getClass().getCanonicalName() + "_BT";
            Class<?> btClass = Class.forName(btClassName);
            Method getBuildTimeMethod = btClass.getMethod("getBuildTime");
            return (long) getBuildTimeMethod.invoke(object);
        } catch (Throwable throwable) {
            return -1L;
        }
    }
}