package io.quarkus.updates.camel.camel43.java;

import io.quarkus.updates.camel.AbstractCamelQuarkusJavaVisitor;
import io.quarkus.updates.camel.RecipesUtil;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.tree.Comment;
import org.openrewrite.java.tree.J;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Recipe migrating changes between Camel 4.3 to 4.4, for more details see the
 * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_4.html#_camel_core" >documentation</a>.
 */
@EqualsAndHashCode(callSuper = true)
@Value
public class CamelCoreRecipe extends Recipe {

    private static final String M_DATAFORMAT_UNMARSHALL = "org.apache.camel.spi.DataFormat unmarshal(org.apache.camel.Exchange, java.io.InputStream)";

    @Override
    public String getDisplayName() {
        return "Camel Core changes";
    }

    @Override
    public String getDescription() {
        return "Apache Camel Core migration from version 4.3 to 4.4.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {

        return new AbstractCamelQuarkusJavaVisitor() {

            @Override
            protected J.MethodInvocation doVisitMethodInvocation(J.MethodInvocation method, ExecutionContext context) {
                J.MethodInvocation mi = super.doVisitMethodInvocation(method, context);

                if (getMethodMatcher(M_DATAFORMAT_UNMARSHALL).matches(mi, false)) {
                    //put the comment for users
                    mi = mi.withComments(Collections.singletonList(RecipesUtil.createMultinlineComment("Changed exception thrown from IOException to Exception.")));
                }

                return mi;
            }
        };
    }
}