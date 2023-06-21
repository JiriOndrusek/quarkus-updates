package org.apache.camel.quarkus.update;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

import java.util.Collections;

public class ExtendedContextRecipe extends Recipe {
    private static final MethodMatcher MATCHER_CONTEXT_GET_EXT =
            new MethodMatcher("org.apache.camel.CamelContext getExtension(java.lang.Class)");
    private static final MethodMatcher MATCHER_GET_NAME_RESOLVER =
            new MethodMatcher("org.apache.camel.ExtendedCamelContext getComponentNameResolver");

    @Override
    public String getDisplayName() {
        return "Replaces removed method involving ExternalCamelContext.";
    }

    @Override
    public String getDescription() {
        return "Changes method call `context.getExtension(ExtendedCamelContext.class).getComponentNameResolver()` to a call of static method `PluginHelper.getComponentNameResolver(context)`";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<>() {

            @Override
            public Expression visitExpression(Expression expression, ExecutionContext executionContext) {
                Expression e = super.visitExpression(expression, executionContext);
                if (MATCHER_GET_NAME_RESOLVER.matches(e)) {
                    J.MethodInvocation mi = (J.MethodInvocation) e;
                    if (mi.getSelect() instanceof J.MethodInvocation && MATCHER_CONTEXT_GET_EXT.matches(((J.MethodInvocation) mi.getSelect()).getMethodType())) {
                        J.MethodInvocation innerInvocation = (J.MethodInvocation) mi.getSelect();
                        e = e.withTemplate(JavaTemplate.builder(() -> getCursor().getParentOrThrow(), "PluginHelper.getComponentNameResolver(#{any(org.apache.camel.CamelContext)})")
                                        //todo import does not work ??
                                        .imports("org.apache.camel.support.PluginHelper")
                                        .build(),
                                e.getCoordinates().replace(), innerInvocation.getSelect());
                    }
                }
                return e;
            }
        };
    }
}

