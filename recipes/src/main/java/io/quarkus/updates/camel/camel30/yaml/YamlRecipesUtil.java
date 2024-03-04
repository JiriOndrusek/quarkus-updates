package io.quarkus.updates.camel.camel30.yaml;

import org.openrewrite.Cursor;
import org.openrewrite.yaml.tree.Yaml;

import java.util.Iterator;

public class YamlRecipesUtil {

    static String getProperty(Cursor cursor) {
        StringBuilder asProperty = new StringBuilder();
        Iterator<Object> path = cursor.getPath();
        int i = 0;
        while (path.hasNext()) {
            Object next = path.next();
            if (next instanceof Yaml.Mapping.Entry) {
                Yaml.Mapping.Entry entry = (Yaml.Mapping.Entry) next;
                if (i++ > 0) {
                    asProperty.insert(0, '.');
                }
                asProperty.insert(0, entry.getKey().getValue());
            }
        }
        return asProperty.toString();
    }
}
