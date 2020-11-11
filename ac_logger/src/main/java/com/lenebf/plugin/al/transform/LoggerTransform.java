package com.lenebf.plugin.al.transform;

import com.android.build.api.transform.Format;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.android.utils.FileUtils;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class LoggerTransform extends Transform {

    @Override
    public String getName() {
        // 转换器的名字
        return "ac_logger";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        // 返回转换器需要消费的数据类型。我们需要处理所有的 class 内容
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        // 返回转换器的作用域，即处理范围。我们只处理 Project 里面的类
        return TransformManager.PROJECT_ONLY;
    }

    @Override
    public boolean isIncremental() {
        // 是否支持增量，我们简单点不支持
        return false;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException,
            InterruptedException, IOException {
        super.transform(transformInvocation);
        // 找到转化输入中所有的 class 文件
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();
        if (outputProvider == null) {
            return;
        }
        // 由于我们不支持增量编译，清空 OutputProvider 的内容
        outputProvider.deleteAll();

        Collection<TransformInput> transformInputs = transformInvocation.getInputs();
        transformInputs.forEach(transformInput -> {
            // 存在两种转换输入，一种是目录，一种是Jar文件(三方库)
            // 处理目录输入
            transformInput.getDirectoryInputs().forEach(directoryInput -> {
                File directoryInputFile = directoryInput.getFile();
                List<File> files = filterClassFiles(directoryInputFile);
                for (File file : files) {
                    FileInputStream inputStream = null;
                    FileOutputStream outputStream = null;
                    try {
                        //对class文件的写入
                        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                        //访问class文件相应的内容，解析到某一个结构就会通知到ClassVisitor的相应方法
                        ClassVisitor classVisitor = new ActivityClassVisitor(classWriter);
                        //对class文件进行读取与解析
                        inputStream = new FileInputStream(file);
                        ClassReader classReader = new ClassReader(inputStream);
                        // 依次调用 ClassVisitor接口的各个方法
                        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
                        // toByteArray方法会将最终修改的字节码以 byte 数组形式返回。
                        byte[] bytes = classWriter.toByteArray();
                        //通过文件流写入方式覆盖掉原先的内容，实现class文件的改写。
                        outputStream = new FileOutputStream(file.getPath());
                        outputStream.write(bytes);
                        outputStream.flush();
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    } finally {
                        closeQuietly(inputStream);
                        closeQuietly(outputStream);
                    }
                }
                // 有输入进来就必须将其输出，否则会出现类缺失的问题，
                // 无论是否经过转换，我们都需要将输入目录复制到目标目录
                File dest = outputProvider.getContentLocation(directoryInput.getName(),
                        directoryInput.getContentTypes(),
                        directoryInput.getScopes(),
                        Format.DIRECTORY);
                try {
                    FileUtils.copyDirectory(directoryInput.getFile(), dest);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            // 处理 jar 输入
            transformInput.getJarInputs().forEach(jarInput -> {
                // 有输入进来就必须将其输出，否则会出现类缺失的问题，这里我们不需要修改Jar文件，直接将其输出
                File jarInputFile = jarInput.getFile();
                File dest = outputProvider.getContentLocation(jarInput.getName(),
                        jarInput.getContentTypes(),
                        jarInput.getScopes(),
                        Format.JAR);
                try {
                    FileUtils.copyFile(jarInputFile, dest);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private List<File> filterClassFiles(File file) {
        List<File> classFiles = new ArrayList<>();
        if (file != null) {
            listFiles(file, classFiles, new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getName().endsWith(".class");
                }
            });
        }
        return classFiles;
    }

    private void listFiles(File file, List<File> result, FileFilter filter) {
        if (result == null || file == null) {
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    listFiles(child, result, filter);
                }
            }
        } else {
            if (filter == null || filter.accept(file)) {
                result.add(file);
            }
        }
    }
}
