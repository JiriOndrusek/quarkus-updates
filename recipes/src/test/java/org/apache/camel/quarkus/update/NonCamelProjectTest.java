package org.apache.camel.quarkus.update;

import org.junit.jupiter.api.Test;
import org.openrewrite.yaml.Assertions;

public class NonCamelProjectTest extends BaseCamelQuarkusTest {

    @Test
    void testNonCamelProject() {
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
    void testWithCamelProject() {
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

}
