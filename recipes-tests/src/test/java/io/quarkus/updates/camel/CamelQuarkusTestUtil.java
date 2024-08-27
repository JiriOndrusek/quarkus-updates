package io.quarkus.updates.camel;

import io.quarkus.updates.core.CoreTestUtil;
import org.openrewrite.Recipe;
import org.openrewrite.config.YamlResourceLoader;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
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

    public static RecipeSpec recipe3_15(RecipeSpec spec, String... activeRecipes) {
        if(activeRecipes.length == 0) {
            return recipe(spec, "3.15");
        }

       return recipe(spec, "3.15", activeRecipes);
    }

//    private static RecipeSpec recipe(RecipeSpec spec, String version) {
//        Path recipePath = switch (version) {
//            case "3.8" -> Path.of("quarkus-updates", "org.apache.camel.quarkus", "camel-quarkus", version + ".yaml");
//            default -> throw new IllegalArgumentException("Version '" + version + "' is not allowed!");
//        };
//        return CoreTestUtil.recipe(spec, recipePath);
//    }

    private static RecipeSpec recipe(RecipeSpec spec, String version) {
        String[] defaultRecipes = switch (version) {
            case "3.8" -> new String[] {"io.quarkus.updates.camel.camel44.CamelQuarkusMigrationRecipe"};
            case "3alpha" -> new String[] {"io.quarkus.updates.camel.camel40.CamelQuarkusMigrationRecipe"};
            default -> throw new IllegalArgumentException("Version '" + version + "' is not allowed!");
        };
        return recipe(spec, version, defaultRecipes);
    }


    public static RecipeSpec recipe(RecipeSpec spec, String version, String... activerecipes) {
//        Path recipeFile = Path.of("quarkus-updates/org.apache.camel.quarkus/camel-quarkus/3.8.yaml");
//        Collection<Recipe> recipes;
//        try (InputStream yamlRecipeInputStream = CoreTestUtil.class.getClassLoader().getResourceAsStream(recipeFile.toString())) {
//            YamlResourceLoader yamlResourceLoader = new YamlResourceLoader(yamlRecipeInputStream, recipeFile.toUri(), new Properties());
//            recipes = yamlResourceLoader.listRecipes();
//
////            return spec.recipeFromResource(Path.of("/").resolve(recipeFile).toAbsolutePath().toString(), recipes.stream()
////                    .map(r -> r.getName()).toArray(String[]::new));
//        } catch (IOException e) {
//            throw new UncheckedIOException("Unable to open recipe file " + recipeFile, e);
//        }
//
        RecipeSpec camelQuarkusRecipe = spec.recipe(CamelQuarkusTestUtil.class.getResourceAsStream("/quarkus-updates/org.apache.camel.quarkus/camel-quarkus/3.8.yaml"));
//        RecipeSpec recipeSpec = spec.recipes(camelQuarkusRecipe.getRecipe()).allSources(spec.getAllSources());
//        return recipeSpec;
//        return spec.recipes(RewriteTest.defaults());
        return null;
    }

}
