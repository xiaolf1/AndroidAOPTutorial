package com.lenebf.plugin.al.transform;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ActivityClassVisitor extends ClassVisitor {

    /**
     * Activity 的父类完整类名，这里我们只处理了 AppcompatActivity 的子类，
     * 生产中需要处理其他的 Activity 子类
     */
    private static final String ACTIVITY_SUPER_NAME = "androidx/appcompat/app/AppCompatActivity";
    private static final String ON_PAUSE = "onPause";
    private static final String ON_RESUME = "onResume";

    private String superName = null;
    private boolean visitedOnPause = false;
    private boolean visitedOnResume = false;

    public ActivityClassVisitor(ClassVisitor classVisitor) {
        // Opcodes.ASM9 表示我们使用的 ASM API 的版本，这里使用的最新版本的 API 9
        super(Opcodes.ASM9, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName,
                      String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        // 访问到了具体的类信息，name 当前类的完整类名，superName 表示父类完整类名，access 可访问性
        // 排除掉抽象类
        if ((access & Opcodes.ACC_ABSTRACT) != Opcodes.ACC_ABSTRACT) {
            this.superName = superName;
        }
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        if (superName != null && superName.equals(ACTIVITY_SUPER_NAME)) {
            // 类解析结束，还没有遍历到 onPause 或者 onResume 方法，直接生成完整函数
            if (!visitedOnResume) {
                visitedOnResume = true;
                insertMethodAndLog(ON_RESUME);
            }
            if (!visitedOnPause) {
                visitedOnPause = true;
                insertMethodAndLog(ON_PAUSE);
            }
        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                     String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (superName != null && superName.equals(ACTIVITY_SUPER_NAME)) {
            // AppcompatActivity 的子类
            if (ON_PAUSE.equals(name)) {
                // onPause 方法
                visitedOnPause = true;
                addLogCodeForMethod(mv, name);
            } else if (ON_RESUME.equals(name)) {
                // onResume 方法
                visitedOnResume = true;
                addLogCodeForMethod(mv, name);
            }
        }
        return mv;
    }

    private void addLogCodeForMethod(MethodVisitor mv, String methodName) {
        mv.visitLdcInsn("lenebf");
        // 新建一个 StringBuilder 实例
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
        mv.visitInsn(Opcodes.DUP);
        // 调用 StringBuilder 的初始化方法
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        // 获取当前类的 SimpleName
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getSimpleName", "()Ljava/lang/String;", false);
        // 将当前类的 SimpleName 追加进 StringBuilder
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        // 将方法名追加进 StringBuilder
        mv.visitLdcInsn(": " + methodName);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        // 调用 StringBuilder 的 toString 方法将 StringBuilder 转化为 String
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        // 调用 Log.d 方法
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "android/util/Log", "d", "(Ljava/lang/String;Ljava/lang/String;)I", false);
        mv.visitInsn(Opcodes.POP);
    }

    private void insertMethodAndLog(String methodName) {
        // 创建新方法
        MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PROTECTED, methodName, "()V", null, null);
        // 访问新方法填充方法逻辑，
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "androidx/appcompat/app/AppCompatActivity", methodName, "()V", false);
        mv.visitLdcInsn("lenebf");
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getSimpleName", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitLdcInsn(": " + methodName);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "android/util/Log", "d", "(Ljava/lang/String;Ljava/lang/String;)I", false);
        mv.visitInsn(Opcodes.POP);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitEnd();
    }
}
