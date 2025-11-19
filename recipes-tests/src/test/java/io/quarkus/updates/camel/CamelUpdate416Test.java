package io.quarkus.updates.camel;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

public class CamelUpdate416Test extends org.apache.camel.upgrade.CamelUpdate416From3xTest {

    @Override
    public void defaults(RecipeSpec spec) {
        //let the parser be initialized in the camel parent
        super.defaults(spec);
        //recipe has to be loaded differently
        CamelQuarkusTestUtil.recipe3_30(spec)
                .typeValidationOptions(TypeValidation.none());
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_16.html#_subscription_monitoring_api_changes">camel-milo subscription monitoring API changes</a>
     */
    @Test
    void miloSubscriptionMonitoringApiChanges() {
        //language=java
        rewriteRun(java(
                """
                  import org.apache.camel.component.milo.server.MiloServerComponent;
                  import org.eclipse.milo.opcua.sdk.client.subscriptions.OpcUaMonitoredItem;
                  
                  public class MiloTest {
                  
                      public void test()  {
                          OpcUaMonitoredItem item = null;
                          item.setValueConsumer(dataValue -> {int i = 0;});
                      }
                  }
                  """,
                """
                  import org.apache.camel.component.milo.server.MiloServerComponent;
                  import org.eclipse.milo.opcua.sdk.client.subscriptions.OpcUaMonitoredItem;
                  
                  public class MiloTest {
                  
                      public void test()  {
                          OpcUaMonitoredItem item = null;
                          item.setDataValueListener((item,dataValue) -> {int i = 0;});
                      }
                  }
                  """));
    }


}
