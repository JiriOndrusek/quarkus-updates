package io.quarkus.updates.camel.camel42;

import io.quarkus.updates.camel.CamelQuarkusTestUtil;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.properties.Assertions;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

public class Camel42Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelQuarkusTestUtil.recipe3_8(spec)
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true).classpath("camel-json-validator"))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testCamelMainDebugger() {
        rewriteRun(Assertions.properties("""
                   #test
                   quarkus.camel.main.debugger=true
                   camel.main.debugger=true
                """,
                """
                            #test
                            quarkus.camel.debug.enabled=true
                            camel.debug.enabled=true
                        """));
    }
}
