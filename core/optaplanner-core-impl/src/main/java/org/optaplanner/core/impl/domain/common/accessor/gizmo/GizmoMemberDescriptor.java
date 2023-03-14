package org.optaplanner.core.impl.domain.common.accessor.gizmo;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Consumer;

import org.optaplanner.core.impl.domain.common.ReflectionHelper;

import io.quarkus.gizmo.BytecodeCreator;
import io.quarkus.gizmo.FieldDescriptor;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;

/**
 * Describe and provide simplified/unified access for a Member
 */
public class GizmoMemberDescriptor {

    /**
     * The name of a member. For a field, it the field name.
     * For a method, if it is a getter, the method name without "get"/"is"
     * and the first letter lowercase; otherwise, the method name.
     */
    private final String name;

    /**
     * If the member is a field, the FieldDescriptor of the member accessor
     * If the member is a method, the MethodDescriptor of the member accessor
     */
    private final GizmoMemberHandler memberHandler;

    /**
     * If the member is a field, the FieldDescriptor of the member
     * If the member is a method, the MethodDescriptor of the member
     *
     * Should only be used for metadata (i.e. Generic Type and Annotated Element).
     */
    private final GizmoMemberHandler metadataHandler;

    /**
     * The class that declared this member
     */
    private final Class<?> declaringClass;

    /**
     * The MethodDescriptor of the corresponding setter. Is empty if not present.
     */
    private final MethodDescriptor setter;

    /**
     * If final checks should be ignored due to Quarkus transformations
     */
    private final boolean ignoreFinalChecks;

    public GizmoMemberDescriptor(Member member) {
        this.declaringClass = member.getDeclaringClass();
        if (!Modifier.isPublic(member.getModifiers())) {
            throw new IllegalStateException("Member (" + member.getName() + ") of class (" +
                    member.getDeclaringClass().getName() + ") is not public and domainAccessType is GIZMO.\n" +
                    ((member instanceof Field) ? "Maybe put the annotations onto the public getter of the field.\n" : "") +
                    "Maybe use domainAccessType REFLECTION instead of GIZMO.");
        }
        if (member instanceof Field) {
            FieldDescriptor fieldDescriptor = FieldDescriptor.of((Field) member);
            this.memberHandler = GizmoMemberHandler.of(fieldDescriptor);
            this.name = member.getName();
            this.setter = null;
        } else if (member instanceof Method) {
            MethodDescriptor methodDescriptor = MethodDescriptor.ofMethod((Method) member);
            this.memberHandler = GizmoMemberHandler.of(methodDescriptor);
            this.name = ReflectionHelper.isGetterMethod((Method) member) ? ReflectionHelper.getGetterPropertyName(member)
                    : member.getName();
            this.setter = lookupSetter(methodDescriptor, declaringClass, name).orElse(null);
        } else {
            throw new IllegalArgumentException(member + " is not a Method or a Field.");
        }
        this.metadataHandler = this.memberHandler;
        this.ignoreFinalChecks = false;
    }

    // For Quarkus
    // (Cannot move to Quarkus module; get runtime
    //  exception since objects created here use classes
    //  from another ClassLoader).
    public GizmoMemberDescriptor(String name, Object memberDescriptor, Object metadataDescriptor, Class<?> declaringClass) {
        this(name, memberDescriptor, metadataDescriptor, declaringClass,
                lookupSetter(memberDescriptor, declaringClass, name).orElse(null), true);
    }

    public GizmoMemberDescriptor(String name, Object memberDescriptor, Object metadataDescriptor, Class<?> declaringClass,
            MethodDescriptor setterDescriptor) {
        this(name, memberDescriptor, metadataDescriptor, declaringClass, setterDescriptor, false);
    }

    public GizmoMemberDescriptor(String name, Object memberDescriptor, Object metadataDescriptor, Class<?> declaringClass,
            MethodDescriptor setterDescriptor, boolean ignoreFinalChecks) {
        this.name = name;
        this.memberHandler = GizmoMemberHandler.of(memberDescriptor);
        this.metadataHandler = GizmoMemberHandler.of(metadataDescriptor);
        this.declaringClass = declaringClass;
        this.setter = setterDescriptor;
        this.ignoreFinalChecks = ignoreFinalChecks;
    }

