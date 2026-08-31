package io.quarkus.updates.quarkiverse.cxf.cxf339;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.marker.SearchResult;
import org.openrewrite.maven.MavenIsoVisitor;
import org.openrewrite.xml.tree.Xml;

@Value
@EqualsAndHashCode(callSuper = true)
public class FindQuarkusCxfWithoutJacksonExclusion extends Recipe {

    @Override
    public String getDisplayName() {
        return "Find `quarkus-cxf` dependencies without a `quarkus-jackson` exclusion";
    }

    @Override
    public String getDescription() {
        return "Search recipe used as a precondition: marks Maven poms that declare " +
                "`io.quarkiverse.cxf:quarkus-cxf` (directly or in dependency management) where no such " +
                "declaration carries an exclusion of `io.quarkus:quarkus-jackson` (`*` wildcard exclusions " +
                "count as well). An exclusion is an explicit user decision against `quarkus-jackson` made " +
                "while quarkus-cxf still pulled it transitively, so the quarkus-cxf 3.39 migration must not " +
                "add the dependency back.";
    }

    private static boolean isQuarkusCxf(Xml.Tag dependency) {
        return "io.quarkiverse.cxf".equals(dependency.getChildValue("groupId").orElse(null))
                && "quarkus-cxf".equals(dependency.getChildValue("artifactId").orElse(null));
    }

    /** Maven exclusions support the full {@code *} wildcard per coordinate. */
    private static boolean matchesCoordinate(String exclusionValue, String coordinate) {
        return "*".equals(exclusionValue) || coordinate.equals(exclusionValue);
    }

    private static boolean hasJacksonExclusion(Xml.Tag dependency) {
        return dependency.getChild("exclusions")
                .map(exclusions -> exclusions.getChildren("exclusion").stream()
                        .anyMatch(exclusion -> matchesCoordinate(exclusion.getChildValue("groupId").orElse(""), "io.quarkus")
                                && matchesCoordinate(exclusion.getChildValue("artifactId").orElse(""), "quarkus-jackson")))
                .orElse(false);
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new MavenIsoVisitor<ExecutionContext>() {
            @Override
            public Xml.Document visitDocument(Xml.Document document, ExecutionContext ctx) {
                Xml.Document d = super.visitDocument(document, ctx);
                // [0] quarkus-cxf declared, [1] quarkus-jackson excluded on one of the declarations
                boolean[] state = new boolean[2];
                new MavenIsoVisitor<boolean[]>() {
                    @Override
                    public Xml.Tag visitTag(Xml.Tag tag, boolean[] s) {
                        if ((isDependencyTag() || isManagedDependencyTag()) && isQuarkusCxf(tag)) {
                            s[0] = true;
                            if (hasJacksonExclusion(tag)) {
                                s[1] = true;
                            }
                        }
                        return super.visitTag(tag, s);
                    }
                }.visit(d, state);
                if (state[0] && !state[1]) {
                    return SearchResult.found(d);
                }
                return d;
            }
        };
    }
}
