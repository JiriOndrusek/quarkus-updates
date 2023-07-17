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
                                                <artifactId>camel-quarkus-activemq</artifactId>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId>camel-quarkus-atmos</artifactId>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId>camel-quarkus-avro-rpc</artifactId>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId>camel-quarkus-caffeine-lrucache</artifactId>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId>camel-quarkus-datasonnet</artifactId>
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
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId>camel-quarkus-hbase</artifactId>
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
                                                <artifactId>camel-quarkus-optaplanner</artifactId>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId>camel-quarkus-rabbitmq</artifactId>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId>camel-quarkus-smallrye-reactive-messaging</artifactId>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId>camel-quarkus-solr</artifactId>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId>camel-quarkus-tika</artifactId>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId>camel-quarkus-vm</artifactId>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId>camel-quarkus-xmlsecurity</artifactId>
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
                                                <artifactId_removed><!--Extension camel-quarkus-activemq was removed, consider camel-quarkus-jms or camel-quarkus-sjms or camel-quarkus-amqp instead.-->camel-quarkus-activemq</artifactId_removed>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId_removed><!--Extension camel-quarkus-atmos was removed.-->camel-quarkus-atmos</artifactId_removed>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId_removed><!--Extension camel-quarkus-avro-rpc was removed.-->camel-quarkus-avro-rpc</artifactId_removed>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId_removed><!--Extension camel-quarkus-caffeine-lrucache was removed, consider camel-quarkus-ignite or camel-quarkus-infinispan instead.-->camel-quarkus-caffeine-lrucache</artifactId_removed>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId_removed><!--Extension camel-quarkus-datasonnet was removed, but should be reintroduced.-->camel-quarkus-datasonnet</artifactId_removed>
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
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId_removed><!--Extension camel-quarkus-hbase was removed.-->camel-quarkus-hbase</artifactId_removed>
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
                                                <artifactId_removed><!--Extension camel-quarkus-johnzon was removed, consider camel-quarkus-jackson or camel-quarkus-fastjson or camel-quarkus-gson instead.-->camel-quarkus-johnzon</artifactId_removed>
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
                                                <artifactId_removed><!--Extension camel-quarkus-optaplanner was removed.-->camel-quarkus-optaplanner</artifactId_removed>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId_removed><!--Extension camel-quarkus-rabbitmq was removed, consider camel-quarkus-spring-rabbitmq??? instead.-->camel-quarkus-rabbitmq</artifactId_removed>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId_removed><!--Extension camel-quarkus-smallrye-reactive-messaging was removed, but should be reintroduced.-->camel-quarkus-smallrye-reactive-messaging</artifactId_removed>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId_removed><!--Extension camel-quarkus-solr was removed.-->camel-quarkus-solr</artifactId_removed>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId_removed><!--Extension camel-quarkus-tika was removed, but should be reintroduced.-->camel-quarkus-tika</artifactId_removed>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId_removed><!--Extension camel-quarkus-vm was removed.-->camel-quarkus-vm</artifactId_removed>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.apache.camel.quarkus</groupId>
                                                <artifactId_removed><!--Extension camel-quarkus-xmlsecurity was removed, but should be reintroduced.-->camel-quarkus-xmlsecurity</artifactId_removed>
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