package io.quarkus.updates.camel.camel40;

import io.quarkus.updates.camel.CamelQuarkusTestUtil;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

public class CamelJsonValidatorTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelQuarkusTestUtil.recipeForVersion(spec, "3.8", "org.openrewrite.java.camel.migrate.DefaultJsonValidatorLoader")
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true).classpath("camel-json-validator"))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testRenamedMethods() {
        //language=java
        rewriteRun(java(
                """
                            import org.apache.camel.component.jsonvalidator.DefaultJsonSchemaLoader;
                            
                            public class CustomJsonValidator extends DefaultJsonSchemaLoader {
                            }
                        """,
                """
                            import org.apache.camel.component.jsonvalidator.DefaultJsonUriSchemaLoader;
                            
                            public class CustomJsonValidator extends DefaultJsonUriSchemaLoader {
                            }
                        """));
    }
}
