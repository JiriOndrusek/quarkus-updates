package io.quarkus.updates.quarkiverse.cxf;

import io.quarkus.updates.core.CoreTestUtil;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import java.nio.file.Path;

import static org.openrewrite.properties.Assertions.properties;

public class CxfUpdate331Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CoreTestUtil.recipe(
                        spec,
                        Path.of("quarkus-updates", "io.quarkiverse.cxf", "quarkus-cxf", "3.31.yaml"))
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void migrateProxyConfiguration() {
        //language=properties
        rewriteRun(
                properties(
                        """
                                quarkus.cxf.client.myService.proxy-server=proxy.example.com
                                quarkus.cxf.client.myService.proxy-server-port=3128
                                quarkus.cxf.client.myService.proxy-server-type=http
                                quarkus.cxf.client.myService.proxy-username=proxyuser
                                quarkus.cxf.client.myService.proxy-password=proxypass
                                quarkus.cxf.client.myService.non-proxy-hosts=localhost|www.example.com
                                quarkus.cxf.client.myService.wsdl=https://example.com/service?wsdl
                                """,
                        """
                                quarkus.proxy.myService.host=proxy.example.com
                                quarkus.cxf.client.myService.proxy-configuration-name=myService
                                quarkus.proxy.myService.port=3128
                                quarkus.proxy.myService.username=proxyuser
                                quarkus.proxy.myService.password=proxypass
                                quarkus.proxy.myService.non-proxy-hosts=localhost,www.example.com
                                quarkus.cxf.client.myService.wsdl=https://example.com/service?wsdl
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void migrateProxyServerOnly() {
        //language=properties
        rewriteRun(
                properties(
                        """
                                quarkus.cxf.client.myService.proxy-server=proxy.example.com
                                """,
                        """
                                quarkus.proxy.myService.host=proxy.example.com
                                quarkus.cxf.client.myService.proxy-configuration-name=myService
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void migrateWithProfile() {
        //language=properties
        rewriteRun(
                properties(
                        """
                                %dev.quarkus.cxf.client.myService.proxy-server=proxy.example.com
                                %dev.quarkus.cxf.client.myService.proxy-server-port=3128
                                """,
                        """
                                %dev.quarkus.proxy.myService.host=proxy.example.com
                                %dev.quarkus.cxf.client.myService.proxy-configuration-name=myService
                                %dev.quarkus.proxy.myService.port=3128
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void notChanged() {
        // all three no-change paths in one file: an already present proxy-configuration-name,
        // orphan proxy options without proxy-server, and a socks proxy
        //language=properties
        rewriteRun(
                properties(
                        """
                                quarkus.cxf.client.named.proxy-server=proxy.example.com
                                quarkus.cxf.client.named.proxy-configuration-name=corporate
                                quarkus.cxf.client.orphan.proxy-username=orphanUser
                                quarkus.cxf.client.orphan.non-proxy-hosts=localhost|*.example.com
                                quarkus.cxf.client.socks.proxy-server=socks.example.com
                                quarkus.cxf.client.socks.proxy-server-type=socks
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }
}
