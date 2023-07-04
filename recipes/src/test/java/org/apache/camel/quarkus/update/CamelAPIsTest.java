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
                        .classpath("camel-api"))
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
}
