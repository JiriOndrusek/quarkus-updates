package org.apache.camel.quarkus.update.properties;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.apache.camel.quarkus.update.RecipesUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.properties.PropertiesIsoVisitor;
import org.openrewrite.properties.tree.Properties;

/**
 * Part of the API changes is - removed Discard and DiscardOldest from org.apache.camel.util.concurrent.ThreadPoolRejectedPolicy.
 * <p>
 *     Bith options could be used as a proprtty in application.properties.
 * </p>
 */
@EqualsAndHashCode(callSuper = true)
@Value
public class CamelQuarkusAPIsPropertiesRecipe extends Recipe {

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

                if(!RecipesUtil.isCamelPresent(context)) {
                    //skipping as the project does not contain camel dependencies
                    return  e;
                };

                if("camel.threadpool.rejectedPolicy".equals(e.getKey()) &&
                        ("DiscardOldest".equals(e.getValue().getText()) || "Discard".equals(e.getValue().getText()))) {
                    return e.withPrefix(String.format("\n#'ThreadPoolRejectedPolicy.%s' has been removed, consider using 'Abort'. ", e.getKey()));
                }

                return e;
            }


        };
    }

}

