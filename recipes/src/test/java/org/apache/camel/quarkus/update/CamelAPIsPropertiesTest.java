package org.apache.camel.quarkus.update;

import org.apache.camel.quarkus.update.properties.CamelQuarkusAPIsPropertiesRecipe;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.properties.Assertions;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.maven.Assertions.pomXml;
import static org.openrewrite.test.RewriteTest.toRecipe;

public class CamelAPIsPropertiesTest extends BaseCamelQuarkusTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new CamelQuarkusMigrationRecipe())
                .parser(JavaParser.fromJavaVersion()
                        .logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());;
    }

    @Test
    void testRejectedPolicyDiscardOldeste() {
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(2),
                withCamel(Assertions.properties(
                        """
                                   #test
                                   camel.threadpool.rejectedPolicy=DiscardOldest
                                """,
                        """
                                    #test
                                    #'ThreadPoolRejectedPolicy.camel.threadpool.rejectedPolicy' has been removed, consider using 'Abort'. camel.threadpool.rejectedPolicy=DiscardOldest   
                                """
                ))
        );
    }

    @Test
    void testNonCamelProject() {
        rewriteRun(
                Assertions.properties(
                        """
                                   #test
                                   camel.threadpool.rejectedPolicy=DiscardOldest
                                """
                )
        );
    }

    @Test
    void testRejectedPolicyDiscard() {
        rewriteRun(
                spec -> spec.expectedCyclesThatMakeChanges(2),
                withCamel(Assertions.properties(
                        """
                                   #test
                                   camel.threadpool.rejectedPolicy=Discard
                                """,
                        """
                                    #test
                                    #'ThreadPoolRejectedPolicy.camel.threadpool.rejectedPolicy' has been removed, consider using 'Abort'. camel.threadpool.rejectedPolicy=Discard  
                                """
                ))
        );
    }

}
