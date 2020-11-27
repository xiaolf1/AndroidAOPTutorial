package com.lenebf.android.mlog;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 我们的 Transform API 处理的是 .class 文件，所以我们需要注解被编译器保留
@Retention(RetentionPolicy.CLASS)
// 我们的注解只能应用于方法函数
@Target({ElementType.METHOD})
public @interface MethodLoggable {
}