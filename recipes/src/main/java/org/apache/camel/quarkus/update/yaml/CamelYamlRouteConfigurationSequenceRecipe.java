package org.apache.camel.quarkus.update.yaml;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.yaml.JsonPathMatcher;
import org.openrewrite.yaml.YamlIsoVisitor;
import org.openrewrite.yaml.format.IndentsVisitor;
import org.openrewrite.yaml.style.IndentsStyle;
import org.openrewrite.yaml.tree.Yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.openrewrite.Tree.randomId;

@EqualsAndHashCode(callSuper = true)
@Value
public class CamelYamlRouteConfigurationSequenceRecipe extends Recipe {

    private static JsonPathMatcher MATCHER_ROUTE_CONFIGURATION = new JsonPathMatcher("$.route-configuration");
    private static JsonPathMatcher MATCHER_ROUTE_CONFIGURATION_ON_EXCEPTION = new JsonPathMatcher("$.route-configuration.on-exception");

    @Override
    public String getDisplayName() {
        return "Camel API changes";
    }

    @Override
    public String getDescription() {
        return "Camel API changes.";
    }


    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {

        return new YamlIsoVisitor<>() {

            Yaml.Mapping routeConfigurationOnException = null;
            Yaml.Mapping.Entry routeConfigurationEntry = null;
            Yaml.Mapping.Entry routeConfigurationONExceptionEntry = null;
            List<Yaml.Sequence.Entry> sEntries = null;
            Yaml.Sequence sequenceWithOnException = null;

            @Override
            public Yaml.Mapping.Entry visitMappingEntry(Yaml.Mapping.Entry entry, ExecutionContext context) {
                Yaml.Mapping.Entry e = super.visitMappingEntry(entry, context);

                if(routeConfigurationEntry == null && MATCHER_ROUTE_CONFIGURATION.matches(getCursor()) ) {

                    routeConfigurationEntry = e;
                    if(routeConfigurationOnException != null) {
                        moveOnException();
                    }
                }
                return e;

            }

            @Override
            public Yaml.Mapping visitMapping(Yaml.Mapping mapping, ExecutionContext context) {
                Yaml.Mapping m =  super.visitMapping(mapping, context);

                String prop = YamlRecipesUtil.getProperty(getCursor());
                if("route-configuration.on-exception".equals(prop) && routeConfigurationOnException == null) {
                    routeConfigurationOnException = m;
                    if(routeConfigurationEntry != null) {
                        moveOnException();
                    }
                }


                return m;
            }

            @Override
            public Yaml.Sequence visitSequence(Yaml.Sequence sequence, ExecutionContext context) {
                Yaml.Sequence s = super.visitSequence(sequence, context);

                Cursor parent = getCursor().getParent();
                if (new JsonPathMatcher("$.route-configuration").matches(parent)) {
                    sequenceWithOnException = s;
                    sEntries = new ArrayList<>(s.getEntries());
                    moveOnException();
                }


                return s;
            }


            private void moveOnException() {
                //remove content of route-configuration
                doAfterVisit(new YamlIsoVisitor<ExecutionContext>() {

                    @Override
                    public Yaml.Sequence visitSequence(Yaml.Sequence sequence, ExecutionContext context) {
                         Yaml.Sequence s = super.visitSequence(sequence, context);

                        Cursor parent = getCursor().getParent();
                        if (new JsonPathMatcher("$.route-configuration").matches(parent)) {
                            return null;
                        }
                         return s;
                    }
                 }
                );

                //create a new content of route-configuration (not sequence, but mapping)
                doAfterVisit(new YamlIsoVisitor<ExecutionContext>() {

                     @Override
                     public Yaml.Mapping.Entry visitMappingEntry(Yaml.Mapping.Entry entry, ExecutionContext context) {
                         Yaml.Mapping.Entry e =  super.visitMappingEntry(entry, context);

                         List<Yaml.Mapping.Entry> entries = new ArrayList<>();
                         if(sEntries != null) {
                             for (Yaml.Sequence.Entry sEntry : sEntries) {
                                 if (sEntry.getBlock() instanceof Yaml.Mapping) {
                                     ((Yaml.Mapping) sEntry.getBlock()).getEntries().forEach(y -> {
                                         entries.add(y.withPrefix("\n"));
                                     });
                                 }
                             }

                             sEntries = null;
                             return e.withValue(new Yaml.Mapping(randomId(), e.getMarkers(), null,  entries, null, null));
                         }
                         //if on-exception
                         if(sequenceWithOnException != null && "on-exception".equals(e.getKey().getValue())) {
                             //create a sequence
                             Yaml.Sequence newSequence = sequenceWithOnException.copyPaste();
                             //keep only on-exception item
                             List<Yaml.Sequence.Entry> filteredEntries = newSequence.getEntries().stream()
                                     .filter(se -> ((Yaml.Mapping)se.getBlock()).getEntries().stream()
                                             .filter(me -> "on-exception".equals(me.getKey().getValue())).findFirst().isPresent())
                                     .collect(Collectors.toList());

                             e = e.withValue(newSequence.withEntries(filteredEntries));
                             sequenceWithOnException = null;
                         }
                         return e;
                     }



                 }

                );

                //TODO might probably change indent in original file, may this happen?
                doAfterVisit(new IndentsVisitor(new IndentsStyle(2), null));
            }
        };
    }
}

