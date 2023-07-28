package org.apache.camel.quarkus.update;

import org.apache.camel.quarkus.update.java.AbstractCamelVisitor;
import org.apache.camel.quarkus.update.java.CamelAPIsRecipe;
import org.apache.camel.quarkus.update.java.CamelBeanRecipe;
import org.apache.camel.quarkus.update.java.CamelEIPRecipe;
import org.apache.camel.quarkus.update.java.CamelHttpRecipe;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.tree.J;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Grouping recipe for all Java recipes.
 * Recipes are registered in the list (order is preserved) and all of them are executed.
 */
public class CamelJavaGroupedRecipe extends Recipe {

    List<Recipe> groupedRecipes = Arrays.asList(
            new CamelAPIsRecipe(),
            new CamelEIPRecipe(),
            new CamelBeanRecipe(),
            new CamelHttpRecipe()
    );

    @Override
    public String getDisplayName() {
        return "Camel Extension recipes for following extension:\n"
                + groupedRecipes.stream().map(r -> r.getDisplayName()).collect(Collectors.joining("\n"));
    }

    @Override
    public String getDescription() {
        return "Camel Extension recipes for following extensions:\n"
                + groupedRecipes.stream().map(r -> r.getDescription()).collect(Collectors.joining("\n"));
    }



    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {

        return new AbstractCamelVisitor() {
            @Override
            protected J.Import doVisitImport(J.Import _import, ExecutionContext context) {

                //run all recipes
                groupedRecipes.stream().sequential().forEach(r -> doAfterVisit(r));

                return  _import;
            }
        };
    }
}
