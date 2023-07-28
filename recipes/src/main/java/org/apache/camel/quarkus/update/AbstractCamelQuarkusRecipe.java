package org.apache.camel.quarkus.update;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.properties.PropertiesIsoVisitor;
import org.openrewrite.properties.tree.Properties;

import java.util.function.Supplier;

/**
 * Part of the API changes is - removed Discard and DiscardOldest from org.apache.camel.util.concurrent.ThreadPoolRejectedPolicy.
 * <p>
 *     Bith options could be used as a proprtty in application.properties.
 * </p>
 */
public abstract class AbstractCamelQuarkusRecipe extends Recipe {

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return super.getVisitor();
    }
}

