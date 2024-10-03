package io.quarkus.updates.camel;

import org.junit.jupiter.api.Disabled;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.TypeValidation;

public class CamelUpdate45Test extends org.apache.camel.upgrade.CamelUpdate45Test {

    @Override
    public void defaults(RecipeSpec spec) {
        //let the parser be initialized in the camel parent
        super.defaults(spec);
        //recipe has to be loaded differently
        CamelQuarkusTestUtil.recipe3_15(spec)
                .typeValidationOptions(TypeValidation.none());
    }

    @Disabled
    @Override
    public void testSearch() {
        super.testSearch();
    }
}
