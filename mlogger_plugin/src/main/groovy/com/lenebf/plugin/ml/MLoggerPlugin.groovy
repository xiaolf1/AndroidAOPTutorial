/*
 * This Groovy source file was generated by the Gradle 'init' task.
 */
package com.lenebf.plugin.ml

import com.android.build.gradle.AppExtension
import com.lenebf.ml.LoggableMethodTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * A simple 'hello world' plugin.
 */
public class MLoggerPlugin implements Plugin<Project> {
    public void apply(Project project) {
        // Register a transform
        def android = project.extensions.getByType(AppExtension)
        android.registerTransform(new LoggableMethodTransform())
    }
}
