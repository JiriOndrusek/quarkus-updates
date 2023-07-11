package org.apache.camel.quarkus.update;

import org.apache.camel.CamelContext;
import org.apache.camel.Category;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePropertyKey;
import org.apache.camel.ExtendedCamelContext;
import org.apache.camel.ExtendedExchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.spi.OnCamelContextStart;
import org.apache.camel.spi.OnCamelContextStarting;
import org.apache.camel.spi.OnCamelContextStop;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.AddImport;
import org.openrewrite.java.ImplementInterface;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.RemoveImplements;
import org.openrewrite.java.tree.Comment;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JContainer;
import org.openrewrite.java.tree.JRightPadded;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;
import org.openrewrite.java.tree.TextComment;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.openrewrite.Tree.randomId;

public class CamelAPIsRecipe extends Recipe {
    private static final MethodMatcher MATCHER_CONTEXT_GET_ENDPOINT_MAP =
            new MethodMatcher("org.apache.camel.CamelContext getEndpointMap()");

    private static final MethodMatcher MATCHER_CONTEXT_ADAPT =
            new MethodMatcher("org.apache.camel.CamelContext adapt(java.lang.Class.class)");

    @Override
    public String getDisplayName() {
        return "Camel API changes.";
    }

    @Override
    public String getDescription() {
        return "Camel API changes";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<>() {

            @Override
            public J.FieldAccess visitFieldAccess(J.FieldAccess fieldAccess, ExecutionContext executionContext) {
                J.FieldAccess fa =  super.visitFieldAccess(fieldAccess, executionContext);
                //The org.apache.camel.ExchangePattern has removed InOptionalOut.
                if("InOptionalOut".equals(fieldAccess.getSimpleName()) && fieldAccess.getType().isAssignableFrom(Pattern.compile("org.apache.camel.ExchangePattern"))) {
                    return fa.withName(new J.Identifier(UUID.randomUUID(), fa.getPrefix(), Markers.EMPTY, "/* " + fa.getSimpleName() + " has been removed */", fa.getType(), null));
                }


                return fa;
            }

            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext executionContext) {
                J.MethodInvocation mi = super.visitMethodInvocation(method, executionContext);

                // context.getExtension(ExtendedCamelContext.class).getComponentNameResolver() -> PluginHelper.getComponentNameResolver(context)
                if (MATCHER_CONTEXT_GET_ENDPOINT_MAP.matches(mi)) {
                    return mi.withName(new J.Identifier(UUID.randomUUID(), mi.getPrefix(), Markers.EMPTY,
                            "/* " + mi.getSimpleName() + " has been removed, consider getEndpointRegistry() instead */", mi.getType(), null));
                }

                else if(mi.getSimpleName().equals("asyncCallback") && mi.getSelect().getType().toString().equals(ProducerTemplate.class.getName()) ) {
                    mi = mi.withComments(Collections.singletonList(RecipesUtil.createComment(" Method '" + mi.getSimpleName() + "(' has been replaced by 'asyncSend(' or 'asyncRequest('.\n").withSuffix(mi.getPrefix().getIndent())));
                }
                //context.adapt(ModelCamelContext.class) -> ((ModelCamelContext) context)
                else if ("adapt".equals(mi.getSimpleName())
                            && mi.getSelect().getType().isAssignableFrom(Pattern.compile(CamelContext.class.getCanonicalName()))) {
                    if (mi.getType().isAssignableFrom(Pattern.compile(ModelCamelContext.class.getCanonicalName()))) {
                        getCursor().putMessage("type_cast", ModelCamelContext.class.getSimpleName());
                    } else if (mi.getType().isAssignableFrom(Pattern.compile(ExtendedCamelContext.class.getCanonicalName()))) {
                        mi = mi.withName(mi.getName().withSimpleName("getCamelContextExtension")).withArguments(Collections.emptyList());

                        maybeRemoveImport(ExtendedCamelContext.class.getCanonicalName());
                    }
                }
                //exchange.adapt(ExtendedExchange.class) -> exchange.getExchangeExtension()
                else if ("adapt".equals(mi.getSimpleName())
                        && mi.getSelect().getType().isAssignableFrom(Pattern.compile(Exchange.class.getCanonicalName()))
                        && mi.getType().isAssignableFrom(Pattern.compile(ExtendedExchange.class.getCanonicalName()))) {
                    mi = mi.withName(mi.getName().withSimpleName("getExchangeExtension")).withArguments(Collections.emptyList());

                    maybeRemoveImport(ExtendedExchange.class.getCanonicalName());
                }
                //newExchange.getProperty(ExchangePropertyKey.FAILURE_HANDLED) -> newExchange.getExchangeExtension().isFailureHandled()
                else if(mi.getSimpleName().equals("getProperty")
                        && mi.getSelect().getType().isAssignableFrom(Pattern.compile(Exchange.class.getCanonicalName()))
                        && !mi.getArguments().isEmpty()
                        && mi.getArguments().size() == 1
                        && mi.getArguments().get(0).getType().isAssignableFrom(Pattern.compile(ExchangePropertyKey.class.getCanonicalName()))
                        && mi.getArguments().get(0).toString().endsWith("FAILURE_HANDLED")) {
                    mi = mi.withName(mi.getName().withSimpleName("getExchangeExtension().isFailureHandled")).withArguments(Collections.emptyList());
                    maybeRemoveImport(ExchangePropertyKey.class.getCanonicalName());
                }
                //exchange.removeProperty(ExchangePropertyKey.FAILURE_HANDLED); -> exchange.getExchangeExtension().setFailureHandled(false);
                else if(mi.getSimpleName().equals("removeProperty")
                        && mi.getSelect().getType().isAssignableFrom(Pattern.compile(Exchange.class.getCanonicalName()))
                        && !mi.getArguments().isEmpty()
                        && mi.getArguments().size() == 1
                        && mi.getArguments().get(0).getType().isAssignableFrom(Pattern.compile(ExchangePropertyKey.class.getCanonicalName()))
                        && mi.getArguments().get(0).toString().endsWith("FAILURE_HANDLED")) {
                    mi = mi.withName(mi.getName().withSimpleName("getExchangeExtension().setFailureHandled")).withArguments(Collections.singletonList(RecipesUtil.createIdentifier(Space.EMPTY, "false", "java.lang.Boolean")));
                    maybeRemoveImport(ExchangePropertyKey.class.getCanonicalName());
                }
                //exchange.setProperty(ExchangePropertyKey.FAILURE_HANDLED, failureHandled); -> exchange.getExchangeExtension().setFailureHandled(failureHandled);
                else if(mi.getSimpleName().equals("setProperty")
                        && mi.getSelect().getType().isAssignableFrom(Pattern.compile(Exchange.class.getCanonicalName()))
                        && !mi.getArguments().isEmpty()
                        && mi.getArguments().size() == 2
                        && mi.getArguments().get(0).getType().isAssignableFrom(Pattern.compile(ExchangePropertyKey.class.getCanonicalName()))
                        && mi.getArguments().get(0).toString().endsWith("FAILURE_HANDLED")) {
                    mi = mi.withName(mi.getName()
                                    .withSimpleName("getExchangeExtension().setFailureHandled"))
                            .withArguments(Collections.singletonList(mi.getArguments().get(1).withPrefix(Space.EMPTY)));
                    maybeRemoveImport(ExchangePropertyKey.class.getCanonicalName());
                }




                return mi;
            }

            @Override
            public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext executionContext) {
                J.ClassDeclaration cd = super.visitClassDeclaration(classDecl, executionContext);

                //Removed org.apache.camel.spi.OnCamelContextStart. Use org.apache.camel.spi.OnCamelContextStarting instead.
                if(cd.getImplements() != null && cd.getImplements().stream()
                        .anyMatch(f -> TypeUtils.isOfClassType(f.getType(), OnCamelContextStart.class.getCanonicalName()))) {

                    doAfterVisit(new ImplementInterface<ExecutionContext>(cd, "org.apache.camel.spi.OnCamelContextStarting"));
                    doAfterVisit(new RemoveImplements(OnCamelContextStart.class.getCanonicalName(), null));

                } //Removed org.apache.camel.spi.OnCamelContextStop. Use org.apache.camel.spi.OnCamelContextStopping instead.
                else if(cd.getImplements() != null && cd.getImplements().stream()
                        .anyMatch(f -> TypeUtils.isOfClassType(f.getType(), OnCamelContextStop.class.getCanonicalName()))) {

                    doAfterVisit(new ImplementInterface<ExecutionContext>(cd, "org.apache.camel.spi.OnCamelContextStopping"));
                    doAfterVisit(new RemoveImplements(OnCamelContextStop.class.getCanonicalName(), null));

                }
                return cd;
            }

