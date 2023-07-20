package org.apache.camel.quarkus.update;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.yaml.JsonPathMatcher;
import org.openrewrite.yaml.YamlIsoVisitor;
import org.openrewrite.yaml.YamlVisitor;
import org.openrewrite.yaml.format.IndentsVisitor;
import org.openrewrite.yaml.style.IndentsStyle;
import org.openrewrite.yaml.tree.Yaml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Value
public class CamelYamlRecipe extends Recipe {

    private static JsonPathMatcher MATCHER_WITHOUT_ROUTE = new JsonPathMatcher("$.steps");
    private static JsonPathMatcher MATCHER_WITH_ROUTE = new JsonPathMatcher("$.route.steps");

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

        return new YamlIsoVisitor<ExecutionContext>() {

            Yaml.Mapping from = null;
            Yaml.Mapping.Entry steps = null;

            //look at deleteProperty
            //in visit mappingEntry (steps) call     doAfterVisit(new DeletePropertyVisitor<>(entry)); -> which takes steps as a param and inserts it into from
            //I get a visitor with reference to steps and will match from to put steps there

            @Override
            public Yaml.Mapping.Entry visitMappingEntry(Yaml.Mapping.Entry entry, ExecutionContext context) {
                Yaml.Mapping.Entry e = super.visitMappingEntry(entry, context);

                if(steps == null && (MATCHER_WITH_ROUTE.matches(getCursor()) || MATCHER_WITHOUT_ROUTE.matches(getCursor()))) {

                    steps = e;
                    if(from != null) {
                        moveSteps();
                    }
                    return null;
                }

                return e;

            }

            @Override
            public Yaml.Mapping visitMapping(Yaml.Mapping mapping, ExecutionContext context) {
                Yaml.Mapping m =  super.visitMapping(mapping, context);

                String prop = RecipesUtil.getProperty(getCursor());
                if(("route.from".equals(prop) || "from".equals(prop)) && from == null) {
                    from = m;
                    if(steps != null) {
                        moveSteps();
                    }
                }


                return m;
            }

            private void moveSteps() {
                doAfterVisit(new YamlIsoVisitor<ExecutionContext>()  {

                    @Override
                    public Yaml.Mapping visitMapping(Yaml.Mapping mapping, ExecutionContext c) {
                        Yaml.Mapping m = (Yaml.Mapping) super.visitMapping(mapping, c);

                        if(m == from) {
                            List<Yaml.Mapping.Entry> entries = new ArrayList<>(m.getEntries());
                            entries.add(steps.copyPaste().withPrefix("\n"));
                            m = m.withEntries(entries);
                        }

                        return m;
                    }});

                //TODO might probably change indent in original file, may this happen?
                doAfterVisit(new IndentsVisitor(new IndentsStyle(2), null));
            }
        };
    }
}

