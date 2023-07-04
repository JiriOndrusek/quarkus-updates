package org.apache.camel.quarkus.update;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Space;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;
import java.util.regex.Pattern;

public class CamelAPIsRecipe extends Recipe {
    @Override
    public String getDisplayName() {
        return "Camel API changes.";
    }

    @Override
    public String getDescription() {
        return "Camel API changes";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<>() {

            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext executionContext) {
                return super.visitMethodInvocation(method,executionContext);
            }

            @Override
            public J.FieldAccess visitFieldAccess(J.FieldAccess fieldAccess, ExecutionContext executionContext) {
                J.FieldAccess fa =  super.visitFieldAccess(fieldAccess, executionContext);
                //The org.apache.camel.ExchangePattern has removed InOptionalOut.
                if("InOptionalOut".equals(fieldAccess.getSimpleName()) && fieldAccess.getType().isAssignableFrom(Pattern.compile("org.apache.camel.ExchangePattern"))) {

                        return fa.withName(new J.Identifier(UUID.randomUUID(), fa.getPrefix(), Markers.EMPTY, "/* " + fa.getSimpleName() + " has been removed */", fa.getType(), null));
                }


                return fa;
            }

            @Override
            public Expression visitExpression(Expression expression, ExecutionContext executionContext) {
                return super.visitExpression(expression, executionContext);
            }
        };
    }
}

