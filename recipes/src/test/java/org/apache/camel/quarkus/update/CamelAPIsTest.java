package org.apache.camel.quarkus.update;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.test.RewriteTest.toRecipe;

public class CamelAPIsTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new CamelJavaGroupedRecipe())
                .parser(JavaParser.fromJavaVersion()
                        .logCompilationWarningsAndErrors(true)
                        .classpath("camel-api","camel-support","camel-core-model", "camel-util", "camel-catalog", "camel-main"))
                .typeValidationOptions(TypeValidation.none());;
    }

    @Test
    void testRemovedExchangePatternInOptionalOut() {
        rewriteRun(
                java(
                        """
                                import org.apache.camel.ExchangePattern;
                                import org.apache.camel.builder.RouteBuilder;
                                
                                public class MySimpleToDRoute extends RouteBuilder {
                                
                                    @Override
                                    public void configure() {
                                
                                        String uri = "log:c";
                                
                                        from("direct:start")
                                                .toD("log:a", true)
                                                .to(ExchangePattern.InOptionalOut, "log:b")
                                                .to(uri);
                                    }
                                }
                            """
                        ,
                        """
                                import org.apache.camel.ExchangePattern;
                                import org.apache.camel.builder.RouteBuilder;

                                public class MySimpleToDRoute extends RouteBuilder {
                                
                                    @Override
                                    public void configure() {
                                
                                        String uri = "log:c";
                                
                                        from("direct:start")
                                                .toD("log:a", true)
                                                .to(ExchangePattern./* InOptionalOut has been removed */, "log:b")
                                                .to(uri);
                                    }
                                }
                                """
                )
        );
    }

    @Test
    void testRemovedFullyExchangePatternInOptionalOut() {
        rewriteRun(
                java(
                        """
                                import org.apache.camel.builder.RouteBuilder;
                                
                                public class MySimpleToDRoute extends RouteBuilder {
                                
                                    @Override
                                    public void configure() {
                                
                                        String uri = "log:c";
                                
                                        from("direct:start")
                                                .toD("log:a", true)
                                                .to(org.apache.camel.ExchangePattern.InOptionalOut, "log:b")
                                                .to(uri);
                                    }
                                }
                            """
                        ,
                        """
                                import org.apache.camel.builder.RouteBuilder;

                                public class MySimpleToDRoute extends RouteBuilder {
                                
                                    @Override
                                    public void configure() {
                                
                                        String uri = "log:c";
                                
                                        from("direct:start")
                                                .toD("log:a", true)
                                                .to(org.apache.camel.ExchangePattern./* InOptionalOut has been removed */, "log:b")
                                                .to(uri);
                                    }
                                }
                                """
                )
        );
    }

    @Test
    void testComponentNameResolver() {
        rewriteRun(
                java(
                        """
                                import org.apache.camel.CamelContext;

                                public class Test {

                                    CamelContext context;

                                    public void test() {
                                        context.getEndpointMap().containsKey("bar://order");
                                    }
                                }
                            """,
                        """
                                import org.apache.camel.CamelContext;

                                public class Test {

                                    CamelContext context;

                                    public void test() {
                                        context./* getEndpointMap has been removed, consider getEndpointRegistry() instead */().containsKey("bar://order");
                                    }
                                }
                                """
                )
        );
    }

    @Test
    void testFallbackConverterOnMethod() {
        rewriteRun(
                java(
                        """
                                import org.apache.camel.FallbackConverter;

                                public class Test {

                                    @FallbackConverter
                                    public void test() {
                                    }
                                }
                            """,
                        """
                                import org.apache.camel.Converter;
                                
                                public class Test {

                                    @Converter(fallback = true)
                                    public void test() {
                                    }
                                }
                                """
                )
        );
    }

    @Test
    void testFallbackConverterOnClassDef() {
        rewriteRun(
                java(
                        """
                                import org.apache.camel.FallbackConverter;

                                @FallbackConverter
                                public class Test {
                                }
                            """,
                        """
                                import org.apache.camel.Converter;
                                
                                @Converter(fallback = true)
                                public class Test {
                                }
                                """
                )
        );
    }

    @Test
    void testEndpointInject() {
        rewriteRun(
                java(
                        """
                                    import org.apache.camel.component.mock.MockEndpoint;
                                    import org.apache.camel.EndpointInject;

                                    public class Test {
                                         
                                             @EndpointInject(uri = "mock:out")
                                             private MockEndpoint endpoint;
                                    }
                                """,
                        """
                                    import org.apache.camel.component.mock.MockEndpoint;
                                    import org.apache.camel.EndpointInject;

                                    public class Test {
                                         
                                             @EndpointInject("mock:out")
                                             private MockEndpoint endpoint;
                                    }
                                """
                )
        );
    }

    @Test
    void testProduce() {
        rewriteRun(
                java(
                        """
                                    import org.apache.camel.component.mock.MockEndpoint;
                                    import org.apache.camel.Produce;

                                    public class Test {
                                         
                                             @Produce(uri = "test")
                                             private MockEndpoint endpoint() {
                                                return null;
                                             }
                                    }
                                """,
                        """
                                    import org.apache.camel.component.mock.MockEndpoint;
                                    import org.apache.camel.Produce;

                                    public class Test {
                                         
                                             @Produce("test")
                                             private MockEndpoint endpoint() {
                                                return null;
                                             }
                                    }
                                """
                )
        );
    }

    @Test
    void testConsume() {
        rewriteRun(
                java(
                        """
                                    import org.apache.camel.component.mock.MockEndpoint;
                                    import org.apache.camel.Consume;

                                    public class Test {
                                         
                                             @Consume(uri = "test")
                                             private MockEndpoint endpoint() {
                                                return null;
                                             }
                                    }
                                """,
                        """
                                    import org.apache.camel.component.mock.MockEndpoint;
                                    import org.apache.camel.Consume;

                                    public class Test {
                                         
                                             @Consume("test")
                                             private MockEndpoint endpoint() {
                                                return null;
                                             }
                                    }
                                """
                )
        );
    }

    @Test
    void testUriEndpoint() {
        rewriteRun(
                java(
                        """
                                import org.apache.camel.spi.UriEndpoint;
                                import org.apache.camel.support.DefaultEndpoint;
                                
                                @UriEndpoint(firstVersion = "2.0.0", label = "rest", lenientProperties = true)
                                public class MicrometerEndpoint extends DefaultEndpoint {
                                }
                                """,
                        """
                                import org.apache.camel.Category;
                                import org.apache.camel.spi.UriEndpoint;
                                import org.apache.camel.support.DefaultEndpoint;
                                
                                @UriEndpoint(firstVersion = "2.0.0",category = {Category.rest}, lenientProperties = true)
                                public class MicrometerEndpoint extends DefaultEndpoint {
                                }
                                """
                )
        );
    }
    @Test
    void testUriEndpointWithUnknownValue() {
        rewriteRun(
                java(
                        """
                                import org.apache.camel.spi.UriEndpoint;
                                import org.apache.camel.support.DefaultEndpoint;
                                
                                @UriEndpoint(firstVersion = "2.0.0", label = "test", lenientProperties = true)
                                public class MicrometerEndpoint extends DefaultEndpoint {
                                }
                                """,
                        """
                                import org.apache.camel.Category;
                                import org.apache.camel.spi.UriEndpoint;
                                import org.apache.camel.support.DefaultEndpoint;
                                
                                @UriEndpoint(firstVersion = "2.0.0",category = {Category."test"/*unknown_value*/}, lenientProperties = true)
                                public class MicrometerEndpoint extends DefaultEndpoint {
                                }
                                """
                )
        );
    }

    @Test
    void testAsyncCallback() {
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(2),
                java(
                        """
                                import org.apache.camel.ProducerTemplate;
                                import org.apache.camel.Exchange;
                                
                                public class Test {
                                    ProducerTemplate template;
                                    
                                    public void test() {
                                        Exchange exchange = context.getEndpoint("direct:start").createExchange();
                                        exchange.getIn().setBody("Hello");
                                
                                        template.asyncCallback("direct:start", exchange, null);
                                    }
                                
                                }
                                """,
                        """
                                import org.apache.camel.ProducerTemplate;
                                import org.apache.camel.Exchange;
                                
                                public class Test {
                                    ProducerTemplate template;
                                    
                                    public void test() {
                                        Exchange exchange = context.getEndpoint("direct:start").createExchange();
                                        exchange.getIn().setBody("Hello");
                                
                                        /* Method 'asyncCallback()' has been replaced by 'asyncSend()' or 'asyncRequest()'.*/template.asyncCallback("direct:start", exchange, null);
                                    }
                                
                                }
                                """
                )
        );
    }

    @Test
    void testOnCamelContextStart() {
        rewriteRun(
                java(
                        """
                                import org.apache.camel.spi.OnCamelContextStart;
                                import org.apache.camel.CamelContext;
                                
                                public class Test implements OnCamelContextStart{
                                    public void onContextStart(CamelContext context) {
                                    }
                                }
                                """,
                        """
                                import org.apache.camel.spi.OnCamelContextStarting;
                                import org.apache.camel.CamelContext;
                                
                                public class Test implements OnCamelContextStarting{
                                    public void onContextStart(CamelContext context) {
                                    }
                                }
                                """
                )
        );
    }

    @Test
    void testOnCamelContextStop() {
        rewriteRun(
                java(
                        """
                                import org.apache.camel.spi.OnCamelContextStop;
                                import org.apache.camel.CamelContext;
                                
                                public class Test implements OnCamelContextStop{
                                    public void onContextStop(CamelContext context) {
                                    }
                                }
                                """,
                        """
                                import org.apache.camel.spi.OnCamelContextStopping;
                                import org.apache.camel.CamelContext;
                                
                                public class Test implements OnCamelContextStopping{
                                    public void onContextStop(CamelContext context) {
                                    }
                                }
                                """
                )
        );
    }

    @Test
    void testAdapt() {
        rewriteRun(
                java(
                        """
                                import org.apache.camel.CamelContext;
                                import org.apache.camel.model.ModelCamelContext;
                                
                                public class Test {
                                
                                    CamelContext context;
                                
                                    public void test() {
                                        context.adapt(ModelCamelContext.class).getRouteDefinition("forMocking");
                                    }
                                }
                            """,
                        """
                                import org.apache.camel.CamelContext;
                                import org.apache.camel.model.ModelCamelContext;
                                
                                public class Test {
                                
                                    CamelContext context;
                                
                                    public void test() {
                                        ((ModelCamelContext)context).getRouteDefinition("forMocking");
                                    }
                                }
                                """
                )
        );
    }

    @Test
    void testMoreOccurrencesAdapt() {
        CamelJavaGroupedRecipe CamelExtensionsGroupedRecipe = new CamelJavaGroupedRecipe();
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(2),
                java(
                        """
                                import org.apache.camel.CamelContext;
                                import org.apache.camel.model.ModelCamelContext;
                                
                                public class Test {
                                
                                    CamelContext context, c1, c2, c3;
                                
                                    public ModelCamelContext test() {
                                        context.adapt(ModelCamelContext.class).getRouteDefinition("forMocking");
                                        c1.adapt(ModelCamelContext.class).getRegistry();
                                        c2.adapt(ModelCamelContext.class);
                                        return c3.adapt(ModelCamelContext.class);
                                    }
                                }
                            """,
                        """
                                import org.apache.camel.CamelContext;
                                import org.apache.camel.model.ModelCamelContext;
                                
                                public class Test {
                                
                                    CamelContext context, c1, c2, c3;
                                
                                    public ModelCamelContext test() {
                                        ((ModelCamelContext)context).getRouteDefinition("forMocking");
                                        ((ModelCamelContext)c1).getRegistry();
                                        /*Method 'adapt' was removed.*/c2.adapt(ModelCamelContext.class);
                                        return /*Method 'adapt' was removed.*/c3.adapt(ModelCamelContext.class);
                                    }
                                }
                                """
                )
        );
    }

    @Test
    void testAdaptStandalone() {
        CamelJavaGroupedRecipe CamelExtensionsGroupedRecipe = new CamelJavaGroupedRecipe();
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(2),
                java(
                        """
                                import org.apache.camel.CamelContext;
                                import org.apache.camel.model.ModelCamelContext;
                                
                                public class Test {
                                
                                    CamelContext context;
                                
                                    public void test() {
                                    
                                        context.adapt(ModelCamelContext.class);
                                    }
                                }
                            """,
                        """
                                import org.apache.camel.CamelContext;
                                import org.apache.camel.model.ModelCamelContext;
                                
                                public class Test {
                                
                                    CamelContext context;
                                
                                    public void test() {
                                       
                                        /*Method 'adapt' was removed.*/context.adapt(ModelCamelContext.class);
                                    }
                                }
                                """
                )
        );
    }

    @Test
    void testAdapt2() {
        rewriteRun(
                java(
                        """
                                package org.apache.camel.quarkus.component.test.it;
                                
                                import org.apache.camel.CamelContext;
                                import org.apache.camel.ExtendedCamelContext;
                                import org.apache.camel.impl.engine.DefaultHeadersMapFactory;
                                
                                public class Test {
                                
                                    CamelContext context;
                                
                                    public DefaultHeadersMapFactory test() {
                                        return context.adapt(ExtendedCamelContext.class).getHeadersMapFactory();
                                    }
                                }
                                """,
                        """
                                package org.apache.camel.quarkus.component.test.it;
                                
                                import org.apache.camel.CamelContext;
                                import org.apache.camel.impl.engine.DefaultHeadersMapFactory;
                                
                                public class Test {
                                
                                    CamelContext context;
                                
                                    public DefaultHeadersMapFactory test() {
                                        return context.getCamelContextExtension().getHeadersMapFactory();
                                    }
                                }
                                """
                )
        );
    }

    @Test
    void testComponenetNameResolverViaPluginHelper() {
        rewriteRun(
                java(
                        """
                                package org.apache.camel.quarkus.component.test.it;

                                import org.apache.camel.CamelContext;
                                import org.apache.camel.ExtendedCamelContext;
                                import org.apache.camel.spi.ComponentNameResolver;

                                public class Test {

                                    CamelContext context;

                                    public void test() {
                                        ComponentNameResolver ec = context.getExtension(ExtendedCamelContext.class).getComponentNameResolver();
                                    }
                                }
                            """,
                        """
                                package org.apache.camel.quarkus.component.test.it;

                                import org.apache.camel.CamelContext;
                                import org.apache.camel.ExtendedCamelContext;
                                import org.apache.camel.spi.ComponentNameResolver;
                                import org.apache.camel.support.PluginHelper;

                                public class Test {

                                    CamelContext context;

                                    public void test() {
                                        ComponentNameResolver ec = PluginHelper.getComponentNameResolver(context);
                                    }
                                }
                                """
                )
        );
    }

    @Test
    void testRuntimeCatalog() {
        rewriteRun(
                java(
                        """
                                package org.apache.camel.quarkus.component.test.it;
                                
                                import org.apache.camel.CamelContext;
                                import org.apache.camel.catalog.RuntimeCamelCatalog;
                                
                                public class Test {
                                
                                    CamelContext context;
                                
                                    public void test() {
                                        final CamelRuntimeCatalog catalog = (CamelRuntimeCatalog) context.getExtension(RuntimeCamelCatalog.class);
                                    }
                                }
                            """,
                        """
                                package org.apache.camel.quarkus.component.test.it;
                                
                                import org.apache.camel.CamelContext;
                                import org.apache.camel.catalog.RuntimeCamelCatalog;
                                
                                public class Test {
                                
                                    CamelContext context;
                                
                                    public void test() {
                                        final CamelRuntimeCatalog catalog = (CamelRuntimeCatalog) context.getCamelContextExtension().getContextPlugin(RuntimeCamelCatalog.class);
                                    }
                                }
                                """
                )
        );
    }

    @Test
    void testAdaptRouteDefinition() {
        rewriteRun(
                java(
                        """
                                package org.apache.camel.quarkus.component.test.it;
                                
                                import org.apache.camel.CamelContext;
                                import org.apache.camel.model.ModelCamelContext;
                                import org.apache.camel.impl.engine.DefaultHeadersMapFactory;
                                
                                public class Test {
                                
                                    CamelContext context;
                                
                                    public DefaultHeadersMapFactory test() {
                                        AdviceWith.adviceWith(context.adapt(ModelCamelContext.class).getRouteDefinition("forMocking"), context, null);
                                    }
                                }
                                """,
                        """
                                package org.apache.camel.quarkus.component.test.it;
                                
                                import org.apache.camel.CamelContext;
                                import org.apache.camel.model.ModelCamelContext;
                                import org.apache.camel.impl.engine.DefaultHeadersMapFactory;
                                
                                public class Test {
                                
                                    CamelContext context;
                                
                                    public DefaultHeadersMapFactory test() {
                                        AdviceWith.adviceWith(((ModelCamelContext)context).getRouteDefinition("forMocking"), context, null);
                                    }
                                }
                                """
                )
        );
    }
    @Test
    void testDecoupleExtendedCamelContext() {
        rewriteRun(
                java(
                        """
                                import org.apache.camel.CamelContext;
                                import org.apache.camel.ExtendedCamelContext;
                                
                                public class Test {
                                
                                    CamelContext getCamelContext() {
                                        return null;
                                    }
                                
                                    public Object test() {
                                        return getCamelContext().adapt(ExtendedCamelContext.class).getPeriodTaskScheduler();
                                    }
                                }
                                """,
                        """
                                import org.apache.camel.CamelContext;
                                
                                public class Test {
                                
                                    CamelContext getCamelContext() {
                                        return null;
                                    }
                                
                                    public Object test() {
                                        return getCamelContext().getCamelContextExtension().getPeriodTaskScheduler();
                                    }
                                }
                                """
                )
        );
    }

    @Test
    void testDecoupleExtendedExchange() {
        rewriteRun(
                java(
                        """
                                import org.apache.camel.Exchange;
                                import org.apache.camel.ExtendedExchange;
                                import org.apache.camel.spi.Synchronization;
                                
                                public class Test {
                                
                                    Exchange exchange;
                                    Synchronization onCompletion;
                                
                                    public void test() {
                                          // add exchange callback
                                          exchange.adapt(ExtendedExchange.class).addOnCompletion(onCompletion);
                                    }
                                }
                                """,
                        """
                                import org.apache.camel.Exchange;
                                import org.apache.camel.spi.Synchronization;
                                
                                public class Test {
                                
                                    Exchange exchange;
                                    Synchronization onCompletion;
                                
                                    public void test() {
                                          // add exchange callback
                                          exchange.getExchangeExtension().addOnCompletion(onCompletion);
                                    }
                                }
                                """
                )
        );
    }

    @Test
    void testDecoupleExtendedExchange2() {
        rewriteRun(
                java(
                        """
                                import org.apache.camel.CamelContext;
                                import org.apache.camel.catalog.RuntimeCamelCatalog;
                                
                                public class Test {
                                
                                    CamelContext context;
                                
                                    public void test() {
                                          final Something catalog = (Something) context.getExtension(RuntimeCamelCatalog.class);
                                    }
                                }
                                """,
                        """
                                import org.apache.camel.CamelContext;
                                import org.apache.camel.catalog.RuntimeCamelCatalog;
                                
                                public class Test {
                                
                                    CamelContext context;
                                
                                    public void test() {
                                          final Something catalog = (Something) context.getCamelContextExtension().getContextPlugin(RuntimeCamelCatalog.class);
                                    }
                                }
                                """
                )
        );
    }

    @Test
    void testExchangeIsFailureHandled() {
        rewriteRun(
                java(
                        """
                                import org.apache.camel.Exchange;
                                import org.apache.camel.ExchangePropertyKey;
                                
                                public class Test {
                                
                                    Exchange exchange;
                                
                                    public void test() {
                                        boolean failureHandled = exchange.getProperty(ExchangePropertyKey.FAILURE_HANDLED);
                                        exchange.removeProperty(ExchangePropertyKey.FAILURE_HANDLED);
                                        exchange.setProperty(ExchangePropertyKey.FAILURE_HANDLED, failureHandled);
                                    }
                                }
                                """,
                        """
                                import org.apache.camel.Exchange;
                                
                                public class Test {
                                
                                    Exchange exchange;
                                
                                    public void test() {
                                        boolean failureHandled = exchange.getExchangeExtension().isFailureHandled();
                                        exchange.getExchangeExtension().setFailureHandled(false);
                                        exchange.getExchangeExtension().setFailureHandled(failureHandled);
                                    }
                                }
                                """
                )
        );
    }

    @Test
    void testThreadPoolRejectedPolicy() {
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(2),
                java(
                        """
                                import org.apache.camel.util.concurrent.ThreadPoolRejectedPolicy;
                                
                                import static org.apache.camel.util.concurrent.ThreadPoolRejectedPolicy.Discard;
                                import static org.apache.camel.util.concurrent.ThreadPoolRejectedPolicy.DiscardOldest;
                                
                                public class Test {
                                
                                    public void test() {
                                        ThreadPoolRejectedPolicy policy = ThreadPoolRejectedPolicy.Discard;
                                        ThreadPoolRejectedPolicy policy2 = Discard;
                                        ThreadPoolRejectedPolicy policy3 = ThreadPoolRejectedPolicy.DiscardOldest;
                                        ThreadPoolRejectedPolicy policy4 = DiscardOldest;
                                    }
                                }
                                """,
                        """
                                import org.apache.camel.util.concurrent.ThreadPoolRejectedPolicy;
                                  
                                /*'ThreadPoolRejectedPolicy.Discard' has been removed, consider using 'ThreadPoolRejectedPolicy.Abort'.*/import static org.apache.camel.util.concurrent.ThreadPoolRejectedPolicy.Discard;
                                /*'ThreadPoolRejectedPolicy.DiscardOldest' has been removed, consider using 'ThreadPoolRejectedPolicy.Abort'.*/import static org.apache.camel.util.concurrent.ThreadPoolRejectedPolicy.DiscardOldest;

                                public class Test {

                                    public void test() {
                                        ThreadPoolRejectedPolicy policy = /*'ThreadPoolRejectedPolicy.Discard' has been removed, consider using 'ThreadPoolRejectedPolicy.Abort'.*/ThreadPoolRejectedPolicy.Discard;
                                        ThreadPoolRejectedPolicy policy2 = Discard;
                                        ThreadPoolRejectedPolicy policy3 = /*'ThreadPoolRejectedPolicy.DiscardOldest' has been removed, consider using 'ThreadPoolRejectedPolicy.Abort'.*/ThreadPoolRejectedPolicy.DiscardOldest;
                                        ThreadPoolRejectedPolicy policy4 = DiscardOldest;
                                    }
                                }
                                """
                )
        );
    }

    @Test
    void testSimpleBuilder() {
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(2).recipe(toRecipe(() -> new CamelJavaGroupedRecipe().getVisitor())),
                java(
                        """
                                import org.apache.camel.builder.SimpleBuilder;
                                """,
                        """
                                /*'java.beans.SimpleBeanInfo' has been removed, (class was used internally).*/import org.apache.camel.builder.SimpleBuilder;
                              """
                )
        );
    }

    @Test
    void test1IntrospectionSupport() {
        rewriteRun(
                java(
                        """
                                    import org.apache.camel.support.IntrospectionSupport;

                                    import static org.apache.camel.support.IntrospectionSupport.extractProperties;
                                """,
                        """
                                    import org.apache.camel.impl.engine.IntrospectionSupport;
                                    
                                    import static org.apache.camel.impl.engine.IntrospectionSupport.extractProperties;
                                """
                )
        );
    }

    @Test
    void testIntrospectionSupport() {
        rewriteRun(
                java(
                        """
                                    import org.apache.camel.support.IntrospectionSupport;

                                    import static org.apache.camel.support.IntrospectionSupport.extractProperties;
                                    import static org.apache.camel.support.IntrospectionSupport.findSetterMethodsOrderedByParameterType;
                                    import static org.apache.camel.support.IntrospectionSupport.getProperties;
                                    import static org.apache.camel.support.IntrospectionSupport.getProperty;
                                    import static org.apache.camel.support.IntrospectionSupport.getPropertyGetter;
                                    import static org.apache.camel.support.IntrospectionSupport.getPropertySetter;
                                    import static org.apache.camel.support.IntrospectionSupport.isGetter;
                                    import static org.apache.camel.support.IntrospectionSupport.isSetter;
                                """,
                        """
                                    import org.apache.camel.impl.engine.IntrospectionSupport;
                                    
                                    import static org.apache.camel.impl.engine.IntrospectionSupport.*;
                                """
                )
        );
    }

    @Test
    void testarchetypeCatalogAsXml() {
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(2),
                java(
                        """
                                    import org.apache.camel.catalog.CamelCatalog;
                                    
                                    public class Test {
                                    
                                        static CamelCatalog catalog;
    
                                        public void test() {
                                            String schema = catalog.archetypeCatalogAsXml();
                                        }
                                    }
                                """,
                        """
                                    import org.apache.camel.catalog.CamelCatalog;
                                    
                                    public class Test {
                                    
                                        static CamelCatalog catalog;
    
                                        public void test() {
                                            String schema = /* Method 'archetypeCatalogAsXml' has been removed. */catalog.archetypeCatalogAsXml();
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void testMainListenerConfigureImpl() {
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(2),
                java(
                        """
                                    import org.apache.camel.CamelContext;
                                    import org.apache.camel.main.MainListener;
                                    
                                    public class Test implements MainListener {
                                    
                                        @Override
                                        public void configure(CamelContext context) {
                                            //do something
                                        }
                                    }
                                """,
                        """
                                    import org.apache.camel.CamelContext;
                                    import org.apache.camel.main.MainListener;
                                    
                                    public class Test implements MainListener {
                                    
                                        /* Method 'configure' was removed from `org.apache.camel.main.MainListener`, consider using 'beforeConfigure' or 'afterConfigure'. */@Override
                                        public void configure(CamelContext context) {
                                            //do something
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void testDumpRoutes() {
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(2),
                java(
                        """
                                    import org.apache.camel.CamelContext;
                                    
                                    public class Test {
                                        public void test(CamelContext context) {
                                            boolean dump = context.isDumpRoutes();
                                            context.setDumpRoutes(true);
                                        }
                                    }
                                """,
                        """
                                    import org.apache.camel.CamelContext;
                                    
                                    public class Test {
                                        public void test(CamelContext context) {
                                            boolean dump = /* Method 'getDumpRoutes' returns String value ('xml' or 'yaml' or 'false'). */context.getDumpRoutes();
                                            /* Method 'setDumpRoutes' accepts String parameter ('xml' or 'yaml' or 'false'). */context.setDumpRoutes(true);
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void testAdapt3() {
        rewriteRun(
                java(
                        """
                                    import jakarta.enterprise.context.ApplicationScoped;
                                    import jakarta.inject.Inject;
                                    import jakarta.ws.rs.Consumes;
                                    import jakarta.ws.rs.GET;
                                    import jakarta.ws.rs.POST;
                                    import jakarta.ws.rs.Path;
                                    import jakarta.ws.rs.PathParam;
                                    import jakarta.ws.rs.core.MediaType;
                                    
                                    import org.apache.camel.CamelContext;
                                    import org.apache.camel.ProducerTemplate;
                                    import org.apache.camel.builder.AdviceWith;
                                    import org.apache.camel.builder.AdviceWithRouteBuilder;
                                    import org.apache.camel.component.mock.MockEndpoint;
                                    import org.apache.camel.model.ModelCamelContext;
                                    import org.jboss.logging.Logger;
                                    import org.wildfly.common.Assert;
                                    
                                    public class Test {
                                        public void test(CamelContext context) {
                                            // advice the first route using the inlined AdviceWith route builder
                                            // which has extended capabilities than the regular route builder
                                            AdviceWith.adviceWith(context.adapt(ModelCamelContext.class).getRouteDefinition("forMocking"), context,
                                                    new AdviceWithRouteBuilder() {
                                                        @Override
                                                        public void configure() throws Exception {
                                                            mockEndpoints("direct:mock.*", "log:mock.*");
                                                        }
                                                    });
                                        }
                                    }
                                    """,
    """
                import jakarta.enterprise.context.ApplicationScoped;
                import jakarta.inject.Inject;
                import jakarta.ws.rs.Consumes;
                import jakarta.ws.rs.GET;
                import jakarta.ws.rs.POST;
                import jakarta.ws.rs.Path;
                import jakarta.ws.rs.PathParam;
                import jakarta.ws.rs.core.MediaType;
                
                import org.apache.camel.CamelContext;
                import org.apache.camel.ProducerTemplate;
                import org.apache.camel.builder.AdviceWith;
                import org.apache.camel.builder.AdviceWithRouteBuilder;
                import org.apache.camel.component.mock.MockEndpoint;
                import org.apache.camel.model.ModelCamelContext;
                import org.jboss.logging.Logger;
                import org.wildfly.common.Assert;
                
                public class Test {
                    public void test(CamelContext context) {
                        // advice the first route using the inlined AdviceWith route builder
                        // which has extended capabilities than the regular route builder
                        AdviceWith.adviceWith(((ModelCamelContext)context).getRouteDefinition("forMocking"), context,
                                new AdviceWithRouteBuilder() {
                                    @Override
                                    public void configure() throws Exception {
                                        mockEndpoints("direct:mock.*", "log:mock.*");
                                    }
                                });
                    }
                }
"""
                )
        );
    }

    @Test
    void testBacklogTracerEventMessage() {
        rewriteRun(
                java(
                        """
                                import org.apache.camel.api.management.mbean.BacklogTracerEventMessage;

                                public class Test  {

                                    public void test() {
                                        org.apache.camel.api.management.mbean.BacklogTracerEventMessage msg;
                                    }
                                }
                                """
                        ,
                        """
                                import org.apache.camel.spi.BacklogTracerEventMessage;

                                public class Test  {

                                    public void test() {
                                        org.apache.camel.spi.BacklogTracerEventMessage msg;
                                    }
                                }
                                """
                )
        );
    }
}
