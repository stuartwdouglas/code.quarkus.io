package io.quarkus.code.rest

import io.quarkus.code.model.CodeQuarkusExtension
import io.quarkus.code.model.ProjectDefinition
import io.quarkus.code.model.PublicConfig
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.web.WebTest
import io.restassured.RestAssured.given
import io.restassured.response.ValidatableResponse
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThan
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import javax.inject.Inject
import javax.ws.rs.core.MediaType

@QuarkusTest
class CodeQuarkusResourceTest {

    @Inject
    lateinit var projectService: QuarkusProjectServiceMock

    @Test
    @WebTest("/api/download")
    fun `Should return a project with default configuration when there is no parameters`(response: ValidatableResponse) {
        response.contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"code-with-quarkus.zip\"")
        assertThat(projectService.createdProjectRef.get(), equalTo(ProjectDefinition()))
    }

    @Test
    @WebTest("/api/download?g=org.acme&a=&pv=1.0.0&c=org.acme.TotoResource&s=98e", status = 400)
    fun `Should fail when artifactId is empty`() {
    }

    @Test
    @DisplayName("Should fail when using invalid groupId")
    @WebTest("/api/download?g=org.acme.&s=98e", status = 400)
    fun testWithInvalidGroupId() {
    }

    @Test
    @DisplayName("Should fail when using invalid artifactId")
    @WebTest("/api/download?a=Art.&s=98e", status = 400)
    fun testWithInvalidArtifactId() {
    }

    @Test
    @DisplayName("Should fail when using invalid path")
    @WebTest("/api/download?p=invalid&s=98e", status = 400)
    fun testWithInvalidPath() {
    }

    @Test
    @DisplayName("Should fail when using invalid className")
    @WebTest("/api/download?c=com.1e&s=98e", status = 400)
    fun testWithInvalidClassName() {
    }

    @Test
    @DisplayName("Should fail when using invalid shortId")
    @WebTest("/api/download?s=inv", status = 400)
    fun testWithInvalidShortId() {
    }

    @Test
    @DisplayName("Should fail when using invalid extensionId")
    @WebTest("/api/download?e=inv", status = 400)
    fun testWithInvalidExtensionId() {
    }

    @Test
    @DisplayName("Should return a project with specified configuration when a few parameters are specified")
    @WebTest("/api/download?a=test-app-with-a-few-arg&v=1.0.0&s=D9x.9Ie")
    fun testWithAFewParams(response: ValidatableResponse) {
        response
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"test-app-with-a-few-arg.zip\"")
        assertThat(
                projectService.getCreatedProject(), equalTo(
                ProjectDefinition(
                        artifactId = "test-app-with-a-few-arg",
                        version = "1.0.0",
                        shortExtensions = "D9x.9Ie"
                ))
        )

    }

    @Test
    @DisplayName("Should return a project with specified configuration when shortIds is empty")
    @WebTest("/api/download?g=org.acme&a=test-empty-shortids&v=1.0.1&b=MAVEN&s=")
    fun testWithEmptyShortIds(response: ValidatableResponse) {
        response
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"test-empty-shortids.zip\"")
        assertThat(
                projectService.getCreatedProject(), equalTo(
                ProjectDefinition(
                        artifactId = "test-empty-shortids",
                        version = "1.0.1"
                ))
        )
    }

    @Test
    @DisplayName("Should return a project with specified configuration when extensions is empty")
    @WebTest("/api/download?g=org.acme&a=test-empty-ext&v=1.0.1&b=MAVEN&c=org.test.ExampleResource&e=")
    fun testWithEmptyExtensions(response: ValidatableResponse) {
        response
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"test-empty-ext.zip\"")
        assertThat(
                projectService.getCreatedProject(), equalTo(
                ProjectDefinition(
                        artifactId = "test-empty-ext",
                        version = "1.0.1",
                        className = "org.test.ExampleResource",
                        extensions = setOf("")
                ))
        )
    }

    @Test
    @DisplayName("Should return a project with the url rewrite")
    @WebTest("/d?g=com.toto&a=test-app&v=1.0.0&p=/toto/titi&c=org.toto.TotoResource&s=7RG.L0j.9Ie")
    fun testWithUrlRewrite(response: ValidatableResponse) {
        response
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"test-app.zip\"")
        assertThat(
                projectService.getCreatedProject(), equalTo(
                ProjectDefinition(
                        groupId = "com.toto",
                        artifactId = "test-app",
                        version = "1.0.0",
                        className = "org.toto.TotoResource",
                        path = "/toto/titi",
                        shortExtensions = "7RG.L0j.9Ie"
                )
        )
        )
    }

    @Test
    @DisplayName("Should return a project with specified configuration when all parameters are specified")
    @WebTest("/api/download?g=com.toto&a=test-app&v=1.0.0&p=/toto/titi&c=org.toto.TotoResource&s=7RG.L0j.9Ie")
    fun testWithAllParams(response: ValidatableResponse) {
        response
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"test-app.zip\"")
        assertThat(
                projectService.getCreatedProject(), equalTo(
                ProjectDefinition(
                        groupId = "com.toto",
                        artifactId = "test-app",
                        version = "1.0.0",
                        className = "org.toto.TotoResource",
                        path = "/toto/titi",
                        shortExtensions = "7RG.L0j.9Ie"
                ))
        )
    }

    @Test
    @DisplayName("Should return a project with specified with old extension syntax")
    @WebTest("/api/download?g=com.toto&a=test-app&v=1.0.0&p=/toto/titi&c=com.toto.TotoResource&e=io.quarkus:quarkus-resteasy&s=9Ie")
    fun testWithOldExtensionSyntaxParams(response: ValidatableResponse) {
        response
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"test-app.zip\"")
        assertThat(
                projectService.getCreatedProject(), equalTo(
                ProjectDefinition(
                        groupId = "com.toto",
                        artifactId = "test-app",
                        version = "1.0.0",
                        className = "com.toto.TotoResource",
                        path = "/toto/titi",
                        extensions = setOf("io.quarkus:quarkus-resteasy"),
                        shortExtensions = "9Ie"
                )
        )
        )
    }

    @Test
    @DisplayName("Should return the default configuration")
    @WebTest("/api/config")
    fun testConfig(conf: PublicConfig) {
        Assertions.assertEquals("dev", conf.environment);
        Assertions.assertNotNull(conf.gitCommitId);
        Assertions.assertNull(conf.gaTrackingId);
        Assertions.assertNull(conf.sentryDSN);
        Assertions.assertNotNull(conf.quarkusVersion);
        Assertions.assertEquals(0, conf.features.size);
    }

    @Test
    @DisplayName("Should return the extension list")
    @WebTest("/api/extensions")
    fun testExtensions(extensions: List<CodeQuarkusExtension>) {
        Assertions.assertTrue(extensions.size > 50);
    }

    @Test
    @DisplayName("Should generate a gradle project")
    @WebTest("/api/download?b=GRADLE&a=test-app-with-a-few-arg&v=1.0.0&s=pDS.L0j")
    fun testGradle(response: ValidatableResponse) {
        response
                .contentType("application/zip")
                .header("Content-Disposition", "attachment; filename=\"test-app-with-a-few-arg.zip\"")
        assertThat(
                projectService.getCreatedProject(), equalTo(
                ProjectDefinition(
                        artifactId = "test-app-with-a-few-arg",
                        version = "1.0.0",
                        buildTool = "GRADLE",
                        shortExtensions = "pDS.L0j"
                )
        )
        )
    }
}
