package org.apache.camel.quarkus.update;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.test.RewriteTest.toRecipe;

public class ExtendedContextRecipeTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ExtendedContextRecipe())
                .parser(JavaParser.fromJavaVersion()
                        .logCompilationWarningsAndErrors(true)
                        .classpath("camel-api"))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testComponentNameResolver() {
        rewriteRun(
                spec -> spec.recipe(toRecipe(() -> new ExtendedContextRecipe().getVisitor())),
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
                spec -> spec.recipe(toRecipe(() -> new ExtendedContextRecipe().getVisitor())),
                java(
                        """
                                package org.apache.camel.quarkus.component.test.it;
                                
                                import org.apache.camel.CamelContext;
                                import org.apache.camel.catalog.RuntimeCamelCatalog;
                                
                                public class Test {
                                
                                    CamelContext context;
                                
                                    public void test() {
                                        RuntimeCamelCatalog rc = context.getExtension(RuntimeCamelCatalog.class);
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
                                        RuntimeCamelCatalog rc = context.getCamelContextExtension().getContextPlugin(RuntimeCamelCatalog.class);
                                    }
                                }
                                """
                )
        );
    }

    @Test
    void expandsExpectedCustomerInfoMethod() {
        rewriteRun(
                java(
                        """
                                    package com.yourorg;
                                        
                                    import java.util.Date;
                                        
                                    public abstract class Customer {
                                        private Date dateOfBirth;
                                        private String firstName;
                                        private String lastName;
                                        
                                        public abstract void setCustomerInfo(String lastName);
                                    }
                                """,
                        """
                                    package com.yourorg;
                                        
                                    import java.util.Date;
                                        
                                    public abstract class Customer {
                                        private Date dateOfBirth;
                                        private String firstName;
                                        private String lastName;
                                        
                                        public void setCustomerInfo(Date dateOfBirth, String firstName, String lastName) {
                                            this.dateOfBirth = dateOfBirth;
                                            this.firstName = firstName;
                                            this.lastName = lastName;
                                        }
                                    }
                                """
                )
        );
    }
}

