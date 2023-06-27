package org.apache.camel.quarkus.update;

import org.apache.camel.catalog.RuntimeCamelCatalog;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ExtendedContextRecipe extends Recipe {
    private static final MethodMatcher MATCHER_CONTEXT_GET_EXT =
            new MethodMatcher("org.apache.camel.CamelContext getExtension(java.lang.Class)");
    private static final MethodMatcher MATCHER_GET_NAME_RESOLVER =
            new MethodMatcher("org.apache.camel.ExtendedCamelContext getComponentNameResolver()");

    private static final MethodMatcher MATCHER_CONTEXT_ADAPT =
            new MethodMatcher("org.apache.camel.CamelContext adapt(java.lang.Class)");

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
                } else
                    //context.getExtension(RuntimeCamelCatalog.class) -> context.getCamelContextExtension().getContextPlugin(RuntimeCamelCatalog.class);
                    if (MATCHER_CONTEXT_GET_EXT.matches(method) && method.getType().isAssignableFrom(Pattern.compile(
                            RuntimeCamelCatalog.class.getCanonicalName()))) {

                        mi = mi.withName(mi.getName().withSimpleName("getCamelContextExtension().getContextPlugin"))
                                .withMethodType(mi.getMethodType());

                } else

                if (MATCHER_CONTEXT_ADAPT.matches(method)) {
                    //context.adapt(ModelCamelContext.class) -> ((ModelCamelContext) context)
                    if (method.getType().isAssignableFrom(Pattern.compile("org.apache.camel.model.ModelCamelContext"))) {
                        mi = mi.withName(mi.getName().withSimpleName("(ModelCamelContext)"))
                                .withMethodType(mi.getMethodType())
                                .withArguments(Collections.singletonList(method.getSelect()))
                                .withSelect(null);
                    } else if(method.getType().isAssignableFrom(Pattern.compile("org.apache.camel.ExtendedCamelContext"))) {
                        JavaTemplate jt = JavaTemplate.builder("#{any()}aaaa)").build();
                        Expression e = jt.apply(getCursor(), mi.getCoordinates().replace(), mi.getSelect());
                        mi = mi.withName(mi.getName().withSimpleName("((ExtendedCamelContext)"))
                                .withMethodType(mi.getMethodType())
                                .withArguments(Collections.singletonList(new J.Identifier(mi.getSelect().getId(), mi.getSelect().getPrefix(), mi.getSelect().getMarkers(), ((J.Identifier) mi.getSelect()).getSimpleName() + ")", mi.getSelect().getType(), null)))
                                .withSelect(null);
                    }



//                    JavaTemplate jt2 = JavaTemplate.builder("#{any()}").build();
//                    Object o = jt2.apply(getCursor(), mi.getCoordinates().replace(), mi.getSelect());
//
//                    JavaTemplate jt = JavaTemplate.builder("(ModelCamelContext)#{any()}").build();
//
//                    J.TypeCast tc = jt.apply(getCursor(), mi.getCoordinates().replace(), mi.getSelect());
//
//                    super.getCursor().putMessage("typeCast", tc);
//                    return null;
//                    super.visitExpression(tc, executionContext);
//                    return null;
//                    mi = ((J.MethodInvocation) tc.getExpression()).withSelect(method.getSelect()).withArguments(method.getArguments());


//                    return (J.Identifier)tc;
//                    mi = mi.withName(mi.getName().withSimpleName("(ModelCamelContext)"))
//                            .withMethodType(mi.getMethodType())
//                            .withArguments(Collections.singletonList(method.getSelect()))
//                            .withSelect(null);
                    }

                return mi;
            }

            @Override
            public Expression visitExpression(Expression expression, ExecutionContext executionContext) {
                Expression ex =  super.visitExpression(expression, executionContext);
                Expression storedExpression = getCursor().getMessage("typeCast");


//                //context.adapt(ModelCamelContext.class) -> ((ModelCamelContext) context)
//                if (expression instanceof J.MethodInvocation && MATCHER_CONTEXT_ADAPT.matches((J.MethodInvocation) ex) && ((J.MethodInvocation)ex).getType().isAssignableFrom(Pattern.compile(
//                        "org.apache.camel.model.ModelCamelContext"))) {
//
//                    JavaTemplate jt = JavaTemplate.builder("(ModelCamelContext)#{any()}").build();
//                    Expression e = jt.apply(getCursor(), ex.getCoordinates().replace(), ((J.MethodInvocation)ex).getSelect());
//                    ex = e;
//                }

                return ex;
            }

            @Override
            public J.TypeCast visitTypeCast(J.TypeCast typeCast, ExecutionContext executionContext) {
                // Retrieve the stored expression from the cursor
                Expression storedExpression = getCursor().getMessage("typeCast");


                return super.visitTypeCast(typeCast, executionContext);
            }
        };
    }
}

