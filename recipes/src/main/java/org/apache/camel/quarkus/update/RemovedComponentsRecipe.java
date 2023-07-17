package org.apache.camel.quarkus.update;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.XmlIsoVisitor;
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private final static XPathMatcher DEPENDENCY_ARTIFACT_ID_MATCHER = new XPathMatcher("//dependencies/dependency/artifactId");

    @Override
    public String getDisplayName() {
        return "Removed Camel components.";
    }

    @Override
    public String getDescription() {
        return "Removed Camel components.";
    }


    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new XmlIsoVisitor<>() {

            @Override
            public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext context) {
                Xml.Tag t =  super.visitTag(tag, context);

                if (DEPENDENCY_ARTIFACT_ID_MATCHER.matches(getCursor()) && !t.getContent().isEmpty()) {
                    Xml.Tag parent = getCursor().getParent().getValue();

                    //TODO what with  <artifactId>camel-quarkus-dozer<!-- aa --></artifactId>
                    for(Content cd : t.getContent()) {
                        if(cd instanceof Xml.CharData) {
                            String text = ((Xml.CharData) cd).getText();
                            if(ARTIFACT_IDS.contains(text)) {
                                //verify groupId
                                Stream<Content> groupIdContent = parent.getContent().stream().filter(c -> "groupId".equals(((Xml.Tag)c).getName())).flatMap(c -> ((Xml.Tag) c).getContent().stream());
                                Optional<Content> camelGroupId = groupIdContent.filter(c -> c instanceof Xml.CharData && GROUP_ID.equals(((Xml.CharData)c).getText())).findFirst();
                                if(camelGroupId.isPresent()) {
                                    //add a comment to the artifactId
                                    LinkedList l = new LinkedList(t.getContent());
                                    l.addFirst(RecipesUtil.createXmlComment(commentToRemovedArtifactId(text)));

                                    return t.withContent(l).withName("artifactId_removed");
                                }
                            }
                        }
                    }
                }

                return t;
            }

            private static String commentToRemovedArtifactId(String artifactId) {
                if (ALTERNATIVE_COMPONENTS.containsKey(artifactId)) {
                    return String.format("Extension %s was removed, consider %s instead.", artifactId, ALTERNATIVE_COMPONENTS.get(artifactId).stream().collect(Collectors.joining(" or ")));
                } else if (TO_BE_REINTRODUCED.contains(artifactId)) {
                    return String.format("Extension %s was removed, but should be reintroduced.", artifactId);
                } else {
                    return String.format("Extension %s was removed.", artifactId);
                }
            }

        };
    }
}
