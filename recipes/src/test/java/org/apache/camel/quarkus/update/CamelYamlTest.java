package org.apache.camel.quarkus.update;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.yaml.Assertions;

import static org.openrewrite.test.RewriteTest.toRecipe;

public class CamelYamlTest implements RewriteTest {

    @Test
    void testStepsToFrom1() {
        rewriteRun(
                spec -> spec.recipe(toRecipe(() -> new CamelYamGroupedRecipe().getVisitor())),
                Assertions.yaml(
                        """
                                  route:
                                    from:
                                      uri: "direct:info"
                                    steps:
                                      log: "message"
                                """,
                        """
                                  route:
                                    from:
                                      uri: "direct:info"
                                      steps:
                                        log: "message"                            
                                """
                )
        );
    }

    @Test
    void testStepsToFrom2() {
        rewriteRun(
                spec -> spec.recipe(toRecipe(() -> new CamelYamGroupedRecipe().getVisitor())),
                Assertions.yaml(
                        """
                                    from:
                                      uri: "direct:info"
                                    steps:
                                      log: "message"
                                """,
                        """
                                    from:
                                      uri: "direct:info"
                                      steps:
                                        log: "message"                            
                                """
                )
        );
    }

    @Test
    void testStepsToFrom3() {
        rewriteRun(
                spec -> spec.recipe(toRecipe(() -> new CamelYamGroupedRecipe().getVisitor())),
                Assertions.yaml(
                        """
                                - from:
                                    uri: "direct:start"
                                  steps:
                                  - filter:
                                      expression:
                                        simple: "${in.header.continue} == true"
                                      steps:
                                        - to:
                                            uri: "log:filtered"
                                  - to:
                                      uri: "log:original"
                                """,
                        """
                                  - from:
                                      uri: "direct:start"
                                      steps:
                                        - filter:
                                            expression:
                                              simple: "${in.header.continue} == true"
                                            steps:
                                              - to:
                                                  uri: "log:filtered"
                                        - to:
                                            uri: "log:original"
                                """
                )
        );
    }

    @Test
    void testRouteConfigurationWithOnException() {
        rewriteRun(
                spec -> spec.recipe(toRecipe(() -> new CamelYamGroupedRecipe().getVisitor())),
                Assertions.yaml(
                        """
                                - route-configuration:
                                    - id: "yamlRouteConfiguration"
                                    - on-exception:
                                        handled:
                                          constant: "true"
                                        exception:
                                          - "org.apache.camel.quarkus.core.it.routeconfigurations.RouteConfigurationsException"
                                        steps:
                                          - set-body:
                                              constant:
                                                  expression: "onException has been triggered in yamlRouteConfiguration"
                                """,
                        """
                                - route-configuration:
                                    id: "yamlRouteConfiguration"
                                    on-exception:
                                      - on-exception:
                                          handled:
                                            constant: "true"
                                          exception:
                                            - "org.apache.camel.quarkus.core.it.routeconfigurations.RouteConfigurationsException"
                                          steps:
                                            - set-body:
                                                constant:
                                                  expression: "onException has been triggered in yamlRouteConfiguration"
                              """
                )
        );
    }

    @Test
    void testRouteConfigurationWithoutOnException() {
        rewriteRun(
                spec -> spec.recipe(toRecipe(() -> new CamelYamGroupedRecipe().getVisitor())),
                Assertions.yaml(
                        """
                                - route-configuration:
                                    - id: "__id"
                                """,
                        """
                                - route-configuration:
                                    id: "__id"
                              """
                )
        );
    }

    @Test
    void testDoubleDocument() {
        rewriteRun(
                spec -> spec.recipe(toRecipe(() -> new CamelYamGroupedRecipe().getVisitor())),
                Assertions.yaml(
                        """
                                - route-configuration:
                                    - id: "yamlRouteConfiguration1"
                                    - on-exception:
                                        handled:
                                          constant: "true"
                                        exception:
                                          - "org.apache.camel.quarkus.core.it.routeconfigurations.RouteConfigurationsException"
                                        steps:
                                          - set-body:
                                              constant:
                                                  expression: "onException has been triggered in yamlRouteConfiguration"
                                ---
                                - route-configuration:
                                    - id: "yamlRouteConfiguration2"
                                    - on-exception:
                                        handled:
                                          constant: "true"
                                        exception:
                                          - "org.apache.camel.quarkus.core.it.routeconfigurations.RouteConfigurationsException"
                                        steps:
                                          - set-body:
                                              constant:
                                                  expression: "onException has been triggered in yamlRouteConfiguration"
                                """,
                        """
                                - route-configuration:
                                    id: "yamlRouteConfiguration1"
                                    on-exception:
                                      - on-exception:
                                          handled:
                                            constant: "true"
                                          exception:
                                            - "org.apache.camel.quarkus.core.it.routeconfigurations.RouteConfigurationsException"
                                          steps:
                                            - set-body:
                                                constant:
                                                  expression: "onException has been triggered in yamlRouteConfiguration"
                                ---
                                - route-configuration:
                                    id: "yamlRouteConfiguration2"
                                    on-exception:
                                      - on-exception:
                                          handled:
                                            constant: "true"
                                          exception:
                                            - "org.apache.camel.quarkus.core.it.routeconfigurations.RouteConfigurationsException"
                                          steps:
                                            - set-body:
                                                constant:
                                                  expression: "onException has been triggered in yamlRouteConfiguration"
                              """
                )
        );
    }

    @Test
    void testDoubleDocumentSimple() {
        rewriteRun(
                spec -> spec.recipe(toRecipe(() -> new CamelYamGroupedRecipe().getVisitor())),
                Assertions.yaml(
                        """
                                - route-configuration:
                                    - id: "__id1"
                                ---
                                - route-configuration:
                                    - id: "__id2"
                                """,
                        """
                                - route-configuration:
                                    id: "__id1"
                                ---
                                - route-configuration:
                                    id: "__id2"
                              """
                )
        );
    }

    @Test
    void testRouteConfigurationIdempotent() {
        rewriteRun(
                spec -> spec.recipe(toRecipe(() -> new CamelYamGroupedRecipe().getVisitor())),
                Assertions.yaml(
                        """
                                - route-configuration:
                                    id: "yamlRouteConfiguration"
                                    on-exception:
                                      - on-exception:
                                          handled:
                                            constant: "true"
                                          exception:
                                            - "org.apache.camel.quarkus.core.it.routeconfigurations.RouteConfigurationsException"
                                          steps:
                                            - set-body:
                                                constant:
                                                  expression: "onException has been triggered in yamlRouteConfiguration"
                              """
                )
        );
    }


}
