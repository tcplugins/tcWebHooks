package jetbrains.buildServer.configs.kotlin.projectFeatures

import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.*
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
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

                headers {
                    header(name = "foo1", value = "bar1")
//                    header{
//                        name = "foo"
//                        value = "bar"
//                    }
                    header(name = "foo2", value = "bar2")
                    header("foo3", "bar3")
                }

                parameters {
                    parameter("fooParam1", "barParam1")
                    parameter(
                        "fooParam2",
                        "barParam2",
                        secure = true,
                        forceResolveTeamCityVariable = true,
                        includedInLegacyPayloads = true,
                        templateEngine = WebHookConfigurationNew.TemplateEngine.VELOCITY)
                }

                triggerFilters {
                    triggerFilter( value = "barTriggerFilter1", regex = "regex1", enabled = true)
                    triggerFilter( 
                        value = "barTriggerFilter2", 
                        regex = "^bt\\d$", 
                        enabled = false
                    )
                    triggerFilter( 
                        "barTriggerFilter3", 
                        "^master$", 
                        false
                    )
                    triggerFilter("barTriggerFilter4", "^bt\\d$", true)
                }
        }
    }

    @Test
    fun getWebHookId() {
        assertEquals(
            "SmallKotlinProject_WebHook_01",
            hook.webHookId
        )

        version = "2024.03"

        /*project {
            id("test")
            name = "Test01"
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
    fun getUrl() {
        assertEquals(
            "http://localhost:8111/webhooks/endpoint.html?vcs_test=1",
            hook.url
        )
    }

    @Test
    fun getHeaders() {
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "header_0_name", value = "foo1" )))
    }

    @Test
    fun getParameters() {
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "parameter_0_name", value = "fooParam1" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "parameter_0_value", value = "barParam1" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "parameter_1_name", value = "fooParam2" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "parameter_1_value", value = "barParam2" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "parameter_1_secure", value = "true" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "parameter_1_includedInLegacyPayloads", value = "true" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "parameter_1_forceResolveTeamCityVariable", value = "true" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "parameter_1_templateEngine", value = "VELOCITY" )))
    }

    @Test
    fun getTriggers() {
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "triggerFilter_0_value", value = "barTriggerFilter1" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "triggerFilter_0_regex", value = "regex1" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "triggerFilter_0_enabled", value = "true" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "triggerFilter_1_value", value = "barTriggerFilter2" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "triggerFilter_1_regex", value = "^bt\\d$" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "triggerFilter_1_enabled", value = "false" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "triggerFilter_2_value", value = "barTriggerFilter3" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "triggerFilter_2_regex", value = "^master$" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "triggerFilter_2_enabled", value = "false" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "triggerFilter_3_value", value = "barTriggerFilter4" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "triggerFilter_3_regex", value = "^bt\\d$" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "triggerFilter_3_enabled", value = "true" )))
    }
}