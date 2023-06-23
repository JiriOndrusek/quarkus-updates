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

            private static boolean pluginHelperImportRequired;

//            @Override
//            public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext executionContext) {
//                if(new MethodMatcher("org.apache.camel.quarkus.component.test.it.Test test").matches(method.getMethodType())) {
//                    System.out.println("");
//                }
//
//                return super.visitMethodDeclaration(method, executionContext);
//            }


//            @Override
//            public J.VariableDeclarations visitVariableDeclarations(J.VariableDeclarations multiVariable, ExecutionContext executionContext) {
//                return super.visitVariableDeclarations(multiVariable, executionContext);
//            }

            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext executionContext) {

                //context.getExtension(RuntimeCamelCatalog.class) -> context.getCamelContextExtension().getContextPlugin(RuntimeCamelCatalog.class);
                if (MATCHER_CONTEXT_GET_EXT_RUNTIME_CATALOG.matches(method) && method.getType().isAssignableFrom(Pattern.compile(
                        RuntimeCamelCatalog.class.getCanonicalName()))) {
                        method = method.withTemplate(JavaTemplate.builder(this::getCursor,
                                        "#{any(org.apache.camel.CamelContext)}.getCamelContextExtension().getContextPlugin(RuntimeCamelCatalog.class)")
                                        .build(),
                                method.getCoordinates().replace(), method.getSelect());
                }
                return super.visitMethodInvocation(method, executionContext);
            }

            @Override
            public Expression visitExpression(Expression expression, ExecutionContext executionContext) {
                Expression e = super.visitExpression(expression, executionContext);

                //todo move to visitMethodInvocation

                if (new MethodMatcher("org.apache.camel.quarkus.component.test.it.Test test").matches(e)) {
                    System.out.println("");
                }
                // context.getExtension(ExtendedCamelContext.class).getComponentNameResolver() -> PluginHelper.getComponentNameResolver(context)
                if (MATCHER_GET_NAME_RESOLVER.matches(e)) {
                    J.MethodInvocation mi = (J.MethodInvocation) e;
                    if (mi.getSelect() instanceof J.MethodInvocation && MATCHER_CONTEXT_GET_EXT.matches(((J.MethodInvocation) mi.getSelect()).getMethodType())) {
                        J.MethodInvocation innerInvocation = (J.MethodInvocation) mi.getSelect();
                        e = e.withTemplate(JavaTemplate.builder(() -> getCursor().getParentOrThrow(), "PluginHelper.getComponentNameResolver(#{any(org.apache.camel.CamelContext)})")
                                        //todo import does not work ??
                                        .build(),
                                e.getCoordinates().replace(), innerInvocation.getSelect());

                        pluginHelperImportRequired = true;


//                        doAfterVisit(new AddImport<>("org.apache.camel.support.PluginHelper", null, false));
//                        doAfterVisit(new RemoveImport("org.apache.camel.ExtendedCamelContext"));
                        doAfterVisit(new AddImport<>("org.apache.camel.support.PluginHelper", null, false));
//                        doAfterVisit(new RemoveUnusedImports());
                    }
                }


                //context.getExtension(RuntimeCamelCatalog.class) -> context.getCamelContextExtension().getContextPlugin(RuntimeCamelCatalog.class);
                if (MATCHER_CONTEXT_GET_EXT_RUNTIME_CATALOG.matches(e)) {
                    J.MethodInvocation mi = (J.MethodInvocation) e;
                }
                return e;
            }

        };
    }
}

