package org.apache.camel.quarkus.update;

import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.test.RewriteTest.toRecipe;

import org.junit.jupiter.api.Test;

public class CamelBeanRecipeTest implements RewriteTest{
    
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new CamelJavaGroupedRecipe())
                .parser(JavaParser.fromJavaVersion()
                        .logCompilationWarningsAndErrors(true)
                        .classpath("camel-bean"))
                .typeValidationOptions(TypeValidation.none());;
    }

    @Test
    void testClassTypeAndInt() {
            rewriteRun(
                    java(
                            """
                                import org.apache.camel.builder.RouteBuilder;

                                public class MySimpleToDRoute extends RouteBuilder {
                                
                                    @Override
                                    public void configure() {
                                        from("direct:a")
                                        .to("bean:myBean?method=foo(com.foo.MyOrder, int)");
                                    }
                                }
                            """,
                            """
                                import org.apache.camel.builder.RouteBuilder;

                                public class MySimpleToDRoute extends RouteBuilder {
                                
                                    @Override
                                    public void configure() {
                                        from("direct:a")
                                        .to("bean:myBean?method=foo(com.foo.MyOrder.class, int.class)");
                                    }
                                }
                            """

                    )
        );
    }

    @Test
    void testClassTypeAndBoolean() {
        rewriteRun(
                java(
                        """
                            import org.apache.camel.builder.RouteBuilder;

                            public class MySimpleToDRoute extends RouteBuilder {
                            
                                @Override
                                public void configure() {
                                    from("direct:a")
                                    .to("bean:myBean?method=foo(com.foo.MyOrder, true)");
                                }
                            }
                        """,
                        """
                            import org.apache.camel.builder.RouteBuilder;

                            public class MySimpleToDRoute extends RouteBuilder {
                            
                                @Override
                                public void configure() {
                                    from("direct:a")
                                    .to("bean:myBean?method=foo(com.foo.MyOrder.class, true)");
                                }
                            }
                        """

                )
        );
    }
    

    @Test
    void testClassTypeAndFloat() {
        rewriteRun(
                java(
                        """
                            import org.apache.camel.builder.RouteBuilder;

                            public class MySimpleToDRoute extends RouteBuilder {
                            
                                @Override
                                public void configure() {
                                    from("direct:a")
                                    .to("bean:myBean?method=foo(com.foo.MyOrder, float)");
                                }
                            }
                        """,
                        """
                            import org.apache.camel.builder.RouteBuilder;

                            public class MySimpleToDRoute extends RouteBuilder {
                            
                                @Override
                                public void configure() {
                                    from("direct:a")
                                    .to("bean:myBean?method=foo(com.foo.MyOrder.class, float.class)");
                                }
                            }
                        """

                )
        );
    }

    @Test
    void testDoubleAndChar() {
        rewriteRun(
                java(
                        """
                            import org.apache.camel.builder.RouteBuilder;

                            public class MySimpleToDRoute extends RouteBuilder {
                            
                                @Override
                                public void configure() {
                                    from("direct:a")
                                    .to("bean:myBean?method=foo(double, char)");
                                }
                            }
                        """,
                        """
                            import org.apache.camel.builder.RouteBuilder;

                            public class MySimpleToDRoute extends RouteBuilder {
                            
                                @Override
                                public void configure() {
                                    from("direct:a")
                                    .to("bean:myBean?method=foo(double.class, char.class)");
                                }
                            }
                        """

                )
        );
    }

    @Test
    void testMultipleTo() {
        rewriteRun(
                spec -> spec.recipe(toRecipe(() -> new CamelJavaGroupedRecipe().getVisitor())),
                java(
                        """
                            import org.apache.camel.builder.RouteBuilder;

                            public class MySimpleToDRoute extends RouteBuilder {
                            
                                @Override
                                public void configure() {
                                    from("direct:a")
                                    .to("bean:myBean?method=foo(double, char)")
                                    .to("bean:myBean?method=bar(float, int)");
                                }
                            }
                            """,
                        """
                            import org.apache.camel.builder.RouteBuilder;

                            public class MySimpleToDRoute extends RouteBuilder {
                            
                                @Override
                                public void configure() {
                                    from("direct:a")
                                    .to("bean:myBean?method=foo(double.class, char.class)")
                                    .to("bean:myBean?method=bar(float.class, int.class)");
                                }
                            }
                        """

                )
        );
    }
}


