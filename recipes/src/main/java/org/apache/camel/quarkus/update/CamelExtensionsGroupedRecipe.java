package org.apache.camel.quarkus.update;

import org.apache.camel.quarkus.update.extensions.CamelHttpRecipe;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.tree.J;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class CamelExtensionsGroupedRecipe extends Recipe {

    List<Recipe> groupedRecipes = Arrays.asList(
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
