package io.quarkus.updates.camel;

import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.TypeValidation;

public class CamelUpdate47Test extends org.apache.camel.updates.CamelUpdate47Test {
    @Override
    public void defaults(RecipeSpec spec) {
        CamelQuarkusTestUtil.recipe3_15(spec)
                .parser(parser())
                .typeValidationOptions(TypeValidation.none());
    }
}
