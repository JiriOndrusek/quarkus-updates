package io.quarkus.updates.camel30;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.properties.Assertions;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.TypeValidation;

import io.quarkus.updates.camel30.CamelQuarkusMigrationRecipe;

public class CamelAPIsPropertiesTest extends BaseCamelQuarkusTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new CamelQuarkusMigrationRecipe())
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
        ;
    }

    @Test
    void testRejectedPolicyDiscardOldeste() {
        rewriteRun(spec -> spec.expectedCyclesThatMakeChanges(2), withCamel(Assertions.properties("""
                   #test
                   camel.threadpool.rejectedPolicy=DiscardOldest
                """,
                """
                            #test
                            #'ThreadPoolRejectedPolicy.camel.threadpool.rejectedPolicy' has been removed, consider using 'Abort'. camel.threadpool.rejectedPolicy=DiscardOldest
                        """)));
    }

    @Test
    void testRejectedPolicyDiscard() {
        rewriteRun(spec -> spec.expectedCyclesThatMakeChanges(2), withCamel(Assertions.properties("""
                   #test
                   camel.threadpool.rejectedPolicy=Discard
                """,
                """
                            #test
                            #'ThreadPoolRejectedPolicy.camel.threadpool.rejectedPolicy' has been removed, consider using 'Abort'. camel.threadpool.rejectedPolicy=Discard
                        """)));
    }

}
