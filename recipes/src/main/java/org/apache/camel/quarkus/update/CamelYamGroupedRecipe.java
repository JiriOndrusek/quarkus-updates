package org.apache.camel.quarkus.update;

import org.apache.camel.quarkus.update.yaml.CamelYamlRouteConfigurationSequenceRecipe;
import org.apache.camel.quarkus.update.yaml.CamelYamlStepsInFromRecipe;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.yaml.YamlIsoVisitor;
import org.openrewrite.yaml.tree.Yaml;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class CamelYamGroupedRecipe extends Recipe {

    List<Recipe> groupedRecipes = Arrays.asList(
            new CamelYamlStepsInFromRecipe(),
            new CamelYamlRouteConfigurationSequenceRecipe()
    );

    @Override
    public String getDisplayName() {
        return "Camel Yaml recipes for following changes:\n"
                + groupedRecipes.stream().map(r -> r.getDisplayName()).collect(Collectors.joining("\n"));
    }

    @Override
    public String getDescription() {
        return "Camel Yaml recipes for following changes:\n"
                + groupedRecipes.stream().map(r -> r.getDisplayName()).collect(Collectors.joining("\n"));
    }



    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new YamlIsoVisitor<>() {
            boolean executed = false;

            @Override
            public Yaml.Documents visitDocuments(Yaml.Documents documents, ExecutionContext context) {
                //execute grouped visitors by the top visit method
                if(!executed) {
                    executed = true;
                    groupedRecipes.stream().sequential().forEach(r -> doAfterVisit(r));
                }
                return documents;
            }
        };
    }
}
