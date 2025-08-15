package jetbrains.buildServer.configs.kotlin.projectFeatures

import jetbrains.buildServer.configs.kotlin.*
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.*

class WebHookConfigurationTest {

    private lateinit var hook: WebHookConfiguration

    @BeforeEach
    fun setup() {

        val airTouchDiscoveryConsole = BuildType {
                name = "AirTouch Discovery Console"
                id = AbsoluteId("MyProject_AirTouchDiscoveryConsole")
                params {
                    param("vcs.project.name", "airtouch-discovery-console")
                }
            }
        val anotherExampleBuildType = BuildType {
                name = "My Example Build Type"
                id = AbsoluteId("MyProject_AnotherExampleBuildType")
                params {
                    param("vcs.project.name", "my-example-build-type")
                }
            }

        hook = WebHookConfiguration {
                webHookId = "SmallKotlinProject_WebHook_01"
                template = "legacy-json"
                url = "http://localhost:8111/webhooks/endpoint.html?vcs_test=1"
                //enabled = true
                //hideSecureValues = true

                buildTypes  = allProjectBuilds {
                    subProjectBuilds = true
                }

                buildTypes = selectedProjectBuilds {
                    subProjectBuilds = true

                    // Use an already defined build type.
                    buildType{airTouchDiscoveryConsole}

                    // Just pass the build type as a String, as that's all we use anyway
                    buildTypeId{anotherExampleBuildType.id.toString()}
                }

                buildStates {
                    buildAddedToQueue = true
                    buildRemovedFromQueue = true
                    testsMuted = true
                    testsUnmuted = false
                    buildPinned = true
                    buildFixed = true
                    serviceMessageReceived = true
                }

                authentication = basic {
                    username = "myUserName"
                    password = "myPassword"
                    realm = "myDomain"
                    preemptive = true
                }

                headers {
                    header(name = "foo1", value = "bar1")
                    header(name = "foo2", value = "bar2")
                    header("foo3", "bar3")
                    header(
                        name = "foo4",
                        value = "bar4"
                    )
                }

                parameters {
                    parameter("fooParam1", "barParam1")
                    parameter(
                        "fooParam2",
                        "barParam2",
                        secure = true,
                        forceResolveTeamCityVariable = true,
                        includedInLegacyPayloads = true,
                        templateEngine = WebHookConfiguration.TemplateEngine.VELOCITY.toString()
                    )
                    parameter(
                        name = "fooParam3",
                        value = "barParam3",
                        secure = false,
                        forceResolveTeamCityVariable = false,
                        includedInLegacyPayloads = false,
                        templateEngine = "STANDARD"
                    )
                }

                triggerFilters {
                    triggerFilter("myValue01", ".+", true)
                    triggerFilter("myValue02", ".+", false)
                    triggerFilter("myValue03", ".+")
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
                        realm = "myDomain"
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
    fun getEnabled() {
        assertEquals(null,hook.enabled)
        hook.enabled = true
        assertEquals(true,hook.enabled)
    }

    @Test
    fun getEnabledBuildStates() {
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "buildAddedToQueue", value = "enabled" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "buildRemovedFromQueue", value = "enabled" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "testsMuted", value = "enabled" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "testsUnmuted", value = "disabled" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "buildPinned", value = "enabled" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "buildFixed", value = "enabled" )))
    }

