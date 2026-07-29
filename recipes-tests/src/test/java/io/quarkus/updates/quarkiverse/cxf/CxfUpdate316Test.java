package io.quarkus.updates.quarkiverse.cxf;

import io.quarkus.updates.core.CoreTestUtil;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import java.nio.file.Path;

import static org.openrewrite.properties.Assertions.properties;

public class CxfUpdate316Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CoreTestUtil.recipe(
                        spec,
                        Path.of("quarkus-updates", "io.quarkiverse.cxf", "quarkus-cxf", "3.16.yaml"))
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void migrateHostnameVerifier() {
        //language=properties
        rewriteRun(
                properties(
                        """
                                quarkus.cxf.client.myService.hostname-verifier=AllowAllHostnameVerifier
                                quarkus.cxf.client.myService.wsdl=https://example.com/service?wsdl
                                """,
                        """
                                quarkus.tls.myService.hostname-verification-algorithm=NONE
                                quarkus.cxf.client.myService.tls-configuration-name=myService
                                quarkus.cxf.client.myService.wsdl=https://example.com/service?wsdl
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void migrateHostnameVerifierWithProfile() {
        //language=properties
        rewriteRun(
                properties(
                        """
                                %dev.quarkus.cxf.client.myService.hostname-verifier=AllowAllHostnameVerifier
                                """,
                        """
                                %dev.quarkus.tls.myService.hostname-verification-algorithm=NONE
                                %dev.quarkus.cxf.client.myService.tls-configuration-name=myService
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void noChangeWhenDifferentVerifier() {
        //language=properties
        rewriteRun(
                properties(
                        """
                                quarkus.cxf.client.myService.hostname-verifier=SomeOtherVerifier
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }
}
