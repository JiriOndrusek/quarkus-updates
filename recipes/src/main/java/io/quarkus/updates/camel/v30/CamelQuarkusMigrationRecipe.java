package io.quarkus.updates.camel.v30;

import io.quarkus.updates.camel.v30.java.CamelAPIsRecipe;
import io.quarkus.updates.camel.v30.java.CamelEIPRecipe;
import io.quarkus.updates.camel.v30.java.CamelHttpRecipe;
import io.quarkus.updates.camel.v30.properties.CamelQuarkusAPIsPropertiesRecipe;
import io.quarkus.updates.camel.v30.xml.XmlDslRecipe;
import io.quarkus.updates.camel.v30.yaml.CamelQuarkusYamlRouteConfigurationSequenceRecipe;
import io.quarkus.updates.camel.v30.yaml.CamelQuarkusYamlStepsInFromRecipe;
import io.quarkus.updates.camel.v30.java.CamelBeanRecipe;
import io.quarkus.updates.camel.v30.maven.RemovedComponentsRecipe;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.SourceFile;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.java.migrate.UpgradeJavaVersion;
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

    private final boolean skipCamelDetection;

    private final Collection<Recipe> recipes;

    public CamelQuarkusMigrationRecipe() {
        this(false);
    }

    //Test do not require the detection of camel-quarkus dependencies (in the majority of the cases)
    //This package protected constructor is designed for the tests only.
    CamelQuarkusMigrationRecipe(boolean skipCamelDetection) {
        this.skipCamelDetection = skipCamelDetection;
        this.recipes =  Arrays.asList(
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
        //if skipCamelDetection == true, there is no need to detect existence of Camel
        //and all recipes could be registered
        if(skipCamelDetection) {
            recipes.forEach(r -> doNext(r));
            //return an empty visitor
            return before;
        }

        //detection of camel-dependency existence
        MavenCamelQuarkusDetectorVisitor visitor = new MavenCamelQuarkusDetectorVisitor();
        before = ListUtils.map(before, (s) -> (SourceFile)visitor.visit(s, ctx));

        //if camel-quarkus was detected, register recipes
        if(visitor.camelPresent) {
            recipes.forEach(r -> doNext(r));
        }

        return before;
    }

    private class MavenCamelQuarkusDetectorVisitor extends MavenVisitor<ExecutionContext> {

        private boolean camelPresent = false;
        private MavenCamelQuarkusDetectorVisitor() {
        }

        @Override
        public Xml visitTag(Xml.Tag tag, ExecutionContext ctx) {
            Xml.Tag t = (Xml.Tag) super.visitTag(tag, ctx);

            if(camelPresent || !DEPENDENCY_MATCHER.matches(getCursor())) {
                return t;
            }

            if (GROUP_ID.equals(t.getChildValue("groupId").orElse(""))){
                camelPresent = true;
            }
            return t;
        }

    }
}

