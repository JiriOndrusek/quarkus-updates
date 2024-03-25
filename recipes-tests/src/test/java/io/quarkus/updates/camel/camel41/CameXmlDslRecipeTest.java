package io.quarkus.updates.camel.camel41;

import io.quarkus.updates.camel.CamelQuarkusTestUtil;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.xml.Assertions.xml;

public class CameXmlDslRecipeTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelQuarkusTestUtil.recipe3_8(spec)
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true))
                .typeValidationOptions(TypeValidation.none());
    }

    /**
     * <a href="https://camel.apache.org/manual/camel-4x-upgrade-guide-4_1.html#_xml_and_yaml_dsl">doc</a>
     */
    @Test
    void testXmlDsl() {
        //language=xml
        rewriteRun(xml("""
                <routes xmlns="http://camel.apache.org/schema/spring">
                    <route id="myRoute">
                        <bean name="myBean" type="groovy" beanType="com.foo.MyBean">
                            <script>
                              <!-- groovy code here to create the bean -->
                            </script>
                        </bean>
                    </route>
                </routes>
                                                """, """
                <routes xmlns="http://camel.apache.org/schema/spring">
                    <route id="myRoute">
                        <bean name="myBean" type="com.foo.MyBean" scriptLanguage="groovy">
                            <script>
                              <!-- groovy code here to create the bean -->
                            </script>
                        </bean>
                    </route>
                </routes>                        
                """));
    }
}
