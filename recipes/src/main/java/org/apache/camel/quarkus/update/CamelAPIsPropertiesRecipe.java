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

@EqualsAndHashCode(callSuper = true)
@Value
public class CamelAPIsPropertiesRecipe extends Recipe {
    private static final String MATCHER_CONTEXT_GET_ENDPOINT_MAP = "org.apache.camel.CamelContext getEndpointMap()";
    private static final String MATCHER_CONTEXT_GET_EXT = "org.apache.camel.CamelContext getExtension(java.lang.Class)";
    private static final String MATCHER_GET_NAME_RESOLVER = "org.apache.camel.ExtendedCamelContext getComponentNameResolver()";
    private static final String M_PRODUCER_TEMPLATE_ASYNC_CALLBACK = "org.apache.camel.ProducerTemplate asyncCallback(..)";
    private static final String M_CONTEXT_ADAPT = "org.apache.camel.CamelContext adapt(java.lang.Class)";
    private static final String M_CONTEXT_SET_DUMP_ROUTES = "org.apache.camel.CamelContext setDumpRoutes(java.lang.Boolean)";
    private static final String M_CONTEXT_IS_DUMP_ROUTES = "org.apache.camel.CamelContext isDumpRoutes()";
    private static final String M_EXCHANGE_ADAPT = "org.apache.camel.Exchange adapt(java.lang.Class)";
    private static final String M_EXCHANGE_GET_PROPERTY = "org.apache.camel.Exchange getProperty(org.apache.camel.ExchangePropertyKey)";
    private static final String M_EXCHANGE_REMOVE_PROPERTY = "org.apache.camel.Exchange removeProperty(org.apache.camel.ExchangePropertyKey)";
    //TODO should work (*,*) or (org.apache.camel.ExchangePropertyKey,*)
    private static final String M_EXCHANGE_SET_PROPERTY = "org.apache.camel.Exchange setProperty(..)";
    private static final String M_CATALOG_ARCHETYPE_AS_XML = "org.apache.camel.catalog.CamelCatalog archetypeCatalogAsXml()";


    @Override
    public String getDisplayName() {
        return "Camel API changes";
    }

    @Override
    public String getDescription() {
        return "Camel API changes.";
    }


    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {

        return new PropertiesIsoVisitor<ExecutionContext>() {


            @Override
            public Properties.Entry visitEntry(Properties.Entry entry, ExecutionContext context) {
                Properties.Entry e = super.visitEntry(entry, context);

                if("camel.threadpool.rejectedPolicy".equals(e.getKey()) && "DiscardOldest".equals(e.getValue().getText())) {
                    return e.withPrefix(String.format("\n#'ThreadPoolRejectedPolicy.%s' has been removed, consider using 'Abort'. ", e.getKey()));
                }

                return e;
            }


        };
    }

}

