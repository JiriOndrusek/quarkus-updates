package io.quarkus.updates.camel;

import org.openrewrite.test.RecipeSpec;

public class CamelQuarkusTestUtil {



    public static RecipeSpec recipe(RecipeSpec spec) {
        return recipe(spec, "io.quarkus.updates.camel30.CamelQuarkusMigrationRecipe");
    }

    public static RecipeSpec recipe(RecipeSpec spec, String... activerecipes) {
        return spec.recipeFromResource("/quarkus-updates/org.apache.camel.quarkus/camel-quarkus/3alpha.yaml", activerecipes);
    }

    public static RecipeSpec recipeForVersion(RecipeSpec spec, String version, String... activerecipes) {
        return spec.recipeFromResource("/quarkus-updates/org.apache.camel.quarkus/camel-quarkus/" + version + ".yaml", activerecipes);
    }

}
