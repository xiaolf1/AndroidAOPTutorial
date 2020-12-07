package com.lenebf.android.buildtime_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 注解处理器处理的是 .java 文件，我们只需要注解在源码级别被保留
@Retention(RetentionPolicy.SOURCE)
// 我们的注解只能应用于类
@Target({ElementType.TYPE})
public @interface KeepBuildTime {
}