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
        spec.recipe(new CamelAPIsRecipe())
                .parser(JavaParser.fromJavaVersion()
                        .logCompilationWarningsAndErrors(true)
                        .classpath("camel-api","camel-support"))
                .typeValidationOptions(TypeValidation.none());;
    }

    @Test
    void testRemovedExchangePatternInOptionalOut() {
        rewriteRun(
                spec -> spec.recipe(toRecipe(() -> new CamelAPIsRecipe().getVisitor())),
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
                spec -> spec.recipe(toRecipe(() -> new CamelAPIsRecipe().getVisitor())),
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
                spec -> spec.recipe(toRecipe(() -> new CamelAPIsRecipe().getVisitor())),
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
                spec -> spec.recipe(toRecipe(() -> new CamelAPIsRecipe().getVisitor())),
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
                spec -> spec.recipe(toRecipe(() -> new CamelAPIsRecipe().getVisitor())),
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
                spec -> spec.recipe(toRecipe(() -> new CamelAPIsRecipe().getVisitor())),
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
                spec -> spec.recipe(toRecipe(() -> new CamelAPIsRecipe().getVisitor())),
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
                spec -> spec.recipe(toRecipe(() -> new CamelAPIsRecipe().getVisitor())),
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
                spec -> spec.recipe(toRecipe(() -> new CamelAPIsRecipe().getVisitor())),
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
                                
                                @UriEndpoint(firstVersion = "2.0.0",category = {Category.REST}, lenientProperties = true)
                                public class MicrometerEndpoint extends DefaultEndpoint {
                                }
                                """
                )
        );
    }
    @Test
    void testUriEndpointWithUnknownValue() {
        rewriteRun(
                spec -> spec.recipe(toRecipe(() -> new CamelAPIsRecipe().getVisitor())),
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
                spec -> spec.expectedCyclesThatMakeChanges(2).recipe(toRecipe(() -> new CamelAPIsRecipe().getVisitor())),
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
                                
                                        // Method 'asyncCallback(' has been replaced by 'asyncSend(' or 'asyncRequest('.
                                        template.asyncCallback("direct:start", exchange, null);
                                    }
                                
                                }
                                """
                )
        );
    }

}
