package com.lenebf.ml;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.HashMap;

public class LoggableMethodPrinter extends ClassVisitor {

    private final HashMap<String, String[]> methodToArgumentArray;

    public LoggableMethodPrinter(ClassVisitor classVisitor,
                                 HashMap<String, String[]> methodToArgumentArray) {
        super(Opcodes.ASM9, classVisitor);
        this.methodToArgumentArray = methodToArgumentArray;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                     String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (methodToArgumentArray != null && methodToArgumentArray.containsKey(name)) {
            boolean staticMethod = (access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC;
            int offset = staticMethod ? 0 : 1;
            Type[] types = Type.getArgumentTypes(descriptor);
            String[] argumentNames = methodToArgumentArray.get(name);
            final int argumentsCount = argumentNames.length;
            mv.visitLdcInsn("lenebf");
            mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            mv.visitLdcInsn("Invoke method " + name + "(");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            String argumentName;
            String argumentTypeDescriptor;
            for (int index = 0; index < argumentsCount; index++) {
                argumentName = argumentNames[index];
                if ("this".equals(argumentName)) {
                    // 排除掉 this 参数
                    continue;
                }
                // 参数名称
                mv.visitLdcInsn(argumentName + ": ");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
                // 参数取值
                argumentTypeDescriptor = types[index - offset].getDescriptor();
                mv.visitVarInsn(Opcodes.ILOAD, index);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(" + argumentTypeDescriptor + ")Ljava/lang/StringBuilder;", false);
                if (index != argumentsCount - 1) {
                    // 非最后一项插入 , 隔开参数
                    mv.visitLdcInsn(", ");
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
                }
            }
            mv.visitLdcInsn(")");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "android/util/Log", "d", "(Ljava/lang/String;Ljava/lang/String;)I", false);
            mv.visitInsn(Opcodes.POP);
        }
        return mv;
    }
}
