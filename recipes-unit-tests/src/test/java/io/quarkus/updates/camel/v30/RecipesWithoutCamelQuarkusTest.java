package io.quarkus.updates.camel.v30;

import io.quarkus.updates.camel.v3_0.CamelQuarkusMigrationRecipe;
import org.junit.jupiter.api.Test;
import org.openrewrite.test.SourceSpecs;
import org.openrewrite.yaml.Assertions;

import java.util.function.BiFunction;
import java.util.function.Function;

// Tests covering that CamelQuarkusRecipe is not applied to the migrated project, if there is no camelQuarkus dependency.
public class RecipesWithoutCamelQuarkusTest extends BaseCamelQuarkusTest {

    @Test
    void testYaml() {
        testCamelVsWithoutCamel(1,
                Assertions::yaml,
                Assertions::yaml,
                "- route-configuration:\n" +
                        "    - id: \"__id\"",
                "- route-configuration:\n" +
                        "    id: \"__id\""
        );
    }

    @Test
    void testProperties(){
        testCamelVsWithoutCamel(2,
                org.openrewrite.properties.Assertions::properties,
                org.openrewrite.properties.Assertions::properties,
                "#test\n" +
                        "camel.threadpool.rejectedPolicy=Discard",
                "#test\n" +
                        "#'ThreadPoolRejectedPolicy.camel.threadpool.rejectedPolicy' has been removed, consider using 'Abort'. camel.threadpool.rejectedPolicy=Discard"
        );
    }

    @Test
    void testJava() {
        testCamelVsWithoutCamel(2,
                org.openrewrite.java.Assertions::java,
                org.openrewrite.java.Assertions::java,
                "import org.apache.camel.builder.SimpleBuilder;",
                "/*'java.beans.SimpleBeanInfo' has been removed, (class was used internally).*/import org.apache.camel.builder.SimpleBuilder;"
        );
    }

    @Test
    void testJava11WithoutCamelQuarkus() {
        rewriteRun(
                spec -> spec.recipe(new CamelQuarkusMigrationRecipe()),
                org.openrewrite.maven.Assertions.pomXml(
                "                        <project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                        "                            <modelVersion>4.0.0</modelVersion>\n" +
                        "                            <groupId>org.apache.camel.quarkus</groupId>\n" +
                        "                            <version>2.13.3</version>\n" +
                        "                            <artifactId>camel-quarkus-migration-test-microprofile</artifactId>\n" +
                        "\n" +
                        "                            <properties>\n" +
                        "                                <maven.compiler.source>11</maven.compiler.source>\n" +
                        "                                <maven.compiler.target>11</maven.compiler.target>\n" +
                        "                            </properties>" +
                        "                        </project>")
        );
    }

    @Test
    void testJava17Update() {
        rewriteRun(
                spec -> spec.recipe(new CamelQuarkusMigrationRecipe()),
                org.openrewrite.maven.Assertions.pomXml(
                "                        <project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                        "                            <modelVersion>4.0.0</modelVersion>\n" +
                        "                            <groupId>org.apache.camel.quarkus</groupId>\n" +
                        "                            <version>2.13.3</version>\n" +
                        "                            <artifactId>camel-quarkus-migration-test-microprofile</artifactId>\n" +
                        "                            <properties>\n" +
                        "                                <maven.compiler.source>11</maven.compiler.source>\n" +
                        "                                <maven.compiler.target>11</maven.compiler.target>\n" +
                        "                            </properties>" +
                        "                           <dependencyManagement>\n" +
                        "                               <dependencies>\n" +
                        "                                   <dependency>\n" +
                        "                                       <groupId>io.quarkus.platform</groupId>\n" +
                        "                                       <artifactId>quarkus-camel-bom</artifactId>\n" +
                        "                                       <version>2.13.7.Final</version>\n" +
                        "                                       <type>pom</type>\n" +
                        "                                       <scope>import</scope>\n" +
                        "                                   </dependency>\n" +
                        "                               </dependencies>\n" +
                        "                           </dependencyManagement>" +
                        "                            <dependencies>\n" +
                        "                                <dependency>\n" +
                        "                                    <groupId>org.apache.camel.quarkus</groupId>\n" +
                        "                                    <artifactId>camel-quarkus-bean</artifactId>\n" +
                        "                                </dependency>\n" +
                        "                            </dependencies>" +
                        "                        </project>"
                        ,
                        "                        <project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                        "                            <modelVersion>4.0.0</modelVersion>\n" +
                        "                            <groupId>org.apache.camel.quarkus</groupId>\n" +
                        "                            <version>2.13.3</version>\n" +
                        "                            <artifactId>camel-quarkus-migration-test-microprofile</artifactId>\n" +
                        "                            <properties>\n" +
                        "                                <maven.compiler.source>17</maven.compiler.source>\n" +
                        "                                <maven.compiler.target>17</maven.compiler.target>\n" +
                        "                            </properties>" +
                        "                           <dependencyManagement>\n" +
                        "                               <dependencies>\n" +
                        "                                   <dependency>\n" +
                        "                                       <groupId>io.quarkus.platform</groupId>\n" +
                        "                                       <artifactId>quarkus-camel-bom</artifactId>\n" +
                        "                                       <version>2.13.7.Final</version>\n" +
                        "                                       <type>pom</type>\n" +
                        "                                       <scope>import</scope>\n" +
                        "                                   </dependency>\n" +
                        "                               </dependencies>\n" +
                        "                           </dependencyManagement>" +
                        "                            <dependencies>\n" +
                        "                                <dependency>\n" +
                        "                                    <groupId>org.apache.camel.quarkus</groupId>\n" +
                        "                                    <artifactId>camel-quarkus-bean</artifactId>\n" +
                        "                                </dependency>\n" +
                        "                            </dependencies>" +
                        "                        </project>")
        );
    }


    //-------------------------------------- internal test method ------------------------------

    private void testCamelVsWithoutCamel(int expectedCyclesThatMakeChanges, Function<String, SourceSpecs> first, BiFunction<String, String, SourceSpecs> second, String... sources) {
        //if camel is not present, content should stay the same
        rewriteRun(
                spec -> spec.recipe(new CamelQuarkusMigrationRecipe()),
                first.apply(sources[0])
        );
        //if camel is present, content should be changed (if the after == before, rewrite test will fail)
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(expectedCyclesThatMakeChanges).recipe(new CamelQuarkusMigrationRecipe()),
                withCamel(second.apply(sources[0], sources[1]))
        );
    }


}