    /**
     * If the member accessor is a field, pass the member's field descriptor to the
     * provided consumer. Otherwise, do nothing. Returns self for chaining.
     *
     * @param fieldDescriptorConsumer What to do if the member a field.
     * @return this
     */
    public GizmoMemberDescriptor whenIsField(Consumer<FieldDescriptor> fieldDescriptorConsumer) {
        memberHandler.whenIsField(fieldDescriptorConsumer);
        return this;
    }

    /**
     * If the member accessor is a method, pass the member's method descriptor to the
     * provided consumer. Otherwise, do nothing. Returns self for chaining.
     *
     * @param methodDescriptorConsumer What to do if the member a method.
     * @return this
     */
    public GizmoMemberDescriptor whenIsMethod(Consumer<MethodDescriptor> methodDescriptorConsumer) {
        memberHandler.whenIsMethod(methodDescriptorConsumer);
        return this;
    }

    public ResultHandle readMemberValue(BytecodeCreator bytecodeCreator, ResultHandle thisObj) {
        return memberHandler.readMemberValue(declaringClass, bytecodeCreator, thisObj);
    }

    /**
     * Write the bytecode for writing to this member. If there is no setter,
     * it write the bytecode for throwing the exception. Return true if
     * it was able to write the member value.
     *
     * @param bytecodeCreator the bytecode creator to use
     * @param thisObj the bean to write the new value to
     * @param newValue to new value of the member
     * @return True if it was able to write the member value, false otherwise
     */
    public boolean writeMemberValue(BytecodeCreator bytecodeCreator, ResultHandle thisObj, ResultHandle newValue) {
        return memberHandler.writeMemberValue(declaringClass, name, setter, bytecodeCreator, thisObj, newValue,
                ignoreFinalChecks);
    }

    /**
     * If the member metadata is on a field, pass the member's field descriptor to the
     * provided consumer. Otherwise, do nothing. Returns self for chaining.
     *
     * @param fieldDescriptorConsumer What to do if the member a field.
     * @return this
     */
    public GizmoMemberDescriptor whenMetadataIsOnField(Consumer<FieldDescriptor> fieldDescriptorConsumer) {
        metadataHandler.whenIsField(fieldDescriptorConsumer);
        return this;
    }

    /**
     * If the member metadata is on a method, pass the member's method descriptor to the
     * provided consumer. Otherwise, do nothing. Returns self for chaining.
     *
     * @param methodDescriptorConsumer What to do if the member a method.
     * @return this
     */
    public GizmoMemberDescriptor whenMetadataIsOnMethod(Consumer<MethodDescriptor> methodDescriptorConsumer) {
        metadataHandler.whenIsMethod(methodDescriptorConsumer);
        return this;
    }

    /**
     * Returns the declaring class name of the member in descriptor format.
     * For instance, the declaring class name of Object.toString() is "java/lang/Object".
     *
     * @return Returns the declaring class name of the member in descriptor format
     */
    public String getDeclaringClassName() {
        return memberHandler.getDeclaringClassName();
    }

    public Optional<MethodDescriptor> getSetter() {
        return Optional.ofNullable(setter);
    }

    private static Optional<MethodDescriptor> lookupSetter(Object memberDescriptor, Class<?> declaringClass, String name) {
        if (memberDescriptor instanceof MethodDescriptor) {
            return Optional.ofNullable(ReflectionHelper.getSetterMethod(declaringClass, name))
                    .map(MethodDescriptor::ofMethod);
        } else {
            return Optional.empty();
        }
    }

    public String getName() {
        return name;
    }

    /**
     * Returns the member type (for fields) / return type (for methods) name.
     * The name does not include generic information.
     */
    public String getTypeName() {
        String typeName = metadataHandler.getTypeName();
        return org.objectweb.asm.Type.getType(typeName).getClassName();
    }

    public Type getType() {
        return metadataHandler.getType(declaringClass);
    }

    @Override
    public String toString() {
        return memberHandler.toString();
    }
}
