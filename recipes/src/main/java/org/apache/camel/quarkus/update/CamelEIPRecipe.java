package org.apache.camel.quarkus.update;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.apache.camel.CamelContext;
import org.apache.camel.Category;
import org.apache.camel.builder.SimpleBuilder;
import org.apache.camel.main.MainListener;
import org.apache.camel.spi.OnCamelContextStart;
import org.apache.camel.spi.OnCamelContextStop;
import org.apache.camel.support.IntrospectionSupport;
import org.apache.camel.util.concurrent.ThreadPoolRejectedPolicy;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.ImplementInterface;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.RemoveImplements;
import org.openrewrite.java.tree.Comment;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.marker.Markers;

import java.beans.SimpleBeanInfo;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Value
@EqualsAndHashCode(callSuper = true)
public class CamelEIPRecipe extends Recipe {

    @Override
    public String getDisplayName() {
        return "Adjust all-open directives in Kotlin plugin configuration";
    }

    @Override
    public String getDescription() {
        return "Adjust all-open directives in Kotlin plugin configuration";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {

        return new JavaIsoVisitor<>() {

            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext context) {
                J.MethodInvocation mi =  super.visitMethodInvocation(method, context);

                if("inOut".equals(mi.getName().getSimpleName())){
                    return mi.withName(mi.getName().withSimpleName("setExchangePattern(ExchangePattern.InOut)\n.to"));
                }

                return mi;
            }
        };
    }
}
