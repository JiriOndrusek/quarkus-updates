package org.apache.camel.quarkus.update.v3_0;

import org.apache.camel.quarkus.update.RecipesUtil;
import org.apache.camel.quarkus.update.v3_0.java.CamelAPIsRecipe;
import org.apache.camel.quarkus.update.v3_0.java.CamelBeanRecipe;
import org.apache.camel.quarkus.update.v3_0.java.CamelEIPRecipe;
import org.apache.camel.quarkus.update.v3_0.java.CamelHttpRecipe;
import org.apache.camel.quarkus.update.v3_0.maven.RemovedComponentsRecipe;
import org.apache.camel.quarkus.update.v3_0.properties.CamelQuarkusAPIsPropertiesRecipe;
import org.apache.camel.quarkus.update.v3_0.xml.XmlDslRecipe;
import org.apache.camel.quarkus.update.v3_0.yaml.CamelQuarkusYamlRouteConfigurationSequenceRecipe;
import org.apache.camel.quarkus.update.v3_0.yaml.CamelQuarkusYamlStepsInFromRecipe;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.migrate.UpgradeJavaVersion;
import org.openrewrite.maven.MavenVisitor;
import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.tree.Xml;

import java.util.Arrays;
import java.util.Collection;

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

    public CamelQuarkusMigrationRecipe() {
        this(false);
    }

    //Test do not require the detection of camel-quarkus dependencies (in the majority of the cases)
    //This package protected constructor is designed for the tests only.
    CamelQuarkusMigrationRecipe(boolean skipCamelDetection) {

      doNext(runRecipes(
              //test can override the detection in pom to make performance better
              skipCamelDetection,
              Arrays.asList(
                        //pom recipes
                        new RemovedComponentsRecipe(),
                        //upgrade to J17 if camel-quarkus is present
                        new UpgradeJavaVersion(17),
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
              )));
    }


    @Override
    public String getDisplayName() {
        return "Recipe for the Camel-quarkus migration";
    }

    @Override
    public String getDescription() {
        return "Recipe for the Camel-quarkus migration. Takes care of Java, YAML, properties and maven automatic migration.";
    }

    @NotNull
    private Recipe runRecipes(boolean skipCamelDetection, Collection<Recipe> recipes) {

        return new Recipe() {

            private Collection<Recipe> recipes;
            private boolean skipCamelDetection;

            //flag, that camel was already detected
            private boolean camelPresent = false;

            Recipe initialise(boolean skipCamelDetection, Collection<Recipe> recipes){
                this.recipes = recipes;
                this.skipCamelDetection = skipCamelDetection;
                return this;
            }

            @Override
            public String getDisplayName() {
                return "Skip detection of Camel Quarkus dependency for tests and register other recipes";
            }

            @Override
            public String getDescription() {
                return "Internal recipe, which ignores requirement of Camel Quarkus dependency for the test execution and registers other recipes.";
            }

            @Override
            public TreeVisitor<?, ExecutionContext> getVisitor() {
                //if skipCamelDetection == true, there is no need to detect existence of Camel
                //and all recipes could be registered
                if(skipCamelDetection) {
                    recipes.forEach(r -> doNext(r));
                    //return empty visitor
                    return new TreeVisitor<>() {};
                }

                //Visitor detecting existence of camel-quarkus dependency.
                return new MavenVisitor<>() {

                    @Override
                    public Xml visitTag(Xml.Tag tag, ExecutionContext ctx) {
                        Xml.Tag t = (Xml.Tag) super.visitTag(tag, ctx);

                        if(camelPresent || !DEPENDENCY_MATCHER.matches(getCursor())) {
                            return t;
                        }

                        if (GROUP_ID.equals(t.getChildValue("groupId").orElse(""))){
                            camelPresent = true;
                            //register all recipes
                            recipes.forEach(r -> doNext(r));
                        }
                        return t;
                    }
                };
            }
        }.initialise(skipCamelDetection, recipes);
    }
}

