package org.optaplanner.core.impl.domain.common.accessor.gizmo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.Objects;

import org.optaplanner.core.api.domain.common.DomainAccessType;
import org.optaplanner.core.impl.domain.common.ReflectionHelper;
import org.optaplanner.core.impl.domain.common.accessor.MemberAccessor;

public class GizmoMemberAccessorFactory {
    /**
     * Returns the generated class name for a given member.
     * (Here as accessing any method of GizmoMemberAccessorImplementor
     * will try to load Gizmo code)
     *
     * @param member The member to get the generated class name for
     * @return The generated class name for member
     */
    public static String getGeneratedClassName(Member member) {
        String memberName = Objects.requireNonNullElse(ReflectionHelper.getGetterPropertyName(member), member.getName());
        String memberType = (member instanceof Field) ? "Field" : "Method";

        return member.getDeclaringClass().getName() + "$OptaPlanner$MemberAccessor$" + memberType + "$" + memberName;
    }

    public static MemberAccessor buildGizmoMemberAccessor(Member member, Class<? extends Annotation> annotationClass) {
        try {
            // Check if Gizmo on the classpath by verifying we can access one of its classes
            Class.forName("io.quarkus.gizmo.ClassCreator", false,
                    Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("When using the domainAccessType (" +
                    DomainAccessType.GIZMO +
                    ") the classpath or modulepath must contain io.quarkus.gizmo:gizmo.\n" +
                    "Maybe add a dependency to io.quarkus.gizmo:gizmo.");
        }
        MemberAccessor accessor = GizmoMemberAccessorImplementor.createAccessorFor(member, annotationClass);
        return accessor;
    }

    private GizmoMemberAccessorFactory() {
    }
}
