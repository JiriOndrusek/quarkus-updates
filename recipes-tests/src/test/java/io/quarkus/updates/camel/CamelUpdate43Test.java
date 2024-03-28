package io.quarkus.updates.camel;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.properties.Assertions;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.xml.Assertions.xml;

public class CamelUpdate43Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        CamelQuarkusTestUtil.recipe3_8(spec)
                .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true).classpath("camel-api", "camel-support", "camel-base-engine"))
                .typeValidationOptions(TypeValidation.none());
    }

    /**
     * <p>Moved class org.apache.camel.impl.engine.MemoryStateRepository from
     * camel-base-engine to org.apache.camel.support.processor.state.MemoryStateRepository in camel-support.</p>
     *
     * <p></p>Moved class org.apache.camel.impl.engine.FileStateRepository
     * from camel-base-engine to org.apache.camel.support.processor.state.FileStateRepository in camel-support.</p>
     *
     * <p>See the <a href=https://camel.apache.org/manual/camel-4x-upgrade-guide-4_3.html#_camel_core>documentation</a></p>
     */
    @Test
    void testStateRepository() {
        //language=java
        rewriteRun(java("""
                    import org.apache.camel.BindToRegistry;
                    import org.apache.camel.impl.engine.FileStateRepository;
                    import org.apache.camel.impl.engine.MemoryStateRepository;
                    
                    import java.io.File;
                    
                    public class CoreTest {
                    
                        @BindToRegistry("stateRepository")
                        private static final MemoryStateRepository stateRepository = new MemoryStateRepository();
                    
                        // Create the repository in which the Kafka offsets will be persisted
                        FileStateRepository repository = FileStateRepository.fileStateRepository(new File("/path/to/repo.dat"));
                    }
                """,
                """
                    import org.apache.camel.BindToRegistry;
                    import org.apache.camel.support.processor.state.FileStateRepository;
                    import org.apache.camel.support.processor.state.MemoryStateRepository;
                    
                    import java.io.File;
                    
                    public class CoreTest {
                    
                        @BindToRegistry("stateRepository")
                        private static final MemoryStateRepository stateRepository = new MemoryStateRepository();
                    
                        // Create the repository in which the Kafka offsets will be persisted
                        FileStateRepository repository = FileStateRepository.fileStateRepository(new File("/path/to/repo.dat"));
                    }
                        """));
    }

    /**
     * <p>The configuration for batch and stream has been renamed from batch-config to batchConfig and stream-config to streamConfig.</p>
     *
     *  <p>For example before:
     * <pre>
     *     &lt;resequence&gt;
     *         &lt;stream-config timeout=&quot;1000&quot; deliveryAttemptInterval=&quot;10&quot;/&gt;
     *         &lt;simple&gt;${header.seqnum}&lt;/simple&gt;
     *         &lt;to uri=&quot;mock:result&quot; /&gt;
     *     &lt;/resequence&gt;
     * </pre>
     * </p>
     *
     * <p>And now after:
     * <pre>
     *     &lt;resequence&gt;
     *         &lt;streamConfig timeout=&quot;1000&quot; deliveryAttemptInterval=&quot;10&quot;/&gt;
     *         &lt;simple&gt;${header.seqnum}&lt;/simple&gt;
     *         &lt;to uri=&quot;mock:result&quot; /&gt;
     *     &lt;/resequence&gt;
     * </pre>
     * </p>
     *
     * <p>See the <a href=https://camel.apache.org/manual/camel-4x-upgrade-guide-4_3.html#_resequence_eip>documentation</a></p>
     */
    @Test
    void testResequenceStramConfig() {
        //language=xml
        rewriteRun(xml("""
                    <routes>
                        <route>
                            <from uri="direct:start"/>
                            <resequence>
                                <stream-config timeout="1000" deliveryAttemptInterval="10"/>
                                <simple>${header.seqnum}</simple>
                                <to uri="mock:result" />
                            </resequence>
                        </route>
                    </routes>
                                                """, """
                    <routes>
                        <route>
                            <from uri="direct:start"/>
                            <resequence>
                                <streamConfig timeout="1000" deliveryAttemptInterval="10"/>
                                <simple>${header.seqnum}</simple>
                                <to uri="mock:result" />
                            </resequence>
                        </route>
                    </routes>
                """));
        }

    /**
     * <p>The configuration for batch and stream has been renamed from batch-config to batchConfig and stream-config to streamConfig.</p>
     * <p>See the <a href=https://camel.apache.org/manual/camel-4x-upgrade-guide-4_3.html#_resequence_eip>documentation</a></p>
     */
    @Test
    void testResequenceBatchConfig() {
        //language=xml
        rewriteRun(xml("""
                    <camelContext id="camel" xmlns="http://camel.apache.org/schema/spring"> 
                        <route>     
                            <from uri="direct:start" /> 
                            <resequence> 
                                <simple>body</simple> 
                                <to uri="mock:result" />
                                <batch-config batchSize="300" batchTimeout="4000" /> 
                            </resequence>
                         </route> 
                     </camelContext>
                                                """, """
                    <camelContext id="camel" xmlns="http://camel.apache.org/schema/spring"> 
                        <route>     
                            <from uri="direct:start" /> 
                            <resequence> 
                                <simple>body</simple> 
                                <to uri="mock:result" />
                                <batchConfig batchSize="300" batchTimeout="4000" /> 
                            </resequence>
                         </route> 
                     </camelContext>
                """));
    }

}
