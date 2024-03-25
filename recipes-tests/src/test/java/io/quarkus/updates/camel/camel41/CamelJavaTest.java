package io.quarkus.updates.camel.camel41;

import io.quarkus.updates.camel.CamelQuarkusTestUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.properties.Assertions;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

public class CamelJavaTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelQuarkusTestUtil.recipe3_8(spec)
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true).classpath("camel-core-model", "camel-tracing"))
                .typeValidationOptions(TypeValidation.none());
    }

    @Test
    void testAws2SnsQueueUrl() {
        //language=java
        rewriteRun(java("""
                    import org.apache.camel.builder.RouteBuilder;
                    
                    public class Jsonpath2Test extends RouteBuilder {
                        @Override
                        public void configure()  {
                            from("direct:start")
                              .to("aws2-sns://mytopic?subject=mySubject&autoCreateTopic=true&subscribeSNStoSQS=true&queueUrl=https://xxxxx");
                        }
                    }
                """,
                """
                        import org.apache.camel.builder.RouteBuilder;
                        
                        public class Jsonpath2Test extends RouteBuilder {
                            @Override
                            public void configure()  {
                                from("direct:start")
                                  .to("aws2-sns://mytopic?subject=mySubject&autoCreateTopic=true&subscribeSNStoSQS=true&queueArn=arn:aws:sqs:xxxxx");
                            }
                        }
                        """));
    }


    @Test
    void testTracingTag() {
        //language=java
        rewriteRun(java("""
                    import org.apache.camel.tracing.Tag;
                    
                    public class Test {
                      
                        public Tag test() {
                            return Tag.URL_SCHEME;
                        }
                    }
                """,
                """
                    import org.apache.camel.tracing.TagConstants;
                    
                    public class Test {
                      
                        public TagConstants test() {
                            return TagConstants.URL_SCHEME;
                        }
                    }
                        """));
    }



}
