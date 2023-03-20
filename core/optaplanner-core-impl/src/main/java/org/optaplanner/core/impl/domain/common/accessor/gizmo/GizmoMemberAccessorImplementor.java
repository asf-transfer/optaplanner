package org.optaplanner.core.impl.domain.common.accessor.gizmo;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import org.optaplanner.core.impl.domain.common.accessor.MemberAccessor;
import org.optaplanner.core.impl.util.MutableReference;

import io.quarkus.gizmo.ClassCreator;
import io.quarkus.gizmo.ClassOutput;
import io.quarkus.gizmo.FieldDescriptor;
import io.quarkus.gizmo.MethodCreator;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;
import io.quarkus.gizmo.TryBlock;

/**
 * Generates the bytecode for the MemberAccessor of a particular Member
 */
public final class GizmoMemberAccessorImplementor {

    final static String GENERIC_TYPE_FIELD = "genericType";
    final static String ANNOTATED_ELEMENT_FIELD = "annotatedElement";

    /**
     * Generates the constructor and implementations of {@link AbstractGizmoMemberAccessor} methods for the given
     * {@link Member}.
     *
     * @param className never null
     * @param classOutput never null, defines how to write the bytecode
     * @param memberInfo never null, member to generate MemberAccessor methods implementation for
     */
    public static void defineAccessorFor(String className, ClassOutput classOutput, GizmoMemberInfo memberInfo) {
        try (ClassCreator classCreator = ClassCreator.builder()
                .className(className)
                .superClass(AbstractGizmoMemberAccessor.class)
                .classOutput(classOutput)
                .setFinal(true)
                .build()) {
            classCreator.getFieldCreator("genericType", Type.class)
                    .setModifiers(Modifier.FINAL);
            classCreator.getFieldCreator("annotatedElement", AnnotatedElement.class)
                    .setModifiers(Modifier.FINAL);

            // ************************************************************************
            // MemberAccessor methods
            // ************************************************************************
            createConstructor(classCreator, memberInfo);
            createGetDeclaringClass(classCreator, memberInfo);
            createGetType(classCreator, memberInfo);
            createGetGenericType(classCreator);
            createGetName(classCreator, memberInfo);
            createExecuteGetter(classCreator, memberInfo);
            if (supportsSetter(memberInfo)) {
                createSupportSetter(classCreator, memberInfo);
                createExecuteSetter(classCreator, memberInfo);
            }
            createGetAnnotation(classCreator);
            createDeclaredAnnotationsByType(classCreator);
        }
    }

    private static boolean supportsSetter(GizmoMemberInfo memberInfo) {
        AtomicBoolean supportsSetter = new AtomicBoolean();
        memberInfo.getDescriptor().whenIsMethod(method -> {
            supportsSetter.set(memberInfo.getDescriptor().getSetter().isPresent());
        });
        memberInfo.getDescriptor().whenIsField(field -> {
            supportsSetter.set(true);
        });
        return supportsSetter.get();
    }

    /**
     * Creates a MemberAccessor for a given member, generating
     * the MemberAccessor bytecode if required
     *
     * @param member The member to generate a MemberAccessor for
     * @param annotationClass The annotation it was annotated with (used for
     *        error reporting)
     * @param gizmoClassLoader never null
     * @return A new MemberAccessor that uses Gizmo generated bytecode.
     *         Will generate the bytecode the first type it is called
     *         for a member, unless a classloader has been set,
     *         in which case no Gizmo code will be generated.
     */
    static MemberAccessor createAccessorFor(Member member, Class<? extends Annotation> annotationClass,
            GizmoClassLoader gizmoClassLoader) {
        String className = GizmoMemberAccessorFactory.getGeneratedClassName(member);
        if (gizmoClassLoader.hasBytecodeFor(className)) {
            return createInstance(className, gizmoClassLoader);
        }
        final MutableReference<byte[]> classBytecodeHolder = new MutableReference<>(null);
        ClassOutput classOutput = (path, byteCode) -> classBytecodeHolder.setValue(byteCode);
        GizmoMemberInfo memberInfo = new GizmoMemberInfo(new GizmoMemberDescriptor(member), annotationClass);
        defineAccessorFor(className, classOutput, memberInfo);
        byte[] classBytecode = classBytecodeHolder.getValue();

        gizmoClassLoader.storeBytecode(className, classBytecode);
        return createInstance(className, gizmoClassLoader);
    }