    @Test
    fun getEnabledBuildTypes() {
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "buildTypes", value = "selectedProjectBuilds" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "buildTypeIds", value= "MyProject_AirTouchDiscoveryConsole, MyProject_AnotherExampleBuildType")))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "subProjectBuilds", value = "true" )))
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
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "parameter_2_name", value = "fooParam3" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "parameter_2_value", value = "barParam3" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "parameter_2_secure", value = "false" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "parameter_2_includedInLegacyPayloads", value = "false" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "parameter_2_forceResolveTeamCityVariable", value = "false" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "parameter_2_templateEngine", value = "STANDARD" )))
//        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "parameter_3_name", value = "fooParam1" )))
//        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "parameter_3_value", value = "barParam1" )))
    }


    @Test
    fun getTriggerFilters() {
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "triggerFilter_0_value", value = "myValue01" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "triggerFilter_0_regex", value = ".+" )))
        assertThat(hook.params, CoreMatchers.hasItem(Parameter(name = "triggerFilter_0_enabled", value = "true" )))
    }

    @Test
    fun validateWebHook() {
        val consumer = Consumer()
        hook.validate(consumer)
        assertFalse(consumer.errorsReported())
    }

    @Test
    fun validationFailsWhenMoreThanOneParametersBlockAdded() {
        hook.parameters {
            parameter("fooParam100", "barParam100")
        }
        val consumer = Consumer()
        hook.validate(consumer)
        assertTrue(consumer.errorsReported())
        assertTrue(consumer.errors.contains("parameters function was called more than once."))
    }

    @Test
    fun validationFailsWhenMoreThanOneHeadersBlockAdded() {
        hook.headers {
            header("fooHeader100", "barHeader100")
        }
        val consumer = Consumer()
        hook.validate(consumer)
        assertTrue(consumer.errorsReported())
        assertTrue(consumer.errors.contains("headers function was called more than once."))
    }

    @Test
    fun validationFailsWhenMoreThanOneTriggerFiltersBlockAdded() {
        hook.triggerFilters {
            triggerFilter("fooHeader100", "barHeader100")
        }
        val consumer = Consumer()
        hook.validate(consumer)
        assertTrue(consumer.errorsReported())
        assertTrue(consumer.errors.contains("triggerFilters function was called more than once."))
    }

    @Test
    fun validationFailsWhenNonStandardParameterTempLateEngineIsDefined() {
        val hook2 = WebHookConfiguration {
            parameters {
                parameter(
                    name = "fred",
                    value = "sam",
                    templateEngine = "FRED"
                )
            }
        }
        val consumer = Consumer()
        hook2.validate(consumer)
        assertTrue(consumer.errorsReported())
        assertTrue(consumer.errors.contains("templateEngine must be one of [STANDARD, VELOCITY]"))
    }

    @Test
    fun validationFailsWhenBearerTokenIsUndefined() {
        var localHook = WebHookConfiguration {
            authentication = bearer {}
        }
        val consumer = Consumer()
        localHook.validate(consumer)
        assertTrue(consumer.errorsReported())
        assertTrue(consumer.errors.contains("mandatory 'authentication.token' property is not specified"))
    }

    @Test
    fun validationFailsWhenBasicUserNameOrPasswordIsUndefined() {
        var localHook = WebHookConfiguration {
            authentication = basic {}
        }
        val consumer = Consumer()
        localHook.validate(consumer)
        assertTrue(consumer.errorsReported())
        assertTrue(consumer.errors.contains("mandatory 'authentication.username' property is not specified"))
        assertTrue(consumer.errors.contains("mandatory 'authentication.password' property is not specified"))
    }

    @Test
    fun validationFailsWhenBasicUserNameIsUndefined() {
        var localHook = WebHookConfiguration {
            authentication = basic {
                password = "my password"
            }
        }
        val consumer = Consumer()
        localHook.validate(consumer)
        assertTrue(consumer.errorsReported())
        assertTrue(consumer.errors.contains("mandatory 'authentication.username' property is not specified"))
    }

    @Test
    fun validationFailsWhenBasicPasswordIsUndefined() {
        var localHook = WebHookConfiguration {
            authentication = basic {
                username = "my-username"
            }
        }
        val consumer = Consumer()
        localHook.validate(consumer)
        assertTrue(consumer.errorsReported())
        assertTrue(consumer.errors.contains("mandatory 'authentication.password' property is not specified"))
    }

    @Test
    fun getBasicAuthentication() {
        var localHook = WebHookConfiguration {
            authentication = basic {
                username = "myUserName"
                password = "myPassword"
                realm = "myRealm"
                preemptive = true
            }
        }
        assertThat(localHook.params, CoreMatchers.hasItem(Parameter(name = "basicAuthUsername", value = "myUserName" )))
        assertThat(localHook.params, CoreMatchers.hasItem(Parameter(name = "basicAuthPassword", value = "myPassword" )))
        assertThat(localHook.params, CoreMatchers.hasItem(Parameter(name = "basicAuthRealm", value = "myRealm" )))
        assertThat(localHook.params, CoreMatchers.hasItem(Parameter(name = "basicAuthPreemptive", value = "true" )))
    }

    @Test
    fun validationFailsWhenNtlmUserNameOrPasswordIsUndefined() {
        var localHook = WebHookConfiguration {
            authentication = ntlm {}
        }
        val consumer = Consumer()
        localHook.validate(consumer)
        assertTrue(consumer.errorsReported())
        assertTrue(consumer.errors.contains("mandatory 'authentication.username' property is not specified"))
        assertTrue(consumer.errors.contains("mandatory 'authentication.password' property is not specified"))
        assertTrue(consumer.errors.contains("mandatory 'authentication.workstation' property is not specified"))
        assertTrue(consumer.errors.contains("mandatory 'authentication.domain' property is not specified"))
    }

    @Test
    fun validationFailsWhenNtlmUserNameIsUndefined() {
        var localHook = WebHookConfiguration {
            authentication = basic {
                password = "my password"
            }
        }
        val consumer = Consumer()
        localHook.validate(consumer)
        assertTrue(consumer.errorsReported())
        assertTrue(consumer.errors.contains("mandatory 'authentication.username' property is not specified"))
    }

    @Test
    fun validationFailsWhenNtlmPasswordIsUndefined() {
        var localHook = WebHookConfiguration {
            authentication = basic {
                username = "my-username"
            }
        }
        val consumer = Consumer()
        localHook.validate(consumer)
        assertTrue(consumer.errorsReported())
        assertTrue(consumer.errors.contains("mandatory 'authentication.password' property is not specified"))
    }

    @Test
    fun getNtlmAuthentication() {
        var localHook = WebHookConfiguration {
            authentication = ntlm {
                username = "myUserName"
                password = "myPassword"
                workstation = "myWorkStation"
                domain = "myDomain"
                preemptive = false
            }
        }
        assertThat(localHook.params, CoreMatchers.hasItem(Parameter(name = "ntlmUsername", value = "myUserName" )))
        assertThat(localHook.params, CoreMatchers.hasItem(Parameter(name = "ntlmPassword", value = "myPassword" )))
        assertThat(localHook.params, CoreMatchers.hasItem(Parameter(name = "ntlmWorkStation", value = "myWorkStation" )))
        assertThat(localHook.params, CoreMatchers.hasItem(Parameter(name = "ntlmDomain", value = "myDomain" )))
        assertThat(localHook.params, CoreMatchers.hasItem(Parameter(name = "ntlmPreemptive", value = "false" )))
    }

    @Test
    fun parameterDeclaredMoreThanOnceFailsValidation() {
        var localHook = WebHookConfiguration {
            parameters{
                parameter("my_name", "my name the first")
                parameter("my_name", "my name the second")
                parameter("my_name", "my name the third")
                parameter(
                        name = "fooParam",
                        value = "barParam",
                        secure = false,
                        forceResolveTeamCityVariable = false,
                        includedInLegacyPayloads = false,
                        templateEngine = "STANDARD"
                )
                parameter(
                        name = "fooParam",
                        value = "barParam",
                        secure = false,
                        forceResolveTeamCityVariable = false,
                        includedInLegacyPayloads = false,
                        templateEngine = "STANDARD"
                )
            }
        }
        val consumer = Consumer()
        localHook.validate(consumer)
        assertTrue(consumer.errorsReported())
        assertTrue(consumer.errors.contains("parameter 'parameters.my_name' is declared 3 times"))
        assertTrue(consumer.errors.contains("parameter 'parameters.fooParam' is declared 2 times"))
    }
}