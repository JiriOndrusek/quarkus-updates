package org.apache.camel.quarkus.update.java;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.AddImport;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;

public class CamelEIPRecipe extends Recipe {

    @Override
    public String getDisplayName() {
        return "Replaces removed method camel EIP";
    }

    @Override
    public String getDescription() {
        return "The InOnly and InOut EIPs has been removed. Instead, use SetExchangePattern or To where you can specify exchange pattern to use.";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new AbstractCamelVisitor() {

            @Override
            protected J.MethodInvocation doVisitMethodInvocation(J.MethodInvocation method, ExecutionContext context) {
                J.MethodInvocation mi =  super.doVisitMethodInvocation(method, context);

                if (mi.getSimpleName().equals("inOut") || mi.getSimpleName().equals("inOnly")) {
                    String name = mi.getSimpleName().substring(0, 1).toUpperCase() + mi.getSimpleName().substring(1);
                    mi = mi.withName(mi.getName().withSimpleName("setExchangePattern(ExchangePattern."+name+").to"));
                    doAfterVisit(new AddImport<>("org.apache.camel.ExchangePattern", null, false));
                }
                return mi;
            }

        };

    };

}
