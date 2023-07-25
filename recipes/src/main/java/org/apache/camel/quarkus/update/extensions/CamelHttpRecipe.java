package org.apache.camel.quarkus.update.extensions;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.apache.camel.quarkus.update.AbstractCamelVisitor;
import org.apache.http.auth.AuthScope;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.ChangePackage;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;

@EqualsAndHashCode(callSuper = true)
@Value
public class CamelHttpRecipe extends Recipe {

    private static final String SET_CREDENTIALS = "org.apache.http.impl.client.BasicCredentialsProvider setCredentials(..)";
    private static final String SCOPE_ANY = "AuthScope.ANY";

    @Override
    public String getDisplayName() {
        return "Camel Http Extension changes";
    }

    @Override
    public String getDescription() {
        return "Camel Http Extension changes.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {

        return new AbstractCamelVisitor() {
            @Override
            protected J.Import doVisitImport(J.Import _import, ExecutionContext context) {
                doAfterVisit( new ChangePackage("org.apache.http.HttpHost", "org.apache.hc.core5.http.HttpHost", null));
                doAfterVisit( new ChangePackage("org.apache.http.client.protocol.HttpClientContext", "org.apache.hc.client5.http.protocol.HttpClientContext", null));
                doAfterVisit( new ChangePackage("org.apache.http.impl.client", "org.apache.hc.client5.http.impl.auth", null));
                doAfterVisit( new ChangePackage("org.apache.http.protocol.HttpContext", "org.apache.hc.core5.http.protocol.HttpContext", null));
                doAfterVisit( new ChangePackage("org.apache.http", "org.apache.hc.client5.http", null));

                return super.doVisitImport(_import, context);
            }

            @Override
            protected J.FieldAccess doVisitFieldAccess(J.FieldAccess fieldAccess, ExecutionContext context) {
                J.FieldAccess f = super.doVisitFieldAccess(fieldAccess, context);

                if("ANY".equals(f.getSimpleName()) && AuthScope.class.getCanonicalName().equals(f.getType().toString())) {
                    JavaTemplate.Builder templateBuilder = JavaTemplate.builder(this::getCursor, "new AuthScope(null, -1)")
                            .imports("org.apache.hc.client5.http.auth.AuthScope");
                    J.NewClass nc = f.withTemplate(
                                        templateBuilder.build(),
                                        f.getCoordinates().replace())
                                     .withPrefix(f.getPrefix()
                    );
                    getCursor().putMessage("newClass", nc);
                }
                return f;
            }

            @Override
            public J.NewClass visitNewClass(J.NewClass newClass, ExecutionContext context) {
                return super.visitNewClass(newClass, context);
            }


            @Override
            public @Nullable J postVisit(J tree, ExecutionContext context) {
                J j = super.postVisit(tree, context);

                J.NewClass newClass = getCursor().getMessage("newClass");
                if(newClass != null) {
                    return newClass;
                }

                return j;
            }

        };
    }

}