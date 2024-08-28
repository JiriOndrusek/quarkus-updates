package io.quarkus.updates.camel;

import org.openrewrite.Recipe;
import org.openrewrite.config.Environment;
import org.openrewrite.config.YamlResourceLoader;
import org.openrewrite.test.RecipeSpec;

import java.net.URI;
import java.util.Properties;

public class CamelQuarkusTestUtil {

    public static RecipeSpec recipe3alpha(RecipeSpec spec) {
       return recipe(spec, "3alpha");
    }

    public static RecipeSpec recipe3alpha(RecipeSpec spec, String... activeRecipes) {
       return recipe(spec, "3alpha", activeRecipes);
    }

    public static RecipeSpec recipe3_8(RecipeSpec spec, String... activeRecipes) {
        if(activeRecipes.length == 0) {
            return recipe(spec, "3.8");
        }

       return recipe(spec, "3.8", activeRecipes);
    }

    private static RecipeSpec recipe(RecipeSpec spec, String version) {
        String[] defaultRecipes = switch (version) {
            case "3.8" -> new String[] {"io.quarkus.updates.camel.camel44.CamelQuarkusMigrationRecipe"};
            case "3alpha" -> new String[] {"io.quarkus.updates.camel.camel40.CamelQuarkusMigrationRecipe"};
            default -> throw new IllegalArgumentException("Version '" + version + "' is not allowed!");
        };
        return recipe(spec, version, defaultRecipes);
    }

    public static RecipeSpec recipe(RecipeSpec spec, String version, String... activerecipes) {
//

        YamlResourceLoader yrl = new YamlResourceLoader(
                CamelQuarkusTestUtil.class.getResourceAsStream("/quarkus-updates/org.apache.camel.quarkus/camel-quarkus/" + version + ".yaml"), URI.create("rewrite.yml"), new Properties());
        Recipe r = Environment.builder()
                .scanYamlResources()
                .load(yrl)
                .build()
                .activateRecipes(activerecipes);

        spec.recipes(r);
        return spec;
    }

}