            @Override
            public J.Annotation visitAnnotation(J.Annotation annotation, ExecutionContext executionContext) {
                J.Annotation a = super.visitAnnotation(annotation, executionContext);

                if (a.getType().toString().equals("org.apache.camel.FallbackConverter")) {
                    maybeAddImport("org.apache.camel.Converter", null, false);
                    maybeRemoveImport("org.apache.camel.FallbackConverter");

                    return RecipesUtil.createAnnotation(annotation, "Converter", null, "fallback = true");
                }
                else if (a.getType().toString().equals("org.apache.camel.EndpointInject")) {
                   Optional<String> originalValue = RecipesUtil.getValueOfArgs(a.getArguments(), "uri");
                   if(originalValue.isPresent()) {
                       return RecipesUtil.createAnnotation(annotation, "EndpointInject", s -> s.startsWith("uri="), originalValue.get());
                   }
                }
                else if (a.getType().toString().equals("org.apache.camel.Produce")) {
                   Optional<String> originalValue = RecipesUtil.getValueOfArgs(a.getArguments(), "uri");
                   if(originalValue.isPresent()) {
                       return RecipesUtil.createAnnotation(annotation, "Produce", s -> s.startsWith("uri="), originalValue.get());
                   }
                }
                else if (a.getType().toString().equals("org.apache.camel.Consume")) {
                   Optional<String> originalValue = RecipesUtil.getValueOfArgs(a.getArguments(), "uri");
                   if(originalValue.isPresent()) {
                       return RecipesUtil.createAnnotation(annotation, "Consume", s -> s.startsWith("uri="), originalValue.get());
                   }
                }
                else if (a.getType().toString().equals("org.apache.camel.spi.UriEndpoint")) {

                    Optional<String> originalValue = RecipesUtil.getValueOfArgs(a.getArguments(), "label");
                    if(originalValue.isPresent()) {
                        maybeAddImport("org.apache.camel.Category", null, false);

                        String newValue;
                         try {
                             newValue = Category.valueOf(originalValue.get().toUpperCase().replaceAll("\"", "")).getValue();
                         } catch(IllegalArgumentException e) {
                             newValue = originalValue.get() + "/*unknown_value*/";
                         }

                        return RecipesUtil.createAnnotation(annotation, "UriEndpoint", s -> s.startsWith("label="), "category = {Category." + newValue + "}");
                    }
                }

                return a;
            }

            @Override
            public @Nullable J postVisit(J tree, ExecutionContext executionContext) {
                J j =  super.postVisit(tree, executionContext);

                String toType = getCursor().getMessage("type_cast");

                if(toType != null) {
                    J.MethodInvocation mi = (J.MethodInvocation)j;

                    J.Identifier type = RecipesUtil.createIdentifier(mi.getPrefix(), toType, "java.lang.Object");
                    return RecipesUtil.createParentheses(RecipesUtil.createTypeCast(type, mi.getSelect()));
                }

                return j;
            }
        };
    }
}

