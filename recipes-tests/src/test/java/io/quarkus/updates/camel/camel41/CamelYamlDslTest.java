package io.quarkus.updates.camel.camel41;

import io.quarkus.updates.camel.CamelQuarkusTestUtil;
import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;
import org.openrewrite.yaml.Assertions;

public class CamelYamlDslTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelQuarkusTestUtil.recipe3_8(spec)
                .typeValidationOptions(TypeValidation.none());
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_1.html#_xml_and_yaml_dsl">doc</a>
     */
    @Test
    void testYamlDsl() {
        //language=yaml
        rewriteRun(Assertions.yaml("""
                - beans:
                  - name: "myClient"
                    beanType: "com.foo.MyBean"
                    type: "groovy"
                    script: |
                      # groovy script here
                """, """
                - beans:
                  - name: "myClient"
                    type: "com.foo.MyBean"
                    scriptLanguage: "groovy"
                    script: |
                      # groovy script here
                """));
    }

}
