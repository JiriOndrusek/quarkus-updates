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
        CamelQuarkusTestUtil.recipeForVersion(spec, "3.8", "io.quarkus.updates.camel.camel43.CamelQuarkusMigrationRecipe",
                        "org.openrewrite.java.camel.migrate.RouteControllerProperties")
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true).classpath("camel-api", "camel-util", "camel-base", "camel-core-model"))
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

    @Test
    void testStopWatchConstructorPrimitive() {
        //language=java
        rewriteRun(java("""
                    import org.apache.camel.util.StopWatch;
                    
                    public class StopWatchTest {
                    
                        public void test() {
                            StopWatch sw = new StopWatch(1l);
                        }
                    }
                """,
            """
                    import org.apache.camel.util.StopWatch;
                    
                    public class StopWatchTest {
                    
                        public void test() {
                            StopWatch sw = /*Removed the deprecated constructor from the internal class org.apache.camel.util.StopWatch.
                    Users of this class are advised to use the default constructor if necessary.Changed exception thrown from IOException to Exception.
                    */new StopWatch();
                        }
                    }
                    """));
    }

    @Test
    void testStopWatchConstructor() {
        //language=java
        rewriteRun(java("""
                    import org.apache.camel.util.StopWatch;
                    
                    public class StopWatchTest {
                    
                        public void test() {
                           StopWatch sw = new StopWatch(Long.parseLong("1"));
                        }
                    }
                """,
            """
                    import org.apache.camel.util.StopWatch;
                    
                    public class StopWatchTest {
                    
                        public void test() {
                           StopWatch sw = /*Removed the deprecated constructor from the internal class org.apache.camel.util.StopWatch.
                    Users of this class are advised to use the default constructor if necessary.Changed exception thrown from IOException to Exception.
                    */new StopWatch();
                        }
                    }
                    """));
    }

    @Test
    void testExchangeGetCreated() {
        //language=java
        rewriteRun(java("""
                    import org.apache.camel.Exchange;
                    
                    public class ExchangeTest {
                    
                        public void test() {
                            Exchange ex = null;
                            ex.getCreated();
                        }
                    }
                """,
            """
                    import org.apache.camel.Exchange;
                    
                    public class ExchangeTest {
                    
                        public void test() {
                            Exchange ex = null;
                            ex.getClock().getCreated();
                        }
                    }
                    """));
    }

    @Test
    void testPropertiesLookup() {
        //language=java
        rewriteRun(java("""
                    import org.apache.camel.component.properties.PropertiesLookup;
                    
                    public class PropertiesLookupTest  {
                    
                        public void test() throws Exception {
                            PropertiesLookup pl = null;
                           
                            pl.lookup("test");
                        }
                    }
                """,
            """
                    import org.apache.camel.component.properties.PropertiesLookup;
                    
                    public class PropertiesLookupTest  {
                    
                        public void test() throws Exception {
                            PropertiesLookup pl = null;
                           
                            pl.lookup("test", null);
                        }
                    }
                    """));
        }


    @Test
    void testJsonpath1() {
        //language=java
        rewriteRun(java("""
                    import org.apache.camel.builder.RouteBuilder;
                    
                    public class Jsonpath1Test extends RouteBuilder {
                        @Override
                        public void configure()  {
                            from("direct:in").choice().when().jsonpath("something", true, Object.class, "header")
                                    .to("mock:premium");
                        }
                    }
                """,
            """
                    import org.apache.camel.builder.RouteBuilder;
                    
                    public class Jsonpath1Test extends RouteBuilder {
                        @Override
                        public void configure()  {
                            /*Some of the Java DSL for tokenize, xmlTokenize, xpath, xquery and jsonpath has been removed as part of making the DSL model consistent.
                    See https://camel.apache.org/manual/camel-4x-upgrade-guide-4_4.html#_camel_core for more details.*/from("direct:in").choice().when().jsonpath("something", true, Object.class, "header")
                                    .to("mock:premium");
                        }
                    }
                    """));
    }

    @Test
    void testJsonpath2() {
        //language=java
        rewriteRun(java("""
                    import org.apache.camel.builder.RouteBuilder;
                    
                    public class Jsonpath2Test extends RouteBuilder {
                        @Override
                        public void configure()  {
                            from("direct:in").choice().when().jsonpathWriteAsString("something", true, "header")
                                    .to("mock:premium");
                        }
                    }
                """,
            """
                    import org.apache.camel.builder.RouteBuilder;
                    
                    public class Jsonpath2Test extends RouteBuilder {
                        @Override
                        public void configure()  {
                            /*Some of the Java DSL for tokenize, xmlTokenize, xpath, xquery and jsonpath has been removed as part of making the DSL model consistent.
                    See https://camel.apache.org/manual/camel-4x-upgrade-guide-4_4.html#_camel_core for more details.*/from("direct:in").choice().when().jsonpathWriteAsString("something", true, "header")
                                    .to("mock:premium");
                        }
                    }
                    """));
    }

    /**
     * Removed tokenize(String token, boolean regex, int group, String groupDelimiter, boolean skipFirst)
     */
    @Test
    void testTokenize1() {
        //language=java
        rewriteRun(java("""
                    import org.apache.camel.builder.RouteBuilder;
                    
                    public class Tokenize1Test extends RouteBuilder {
                        @Override
                        public void configure()  {
                            from("direct:in").choice().when().tokenize("token", true, 0, "groupDelimiter", true)
                                    .to("mock:premium");
                        }
                    }
                """,
            """
                    import org.apache.camel.builder.RouteBuilder;
                    
                    public class Tokenize1Test extends RouteBuilder {
                        @Override
                        public void configure()  {
                            /*Some of the Java DSL for tokenize, xmlTokenize, xpath, xquery and jsonpath has been removed as part of making the DSL model consistent.
                    See https://camel.apache.org/manual/camel-4x-upgrade-guide-4_4.html#_camel_core for more details.*/from("direct:in").choice().when().tokenize("token", true, 0, "groupDelimiter", true)
                                    .to("mock:premium");
                        }
                    }
                    """));
    }

    /**
     * Removed tokenize(String token, String headerName)
     */
    @Test
    void testTokenize2() {
        //language=java
        rewriteRun(java("""
                    import org.apache.camel.builder.RouteBuilder;
                    
                    public class Tokenize2Test extends RouteBuilder {
                        @Override
                        public void configure()  {
                            from("direct:in").choice().when().tokenize("token", "header")
                                    .to("mock:premium");
                        }
                    }
                """,
            """
                    import org.apache.camel.builder.RouteBuilder;
                    
                    public class Tokenize2Test extends RouteBuilder {
                        @Override
                        public void configure()  {
                            /*Some of the Java DSL for tokenize, xmlTokenize, xpath, xquery and jsonpath has been removed as part of making the DSL model consistent.
                    See https://camel.apache.org/manual/camel-4x-upgrade-guide-4_4.html#_camel_core for more details.*/from("direct:in").choice().when().tokenize("token", "header")
                                    .to("mock:premium");
                        }
                    }
                    """));
    }

    /**
     * Removed tokenize(String token, String headerName, boolean regex)
     */
    @Test
    void testTokenize3() {
        //language=java
        rewriteRun(java("""
                    import org.apache.camel.builder.RouteBuilder;
                    
                    public class Tokenize3Test extends RouteBuilder {
                        @Override
                        public void configure()  {
                            from("direct:in").choice().when().tokenize("token", "header", true)
                                    .to("mock:premium");
                        }
                    }
                """,
            """
                    import org.apache.camel.builder.RouteBuilder;
                    
                    public class Tokenize3Test extends RouteBuilder {
                        @Override
                        public void configure()  {
                            /*Some of the Java DSL for tokenize, xmlTokenize, xpath, xquery and jsonpath has been removed as part of making the DSL model consistent.
                    See https://camel.apache.org/manual/camel-4x-upgrade-guide-4_4.html#_camel_core for more details.*/from("direct:in").choice().when().tokenize("token", "header", true)
                                    .to("mock:premium");
                        }
                    }
                    """));
    }

    /**
     * Removed xpath(String text, String headerName)
     */
    @Test
    void testXpath1() {
        //language=java
        rewriteRun(java("""
                    import org.apache.camel.builder.RouteBuilder;
                    
                    public class Xpath1Test extends RouteBuilder {
                        @Override
                        public void configure()  {
                            from("direct:in").choice().when().xpath("text", "header")
                                    .to("mock:premium");
                        }
                    }
                """,
            """
                    import org.apache.camel.builder.RouteBuilder;
                    
                    public class Xpath1Test extends RouteBuilder {
                        @Override
                        public void configure()  {
                            /*Some of the Java DSL for tokenize, xmlTokenize, xpath, xquery and jsonpath has been removed as part of making the DSL model consistent.
                    See https://camel.apache.org/manual/camel-4x-upgrade-guide-4_4.html#_camel_core for more details.*/from("direct:in").choice().when().xpath("text", "header")
                                    .to("mock:premium");
                        }
                    }
                    """));
    }

    /**
     * Removed xpath(String text, Class<?> resultType, String headerName)
     */
    @Test
    void testXpath2() {
        //language=java
        rewriteRun(java("""
                    import org.apache.camel.builder.RouteBuilder;
                    
                    public class Xpath2Test extends RouteBuilder {
                        @Override
                        public void configure()  {
                            from("direct:in").choice().when().xpath("text", Object.class, "header")
                                    .to("mock:premium");
                        }
                    }
                """,
            """
                    import org.apache.camel.builder.RouteBuilder;
                    
                    public class Xpath2Test extends RouteBuilder {
                        @Override
                        public void configure()  {
                            /*Some of the Java DSL for tokenize, xmlTokenize, xpath, xquery and jsonpath has been removed as part of making the DSL model consistent.
                    See https://camel.apache.org/manual/camel-4x-upgrade-guide-4_4.html#_camel_core for more details.*/from("direct:in").choice().when().xpath("text", Object.class, "header")
                                    .to("mock:premium");
                        }
                    }
                    """));
    }

    /**
     * Removed xpath(String text, Class<?> resultType, Namespaces namespaces, String headerName) {
     */
    @Test
    void testXpath3() {
        //language=java
        rewriteRun(java("""
                    import org.apache.camel.builder.RouteBuilder;
                    
                    public class Xpath3Test extends RouteBuilder {
                        @Override
                        public void configure()  {
                             from("direct:in").choice().when().xpath("text", Object.class, "namespace", "header")
                                    .to("mock:premium");
                        }
                    }
                """,
            """
                    import org.apache.camel.builder.RouteBuilder;
                    
                    public class Xpath2Test extends RouteBuilder {
                        @Override
                        public void configure()  {
                            /*Some of the Java DSL for tokenize, xmlTokenize, xpath, xquery and jsonpath has been removed as part of making the DSL model consistent.
                    See https://camel.apache.org/manual/camel-4x-upgrade-guide-4_4.html#_camel_core for more details.*/from("direct:in").choice().when().xpath("text", Object.class, "namespace", "header")
                                    .to("mock:premium");
                        }
                    }
                    """));
    }

    /**
     * Removed xquery(String text, String headerName)
     */
    @Test
    void testXquery1() {
        //language=java
        rewriteRun(java("""
                    import org.apache.camel.builder.RouteBuilder;
                    
                    public class Xquery1Test extends RouteBuilder {
                        @Override
                        public void configure()  {
                            from("direct:in").choice().when().xquery("text", "header")
                                    .to("mock:premium");
                        }
                    }
                """,
            """
                    import org.apache.camel.builder.RouteBuilder;
                    
                    public class Xquery1Test extends RouteBuilder {
                        @Override
                        public void configure()  {
                            /*Some of the Java DSL for tokenize, xmlTokenize, xpath, xquery and jsonpath has been removed as part of making the DSL model consistent.
                    See https://camel.apache.org/manual/camel-4x-upgrade-guide-4_4.html#_camel_core for more details.*/from("direct:in").choice().when().xquery("text", "header")
                                    .to("mock:premium");
                        }
                    }
                    """));
    }
    /**
     * Removed xquery(String text, Class<?> resultType, String headerName)
     */
    @Test
    void testXquery2() {
        //language=java
        rewriteRun(java("""
                    import org.apache.camel.builder.RouteBuilder;
                    
                    public class Tokenize1Test extends RouteBuilder {
                        @Override
                        public void configure()  {
                            from("direct:in").choice().when().xquery("text", Object.class, "header")
                                    .to("mock:premium");
                        }
                    }
                """,
            """
                    import org.apache.camel.builder.RouteBuilder;
                    
                    public class Jsonpath2Test extends RouteBuilder {
                        @Override
                        public void configure()  {
                            /*Some of the Java DSL for tokenize, xmlTokenize, xpath, xquery and jsonpath has been removed as part of making the DSL model consistent.
                    See https://camel.apache.org/manual/camel-4x-upgrade-guide-4_4.html#_camel_core for more details.*/from("direct:in").choice().when().jsonpathWriteAsString("something", true, "header")
                                    .to("mock:premium");
                        }
                    }
                    """));
    }
    /**
     * Removed xquery(String text, Class<?> resultType, Namespaces namespaces, String headerName) {
     */
    @Test
    void testXquery3() {
        //language=java
        rewriteRun(java("""
                    import org.apache.camel.builder.RouteBuilder;
                    
                    public class Tokenize1Test extends RouteBuilder {
                        @Override
                        public void configure()  {
                             from("direct:in").choice().when().xquery("text", Object.class, "namespace", "header")
                                    .to("mock:premium");
                        }
                    }
                """,
            """
                    import org.apache.camel.builder.RouteBuilder;
                    
                    public class Jsonpath2Test extends RouteBuilder {
                        @Override
                        public void configure()  {
                            /*Some of the Java DSL for tokenize, xmlTokenize, xpath, xquery and jsonpath has been removed as part of making the DSL model consistent.
                    See https://camel.apache.org/manual/camel-4x-upgrade-guide-4_4.html#_camel_core for more details.*/from("direct:in").choice().when().jsonpathWriteAsString("something", true, "header")
                                    .to("mock:premium");
                        }
                    }
                    """));
    }

}
