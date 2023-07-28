package io.quarkus.updates.camel30.maven;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.maven.MavenVisitor;
import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.tree.Xml;

import io.quarkus.updates.camel30.RecipesUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Several camel/CQ extensions were removed.
 * All of such dependencies are commented-out with a comment about removal. In case that different extension is suggested as a reoplacement,
 * comment suggest that.
 * TODO usu official links
 * See https://camel.apache.org/manual/camel-4-migration-guide.html#_removed_components and https://github.com/apache/camel-quarkus/blob/main/docs/modules/ROOT/pages/migration-guide/3.0.0.adoc#removed-extensions
 */
public class RemovedComponentsRecipe extends Recipe {

    private static String GROUP_ID = "org.apache.camel.quarkus";
    private static Set<String> ARTIFACT_IDS = new HashSet<>(Arrays.asList(
            "camel-quarkus-activemq",
            "camel-quarkus-atmos",
            "camel-quarkus-avro-rpc",
            "camel-quarkus-caffeine-lrucache",
            "camel-quarkus-datasonnet",
            "camel-quarkus-dozer",
            "camel-quarkus-elasticsearch-rest",
            "camel-quarkus-gora",
            "camel-quarkus-hbase",
            "camel-quarkus-iota",
            "camel-quarkus-jbpm",
            "camel-quarkus-jclouds",
            "camel-quarkus-johnzon",
            "camel-quarkus-microprofile-metrics",
            "camel-quarkus-milo",
            "camel-quarkus-opentracing",
            "camel-quarkus-optaplanner",
            "camel-quarkus-rabbitmq",
            "camel-quarkus-smallrye-reactive-messaging",
            "camel-quarkus-solr",
            "camel-quarkus-tika",
            "camel-quarkus-vm",
            "camel-quarkus-xmlsecurity",
            "camel-quarkus-xstream"));
    private static Map<String, List<String>> ALTERNATIVE_COMPONENTS = Stream.of(new String[][] {
        { "camel-quarkus-activemq", "camel-quarkus-jms", "camel-quarkus-sjms", "camel-quarkus-amqp" },
        { "camel-quarkus-caffeine-lrucache", "camel-quarkus-ignite", "camel-quarkus-infinispan" },
        { "camel-quarkus-johnzon", "camel-quarkus-jackson", "camel-quarkus-fastjson", "camel-quarkus-gson" },
        { "camel-quarkus-microprofile-metrics", "camel-quarkus-opentelemetry", "camel-quarkus-micrometer" },
        { "camel-quarkus-opentracing", "camel-quarkus-opentelemetry", "camel-quarkus-micrometer" },
        { "camel-quarkus-rabbitmq", "camel-quarkus-spring-rabbitmq???" },
        { "camel-quarkus-xstream", "camel-quarkus-jacksonxml" },
    }).collect(Collectors.toMap(data -> data[0], data -> Arrays.asList(Arrays.copyOfRange(data, 1, data.length))));

    private static Set<String> TO_BE_REINTRODUCED  = new HashSet<>(Arrays.asList(
            "camel-quarkus-datasonnet",
            "camel-quarkus-smallrye-reactive-messaging",
            "camel-quarkus-tika",
            "camel-quarkus-xmlsecurity"));

    private final static XPathMatcher DEPENDENCY_MATCHER = new XPathMatcher("//dependencies/dependency");

    @Override
    public String getDisplayName() {
        return "Removed Camel components.";
    }

    @Override
    public String getDescription() {
        return "Removed Camel components.";
    }


    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new MavenVisitor<>() {

            @Override
            public Xml visitTag(Xml.Tag tag, ExecutionContext ctx) {
                Xml.Tag t = (Xml.Tag) super.visitTag(tag, ctx);

                if(!DEPENDENCY_MATCHER.matches(getCursor())) {
                    return t;
                }

                if (GROUP_ID.equals(t.getChildValue("groupId").orElse(null))) {
                    String artifactdD = t.getChildValue("artifactId").orElse(null);
                    if(artifactdD != null && ARTIFACT_IDS.contains(artifactdD)) {
                        //add a comment to the artifactId
                        LinkedList l = new LinkedList(t.getContent());
                        l.addFirst(RecipesUtil.createXmlComment(commentToRemovedArtifactId(artifactdD, t.toString())));

                        return RecipesUtil.createXmlComment(commentToRemovedArtifactId(artifactdD, t.print(getCursor()))).withPrefix(t.getPrefix());
                    }
                }
                return t;
            }

            @Override
            protected void doAfterVisit(Recipe recipe) {
                super.doAfterVisit(recipe);
            }

            private String commentToRemovedArtifactId(String artifactId, String theDefinition) {
                if (ALTERNATIVE_COMPONENTS.containsKey(artifactId)) {
                    return String.format("Extension %s was removed, consider %s instead. %s", artifactId, ALTERNATIVE_COMPONENTS.get(artifactId).stream().collect(Collectors.joining(" or ")), theDefinition);
                } else if (TO_BE_REINTRODUCED.contains(artifactId)) {
                    return String.format("Extension %s was removed, but should be reintroduced. %s", artifactId, theDefinition);
                } else {
                    return String.format("Extension %s was removed. %s", artifactId, theDefinition);
                }
            }

        };
    }
}
