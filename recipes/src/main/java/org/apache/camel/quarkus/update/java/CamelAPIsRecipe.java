package org.apache.camel.quarkus.update.java;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.apache.camel.CamelContext;
import org.apache.camel.Category;
import org.apache.camel.ExchangePropertyKey;
import org.apache.camel.ExtendedCamelContext;
import org.apache.camel.ExtendedExchange;
import org.apache.camel.api.management.mbean.BacklogTracerEventMessage;
import org.apache.camel.builder.SimpleBuilder;
import org.apache.camel.main.MainListener;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.quarkus.update.RecipesUtil;
import org.apache.camel.spi.OnCamelContextStart;
import org.apache.camel.spi.OnCamelContextStop;
import org.apache.camel.support.IntrospectionSupport;
import org.apache.camel.util.concurrent.ThreadPoolRejectedPolicy;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.AddImport;
import org.openrewrite.java.ChangePackage;
import org.openrewrite.java.ImplementInterface;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.RemoveImplements;
import org.openrewrite.java.tree.Comment;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.marker.Markers;

import java.beans.SimpleBeanInfo;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Value
public class CamelAPIsRecipe extends Recipe {
    private static final String MATCHER_CONTEXT_GET_ENDPOINT_MAP = "org.apache.camel.CamelContext getEndpointMap()";
    private static final String MATCHER_CONTEXT_GET_EXT = "org.apache.camel.CamelContext getExtension(java.lang.Class)";
    private static final String MATCHER_GET_NAME_RESOLVER = "org.apache.camel.ExtendedCamelContext getComponentNameResolver()";
    private static final String M_PRODUCER_TEMPLATE_ASYNC_CALLBACK = "org.apache.camel.ProducerTemplate asyncCallback(..)";
    private static final String M_CONTEXT_ADAPT = "org.apache.camel.CamelContext adapt(java.lang.Class)";
    private static final String M_CONTEXT_SET_DUMP_ROUTES = "org.apache.camel.CamelContext setDumpRoutes(java.lang.Boolean)";
    private static final String M_CONTEXT_IS_DUMP_ROUTES = "org.apache.camel.CamelContext isDumpRoutes()";
    private static final String M_EXCHANGE_ADAPT = "org.apache.camel.Exchange adapt(java.lang.Class)";
    private static final String M_EXCHANGE_GET_PROPERTY = "org.apache.camel.Exchange getProperty(org.apache.camel.ExchangePropertyKey)";
    private static final String M_EXCHANGE_REMOVE_PROPERTY = "org.apache.camel.Exchange removeProperty(org.apache.camel.ExchangePropertyKey)";
    //TODO should work (*,*) or (org.apache.camel.ExchangePropertyKey,*)
    private static final String M_EXCHANGE_SET_PROPERTY = "org.apache.camel.Exchange setProperty(..)";
    private static final String M_CATALOG_ARCHETYPE_AS_XML = "org.apache.camel.catalog.CamelCatalog archetypeCatalogAsXml()";


    @Override
    public String getDisplayName() {
        return "Camel API changes";
    }

