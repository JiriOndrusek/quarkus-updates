package io.quarkus.updates.camel;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.properties.Assertions;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

public class CamelUpdate42Test extends org.apache.camel.updates.camel44.CamelUpdate42Test {

    @Override
    public void defaults(RecipeSpec spec) {
        //initialize parser
        super.defaults(spec);
        //use Quarkus recipe
        spec.recipe(getClass().getResourceAsStream("/quarkus-updates/org.apache.camel.quarkus/camel-quarkus/3.8.yaml"),
                "io.quarkus.updates.camel.camel44.CamelQuarkusMigrationRecipe");
    }
}
