package io.quarkus.updates.quarkiverse.cxf;

import io.quarkus.updates.core.CoreTestUtil;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import java.nio.file.Path;

import static org.openrewrite.properties.Assertions.properties;

public class CxfUpdate338Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CoreTestUtil.recipe(
                        spec,
                        Path.of("quarkus-updates", "io.quarkiverse.cxf", "quarkus-cxf", "3.38.yaml"))
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void renameWsAddressingProperties() {
        //language=properties
        rewriteRun(
                properties(
                        """
                                org.apache.cxf.ws.addressing.decoupled.enabled=true
                                org.apache.cxf.ws.addressing.decoupled.allowedSchemes=http,https
                                """,
                        """
                                quarkus.cxf.endpoint.addressing.decoupled.enabled=true
                                quarkus.cxf.endpoint.addressing.decoupled.allowed-schemes=http,https
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }
}
