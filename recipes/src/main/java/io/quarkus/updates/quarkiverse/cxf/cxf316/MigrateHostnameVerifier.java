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
public class MigrateHostnameVerifier extends Recipe {

    private static final Pattern HOSTNAME_VERIFIER_PATTERN = Pattern
            .compile("(%[^.]+\\.)?quarkus\\.cxf\\.client\\.([^.]+)\\.hostname-verifier");
    private static final Pattern GLOBAL_CONDUIT_PATTERN = Pattern
            .compile("(%[^.]+\\.)?quarkus\\.cxf\\.http-conduit-factory");
    private static final Pattern CLIENT_CONDUIT_PATTERN = Pattern
            .compile("(%[^.]+\\.)?quarkus\\.cxf\\.client\\.([^.]+)\\.http-conduit-factory");
    private static final Pattern KEY_STORE_PATTERN = Pattern
            .compile("(%[^.]+\\.)?quarkus\\.cxf\\.client\\.([^.]+)\\.(key-store|key-store-password|key-store-type|key-password)");
    private static final Pattern TLS_CONFIGURATION_NAME_PATTERN = Pattern
            .compile("(%[^.]+\\.)?quarkus\\.cxf\\.client\\.([^.]+)\\.tls-configuration-name");

    @Override
    public String getDisplayName() {
        return "Migrate CXF client hostname-verifier to Quarkus TLS registry";
    }

    @Override
    public String getDescription() {
        return "Migrates `quarkus.cxf.client.<name>.hostname-verifier=AllowAllHostnameVerifier` to a named TLS registry " +
                "configuration with `quarkus.tls.<name>.hostname-verification-algorithm=NONE` referenced via " +
                "`quarkus.cxf.client.<name>.tls-configuration-name`, moving the deprecated `trust-store*` options of the " +
                "client into the same TLS configuration (setting `tls-configuration-name` next to `trust-store*` fails " +
                "at runtime). Clients using a key store, an unrecognized trust store type, an already present " +
                "`tls-configuration-name` or a non-Vert.x `http-conduit-factory` (where `hostname-verifier` keeps " +
                "working and `hostname-verification-algorithm` is unsupported) are left untouched.";
    }

    private static boolean isVertxConduit(String value) {
        return "VertxHttpClientHTTPConduitFactory".equals(value) || "QuarkusCXFDefault".equals(value);
    }