    private static MemberAccessor createInstance(String className, GizmoClassLoader gizmoClassLoader) {
        try {
            return (MemberAccessor) gizmoClassLoader.loadClass(className)
                    .getConstructor().newInstance();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | ClassNotFoundException
                | NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    // ************************************************************************
    // MemberAccessor methods
    // ************************************************************************

    private static MethodCreator getMethodCreator(ClassCreator classCreator, String methodName, Class<?>... parameters) {
        try {
            return classCreator.getMethodCreator(
                    MethodDescriptor.ofMethod(MemberAccessor.class.getMethod(methodName, parameters)));
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("No such method: " + methodName, e);
        }
    }

    private static void createConstructor(ClassCreator classCreator, GizmoMemberInfo memberInfo) {
        MethodCreator methodCreator =
                classCreator.getMethodCreator(MethodDescriptor.ofConstructor(classCreator.getClassName()));

        ResultHandle thisObj = methodCreator.getThis();

        // Invoke Object's constructor
        methodCreator.invokeSpecialMethod(MethodDescriptor.ofConstructor(classCreator.getSuperClass()), thisObj);

        ResultHandle declaringClass = methodCreator.loadClass(memberInfo.getDescriptor().getDeclaringClassName());
        memberInfo.getDescriptor().whenMetadataIsOnField(fd -> {
            TryBlock tryBlock = methodCreator.tryBlock();
            ResultHandle name = tryBlock.load(fd.getName());
            ResultHandle field = tryBlock.invokeVirtualMethod(MethodDescriptor.ofMethod(Class.class, "getDeclaredField",
                    Field.class, String.class),
                    declaringClass, name);
            ResultHandle type =
                    tryBlock.invokeVirtualMethod(MethodDescriptor.ofMethod(Field.class, "getGenericType", Type.class),
                            field);
            tryBlock.writeInstanceField(FieldDescriptor.of(classCreator.getClassName(), GENERIC_TYPE_FIELD, Type.class),
                    thisObj, type);
            tryBlock.writeInstanceField(
                    FieldDescriptor.of(classCreator.getClassName(), ANNOTATED_ELEMENT_FIELD, AnnotatedElement.class),
                    thisObj, field);

            tryBlock.addCatch(NoSuchFieldException.class).throwException(IllegalStateException.class, "Unable to find field (" +
                    fd.getName() + ") in class (" + fd.getDeclaringClass() + ").");
        });

        memberInfo.getDescriptor().whenMetadataIsOnMethod(md -> {
            TryBlock tryBlock = methodCreator.tryBlock();
            ResultHandle name = tryBlock.load(md.getName());
            ResultHandle method = tryBlock.invokeVirtualMethod(MethodDescriptor.ofMethod(Class.class, "getDeclaredMethod",
                    Method.class, String.class, Class[].class),
                    declaringClass, name,
                    tryBlock.newArray(Class.class, 0));
            ResultHandle type =
                    tryBlock.invokeVirtualMethod(MethodDescriptor.ofMethod(Method.class, "getGenericReturnType", Type.class),
                            method);
            tryBlock.writeInstanceField(FieldDescriptor.of(classCreator.getClassName(), GENERIC_TYPE_FIELD, Type.class),
                    thisObj, type);
            tryBlock.writeInstanceField(
                    FieldDescriptor.of(classCreator.getClassName(), ANNOTATED_ELEMENT_FIELD, AnnotatedElement.class),
                    thisObj, method);

            tryBlock.addCatch(NoSuchMethodException.class).throwException(IllegalStateException.class,
                    "Unable to find method (" +
                            md.getName() + ") in class (" + md.getDeclaringClass() + ").");
        });

        // Return this (it a constructor)
        methodCreator.returnValue(thisObj);
    }

    /**
     * Generates the following code:
     *
     * <pre>
     * Class getDeclaringClass() {
     *     return ClassThatDeclaredMember.class;
     * }
     * </pre>
     */
    private static void createGetDeclaringClass(ClassCreator classCreator, GizmoMemberInfo memberInfo) {
        MethodCreator methodCreator = getMethodCreator(classCreator, "getDeclaringClass");
        ResultHandle out = methodCreator.loadClass(memberInfo.getDescriptor().getDeclaringClassName());
        methodCreator.returnValue(out);
    }

    /**
     * Asserts method is a getter or read method
     *
     * @param method Method to assert is getter or read
     * @param annotationClass Used in exception message
     */
    private static void assertIsGoodMethod(MethodDescriptor method, Class<? extends Annotation> annotationClass) {
        // V = void return type
        // Z = primitive boolean return type
        String methodName = method.getName();
        if (method.getParameterTypes().length != 0) {
            // not read or getter method
            throw new IllegalStateException("The getterMethod (" + methodName + ") with a "
                    + annotationClass.getSimpleName() + " annotation must not have any parameters, but has parameters ("
                    + Arrays.toString(method.getParameterTypes()) + ").");
        }
        if (methodName.startsWith("get")) {
            if (method.getReturnType().equals("V")) {
                throw new IllegalStateException("The getterMethod (" + methodName + ") with a "
                        + annotationClass.getSimpleName() + " annotation must have a non-void return type.");
            }
        } else if (methodName.startsWith("is")) {
            if (!method.getReturnType().equals("Z")) {
                throw new IllegalStateException("The getterMethod (" + methodName + ") with a "
                        + annotationClass.getSimpleName()
                        + " annotation must have a primitive boolean return type but returns ("
                        + method.getReturnType() + "). Maybe rename the method ("
                        + "get" + methodName.substring(2) + ")?");
            }
        } else {
            // must be a read method
            if (method.getReturnType().equals("V")) {
                throw new IllegalStateException("The readMethod (" + methodName + ") with a "
                        + annotationClass.getSimpleName() + " annotation must have a non-void return type.");
            }
        }
    }

    /**
     * Generates the following code:
     *
     * <pre>
     * String getName() {
     *     return "fieldOrMethodName";
     * }
     * </pre>
     *
     * If it is a getter method, "get" is removed and the first
     * letter become lowercase
     */
    private static void createGetName(ClassCreator classCreator, GizmoMemberInfo memberInfo) {
        MethodCreator methodCreator = getMethodCreator(classCreator, "getName");

        // If it is a method, assert that it has the required
        // properties
        memberInfo.getDescriptor().whenIsMethod(method -> {
            assertIsGoodMethod(method, memberInfo.getAnnotationClass());
        });

        String fieldName = memberInfo.getDescriptor().getName();
        ResultHandle out = methodCreator.load(fieldName);
        methodCreator.returnValue(out);
    }

    /**
     * Generates the following code:
     *
     * <pre>
     * Class getType() {
     *     return FieldTypeOrMethodReturnType.class;
     * }
     * </pre>
     */
    private static void createGetType(ClassCreator classCreator, GizmoMemberInfo memberInfo) {
        MethodCreator methodCreator = getMethodCreator(classCreator, "getType");
        ResultHandle out = methodCreator.loadClass(memberInfo.getDescriptor().getTypeName());
        methodCreator.returnValue(out);
    }

    /**
     * Generates the following code:
     *
     * <pre>
     * Type getGenericType() {
     *     return GizmoMemberAccessorImplementor.getGenericTypeFor(this.getClass().getName());
     * }
     * </pre>
     *
     * We are unable to load a non-primitive object constant, so we need to store it
     * in the implementor, which then can return us the Type when needed. The type
     * is stored in gizmoMemberAccessorNameToGenericType when this method is called.
     */
    private static void createGetGenericType(ClassCreator classCreator) {
        MethodCreator methodCreator = getMethodCreator(classCreator, "getGenericType");
        ResultHandle thisObj = methodCreator.getThis();

        ResultHandle out =
                methodCreator.readInstanceField(FieldDescriptor.of(classCreator.getClassName(), GENERIC_TYPE_FIELD, Type.class),
                        thisObj);
        methodCreator.returnValue(out);
    }

    /**
     * Generates the following code:
     *
     * For a field
     *
     * <pre>
     * Object executeGetter(Object bean) {
     *     return ((DeclaringClass) bean).field;
     * }
     * </pre>
     *
     * For a method
     *
     * <pre>
     * Object executeGetter(Object bean) {
     *     return ((DeclaringClass) bean).method();
     * }
     * </pre>
     *
     * The member MUST be public if not called in Quarkus
     * (i.e. we don't delegate to the field getter/setter).
     * In Quarkus, we generate simple getter/setter for the
     * member if it is private (which get passed to the MemberDescriptor).
     */
    private static void createExecuteGetter(ClassCreator classCreator, GizmoMemberInfo memberInfo) {
        MethodCreator methodCreator = getMethodCreator(classCreator, "executeGetter", Object.class);
        ResultHandle bean = methodCreator.getMethodParam(0);
        methodCreator.returnValue(memberInfo.getDescriptor().readMemberValue(methodCreator, bean));
    }

    /**
     * Generates the following code:
     *
     * For a field or a getter method that also have a corresponding setter
     *
     * <pre>
     * boolean supportSetter() {
     *     return true;
     * }
     * </pre>
     *
     * For a read method or a getter method without a setter
     *
     * <pre>
     * boolean supportSetter() {
     *     return false;
     * }
     * </pre>
     */
    private static void createSupportSetter(ClassCreator classCreator, GizmoMemberInfo memberInfo) {
        MethodCreator methodCreator = getMethodCreator(classCreator, "supportSetter");
        memberInfo.getDescriptor().whenIsMethod(method -> {
            boolean supportSetter = memberInfo.getDescriptor().getSetter().isPresent();
            ResultHandle out = methodCreator.load(supportSetter);
            methodCreator.returnValue(out);
        });
        memberInfo.getDescriptor().whenIsField(field -> {
            ResultHandle out = methodCreator.load(true);
            methodCreator.returnValue(out);
        });
    }

    /**
     * Generates the following code:
     *
     * For a field
     *
     * <pre>
     * void executeSetter(Object bean, Object value) {
     *     return ((DeclaringClass) bean).field = value;
     * }
     * </pre>
     *
     * For a getter method with a corresponding setter
     *
     * <pre>
     * void executeSetter(Object bean, Object value) {
     *     return ((DeclaringClass) bean).setValue(value);
     * }
     * </pre>
     *
     * For a read method or a getter method without a setter
     *
     * <pre>
     * void executeSetter(Object bean, Object value) {
     *     throw new UnsupportedOperationException("Setter not supported");
     * }
     * </pre>
     */
    private static void createExecuteSetter(ClassCreator classCreator, GizmoMemberInfo memberInfo) {
        MethodCreator methodCreator = getMethodCreator(classCreator, "executeSetter", Object.class,
                Object.class);

        ResultHandle bean = methodCreator.getMethodParam(0);
        ResultHandle value = methodCreator.getMethodParam(1);
        if (memberInfo.getDescriptor().writeMemberValue(methodCreator, bean, value)) {
            // we are here only if the write is successful
            methodCreator.returnValue(null);
        } else {
            methodCreator.throwException(UnsupportedOperationException.class, "Setter not supported");
        }
    }

    private static MethodCreator getAnnotationMethodCreator(ClassCreator classCreator, String methodName,
            Class<?>... parameters) {
        return classCreator.getMethodCreator(getAnnotationMethod(methodName, parameters));
    }

    private static MethodDescriptor getAnnotationMethod(String methodName, Class<?>... parameters) {
        try {
            return MethodDescriptor.ofMethod(AnnotatedElement.class.getMethod(methodName, parameters));
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("No such method: " + methodName, e);
        }
    }

    /**
     * Generates the following code:
     *
     * <pre>
     * Object getAnnotation(Class annotationClass) {
     *     AnnotatedElement annotatedElement = GizmoMemberAccessorImplementor
     *             .getAnnotatedElementFor(this.getClass().getName());
     *     return annotatedElement.getAnnotation(annotationClass);
     * }
     * </pre>
     */
    private static void createGetAnnotation(ClassCreator classCreator) {
        MethodCreator methodCreator = getAnnotationMethodCreator(classCreator, "getAnnotation", Class.class);
        ResultHandle thisObj = methodCreator.getThis();

        ResultHandle annotatedElement = methodCreator.readInstanceField(
                FieldDescriptor.of(classCreator.getClassName(), ANNOTATED_ELEMENT_FIELD, AnnotatedElement.class),
                thisObj);
        ResultHandle query = methodCreator.getMethodParam(0);
        ResultHandle out = methodCreator.invokeInterfaceMethod(getAnnotationMethod("getAnnotation", Class.class),
                annotatedElement, query);
        methodCreator.returnValue(out);
    }

    private static void createDeclaredAnnotationsByType(ClassCreator classCreator) {
        MethodCreator methodCreator = getAnnotationMethodCreator(classCreator, "getDeclaredAnnotationsByType", Class.class);
        ResultHandle thisObj = methodCreator.getThis();

        ResultHandle annotatedElement = methodCreator.readInstanceField(
                FieldDescriptor.of(classCreator.getClassName(), ANNOTATED_ELEMENT_FIELD, AnnotatedElement.class),
                thisObj);
        ResultHandle query = methodCreator.getMethodParam(0);
        ResultHandle out = methodCreator.invokeInterfaceMethod(getAnnotationMethod("getDeclaredAnnotationsByType", Class.class),
                annotatedElement, query);
        methodCreator.returnValue(out);
    }

    private GizmoMemberAccessorImplementor() {

    }

}
