package org.apache.camel.quarkus.update;

import org.apache.camel.catalog.RuntimeCamelCatalog;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;

import java.util.Collections;
import java.util.regex.Pattern;

public class ExtendedContextRecipe extends Recipe {
    private static final MethodMatcher MATCHER_CONTEXT_GET_EXT =
            new MethodMatcher("org.apache.camel.CamelContext getExtension(java.lang.Class)");
    private static final MethodMatcher MATCHER_GET_NAME_RESOLVER =
            new MethodMatcher("org.apache.camel.ExtendedCamelContext getComponentNameResolver()");

    private static final MethodMatcher MATCHER_CONTEXT_GET_EXT_RUNTIME_CATALOG =
            new MethodMatcher("org.apache.camel.CamelContext getExtension(java.lang.Class)");

    @Override
    public String getDisplayName() {
        return "Replaces removed method involving ExternalCamelContext";
    }

    @Override
    public String getDescription() {
        return "Changes method call `context.getExtension(ExtendedCamelContext.class).getComponentNameResolver()` to a call of static method `PluginHelper.getComponentNameResolver(context)`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<>() {

            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext executionContext) {
                J.MethodInvocation mi = super.visitMethodInvocation(method, executionContext);

                // context.getExtension(ExtendedCamelContext.class).getComponentNameResolver() -> PluginHelper.getComponentNameResolver(context)
                if (MATCHER_GET_NAME_RESOLVER.matches(method)) {
                    if (mi.getSelect() instanceof J.MethodInvocation && MATCHER_CONTEXT_GET_EXT.matches(((J.MethodInvocation) mi.getSelect()).getMethodType())) {
                        J.MethodInvocation innerInvocation = (J.MethodInvocation) mi.getSelect();

                        mi = mi.withName(mi.getName().withSimpleName("PluginHelper.getComponentNameResolver"))
                                .withMethodType(mi.getMethodType())
                                .withSelect(null)
                                .withArguments(Collections.singletonList(innerInvocation.getSelect()));
                        maybeAddImport("org.apache.camel.support.PluginHelper",false);

                        return mi;
                    }
                }

                //context.getExtension(RuntimeCamelCatalog.class) -> context.getCamelContextExtension().getContextPlugin(RuntimeCamelCatalog.class);
                if (MATCHER_CONTEXT_GET_EXT_RUNTIME_CATALOG.matches(method) && method.getType().isAssignableFrom(Pattern.compile(
                        RuntimeCamelCatalog.class.getCanonicalName()))) {

                    mi = mi.withName(mi.getName().withSimpleName("getCamelContextExtension().getContextPlugin"))
                            .withMethodType(mi.getMethodType());
                }

                return mi;
            }
        };
    }
}