    private static String storeExtension(String storeType, String storePath) {
        if (storeType != null) {
            String normalized = storeType.toLowerCase(Locale.ROOT);
            if ("pkcs12".equals(normalized) || "p12".equals(normalized)) {
                return "p12";
            }
            if ("jks".equals(normalized)) {
                return "jks";
            }
            return null;
        }
        if (storePath != null) {
            String normalized = storePath.toLowerCase(Locale.ROOT);
            if (normalized.endsWith(".p12") || normalized.endsWith(".pfx") || normalized.endsWith(".pkcs12")) {
                return "p12";
            }
        }
        // JKS is the documented default of quarkus.cxf.client."client-name".trust-store-type
        return "jks";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new PropertiesIsoVisitor<ExecutionContext>() {
            @Override
            public Properties.File visitFile(Properties.File file, ExecutionContext ctx) {
                Properties.File f = super.visitFile(file, ctx);

                Map<String, String> valuesByKey = new HashMap<>();
                for (Properties.Content content : f.getContent()) {
                    if (content instanceof Properties.Entry) {
                        Properties.Entry entry = (Properties.Entry) content;
                        valuesByKey.put(entry.getKey(), entry.getValue().getText());
                    }
                }

                Set<String> skippedClients = new HashSet<>();
                for (Map.Entry<String, String> e : valuesByKey.entrySet()) {
                    if (GLOBAL_CONDUIT_PATTERN.matcher(e.getKey()).matches() && !isVertxConduit(e.getValue())) {
                        // hostname-verifier keeps working with the pinned non-Vert.x conduit
                        return f;
                    }
                    Matcher clientConduit = CLIENT_CONDUIT_PATTERN.matcher(e.getKey());
                    if (clientConduit.matches() && !isVertxConduit(e.getValue())) {
                        skippedClients.add(clientConduit.group(2));
                    }
                    Matcher keyStore = KEY_STORE_PATTERN.matcher(e.getKey());
                    if (keyStore.matches()) {
                        skippedClients.add(keyStore.group(2));
                    }
                    Matcher tlsConfigurationName = TLS_CONFIGURATION_NAME_PATTERN.matcher(e.getKey());
                    if (tlsConfigurationName.matches()) {
                        skippedClients.add(tlsConfigurationName.group(2));
                    }
                }

                // key of the hostname-verifier entry -> client name, per migrated (profile, client) scope
                Map<String, String> verifiersToMigrate = new HashMap<>();
                // old trust store key -> new TLS registry key
                Map<String, String> trustStoreRenames = new HashMap<>();
                Set<String> keysToDelete = new HashSet<>();
                for (Map.Entry<String, String> e : valuesByKey.entrySet()) {
                    Matcher matcher = HOSTNAME_VERIFIER_PATTERN.matcher(e.getKey());
                    if (!matcher.matches() || !"AllowAllHostnameVerifier".equals(e.getValue())) {
                        continue;
                    }
                    String prefix = matcher.group(1) != null ? matcher.group(1) : "";
                    String clientName = matcher.group(2);
                    if (skippedClients.contains(clientName)) {
                        continue;
                    }
                    String oldStorePrefix = prefix + "quarkus.cxf.client." + clientName + ".trust-store";
                    String extension = storeExtension(valuesByKey.get(oldStorePrefix + "-type"), valuesByKey.get(oldStorePrefix));
                    if (extension == null) {
                        // unrecognized trust-store-type, do not touch this client
                        continue;
                    }
                    verifiersToMigrate.put(e.getKey(), clientName);
                    String newStorePrefix = prefix + "quarkus.tls." + clientName + ".trust-store." + extension;
                    if (valuesByKey.containsKey(oldStorePrefix)) {
                        trustStoreRenames.put(oldStorePrefix, newStorePrefix + ".path");
                    }
                    if (valuesByKey.containsKey(oldStorePrefix + "-password")) {
                        trustStoreRenames.put(oldStorePrefix + "-password", newStorePrefix + ".password");
                    }
                    if (valuesByKey.containsKey(oldStorePrefix + "-type")) {
                        keysToDelete.add(oldStorePrefix + "-type");
                    }
                }
                if (verifiersToMigrate.isEmpty()) {
                    return f;
                }

                List<Properties.Content> newContent = new ArrayList<>();
                for (Properties.Content content : f.getContent()) {
                    if (!(content instanceof Properties.Entry)) {
                        newContent.add(content);
                        continue;
                    }
                    Properties.Entry entry = (Properties.Entry) content;
                    String clientName = verifiersToMigrate.get(entry.getKey());
                    if (clientName != null) {
                        Matcher matcher = HOSTNAME_VERIFIER_PATTERN.matcher(entry.getKey());
                        matcher.matches();
                        String prefix = matcher.group(1) != null ? matcher.group(1) : "";
                        newContent.add(entry
                                .withKey(prefix + "quarkus.tls." + clientName + ".hostname-verification-algorithm")
                                .withValue(entry.getValue().withText("NONE")));
                        newContent.add(entry
                                .withId(Tree.randomId())
                                .withPrefix("\n")
                                .withKey(prefix + "quarkus.cxf.client." + clientName + ".tls-configuration-name")
                                .withValue(entry.getValue().withText(clientName)));
                    } else if (trustStoreRenames.containsKey(entry.getKey())) {
                        newContent.add(entry.withKey(trustStoreRenames.get(entry.getKey())));
                    } else if (!keysToDelete.contains(entry.getKey())) {
                        newContent.add(entry);
                    }
                }
                return f.withContent(newContent);
            }
        };
    }
}
