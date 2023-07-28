package org.apache.camel.quarkus.update.yaml;

import org.openrewrite.ExecutionContext;
import org.openrewrite.yaml.YamlIsoVisitor;
import org.openrewrite.yaml.tree.Yaml;

/**
 * TODO add constraint to runthis recipe only of project contains Camel.
 */
public abstract class CamelYamlVisitor extends YamlIsoVisitor<ExecutionContext> {

    /**
     * Method is called before start of visiting a new document.
     * Implementations might need to clear all local state from previous document.
     */
    abstract void clearLocalCache();

    @Override
    public Yaml.Document visitDocument(Yaml.Document document, ExecutionContext o) {
        clearLocalCache();
        Yaml.Document d =  super.visitDocument(document, o);
        return d;
    }

    @Override
    public Yaml.Documents visitDocuments(Yaml.Documents documents, ExecutionContext context) {
        boolean visited = context.getMessage(CamelYamlVisitor.class.getSimpleName() + "_visited", false);
        if(!visited) {
            context.putMessage(CamelYamlVisitor.class.getSimpleName() + "_visited", true);
            documents = super.visitDocuments(documents, context);
        }
        return documents;
    }
}
