package org.apache.camel.quarkus.update;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openrewrite.yaml.Assertions;

import static org.openrewrite.java.Assertions.java;

public class NonCamelProjectTest extends BaseCamelQuarkusTest {

    @BeforeAll
    static void before() {
        //end overriding
        RecipesUtil.overrideInternallyCamelPresent(false);
    }

    @Test
    void testYamlNoCamelProject() {
        rewriteRun(
                spec -> spec.recipe(new CamelQuarkusMigrationRecipe()),
                Assertions.yaml(
                                """
                                        - route-configuration:
                                            - id: "__id"
                                        """
                )
        );
    }

    @Test
    void testYamlCamelProject() {
        rewriteRun(
                spec -> spec.recipe(new CamelQuarkusMigrationRecipe()),
                withCamel(Assertions.yaml(
                        """
                                - route-configuration:
                                    - id: "__id"
                                """,
                        """
                                - route-configuration:
                                    id: "__id"
                              """
                ))
        );
    }

    @Test
    void testPropertiesNoCamelProject() {
        rewriteRun(
                spec -> spec.recipe(new CamelQuarkusMigrationRecipe()),
                org.openrewrite.properties.Assertions.properties(
                        """
                                   camel.threadpool.rejectedPolicy=DiscardOldest
                                """
                )
        );
    }

    @Test
    void testPropertiesCamelProject() {
        rewriteRun(
                spec -> spec.recipe(new CamelQuarkusMigrationRecipe()).expectedCyclesThatMakeChanges(2),
                withCamel(org.openrewrite.properties.Assertions.properties(
                        """
                                   #test
                                   camel.threadpool.rejectedPolicy=Discard
                                """,
                        """
                                    #test
                                    #'ThreadPoolRejectedPolicy.camel.threadpool.rejectedPolicy' has been removed, consider using 'Abort'. camel.threadpool.rejectedPolicy=Discard  
                                """
                ))
        );
    }

    @Test
    void testJavaNonCamel() {
        rewriteRun(
                spec -> spec.recipe(new CamelQuarkusMigrationRecipe()),
                java(
                        """
                                import org.apache.camel.builder.SimpleBuilder;
                                """
                )
        );
    }

    @Test
    void testJavaCamel() {
        rewriteRun(
                spec -> spec.recipe(new CamelQuarkusMigrationRecipe()).expectedCyclesThatMakeChanges(2),
                withCamel(java(
                        """
                                import org.apache.camel.builder.SimpleBuilder;
                                """,
                        """
                                /*'java.beans.SimpleBeanInfo' has been removed, (class was used internally).*/import org.apache.camel.builder.SimpleBuilder;
                              """
                ))
        );
    }

}
