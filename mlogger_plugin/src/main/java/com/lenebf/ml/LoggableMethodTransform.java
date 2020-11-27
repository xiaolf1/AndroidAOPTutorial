package com.lenebf.ml;

import com.android.SdkConstants;
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
import org.objectweb.asm.ClassWriter;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LoggableMethodTransform extends Transform {

    @Override
    public String getName() {
        // 转换器的名字
        return "mlogger";
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
                File inputDir = directoryInput.getFile();
                for (File file : FileUtils.getAllFiles(inputDir)) {
                    // 找到转化输入中所有的 class 文件
                    if (!file.getName().endsWith(SdkConstants.DOT_CLASS)) {
                        continue;
                    }
                    FileInputStream inputStream = null;
                    FileOutputStream outputStream = null;
                    try {
                        // 由于遍历到方法参数名称时，方法遍历已结束，无法再修改方法逻辑了
                        // 只能先解析一遍获取到所有被标记的方法参数名称，再去修改 .class
                        inputStream = new FileInputStream(file);
                        ClassReader classReader = new ClassReader(inputStream);
                        HashMap<String, String[]> methodToArgumentArray = new HashMap<>();
                        classReader.accept(new LoggableMethodParser(null,
                                methodToArgumentArray), ClassReader.EXPAND_FRAMES);
                        closeQuietly(inputStream);
                        if (methodToArgumentArray.size() > 0) {
                            for (Map.Entry<String, String[]> entity : methodToArgumentArray.entrySet()) {
                                System.out.println("\n\n-------------------\n" +
                                        entity.getKey() + ": ");
                                String[] names = entity.getValue();
                                for (String name : names) {
                                    System.out.println("      " + name);
                                }
                            }
                            // 添加方法日志打印代码
                            inputStream = new FileInputStream(file);
                            classReader = new ClassReader(inputStream);
                            //对class文件的写入
                            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                            //访问class文件相应的内容，解析到某一个结构就会通知到ClassVisitor的相应方法
                            LoggableMethodPrinter printer = new LoggableMethodPrinter(classWriter,
                                    methodToArgumentArray);
                            //对class文件进行读取与解析
                            // 依次调用 ClassVisitor接口的各个方法
                            classReader.accept(printer, ClassReader.EXPAND_FRAMES);
                            closeQuietly(inputStream);
                            // toByteArray方法会将最终修改的字节码以 byte 数组形式返回。
                            byte[] bytes = classWriter.toByteArray();
                            //通过文件流写入方式覆盖掉原先的内容，实现class文件的改写。
                            outputStream = new FileOutputStream(file);
                            outputStream.write(bytes);
                            outputStream.flush();
                        }
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
}
