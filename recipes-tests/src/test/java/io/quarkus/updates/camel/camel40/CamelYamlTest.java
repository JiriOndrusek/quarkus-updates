package io.quarkus.updates.camel.camel40;

import io.quarkus.updates.camel.CamelQuarkusTestUtil;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.TypeValidation;

public class CamelYamlTest extends org.apache.camel.updates.camel40.CamelYamlTest {
    @Override
    public void defaults(RecipeSpec spec) {
        CamelQuarkusTestUtil.recipe3alpha(spec)
                .parser(parser())
                .typeValidationOptions(TypeValidation.none());
    }
}
