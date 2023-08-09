package org.apache.camel.quarkus.update.v3_0;

import io.quarkus.devtools.codestarts.quarkus.QuarkusCodestartCatalog;
import io.quarkus.devtools.codestarts.quarkus.QuarkusCodestartProjectInput;
import io.quarkus.devtools.testing.PlatformAwareTestBase;
import io.quarkus.devtools.testing.SnapshotTesting;
import io.quarkus.devtools.testing.codestarts.QuarkusCodestartTest;
import io.quarkus.devtools.testing.codestarts.QuarkusCodestartTesting;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static io.quarkus.devtools.testing.FakeExtensionCatalog.FAKE_QUARKUS_CODESTART_CATALOG;
import static io.quarkus.devtools.testing.SnapshotTesting.checkContains;
import static io.quarkus.devtools.testing.SnapshotTesting.checkMatches;
import static io.quarkus.platform.tools.ToolsConstants.PROP_COMPILER_PLUGIN_VERSION;
import static io.quarkus.platform.tools.ToolsConstants.PROP_SUREFIRE_PLUGIN_VERSION;
import static org.assertj.core.api.Assertions.assertThat;

public class CamelQuarkusYamlTest {

    private static final Path testDirPath = Paths.get("target/cq");

    private Map<String, Object> getGenerationTestInputData() {
        return QuarkusCodestartTesting.getMockedTestInputData(Collections.emptyMap());
    }
    @BeforeAll
    static void setUp() throws Throwable {
        SnapshotTesting.deleteTestDirectory(testDirPath.toFile());
    }
    @Test
    public void test() throws Throwable {

        final Path projectDir = testDirPath.resolve("yaml");

        final QuarkusCodestartProjectInput input = QuarkusCodestartProjectInput.builder()
                .noCode()
                .noDockerfiles()
                .noBuildToolWrapper()
                .addData(getGenerationTestInputData())
                .addBoms(QuarkusCodestartTesting.getPlatformBoms())
                .build();
        getCatalog().createProject(input).generate(projectDir);


        SnapshotTesting.assertThatMatchSnapshot(projectDir, "yaml");
    }

    private QuarkusCodestartCatalog getCatalog() {
        return FAKE_QUARKUS_CODESTART_CATALOG;
    }
}
