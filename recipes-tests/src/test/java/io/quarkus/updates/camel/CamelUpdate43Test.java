package io.quarkus.updates.camel;

import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.TypeValidation;

public class CamelUpdate43Test extends org.apache.camel.updates.camel44.CamelUpdate43Test {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelQuarkusTestUtil.recipe3_8(spec)
                .parser(parser())
                .typeValidationOptions(TypeValidation.none());
    }
}
