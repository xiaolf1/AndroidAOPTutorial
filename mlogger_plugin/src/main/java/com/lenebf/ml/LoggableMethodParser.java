package com.lenebf.ml;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.HashMap;

/**
 * @author lenebf
 * @since 2020/11/25
 */
public class LoggableMethodParser extends ClassVisitor {
    private static final String METHOD_LOGGABLE_DESC = "Lcom/lenebf/android/mlog/MethodLoggable;";

    final private HashMap<String, String[]> methodToArgumentArray;

    /**
     * @param methodToArgumentArray 用以保存解析出来的方法和对应的参数名称
     */
    public LoggableMethodParser(ClassVisitor classVisitor,
                                HashMap<String, String[]> methodToArgumentArray) {
        super(Opcodes.ASM9, classVisitor);
        this.methodToArgumentArray = methodToArgumentArray;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                     String[] exceptions) {
        // 参数名称只能从本地变量里获取，但是本地变量不仅仅包含函数参数。
        // 我们可以通过参数 Type 数组得知具体有多少个参数
        Type[] types = Type.getArgumentTypes(descriptor);
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        // 非静态方法第一个参数是 this
        boolean staticMethod = (access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC;
        return new ArgumentsReader(mv, staticMethod, name, types);
    }

    class ArgumentsReader extends MethodVisitor {

        private String methodName;

        private boolean loggableMethod = false;
        private int argumentCount;
        private String[] argumentNames;

        public ArgumentsReader(MethodVisitor methodVisitor, boolean staticMethod, String methodName,
                               Type[] types) {
            super(Opcodes.ASM9, methodVisitor);
            this.methodName = methodName;
            argumentCount = types == null ? 0 : types.length;
            if (!staticMethod) {
                // 非静态方法，多一个 this 参数
                argumentCount++;
            }
            argumentNames = new String[argumentCount];
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            // 遍历到函数注解，我们在这里可以筛选出被我们比较的函数
            if (METHOD_LOGGABLE_DESC.equals(descriptor)) {
                loggableMethod = true;
            }
            return super.visitAnnotation(descriptor, visible);
        }

        @Override
        public void visitLocalVariable(String name, String descriptor, String signature,
                                       Label start, Label end, int index) {
            super.visitLocalVariable(name, descriptor, signature, start, end, index);
            if (loggableMethod && argumentNames != null) {
                // 只收集被标记的函数参数名称
                if (index >= 0 && index < argumentCount) {
                    argumentNames[index] = name;
                }
            }
        }

        @Override
        public void visitEnd() {
            super.visitEnd();
            if (loggableMethod && argumentCount > 0) {
                // 方法解析完成，保存我们的收集结果
                methodToArgumentArray.put(methodName, argumentNames);
            }
        }
    }
}
