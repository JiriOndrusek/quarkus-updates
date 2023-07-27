package org.apache.camel.quarkus.update;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.properties.Assertions;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.maven.Assertions.pomXml;
import static org.openrewrite.test.RewriteTest.toRecipe;

public class CamelAPIsPropertiesTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new CamelJavaGroupedRecipe())
                .parser(JavaParser.fromJavaVersion()
                        .logCompilationWarningsAndErrors(true)
                        .classpath("camel-api","camel-support","camel-core-model", "camel-util", "camel-catalog", "camel-main"))
                .typeValidationOptions(TypeValidation.none());;
    }

    @Test
    void testPropertyUpdate() {
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(2).recipe(toRecipe(() -> new CamelAPIsPropertiesRecipe().getVisitor())),
                Assertions.properties(
                        """
                                   #test
                                   camel.threadpool.rejectedPolicy=DiscardOldest
                                """,
                        """
                                    #test
                                    #'ThreadPoolRejectedPolicy.camel.threadpool.rejectedPolicy' has been removed, consider using 'Abort'. camel.threadpool.rejectedPolicy=DiscardOldest   
                                """
                )
        );
    }

}
