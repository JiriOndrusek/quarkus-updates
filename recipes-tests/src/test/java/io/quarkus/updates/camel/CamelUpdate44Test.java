package io.quarkus.updates.camel;

import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.TypeValidation;

public class CamelUpdate44Test extends org.apache.camel.updates.camel44.CamelUpdate44Test {
    @Override
    public void defaults(RecipeSpec spec) {
        CamelQuarkusTestUtil.recipe3_8(spec)
                .parser(parser())
                .typeValidationOptions(TypeValidation.none());
    }
}
