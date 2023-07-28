package org.apache.camel.quarkus.update.v3_0;

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
