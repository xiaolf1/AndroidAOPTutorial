package com.lenebf.android.buildtime_processor;

import com.google.auto.service.AutoService;
import com.lenebf.android.buildtime_annotation.KeepBuildTime;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;


@AutoService(Processor.class)
public class BuildTimeProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        // 获取元素处理工具类
        elementUtils = processingEnvironment.getElementUtils();
        // 用于创建新源文件、类文件或辅助文件的文件管理器
        filer = processingEnvironment.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        // 注解处理器需要处理哪些注解
        return Collections.singleton(KeepBuildTime.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        // 处理注解
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(KeepBuildTime.class);
        for (Element element : elements) {
            generateBTCLass((TypeElement) element);
        }
        return true;
    }

    private void generateBTCLass(TypeElement element) {
        // 被注解的类名
        String btClassName = element.getSimpleName().toString() + "_BT";
        // 被注解的类包名
        String packageName = elementUtils.getPackageOf(element).toString();
        // 生成获取编译时间的工具类
        TypeSpec btClass = TypeSpec.classBuilder(btClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                // 生成获取编译时间的方法
                .addMethod(generateGetBuildTimeMethod())
                .build();
        JavaFile javaFile = JavaFile.builder(packageName, btClass).build();
        try {
            // 输出生成的新类
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MethodSpec generateGetBuildTimeMethod() {
        long buildTime = System.currentTimeMillis();
        // 方法名为 getBuildTime
        return MethodSpec.methodBuilder("getBuildTime")
                // 方法为静态公开方法
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                // 返回 long 型
                .returns(long.class)
                .addStatement("return " + buildTime + "L")
                .build();
    }
}