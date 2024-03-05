package io.quarkus.updates.camel.camel40;

import io.quarkus.updates.camel.CamelQuarkusTestUtil;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.properties.Assertions;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

public class CamelMainTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelQuarkusTestUtil.recipeForVersion(spec, "3.8", "org.openrewrite.java.camel.migrate.RrouteControllerProperties")
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testCamelMainRouteConroller() {
        //language=java
        rewriteRun(Assertions.properties("""
                   #test
                   camel.main.routeControllerBackOffDelay=1000
                """,
                """
                            #test
                            camel.routecontroller.backOffDelay=1000
                        """));
    }

}
