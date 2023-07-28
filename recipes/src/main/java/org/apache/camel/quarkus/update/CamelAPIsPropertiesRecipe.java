package org.apache.camel.quarkus.update;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.apache.camel.CamelContext;
import org.apache.camel.Category;
import org.apache.camel.ExchangePropertyKey;
import org.apache.camel.ExtendedCamelContext;
import org.apache.camel.ExtendedExchange;
import org.apache.camel.builder.SimpleBuilder;
import org.apache.camel.main.MainListener;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.spi.OnCamelContextStart;
import org.apache.camel.spi.OnCamelContextStop;
import org.apache.camel.support.IntrospectionSupport;
import org.apache.camel.util.concurrent.ThreadPoolRejectedPolicy;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.AddImport;
import org.openrewrite.java.ImplementInterface;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.RemoveImplements;
import org.openrewrite.java.tree.Comment;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Space;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.marker.Markers;
import org.openrewrite.properties.PropertiesIsoVisitor;
import org.openrewrite.properties.tree.Properties;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.beans.SimpleBeanInfo;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Part of the API changes is - removed Discard and DiscardOldest from org.apache.camel.util.concurrent.ThreadPoolRejectedPolicy.
 * <p>
 *     Bith options could be used as a proprtty in application.properties.
 * </p>
 */
@EqualsAndHashCode(callSuper = true)
@Value
public class CamelAPIsPropertiesRecipe extends Recipe {

    @Override
    public String getDisplayName() {
        return "Camel API changes in application.properties";
    }

    @Override
    public String getDescription() {
        return "Apache Camel API migration from version 3.20 or higher to 4.0. Removal of deprecated APIs, which could be part of the application.properties.";
    }

    @Override
    public PropertiesIsoVisitor getVisitor() {

        return new PropertiesIsoVisitor<ExecutionContext>() {


            @Override
            public Properties.Entry visitEntry(Properties.Entry entry, ExecutionContext context) {
                Properties.Entry e = super.visitEntry(entry, context);

                if("camel.threadpool.rejectedPolicy".equals(e.getKey()) &&
                        ("DiscardOldest".equals(e.getValue().getText()) || "Discard".equals(e.getValue().getText()))) {
                    return e.withPrefix(String.format("\n#'ThreadPoolRejectedPolicy.%s' has been removed, consider using 'Abort'. ", e.getKey()));
                }

                return e;
            }


        };
    }

}