    @Override
    public String getDescription() {
        return "Camel API changes.";
    }


    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {

        return new AbstractCamelVisitor() {

            private Map<Tree, Tree> customComments = new HashMap<>();

            @Override
            protected J.Import doVisitImport(J.Import _import, ExecutionContext context) {
                J.Import im = super.doVisitImport(_import, context);

                if(im.isStatic() && im.getTypeName().equals(ThreadPoolRejectedPolicy.class.getCanonicalName())
                        && im.getQualid() != null
                        && ("Discard".equals(im.getQualid().getSimpleName()) || "DiscardOldest".equals(im.getQualid().getSimpleName()))) {
                    Comment comment = RecipesUtil.createMultinlineComment(String.format("'ThreadPoolRejectedPolicy.%s' has been removed, consider using 'ThreadPoolRejectedPolicy.Abort'.", im.getQualid().getSimpleName()));
                    im = im.withComments(Collections.singletonList(comment));

                }
                //removed `org.apache.camel.builder.SimpleBuilder; typically used internally`
                else if(SimpleBuilder.class.getCanonicalName().equals(im.getTypeName())) {
                    Comment comment = RecipesUtil.createMultinlineComment(String.format("'%s' has been removed, (class was used internally).", SimpleBeanInfo.class.getCanonicalName()));
                    im = im.withComments(Collections.singletonList(comment));

                }
                //IntrospectionSupport moved from `org.apache.camel.support` to  `org.apache.camel.impl.engine`
                else if(IntrospectionSupport.class.getCanonicalName().equals(im.getTypeName())) {
                    maybeRemoveImport(im.getTypeName());
                    String newImportName = im.getQualid() == null ? im.getTypeName() : im.getTypeName() /*+ "." + im.getQualid().getSimpleName()*/;
                    newImportName = newImportName.replaceAll(".support.", ".impl.engine.");
                    if(im.isStatic() && im.getQualid() != null) {
                        maybeAddImport(newImportName, im.getQualid().getSimpleName(), false);
                    } else {
                        maybeAddImport(newImportName, null, false);
                    }
                }


                //BacklogTracerEventMessage moved from `org.apache.camel.api.management.mbean.BacklogTracerEventMessage`
                // to  `org.apache.camel.spi.BacklogTracerEventMessage`
                doAfterVisit(
                        new ChangePackage(BacklogTracerEventMessage.class.getCanonicalName(),
                        "org.apache.camel.spi.BacklogTracerEventMessage", null));



                return im;
            }

            @Override
            J.ClassDeclaration doVisitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext context) {
                J.ClassDeclaration cd = super.doVisitClassDeclaration(classDecl, context);

                if (classDecl.getImplements() != null && !classDecl.getImplements().isEmpty()) {
                    getImplementsList().addAll(classDecl.getImplements().stream().map(i -> i.getType()).collect(Collectors.toList()));
                }

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
            protected J.FieldAccess doVisitFieldAccess(J.FieldAccess fieldAccess, ExecutionContext context) {
                J.FieldAccess fa =  super.doVisitFieldAccess(fieldAccess, context);
                //The org.apache.camel.ExchangePattern has removed InOptionalOut.
                if("InOptionalOut".equals(fieldAccess.getSimpleName()) && fa.getType() != null && fa.getType().isAssignableFrom(Pattern.compile("org.apache.camel.ExchangePattern"))) {
                    return fa.withName(new J.Identifier(UUID.randomUUID(), fa.getPrefix(), Markers.EMPTY, "/* " + fa.getSimpleName() + " has been removed */", fa.getType(), null));
                }

                else if(("Discard".equals(fa.getSimpleName()) || "DiscardOldest".equals(fa.getSimpleName()))
                        && fa.getType() != null && fa.getType().isAssignableFrom(Pattern.compile(ThreadPoolRejectedPolicy.class.getCanonicalName()))
                ) {
                    Comment comment = RecipesUtil.createMultinlineComment(String.format("'ThreadPoolRejectedPolicy.%s' has been removed, consider using 'ThreadPoolRejectedPolicy.Abort'.", fa.getSimpleName()));
                    fa = fa.withComments(Collections.singletonList(comment));

                }


                return fa;
            }

            @Override
            protected J.MethodDeclaration doVisitMethodDeclaration(J.MethodDeclaration method, ExecutionContext context) {
                J.MethodDeclaration md = super.doVisitMethodDeclaration(method, context);

                //Method 'configure' was removed from `org.apache.camel.main.MainListener`, consider using 'beforeConfigure' or 'afterConfigure'.
                if("configure".equals(md.getSimpleName())
                        && md.getReturnTypeExpression().getType().equals(JavaType.Primitive.Void)
                        && getImplementsList().stream().anyMatch(jt -> MainListener.class.getCanonicalName().equals(jt.toString()))
                        && !md.getParameters().isEmpty()
                        && md.getParameters().size() == 1
                        && md.getParameters().get(0) instanceof J.VariableDeclarations
                        && ((J.VariableDeclarations)md.getParameters().get(0)).getType().isAssignableFrom(Pattern.compile(CamelContext.class.getCanonicalName()))
                ) {
                    Comment comment = RecipesUtil.createMultinlineComment(String.format(" Method '%s' was removed from `%s`, consider using 'beforeConfigure' or 'afterConfigure'. ", md.getSimpleName(), MainListener.class.getCanonicalName()));
                    md = md.withComments(Collections.singletonList(comment));
                }

                return md;
            }

            @Override
            J.Annotation doVisitAnnotation(J.Annotation annotation, ExecutionContext context) {
                J.Annotation a = super.doVisitAnnotation(annotation, context);

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
                else
                if (a.getType().toString().equals("org.apache.camel.Consume")) {
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
            protected J.MethodInvocation doVisitMethodInvocation(J.MethodInvocation method, ExecutionContext context) {
                J.MethodInvocation mi = super.doVisitMethodInvocation(method, context);

                if(customComments.containsKey(mi.getSelect())) {
                    getCursor().putMessage("type_cast", ModelCamelContext.class.getSimpleName());
                } else
                // context.getExtension(ExtendedCamelContext.class).getComponentNameResolver() -> PluginHelper.getComponentNameResolver(context)
                if (getMethodMatcher(MATCHER_CONTEXT_GET_ENDPOINT_MAP).matches(mi)) {
                    mi = mi.withName(new J.Identifier(UUID.randomUUID(), mi.getPrefix(), Markers.EMPTY,
                            "/* " + mi.getSimpleName() + " has been removed, consider getEndpointRegistry() instead */", mi.getType(), null));
                }
                // ProducerTemplate.asyncCallback() has been replaced by 'asyncSend(') or 'asyncRequest()'
                else if(getMethodMatcher(M_PRODUCER_TEMPLATE_ASYNC_CALLBACK).matches(mi)) {
                    Comment comment = RecipesUtil.createMultinlineComment(String.format(" Method '%s()' has been replaced by 'asyncSend()' or 'asyncRequest()'.", mi.getSimpleName()));
                    mi = mi.withComments(Collections.singletonList(comment));
                }
                //context.adapt(ModelCamelContext.class) -> ((ModelCamelContext) context)
                else if (getMethodMatcher(M_CONTEXT_ADAPT).matches(mi)) {
                    if (mi.getType().isAssignableFrom(Pattern.compile(ModelCamelContext.class.getCanonicalName()))) {
//                        JavaType.FullyQualified targetType = TypeUtils.asFullyQualified(JavaType.buildType(ModelCamelContext.class.getSimpleName()));
//                       return  mi.withType(targetType);
//                        boolean hasFollowingElement = RecipesUtil.isThereFollower(mi);
//                    getCursor().putMessage("type_cast", ModelCamelContext.class.getSimpleName());
                        J.Identifier type = RecipesUtil.createIdentifier(mi.getPrefix(), ModelCamelContext.class.getSimpleName(), "java.lang.Object");
                        J.ControlParentheses cp  =  RecipesUtil.createParentheses(RecipesUtil.createTypeCast(type, mi.getSelect()));
                        customComments.put(mi, cp);
                        return mi.withComments(Collections.singletonList(RecipesUtil.createMultinlineComment("Method 'adapt' was removed.")));
                    } else if (mi.getType().isAssignableFrom(Pattern.compile(ExtendedCamelContext.class.getCanonicalName()))) {
                        mi = mi.withName(mi.getName().withSimpleName("getCamelContextExtension")).withArguments(Collections.emptyList());
                        maybeRemoveImport(ExtendedCamelContext.class.getCanonicalName());
                    }
                }
                //exchange.adapt(ExtendedExchange.class) -> exchange.getExchangeExtension()
                else if (getMethodMatcher(M_EXCHANGE_ADAPT).matches(mi)
                        && mi.getType().isAssignableFrom(Pattern.compile(ExtendedExchange.class.getCanonicalName()))) {
                    mi = mi.withName(mi.getName().withSimpleName("getExchangeExtension")).withArguments(Collections.emptyList());
                    maybeRemoveImport(ExtendedExchange.class.getCanonicalName());
                }
                //newExchange.getProperty(ExchangePropertyKey.FAILURE_HANDLED) -> newExchange.getExchangeExtension().isFailureHandled()
                else if(getMethodMatcher(M_EXCHANGE_GET_PROPERTY).matches(mi)
                        && mi.getArguments().get(0).toString().endsWith("FAILURE_HANDLED")) {
                    mi = mi.withName(mi.getName().withSimpleName("getExchangeExtension().isFailureHandled")).withArguments(Collections.emptyList());
                    maybeRemoveImport(ExchangePropertyKey.class.getCanonicalName());
                }
                //exchange.removeProperty(ExchangePropertyKey.FAILURE_HANDLED); -> exchange.getExchangeExtension().setFailureHandled(false);
                else if(getMethodMatcher(M_EXCHANGE_REMOVE_PROPERTY).matches(mi)
                        && mi.getArguments().get(0).toString().endsWith("FAILURE_HANDLED")) {
                    mi = mi.withName(mi.getName().withSimpleName("getExchangeExtension().setFailureHandled")).withArguments(Collections.singletonList(RecipesUtil.createIdentifier(Space.EMPTY, "false", "java.lang.Boolean")));
                    maybeRemoveImport(ExchangePropertyKey.class.getCanonicalName());
                }
                //exchange.setProperty(ExchangePropertyKey.FAILURE_HANDLED, failureHandled); -> exchange.getExchangeExtension().setFailureHandled(failureHandled);
                else if(getMethodMatcher(M_EXCHANGE_SET_PROPERTY).matches(mi)
                        && mi.getArguments().get(0).toString().endsWith("FAILURE_HANDLED")) {
                    mi = mi.withName(mi.getName()
                                    .withSimpleName("getExchangeExtension().setFailureHandled"))
                            .withArguments(Collections.singletonList(mi.getArguments().get(1).withPrefix(Space.EMPTY)));
                    maybeRemoveImport(ExchangePropertyKey.class.getCanonicalName());
                }
                //'org.apache.camel.catalogCamelCatalog.archetypeCatalogAsXml()` has been removed
                else if(getMethodMatcher(M_CATALOG_ARCHETYPE_AS_XML).matches(mi)) {
                    mi = mi.withComments(Collections.singletonList(RecipesUtil.createMultinlineComment(" Method '" + mi.getSimpleName() + "' has been removed. ")));
                }
                //context().setDumpRoutes(true); -> context().setDumpRoutes("xml");(or "yaml")
                else if(getMethodMatcher(M_CONTEXT_SET_DUMP_ROUTES).matches(mi)) {
                    mi = mi.withComments(Collections.singletonList(RecipesUtil.createMultinlineComment(" Method '" + mi.getSimpleName() + "' accepts String parameter ('xml' or 'yaml' or 'false'). ")));
                }
                //Boolean isDumpRoutes(); -> getDumpRoutes(); with returned type String
                else if(getMethodMatcher(M_CONTEXT_IS_DUMP_ROUTES).matches(mi)) {
                    mi = mi.withName(mi.getName().withSimpleName("getDumpRoutes")).withComments(Collections.singletonList(RecipesUtil.createMultinlineComment(" Method 'getDumpRoutes' returns String value ('xml' or 'yaml' or 'false'). ")));
                }
                // context.getExtension(ExtendedCamelContext.class).getComponentNameResolver() -> PluginHelper.getComponentNameResolver(context)
                else if (getMethodMatcher(MATCHER_GET_NAME_RESOLVER).matches(mi)) {
                    if (mi.getSelect() instanceof J.MethodInvocation && getMethodMatcher(MATCHER_CONTEXT_GET_EXT).matches(((J.MethodInvocation) mi.getSelect()).getMethodType())) {
                        J.MethodInvocation innerInvocation = (J.MethodInvocation) mi.getSelect();
                        mi = mi.withTemplate(JavaTemplate.builder(() -> getCursor().getParentOrThrow(), "PluginHelper.getComponentNameResolver(#{any(org.apache.camel.CamelContext)})")
                                        .build(),
                                mi.getCoordinates().replace(), innerInvocation.getSelect());
                        doAfterVisit(new AddImport<>("org.apache.camel.support.PluginHelper", null, false));
                    }
                }
//                // (CamelRuntimeCatalog) context.getExtension(RuntimeCamelCatalog.class) -> context.getCamelContextExtension().getContextPlugin(RuntimeCamelCatalog.class);
                else if (getMethodMatcher(MATCHER_CONTEXT_GET_EXT).matches(mi)) {

                    mi = mi.withName(mi.getName().withSimpleName("getCamelContextExtension().getContextPlugin"))
                            .withMethodType(mi.getMethodType());
                    //remove type cast before expression
                    if(getCursor().getParent().getValue() instanceof J.TypeCast && ((J.TypeCast)getCursor().getParent().getValue()).getType().equals(mi.getType())) {
                        getCursor().getParent().putMessage("remove_type_cast", mi);
                    }

                }
                return mi;
            }

            @Override
            public @Nullable J postVisit(J tree, ExecutionContext context) {
                J j =  super.postVisit(tree, context);

                String toType = getCursor().getMessage("type_cast");

                if(toType != null) {
                    J.MethodInvocation mi = (J.MethodInvocation)j;
                    Comment c = RecipesUtil.createComment("");

//                getCursor().putMessage("aaa", "aaa");
//                J.Identifier type = RecipesUtil.createIdentifier(mi.getPrefix(), toType, "java.lang.Object");
//                J.ControlParentheses cp  =  RecipesUtil.createParentheses(RecipesUtil.createTypeCast(type, mi.getSelect()).withComments(Collections.singletonList(c)));
                    //todo quick
                    J.ControlParentheses cp = (J.ControlParentheses)customComments.values().iterator().next();

                    J.MethodInvocation m = mi.withSelect(cp);
                    return m;
                }

                J removeTypeCast = getCursor().getMessage("remove_type_cast");

                if(removeTypeCast != null) {
                    return removeTypeCast;
                }

                return j;
            }


        };
    }

}

