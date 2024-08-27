package io.quarkus.updates.camel;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.TypeValidation;

public class CamelUpdate46Test extends org.apache.camel.updates.CamelUpdate46Test {

    public CamelUpdate46Test() {
        setSkipUnknownDependencies(true);
    }

    @Override
    public void defaults(RecipeSpec spec) {
        CamelQuarkusTestUtil.recipe3_15(spec)
                .parser(parser())
                .typeValidationOptions(TypeValidation.none());
    }

    //following tests have to be disabled, the migration they cover is happening only between Camel 4.5-4.6
    // module which introduced the code before migration does not exist in Camel 4.4 (which is used by camel-quarkus 3.8)
    @Test
    @Disabled
    @Override
    public void testLangchainEmbeddings() {
    }

    @Test
    @Disabled
    @Override
    public void testLangchainChat() {

    }

    @Test
    @Override
    public void testSearch() {
        super.testSearch();
    }
}
