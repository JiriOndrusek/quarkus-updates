package org.apache.camel.quarkus.update.v3_0;

import io.quarkus.devtools.codestarts.extension.QuarkusExtensionCodestartCatalog;
import io.quarkus.devtools.codestarts.extension.QuarkusExtensionCodestartProjectInput;
import io.quarkus.devtools.codestarts.extension.QuarkusExtensionCodestartProjectInputBuilder;
import io.quarkus.devtools.testing.SnapshotTesting;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInfo;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.quarkus.devtools.testing.SnapshotTesting.assertThatDirectoryTreeMatchSnapshots;

public class Test {


    private static final Path testDirPath = Paths.get("target/cq");

    @BeforeAll
    static void setUp() throws IOException {
        SnapshotTesting.deleteTestDirectory(testDirPath.resolve("yaml").toFile());
    }




    @org.junit.jupiter.api.Test
    void generateDefaultProject(TestInfo testInfo) throws Throwable {
        final QuarkusExtensionCodestartProjectInput input = prepareInput()
                .build();
        final Path projectDir = testDirPath.resolve("yaml");
        getCatalog().createProject(input).generate(projectDir);
        assertThatDirectoryTreeMatchSnapshots(testInfo, projectDir);
    }

//    @org.junit.jupiter.api.Test
//    void generateProjectWithoutTests(TestInfo testInfo) throws Throwable {
//        final QuarkusExtensionCodestartProjectInput input = prepareInput()
//                .withoutDevModeTest(true)
//                .withoutIntegrationTests(true)
//                .withoutUnitTest(true)
//                .build();
//        final Path projectDir = testDirPath.resolve("without-tests");
//        getCatalog().createProject(input).generate(projectDir);
//        assertThatDirectoryTreeMatchSnapshots(testInfo, projectDir);
//    }
    private QuarkusExtensionCodestartProjectInputBuilder prepareInput() {
        return QuarkusExtensionCodestartProjectInput.builder()
                .putData(QuarkusExtensionCodestartCatalog.QuarkusExtensionData.GROUP_ID, "org.extension")
                .putData(QuarkusExtensionCodestartCatalog.QuarkusExtensionData.NAMESPACE_ID, "quarkus-")
                .putData(QuarkusExtensionCodestartCatalog.QuarkusExtensionData.NAMESPACE_NAME, "Quarkus -")
                .putData(QuarkusExtensionCodestartCatalog.QuarkusExtensionData.EXTENSION_ID, "my-extension")
                .putData(QuarkusExtensionCodestartCatalog.QuarkusExtensionData.EXTENSION_NAME, "My Extension")
                .putData(QuarkusExtensionCodestartCatalog.QuarkusExtensionData.VERSION, "1.0.0-SNAPSHOT")
                .putData(QuarkusExtensionCodestartCatalog.QuarkusExtensionData.PACKAGE_NAME, "org.extension")
                .putData(QuarkusExtensionCodestartCatalog.QuarkusExtensionData.CLASS_NAME_BASE, "MyExtension");
    }

    private QuarkusExtensionCodestartCatalog getCatalog() throws IOException {
        return QuarkusExtensionCodestartCatalog.fromBaseCodestartsResources();
    }

}
