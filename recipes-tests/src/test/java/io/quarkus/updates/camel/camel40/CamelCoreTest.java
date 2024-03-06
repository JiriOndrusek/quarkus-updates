package io.quarkus.updates.camel.camel40;

import io.quarkus.updates.camel.CamelQuarkusTestUtil;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.properties.Assertions;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

public class CamelCoreTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelQuarkusTestUtil.recipeForVersion(spec, "3.8", "io.quarkus.updates.camel.camel43.CamelQuarkusMigrationRecipe", "org.openrewrite.java.camel.migrate.RrouteControllerProperties")
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true).classpath("camel-api"))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testCamelMainRouteConrollerProperty() {
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
    @Test
    void testDataFormatUnmarshall() {
        //language=java
        rewriteRun(java("""
                    import org.apache.camel.spi.DataFormat;
                    
                    public class Test {
                    
                        public void test() throws Exception {
                            DataFormat df = null;
                            df.unmarshal(null, null);
                        }
                    
                    }
                """,
            """
                    import org.apache.camel.spi.DataFormat;
                    
                    public class Test {
                    
                        public void test() throws Exception {
                            DataFormat df = null;
                            /*Changed exception thrown from IOException to Exception.*/df.unmarshal(null, null);
                        }
                    
                    }
                    """));
    }

}
