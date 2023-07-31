package org.apache.camel.quarkus.update;

import org.apache.camel.quarkus.update.java.CamelAPIsRecipe;
import org.apache.camel.quarkus.update.java.CamelBeanRecipe;
import org.apache.camel.quarkus.update.java.CamelEIPRecipe;
import org.apache.camel.quarkus.update.java.CamelHttpRecipe;
import org.apache.camel.quarkus.update.pom.RemovedComponentsRecipe;
import org.apache.camel.quarkus.update.properties.CamelQuarkusAPIsPropertiesRecipe;
import org.apache.camel.quarkus.update.yaml.CamelQuarkusYamlRouteConfigurationSequenceRecipe;
import org.apache.camel.quarkus.update.yaml.CamelQuarkusYamlStepsInFromRecipe;
import org.apache.camel.quarkus.update.yaml.CamelQuarkusYamlVisitor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.maven.MavenVisitor;
import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.tree.Xml;
import org.openrewrite.yaml.YamlIsoVisitor;


public class CamelQuarkusMigrationRecipe extends Recipe {

    private static String GROUP_ID = "org.apache.camel.quarkus";
    private final static XPathMatcher DEPENDENCY_MATCHER = new XPathMatcher("//dependencies/dependency");

    private boolean camelPresent = false;

    public CamelQuarkusMigrationRecipe() {
        this(false);
    }

    CamelQuarkusMigrationRecipe(boolean skipPomDetection) {
        //test can override the detection in pom to make performance better
        if(skipPomDetection) {
            camelPresent = true;
        }


        //pom recipes
        doNext(new RemovedComponentsRecipe());
        //properties recipes
        doNext(new CamelQuarkusAPIsPropertiesRecipe());
        //yaml recipes
        doNext(new CamelQuarkusYamlRouteConfigurationSequenceRecipe());
        doNext(new CamelQuarkusYamlStepsInFromRecipe());
        //java recipes
        doNext(new CamelAPIsRecipe());
        doNext(new CamelEIPRecipe());
        doNext(new CamelBeanRecipe());
        doNext(new CamelHttpRecipe());
    }

    @Override
    public String getDisplayName() {
        return "Recipes for CQ migration";
    }

    @Override
    public String getDescription() {
        return "Recipes for CQ migration.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new MavenVisitor<>() {

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
        };
    }
}

