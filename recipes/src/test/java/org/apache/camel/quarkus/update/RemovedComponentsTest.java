package org.apache.camel.quarkus.update;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.test.RewriteTest.toRecipe;
import static org.openrewrite.maven.Assertions.pomXml;

public class RemovedComponentsTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new RemovedComponentsRecipe())
                .parser(JavaParser.fromJavaVersion()
                        .logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
        ;
    }

    @Test
    void testRemovedExchangePatternInOptionalOut() {
        rewriteRun(
                spec -> spec.recipe(toRecipe(() -> new RemovedComponentsRecipe().getVisitor())),
                pomXml(
                        """
                                    <project>
                                        <modelVersion>4.0.0</modelVersion>
                                    
                                        <artifactId>test</artifactId>
                                        <groupId>org.apache.camel.quarkus.test</groupId>
                                        <version>1.0.0</version>
                                    
                                    
                                        <properties>
                                            <quarkus.platform.version>2.13.3.Final</quarkus.platform.version>
                                        </properties>
                                    
                                        <dependencyManagement>
                                            <dependencies>
                                                <dependency>
                                                    <groupId>io.quarkus.platform</groupId>
                                                    <artifactId>quarkus-camel-bom</artifactId>
                                                    <version>2.13.7.Final</version>
                                                    <type>pom</type>
                                                    <scope>import</scope>
                                                </dependency>
                                            </dependencies>
                                        </dependencyManagement>
                                    
                                        <dependencies>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId>camel-quarkus-atmos</artifactId>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId>camel-quarkus-bean</artifactId>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId>camel-quarkus-dozer</artifactId>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId>camel-quarkus-elasticsearch-rest</artifactId>
                                            </dependency>
                                <!--<dependency>  org.openrewrite.maven.MavenDownloadingException: org.iota:java-md-doclet:2.2 failed. Unable to download POM. Tried repositories:
                                       <groupId>org.apache.camel.quarkus</groupId>
                                       <artifactId>camel-quarkus-iota</artifactId>
                                   </dependency>-->
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId>camel-quarkus-jbpm</artifactId>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId>camel-quarkus-jclouds</artifactId>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId>camel-quarkus-johnzon</artifactId>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId>camel-quarkus-microprofile-metrics</artifactId>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId>camel-quarkus-milo</artifactId>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId>camel-quarkus-opentracing</artifactId>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId>camel-quarkus-rabbitmq</artifactId>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId>camel-quarkus-solr</artifactId>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId>camel-quarkus-vm</artifactId>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId>camel-quarkus-xstream</artifactId>
                                            </dependency>
                                        </dependencies>
                                    
                                    </project>
                                """
                        ,
                        """
                                <project>
                                    <modelVersion>4.0.0</modelVersion>
                            
                                    <artifactId>test</artifactId>
                                    <groupId>org.apache.camel.quarkus.test</groupId>
                                    <version>1.0.0</version>
                            
                            
                                    <properties>
                                        <quarkus.platform.version>2.13.3.Final</quarkus.platform.version>
                                    </properties>
                            
                                    <dependencyManagement>
                                        <dependencies>
                                            <dependency>
                                                <groupId>io.quarkus.platform</groupId>
                                                <artifactId>quarkus-camel-bom</artifactId>
                                                <version>2.13.7.Final</version>
                                                <type>pom</type>
                                                <scope>import</scope>
                                            </dependency>
                                        </dependencies>
                                    </dependencyManagement>
                            
                                    <dependencies>
                                        <dependency>
                                            <groupId>org.apache.camel.quarkus</groupId>
                                            <artifactId_removed><!--Extension camel-quarkus-atmos was removed.-->camel-quarkus-atmos</artifactId_removed>
                                        </dependency>
                                        <dependency>
                                            <groupId>org.apache.camel.quarkus</groupId>
                                            <artifactId>camel-quarkus-bean</artifactId>
                                        </dependency>
                                        <dependency>
                                            <groupId>org.apache.camel.quarkus</groupId>
                                            <artifactId_removed><!--Extension camel-quarkus-dozer was removed.-->camel-quarkus-dozer</artifactId_removed>
                                        </dependency>
                                        <dependency>
                                            <groupId>org.apache.camel.quarkus</groupId>
                                            <artifactId_removed><!--Extension camel-quarkus-elasticsearch-rest was removed.-->camel-quarkus-elasticsearch-rest</artifactId_removed>
                                        </dependency>
                            <!--<dependency>  org.openrewrite.maven.MavenDownloadingException: org.iota:java-md-doclet:2.2 failed. Unable to download POM. Tried repositories:
                                   <groupId>org.apache.camel.quarkus</groupId>
                                   <artifactId>camel-quarkus-iota</artifactId>
                               </dependency>-->
                                        <dependency>
                                            <groupId>org.apache.camel.quarkus</groupId>
                                            <artifactId_removed><!--Extension camel-quarkus-jbpm was removed.-->camel-quarkus-jbpm</artifactId_removed>
                                        </dependency>
                                        <dependency>
                                            <groupId>org.apache.camel.quarkus</groupId>
                                            <artifactId_removed><!--Extension camel-quarkus-jclouds was removed.-->camel-quarkus-jclouds</artifactId_removed>
                                        </dependency>
                                        <dependency>
                                            <groupId>org.apache.camel.quarkus</groupId>
                                            <artifactId_removed><!--Extension camel-quarkus-johnzon was removed, consider camel-quarkus-jsonb instead.-->camel-quarkus-johnzon</artifactId_removed>
                                        </dependency>
                                        <dependency>
                                            <groupId>org.apache.camel.quarkus</groupId>
                                            <artifactId_removed><!--Extension camel-quarkus-microprofile-metrics was removed, consider camel-quarkus-opentelemetry or camel-quarkus-micrometer instead.-->camel-quarkus-microprofile-metrics</artifactId_removed>
                                        </dependency>
                                        <dependency>
                                            <groupId>org.apache.camel.quarkus</groupId>
                                            <artifactId_removed><!--Extension camel-quarkus-milo was removed.-->camel-quarkus-milo</artifactId_removed>
                                        </dependency>
                                        <dependency>
                                            <groupId>org.apache.camel.quarkus</groupId>
                                            <artifactId_removed><!--Extension camel-quarkus-opentracing was removed, consider camel-quarkus-opentelemetry or camel-quarkus-micrometer instead.-->camel-quarkus-opentracing</artifactId_removed>
                                        </dependency>
                                        <dependency>
                                            <groupId>org.apache.camel.quarkus</groupId>
                                            <artifactId_removed><!--Extension camel-quarkus-rabbitmq was removed, consider camel-quarkus-spring-rabbitmq??? instead.-->camel-quarkus-rabbitmq</artifactId_removed>
                                        </dependency>
                                        <dependency>
                                            <groupId>org.apache.camel.quarkus</groupId>
                                            <artifactId_removed><!--Extension camel-quarkus-solr was removed.-->camel-quarkus-solr</artifactId_removed>
                                        </dependency>
                                        <dependency>
                                            <groupId>org.apache.camel.quarkus</groupId>
                                            <artifactId_removed><!--Extension camel-quarkus-vm was removed.-->camel-quarkus-vm</artifactId_removed>
                                        </dependency>
                                        <dependency>
                                            <groupId>org.apache.camel.quarkus</groupId>
                                            <artifactId_removed><!--Extension camel-quarkus-xstream was removed, consider camel-quarkus-jacksonxml instead.-->camel-quarkus-xstream</artifactId_removed>
                                        </dependency>
                                    </dependencies>
                            
                                </project>
                                """
                )
        );
    }
}