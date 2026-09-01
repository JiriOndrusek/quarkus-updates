package io.quarkus.updates.quarkiverse.cxf.cxf331;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.properties.PropertiesIsoVisitor;
import org.openrewrite.properties.tree.Properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
@EqualsAndHashCode(callSuper = true)
public class MigrateClientProxyConfiguration extends Recipe {

    private static final Pattern PROXY_SERVER_PATTERN = Pattern
            .compile("(%[^.]+\\.)?quarkus\\.cxf\\.client\\.([^.]+)\\.proxy-server");
    private static final Pattern PROXY_CONFIGURATION_NAME_PATTERN = Pattern
            .compile("(%[^.]+\\.)?quarkus\\.cxf\\.client\\.([^.]+)\\.proxy-configuration-name");

    @Override
    public String getDisplayName() {
        return "Migrate CXF client proxy options to Quarkus Proxy Registry";
    }

    @Override
    public String getDescription() {
        return "Migrates the deprecated `quarkus.cxf.client.<name>.proxy-*` options to a named Quarkus Proxy Registry " +
                "configuration (`quarkus.proxy.<name>.*`) referenced via `quarkus.cxf.client.<name>.proxy-configuration-name`. " +
                "The old options are removed because `proxy-server` takes precedence over `proxy-configuration-name`. " +
                "The `|` separators of `non-proxy-hosts` are converted to the comma separated list format of " +
                "`quarkus.proxy.<name>.non-proxy-hosts`. Clients with `proxy-server-type=socks` are left untouched, " +
                "because the SOCKS version (`socks4` or `socks5`) cannot be determined automatically.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new PropertiesIsoVisitor<ExecutionContext>() {
            @Override
            public Properties.File visitFile(Properties.File file, ExecutionContext ctx) {
                Properties.File f = super.visitFile(file, ctx);

                Map<String, String> valuesByKey = new HashMap<>();
                // plain loop instead of Collectors.toMap: duplicate keys are parseable input and
                // must keep the last-wins semantics of put(), where toMap would throw
                for (Properties.Content content : f.getContent()) {
                    if (content instanceof Properties.Entry) {
                        Properties.Entry entry = (Properties.Entry) content;
                        valuesByKey.put(entry.getKey(), entry.getValue().getText());
                    }
                }

                // an existing proxy-configuration-name next to proxy-server is an ambiguous, user
                // authored mid-migration state where every automatic resolution is unsafe:
                // overwriting the name destroys explicit intent, removing the old keys activates a
                // previously ignored (possibly nonexistent or shared) configuration and silently
                // changes behavior; skipping keeps the client working, because proxy-server wins
                // at runtime anyway
                Set<String> skippedClients = new HashSet<>();
                for (String key : valuesByKey.keySet()) {
                    Matcher matcher = PROXY_CONFIGURATION_NAME_PATTERN.matcher(key);
                    if (matcher.matches()) {
                        skippedClients.add(matcher.group(2));
                    }
                }

                // key of the proxy-server entry -> client name, per migrated (profile, client) scope
                Map<String, String> proxyServersToMigrate = new HashMap<>();
                // old proxy option key -> new quarkus.proxy key
                Map<String, String> renames = new HashMap<>();
                Set<String> keysToDelete = new HashSet<>();
                for (Map.Entry<String, String> e : valuesByKey.entrySet()) {
                    Matcher matcher = PROXY_SERVER_PATTERN.matcher(e.getKey());
                    if (!matcher.matches()) {
                        continue;
                    }
                    String prefix = matcher.group(1) != null ? matcher.group(1) : "";
                    String clientName = matcher.group(2);
                    if (skippedClients.contains(clientName)) {
                        continue;
                    }
                    String oldPrefix = prefix + "quarkus.cxf.client." + clientName + ".";
                    String serverType = valuesByKey.get(oldPrefix + "proxy-server-type");
                    if (serverType != null && !"http".equals(serverType.toLowerCase(Locale.ROOT))) {
                        continue;
                    }
                    proxyServersToMigrate.put(e.getKey(), clientName);
                    String newPrefix = prefix + "quarkus.proxy." + clientName + ".";
                    renames.put(oldPrefix + "proxy-server-port", newPrefix + "port");
                    renames.put(oldPrefix + "proxy-username", newPrefix + "username");
                    renames.put(oldPrefix + "proxy-password", newPrefix + "password");
                    renames.put(oldPrefix + "non-proxy-hosts", newPrefix + "non-proxy-hosts");
                    if (serverType != null) {
                        // http is the default on both sides
                        keysToDelete.add(oldPrefix + "proxy-server-type");
                    }
                }
                if (proxyServersToMigrate.isEmpty()) {
                    return f;
                }

                List<Properties.Content> newContent = new ArrayList<>();
                for (Properties.Content content : f.getContent()) {
                    if (!(content instanceof Properties.Entry)) {
                        newContent.add(content);
                        continue;
                    }
                    Properties.Entry entry = (Properties.Entry) content;
                    String clientName = proxyServersToMigrate.get(entry.getKey());
                    if (clientName != null) {
                        Matcher matcher = PROXY_SERVER_PATTERN.matcher(entry.getKey());
                        matcher.matches();
                        String prefix = matcher.group(1) != null ? matcher.group(1) : "";
                        newContent.add(entry.withKey(prefix + "quarkus.proxy." + clientName + ".host"));
                        newContent.add(entry
                                .withId(Tree.randomId())
                                .withPrefix("\n")
                                .withKey(prefix + "quarkus.cxf.client." + clientName + ".proxy-configuration-name")
                                .withValue(entry.getValue().withText(clientName)));
                    } else if (renames.containsKey(entry.getKey())) {
                        Properties.Entry renamed = entry.withKey(renames.get(entry.getKey()));
                        if (renames.get(entry.getKey()).endsWith(".non-proxy-hosts")) {
                            renamed = renamed.withValue(entry.getValue().withText(entry.getValue().getText().replace('|', ',')));
                        }
                        newContent.add(renamed);
                    } else if (!keysToDelete.contains(entry.getKey())) {
                        newContent.add(entry);
                    }
                }
                return f.withContent(newContent);
            }
        };
    }
}
