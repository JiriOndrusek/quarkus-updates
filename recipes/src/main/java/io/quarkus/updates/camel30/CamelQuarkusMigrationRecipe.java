package io.quarkus.updates.camel30;

import io.quarkus.updates.camel30.java.CamelAPIsRecipe;
import io.quarkus.updates.camel30.java.CamelBeanRecipe;
import io.quarkus.updates.camel30.java.CamelEIPRecipe;
import io.quarkus.updates.camel30.java.CamelHttpRecipe;
import io.quarkus.updates.camel30.maven.RemovedComponentsRecipe;
import io.quarkus.updates.camel30.properties.CamelQuarkusAPIsPropertiesRecipe;
import io.quarkus.updates.camel30.xml.XmlDslRecipe;
import io.quarkus.updates.camel30.yaml.CamelQuarkusYamlRouteConfigurationSequenceRecipe;
import io.quarkus.updates.camel30.yaml.CamelQuarkusYamlStepsInFromRecipe;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.maven.MavenVisitor;
import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.tree.Xml;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Root recipe of all camel-quarkus related recipes.
 *
 * 2 main functionalities:
 * <ul>
 *     <li>
 *         All Camel-quarkus recipes are gathered here.
 *         See the constructor, recipes have to be registered via the <i>doNext()</i> method.</li>
 *     <li>
 *         Main recipe implements MavenVisitor for detection of camel-quarkus dependencies.
 *         If no camel-quarkus dependency is detected, no camel-quarkus recipe is executed
 *     </li>
 * </ul>
 *
 */
public class CamelQuarkusMigrationRecipe extends Recipe {

    //GroupId of camel-quarkus dependencies.
    private final static String GROUP_ID = "org.apache.camel.quarkus";

    private final static XPathMatcher DEPENDENCY_MATCHER = new XPathMatcher("//dependencies/dependency");

    private final Collection<Recipe> recipes;

    //Test do not require the detection of camel-quarkus dependencies (in the majority of the cases)
    public CamelQuarkusMigrationRecipe() {
        this.recipes =  Arrays.asList(
                //pom recipes
                new RemovedComponentsRecipe(),
                //xml recipe
                new XmlDslRecipe(),
                //properties recipes
                new CamelQuarkusAPIsPropertiesRecipe(),
                //yaml recipes
                new CamelQuarkusYamlRouteConfigurationSequenceRecipe(),
                new CamelQuarkusYamlStepsInFromRecipe(),
                //java recipes
                new CamelAPIsRecipe(),
                new CamelEIPRecipe(),
                new CamelBeanRecipe(),
                new CamelHttpRecipe()
        );
    }

    @Override
    public String getDisplayName() {
        return "Recipe for the Camel-quarkus migration";
    }

    @Override
    public String getDescription() {
        return "Recipe for the Camel-quarkus migration. Takes care of Java, YAML, properties and maven automatic migration.";
    }

    protected List<SourceFile> visit(List<SourceFile> before, ExecutionContext ctx) {

        recipes.forEach(r -> doNext(r));
        //return an empty visitor
        return before;

    }
}

