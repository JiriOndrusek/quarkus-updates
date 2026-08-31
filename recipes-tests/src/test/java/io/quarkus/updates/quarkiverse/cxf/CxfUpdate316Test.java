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
    void migrateHostnameVerifierWithTrustStore() {
        // mirrors the example from the 3.16.0 release notes: the trust store must move into the
        // TLS configuration, because tls-configuration-name plus trust-store* fails at runtime
        //language=properties
        rewriteRun(
                properties(
                        """
                                quarkus.cxf.client.helloAllowAll.client-endpoint-url=https://localhost:8444/services/hello
                                quarkus.cxf.client.helloAllowAll.trust-store-type=pkcs12
                                quarkus.cxf.client.helloAllowAll.trust-store=client-truststore.pkcs12
                                quarkus.cxf.client.helloAllowAll.trust-store-password=secret
                                quarkus.cxf.client.helloAllowAll.hostname-verifier=AllowAllHostnameVerifier
                                """,
                        """
                                quarkus.cxf.client.helloAllowAll.client-endpoint-url=https://localhost:8444/services/hello
                                quarkus.tls.helloAllowAll.trust-store.p12.path=client-truststore.pkcs12
                                quarkus.tls.helloAllowAll.trust-store.p12.password=secret
                                quarkus.tls.helloAllowAll.hostname-verification-algorithm=NONE
                                quarkus.cxf.client.helloAllowAll.tls-configuration-name=helloAllowAll
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void migrateHostnameVerifierWithJksTrustStoreWithoutType() {
        // JKS is the documented default of trust-store-type
        //language=properties
        rewriteRun(
                properties(
                        """
                                quarkus.cxf.client.myService.trust-store=client-truststore.jks
                                quarkus.cxf.client.myService.trust-store-password=secret
                                quarkus.cxf.client.myService.hostname-verifier=AllowAllHostnameVerifier
                                """,
                        """
                                quarkus.tls.myService.trust-store.jks.path=client-truststore.jks
                                quarkus.tls.myService.trust-store.jks.password=secret
                                quarkus.tls.myService.hostname-verification-algorithm=NONE
                                quarkus.cxf.client.myService.tls-configuration-name=myService
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

    @Test
    void noChangeWhenClientPinnedToUrlConnectionConduit() {
        // hostname-verifier keeps working with URLConnectionHTTPConduitFactory, while
        // hostname-verification-algorithm would fail at runtime there
        //language=properties
        rewriteRun(
                properties(
                        """
                                quarkus.cxf.client.myService.http-conduit-factory=URLConnectionHTTPConduitFactory
                                quarkus.cxf.client.myService.hostname-verifier=AllowAllHostnameVerifier
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void noChangeWhenGlobalUrlConnectionConduit() {
        //language=properties
        rewriteRun(
                properties(
                        """
                                quarkus.cxf.http-conduit-factory=URLConnectionHTTPConduitFactory
                                quarkus.cxf.client.myService.hostname-verifier=AllowAllHostnameVerifier
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void noChangeWhenKeyStorePresent() {
        // mapping a key store automatically is not safe, the client is left for manual migration
        //language=properties
        rewriteRun(
                properties(
                        """
                                quarkus.cxf.client.myService.key-store=client-keystore.pkcs12
                                quarkus.cxf.client.myService.key-store-password=secret
                                quarkus.cxf.client.myService.hostname-verifier=AllowAllHostnameVerifier
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void noChangeWhenOtherProfilePinsUrlConnectionConduit() {
        // the default scoped verifier also applies in the prod profile, where the URLConnection conduit
        // keeps it working; migrating the default scope would make prod inherit tls-configuration-name
        // with hostname-verification-algorithm, which fails at runtime on that conduit
        //language=properties
        rewriteRun(
                properties(
                        """
                                quarkus.cxf.client.myService.hostname-verifier=AllowAllHostnameVerifier
                                %prod.quarkus.cxf.client.myService.http-conduit-factory=URLConnectionHTTPConduitFactory
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void noChangeWhenProfileGlobalUrlConnectionConduitOverlapsDefaultVerifier() {
        //language=properties
        rewriteRun(
                properties(
                        """
                                %test.quarkus.cxf.http-conduit-factory=URLConnectionHTTPConduitFactory
                                quarkus.cxf.client.myService.hostname-verifier=AllowAllHostnameVerifier
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void migrateWhenClientPinnedToVertxDespiteProfileGlobalUrlConnection() {
        // the per client Vert.x pin overrides the %test global URLConnection pin in every profile, so this
        // client is safe to migrate; a profile scoped global pin no longer suppresses the whole file
        //language=properties
        rewriteRun(
                properties(
                        """
                                %test.quarkus.cxf.http-conduit-factory=URLConnectionHTTPConduitFactory
                                quarkus.cxf.client.myService.http-conduit-factory=VertxHttpClientHTTPConduitFactory
                                quarkus.cxf.client.myService.hostname-verifier=AllowAllHostnameVerifier
                                """,
                        """
                                %test.quarkus.cxf.http-conduit-factory=URLConnectionHTTPConduitFactory
                                quarkus.cxf.client.myService.http-conduit-factory=VertxHttpClientHTTPConduitFactory
                                quarkus.tls.myService.hostname-verification-algorithm=NONE
                                quarkus.cxf.client.myService.tls-configuration-name=myService
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void migrateProfileVerifierWhenKeyStoreInDisjointProfile() {
        // the %prod key store does not apply in the dev profile, so the %dev verifier can be migrated
        //language=properties
        rewriteRun(
                properties(
                        """
                                %dev.quarkus.cxf.client.myService.hostname-verifier=AllowAllHostnameVerifier
                                %prod.quarkus.cxf.client.myService.key-store=client-keystore.pkcs12
                                """,
                        """
                                %dev.quarkus.tls.myService.hostname-verification-algorithm=NONE
                                %dev.quarkus.cxf.client.myService.tls-configuration-name=myService
                                %prod.quarkus.cxf.client.myService.key-store=client-keystore.pkcs12
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void noChangeWhenUnrecognizedTrustStoreType() {
        // only jks and pkcs12 map to the quarkus.tls.<name>.trust-store.<ext> sub-keys, a client
        // with any other trust-store-type is left for manual migration
        //language=properties
        rewriteRun(
                properties(
                        """
                                quarkus.cxf.client.myService.trust-store-type=jceks
                                quarkus.cxf.client.myService.trust-store=client-truststore.jceks
                                quarkus.cxf.client.myService.trust-store-password=secret
                                quarkus.cxf.client.myService.hostname-verifier=AllowAllHostnameVerifier
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void noChangeWhenTlsConfigurationNameAlreadyPresent() {
        // an existing tls-configuration-name is an already migrated or hand written TLS setup
        //language=properties
        rewriteRun(
                properties(
                        """
                                quarkus.cxf.client.myService.tls-configuration-name=myTls
                                quarkus.cxf.client.myService.hostname-verifier=AllowAllHostnameVerifier
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void noChangeWhenTrustStoreInDifferentOverlappingProfile() {
        // the %prod trust store cannot be moved together with the default scoped verifier; migrating the
        // verifier alone would combine the inherited tls-configuration-name with trust-store in prod,
        // which fails at runtime
        //language=properties
        rewriteRun(
                properties(
                        """
                                quarkus.cxf.client.myService.hostname-verifier=AllowAllHostnameVerifier
                                %prod.quarkus.cxf.client.myService.trust-store=prod-truststore.jks
                                """,
                        spec -> spec.path("src/main/resources/application.properties"))
        );
    }
}
