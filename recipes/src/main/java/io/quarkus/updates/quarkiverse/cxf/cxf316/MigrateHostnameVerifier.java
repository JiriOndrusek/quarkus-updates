package io.quarkus.updates.quarkiverse.cxf.cxf316;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.properties.PropertiesIsoVisitor;
import org.openrewrite.properties.tree.Properties;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
@EqualsAndHashCode(callSuper = true)
public class MigrateHostnameVerifier extends Recipe {

    private static final Pattern HOSTNAME_VERIFIER_PATTERN =
            Pattern.compile("(%[^.]+\\.)?quarkus\\.cxf\\.client\\.([^.]+)\\.hostname-verifier");

    @Override
    public String getDisplayName() {
        return "Migrate CXF hostname-verifier to Quarkus TLS registry";
    }

    @Override
    public String getDescription() {
        return "Migrates quarkus.cxf.client.<name>.hostname-verifier=AllowAllHostnameVerifier " +
                "to quarkus.tls.<name>.hostname-verification-algorithm=NONE and adds " +
                "quarkus.cxf.client.<name>.tls-configuration-name=<name>.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new PropertiesIsoVisitor<ExecutionContext>() {
            @Override
            public Properties.File visitFile(Properties.File file, ExecutionContext ctx) {
                Properties.File f = super.visitFile(file, ctx);

                List<Properties.Content> newContent = new ArrayList<>();
                boolean changed = false;

                for (Properties.Content content : f.getContent()) {
                    if (content instanceof Properties.Entry) {
                        Properties.Entry entry = (Properties.Entry) content;
                        Matcher matcher = HOSTNAME_VERIFIER_PATTERN.matcher(entry.getKey());
                        if (matcher.matches() && "AllowAllHostnameVerifier".equals(entry.getValue().getText())) {
                            String prefix = matcher.group(1) != null ? matcher.group(1) : "";
                            String clientName = matcher.group(2);

                            newContent.add(entry
                                    .withKey(prefix + "quarkus.tls." + clientName + ".hostname-verification-algorithm")
                                    .withValue(entry.getValue().withText("NONE")));
                            newContent.add(entry
                                    .withId(Tree.randomId())
                                    .withPrefix("\n")
                                    .withKey(prefix + "quarkus.cxf.client." + clientName + ".tls-configuration-name")
                                    .withValue(entry.getValue().withText(clientName)));
                            changed = true;
                        } else {
                            newContent.add(content);
                        }
                    } else {
                        newContent.add(content);
                    }
                }

                return changed ? f.withContent(newContent) : f;
            }
        };
    }
}
