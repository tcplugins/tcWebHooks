package jetbrains.buildServer.configs.kotlin.projectFeatures

import jetbrains.buildServer.configs.kotlin.ProjectFeature
import jetbrains.buildServer.configs.kotlin.ProjectFeatures
import jetbrains.buildServer.configs.kotlin.project
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.*

class WebHookConfigurationNewTest {

    lateinit var hook: WebHookConfigurationNew

    @BeforeEach
    fun setup() {

        hook = WebHookConfigurationNew {
                webHookId = "SmallKotlinProject_WebHook_01"
                template = "legacy-json"
                url = "http://localhost:8111/webhooks/endpoint.html?vcs_test=1"
                buildTypes = selectedProjectBuilds {
                    subProjectBuilds = true
                    buildTypeIds = "01, 02"
                }
                buildStates = enabledBuildStates {
                    buildAddedToQueue = true
                    buildRemovedFromQueue = true
                }
                authentication = basic {
                    username = "myUserName"
                    password = "myPassword"
                    realm = "myRealm"
                    preemptive = true
                }
        }

        /*project {
            name ="Test01"
            description = "Small Kotlin based project from VCS"

            features {
                webHookConfigurationNew {
                    webHookId = "SmallKotlinProject_WebHook_01"
                    template = "legacy-json"
                    url = "http://localhost:8111/webhooks/endpoint.html?vcs_test=1"
                    buildTypes = selectedProjectBuilds {
                        subProjectBuilds = true
                        buildTypeIds = "01, 02"
                    }
                    buildStates = enabledBuildStates {
                        buildAddedToQueue = true
                        buildRemovedFromQueue = true
                    }
                    authentication = basic {
                        username = "myUserName"
                        password = "myPassword"
                        realm = "myRealm"
                        preemptive = true
                    }
                }
            }
        }*/
    }

    @Test
    fun getWebHookId() {
        assertEquals(
            "SmallKotlinProject_WebHook_01",
            hook.webHookId
        )
    }

    @Test
    fun getUrl() {
        assertEquals(
            "http://localhost:8111/webhooks/endpoint.html?vcs_test=1",
            hook.url
        )
    }
}