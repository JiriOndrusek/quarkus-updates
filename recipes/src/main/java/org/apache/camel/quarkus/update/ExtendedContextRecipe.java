package org.apache.camel.quarkus.update;

import org.apache.camel.catalog.RuntimeCamelCatalog;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.AddImport;
import org.openrewrite.java.ChangeMethodName;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.RemoveImport;
import org.openrewrite.java.RemoveUnusedImports;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

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
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<>() {

            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext executionContext) {
                J.MethodInvocation mi = super.visitMethodInvocation(method, executionContext);

                // context.getExtension(ExtendedCamelContext.class).getComponentNameResolver() -> PluginHelper.getComponentNameResolver(context)
                if (MATCHER_GET_NAME_RESOLVER.matches(method)) {
                    if (mi.getSelect() instanceof J.MethodInvocation && MATCHER_CONTEXT_GET_EXT.matches(((J.MethodInvocation) mi.getSelect()).getMethodType())) {
                        J.MethodInvocation innerInvocation = (J.MethodInvocation) mi.getSelect();

                        mi = mi.withTemplate(JavaTemplate.builder(() -> getCursor().getParentOrThrow(), "PluginHelper.getComponentNameResolver(#{any(org.apache.camel.CamelContext)})")
                                        .build(),
                                mi.getCoordinates().replace(), innerInvocation.getSelect());

                        doAfterVisit(new AddImport<>("org.apache.camel.support.PluginHelper", null, false));
                    }
                }
                //context.getExtension(RuntimeCamelCatalog.class) -> context.getCamelContextExtension().getContextPlugin(RuntimeCamelCatalog.class);
                if (MATCHER_CONTEXT_GET_EXT_RUNTIME_CATALOG.matches(method) && method.getType().isAssignableFrom(Pattern.compile(
                        RuntimeCamelCatalog.class.getCanonicalName()))) {
                    mi = method.withTemplate(JavaTemplate.builder(this::getCursor,
                                            "#{any(org.apache.camel.CamelContext)}.getCamelContextExtension().getContextPlugin(RuntimeCamelCatalog.class)")
                                    .build(),
                            mi.getCoordinates().replace(), mi.getSelect());
                }

                return mi;

            }
        };
    }
}

