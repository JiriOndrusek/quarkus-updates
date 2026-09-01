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
import java.util.Arrays;
import java.util.Collections;
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
    private static final Pattern TRUST_STORE_PATTERN = Pattern
            .compile("(%[^.]+\\.)?quarkus\\.cxf\\.client\\.([^.]+)\\.(trust-store|trust-store-password|trust-store-type)");
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
                "at runtime). Every `hostname-verifier` entry is decided per profile scope: it is only migrated when the " +
                "effective `http-conduit-factory` (per client pin over global, profile scoped pin over default scoped) is " +
                "the Vert.x one in every profile the entry applies to (`hostname-verifier` keeps working on the " +
                "`URLConnectionHTTPConduitFactory` conduit, where `hostname-verification-algorithm` is unsupported), and " +
                "when no key store, no already present `tls-configuration-name` and no differently scoped `trust-store*` " +
                "option interferes in an overlapping profile. Entries with an unrecognized trust store type are also " +
                "left untouched.";
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

    /** A property value together with the profile scope of its key. */
    private static final class ScopedValue {
        final String prefix;
        final Set<String> profiles;
        final String value;

        ScopedValue(String prefix, String value) {
            this.prefix = prefix == null ? "" : prefix;
            this.profiles = profilesOf(this.prefix);
            this.value = value;
        }
    }

    /** Profile prefix and client name captured from a migrated hostname-verifier key. */
    private static final class MigrationTarget {
        final String prefix;
        final String clientName;

        MigrationTarget(String prefix, String clientName) {
            this.prefix = prefix;
            this.clientName = clientName;
        }
    }

    /** {@code ""} is the default scope (empty set), {@code "%dev,test."} the {dev, test} scope. */
    private static Set<String> profilesOf(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(Arrays.asList(prefix.substring(1, prefix.length() - 1).split(",")));
    }

    /** The default scope applies in every profile, so it overlaps everything. */
    private static boolean overlaps(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) {
            return true;
        }
        for (String profile : a) {
            if (b.contains(profile)) {
                return true;
            }
        }
        return false;
    }

    private static boolean anyOverlapping(List<ScopedValue> scopedValues, Set<String> scope) {
        if (scopedValues == null) {
            return false;
        }
        for (ScopedValue scopedValue : scopedValues) {
            if (overlaps(scopedValue.profiles, scope)) {
                return true;
            }
        }
        return false;
    }

    private static boolean anyForeignOverlapping(List<ScopedValue> scopedValues, Set<String> scope, String prefix) {
        if (scopedValues == null) {
            return false;
        }
        for (ScopedValue scopedValue : scopedValues) {
            if (!scopedValue.prefix.equals(prefix) && overlaps(scopedValue.profiles, scope)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The hostname-verifier entry applies in every profile of its scope - for the default scope also in every
     * profile that only overrides the conduit. {@code hostname-verification-algorithm} is supported by the
     * Vert.x conduit only, so the entry may be migrated when the effective conduit is the Vert.x one in each
     * of those profiles.
     */
    private static boolean isVertxConduitEverywhere(Set<String> scope, List<ScopedValue> clientPins,
            List<ScopedValue> globalPins) {
        Set<String> profilesToCheck = new HashSet<>(scope);
        if (scope.isEmpty()) {
            collectProfiles(clientPins, profilesToCheck);
            collectProfiles(globalPins, profilesToCheck);
            if (!isVertxConduitIn(null, clientPins, globalPins)) {
                return false;
            }
        }
        for (String profile : profilesToCheck) {
            if (!isVertxConduitIn(profile, clientPins, globalPins)) {
                return false;
            }
        }
        return true;
    }

    private static void collectProfiles(List<ScopedValue> pins, Set<String> collected) {
        if (pins == null) {
            return;
        }
        for (ScopedValue pin : pins) {
            collected.addAll(pin.profiles);
        }
    }

    /** Effective conduit of one profile ({@code null} is the default profile): the client pin wins over the global one. */
    private static boolean isVertxConduitIn(String profile, List<ScopedValue> clientPins, List<ScopedValue> globalPins) {
        String value = pinnedConduit(profile, clientPins);
        if (value == null) {
            value = pinnedConduit(profile, globalPins);
        }
        // no pin: the default conduit is the Vert.x one since 3.16.0
        return value == null || isVertxConduit(value);
    }

    /** A profile scoped pin overrides the default scoped one. */
    private static String pinnedConduit(String profile, List<ScopedValue> pins) {
        if (pins == null) {
            return null;
        }
        String defaultScoped = null;
        for (ScopedValue pin : pins) {
            if (pin.profiles.isEmpty()) {
                defaultScoped = pin.value;
            } else if (profile != null && pin.profiles.contains(profile)) {
                return pin.value;
            }
        }
        return defaultScoped;
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

                List<ScopedValue> globalConduits = new ArrayList<>();
                Map<String, List<ScopedValue>> clientConduits = new HashMap<>();
                Map<String, List<ScopedValue>> keyStores = new HashMap<>();
                Map<String, List<ScopedValue>> trustStores = new HashMap<>();
                Map<String, List<ScopedValue>> tlsConfigurationNames = new HashMap<>();
                for (Map.Entry<String, String> e : valuesByKey.entrySet()) {
                    Matcher matcher = GLOBAL_CONDUIT_PATTERN.matcher(e.getKey());
                    if (matcher.matches()) {
                        globalConduits.add(new ScopedValue(matcher.group(1), e.getValue()));
                        continue;
                    }
                    matcher = CLIENT_CONDUIT_PATTERN.matcher(e.getKey());
                    if (matcher.matches()) {
                        clientConduits.computeIfAbsent(matcher.group(2), k -> new ArrayList<>())
                                .add(new ScopedValue(matcher.group(1), e.getValue()));
                        continue;
                    }
                    matcher = KEY_STORE_PATTERN.matcher(e.getKey());
                    if (matcher.matches()) {
                        keyStores.computeIfAbsent(matcher.group(2), k -> new ArrayList<>())
                                .add(new ScopedValue(matcher.group(1), e.getValue()));
                        continue;
                    }
                    matcher = TRUST_STORE_PATTERN.matcher(e.getKey());
                    if (matcher.matches()) {
                        trustStores.computeIfAbsent(matcher.group(2), k -> new ArrayList<>())
                                .add(new ScopedValue(matcher.group(1), e.getValue()));
                        continue;
                    }
                    matcher = TLS_CONFIGURATION_NAME_PATTERN.matcher(e.getKey());
                    if (matcher.matches()) {
                        tlsConfigurationNames.computeIfAbsent(matcher.group(2), k -> new ArrayList<>())
                                .add(new ScopedValue(matcher.group(1), e.getValue()));
                    }
                }

                // key of the hostname-verifier entry -> its captured profile prefix and client name
                Map<String, MigrationTarget> verifiersToMigrate = new HashMap<>();
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
                    Set<String> scope = profilesOf(prefix);

                    if (anyOverlapping(keyStores.get(clientName), scope)
                            || anyOverlapping(tlsConfigurationNames.get(clientName), scope)) {
                        // mapping a key store automatically is not safe, an existing tls-configuration-name is
                        // an already migrated or hand written setup; both block every profile they overlap with
                        continue;
                    }
                    if (!isVertxConduitEverywhere(scope, clientConduits.get(clientName), globalConduits)) {
                        // hostname-verifier keeps working on a non-Vert.x conduit in at least one profile the
                        // entry applies to, and hostname-verification-algorithm would fail at runtime there
                        continue;
                    }
                    if (anyForeignOverlapping(trustStores.get(clientName), scope, prefix)) {
                        // a trust store of another overlapping scope cannot be moved together with this entry
                        // and would clash with the generated tls-configuration-name at runtime
                        continue;
                    }
                    String oldStorePrefix = prefix + "quarkus.cxf.client." + clientName + ".trust-store";
                    String extension = storeExtension(valuesByKey.get(oldStorePrefix + "-type"), valuesByKey.get(oldStorePrefix));
                    if (extension == null) {
                        // unrecognized trust-store-type, do not touch this client
                        continue;
                    }
                    verifiersToMigrate.put(e.getKey(), new MigrationTarget(prefix, clientName));
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
                    MigrationTarget target = verifiersToMigrate.get(entry.getKey());
                    if (target != null) {
                        newContent.add(entry
                                .withKey(target.prefix + "quarkus.tls." + target.clientName + ".hostname-verification-algorithm")
                                .withValue(entry.getValue().withText("NONE")));
                        newContent.add(entry
                                .withId(Tree.randomId())
                                .withPrefix("\n")
                                .withKey(target.prefix + "quarkus.cxf.client." + target.clientName + ".tls-configuration-name")
                                .withValue(entry.getValue().withText(target.clientName)));
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
