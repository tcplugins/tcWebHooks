import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.projectFeatures.WebHookConfiguration
import jetbrains.buildServer.configs.kotlin.projectFeatures.webHookConfiguration
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2024.03"

project {
    description = "My Example Kotlin based project from VCS"
    features {
        webHookConfiguration {
            webHookId = "RootProjectId_WebHook_01"
            template = "legacy-json"
            url = "http://localhost:8111/webhooks/endpoint.html?vcs_test=1"
            buildTypes = selectedProjectBuilds {
                subProjectBuilds = true
                /* Define the specific buildTypes that this webhook should execute for.
                 * There are 3 ways to define a buildType.
                 *
                 *     This function takes an object of type "jetbrains. buildServer. configs. kotlin.BuildType".
                 *     Typically, the buildType will already be defined in this file, and we can just reference it.
                 * buildType(myBuildType)
                 *
                 *     This function takes the id of the BuildType. Again, we already know the buildType config, so
                 *     we can use that by calling toString() on it.
                 * buildTypeId(myBuildType.id.toString())
                 *
                 *     This example calls the same function, but we are hard coding the BuildType's ID string.
                 *     This is the least preferred method as it would need to be updated if the ID changes.
                 * buildTypeId("RootProjectId_MyBuildTypeId")
                 */

                buildType{TcDummyDeb}
                buildTypeId{TcWebHooks.id.toString()}
                buildTypeId{"RootProjectId_TcChatBot"}
            }
            buildStates {
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
        webHookConfiguration {
            webHookId = "RootProjectId_WebHook_02"
            template = "legacy-json"
            url = "http://localhost:8111/webhooks/endpoint.html?vcs_test=2"
            buildTypes = allProjectBuilds {
                subProjectBuilds = true
            }
            buildStates {
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
                header(name = "foo2", value = "bar2")
                header("foo3", "bar3")
            }
            parameters {
                parameter(name="colour", value="blue")
            }
        }
        webHookConfiguration {
            webHookId = "RootProjectId_WebHook_03"
            template = "legacy-json"
            url = "http://localhost:8111/webhooks/endpoint.html?vcs_test=3"
            buildTypes = allProjectBuilds {
                subProjectBuilds = true
            }
            buildStates {
                buildAddedToQueue = true
                buildRemovedFromQueue = true
            }
            authentication = bearer {
                token = "dkfjsdlfjldfjk"
                preemptive = true
            }
        }
        webHookConfiguration {
            webHookId = "RootProjectId_WebHook_04"
            template = "legacy-json"
            url = "http://localhost:8111/webhooks/endpoint.html?vcs_test=4"
            buildTypes = allProjectBuilds {
                subProjectBuilds = true
            }
            buildStates {
                buildAddedToQueue = true
            }
            authentication = bearer {
                token = "new-bearer-toke"
                preemptive = true
            }
        }
        webHookConfiguration {
            webHookId = "RootProjectId_WebHook_05"
            template = "legacy-json"
            url = "http://localhost:8111/webhooks/endpoint.html?vcs_test=5"
            buildTypes = allProjectBuilds {
                subProjectBuilds = true
            }
            buildStates {
                buildAddedToQueue = true
                buildRemovedFromQueue = true
            }
            authentication = bearer {
                token = "dkfjsdlfjldfjk"
                preemptive = true
            }
        }
        webHookConfiguration {
            webHookId = "RootProjectId_WebHook_06"
            template = "legacy-json"
            url = "http://localhost:8111/webhooks/endpoint.html?vcs_test=6"
            buildTypes = allProjectBuilds {
                subProjectBuilds = true
            }
            buildStates {
                buildAddedToQueue = true
                buildRemovedFromQueue = true
            }
            authentication = bearer {
                token = "dkfjsdlfjldfjk"
                preemptive = true
            }
        }
        webHookConfiguration {
            webHookId = "RootProjectId_WebHook_07"
            template = "slack.com-compact"
            url = "http://localhost:8111/webhooks/endpoint.html?vcs_test=7"
            buildTypes = allProjectBuilds {
                subProjectBuilds = true
            }
            buildStates {
                buildAddedToQueue = true
                buildRemovedFromQueue = true
            }
            authentication = bearer {
                token = "this-is-my-token"
                preemptive = true
            }
        }
        webHookConfiguration {
            webHookId = "RootProjectId_WebHook_08"
            template = "slack.com-compact"
            url = "http://localhost:8111/webhooks/endpoint.html?vcs_test=8"
            buildTypes = allProjectBuilds {
                subProjectBuilds = true
            }
            buildStates {
                buildAddedToQueue = true
                buildRemovedFromQueue = true
            }
            authentication = bearer {
                token = "this-is-my-updated-token"
                preemptive = true
            }
            headers {
                header("foo1", "bar1")
                header("foo2", "bar2")
                header("foo3", "bar3")
            }
            parameters {
                parameter(name="colour", value="blue", secure = true)
                parameter(
                    name = "fooParam2",
                    value = "barParam2",
                    secure = true,
                    forceResolveTeamCityVariable = true,
                    includedInLegacyPayloads = true,
                    templateEngine = "VELOCITY"
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
        }
        webHookConfiguration(WebHookConfiguration09)
        webHookConfiguration(WebHookConfiguration10)
        webHookConfiguration(WebHookConfiguration11)
        webHookConfiguration(WebHookConfiguration12)
        webHookConfiguration(WebHookConfiguration13)
    }
    vcsRoot(AirTouchJavaTemplate)
    vcsRoot(TcPluginGitHubTemplate)
    
    buildType(TcDummyDeb)
    buildType(TcWebHooks)
    buildType(TcChatBot)
    buildType(AirTouchDiscoveryConsole)
    buildType(AirTouchUtils)
}

    object WebHookConfiguration09 : WebHookConfiguration ({
        webHookId = "RootProjectId_WebHook_09"
        template = "legacy-json"
        url = "http://localhost:8111/webhooks/endpoint.html?vcs_test=9"
        buildTypes = allProjectBuilds {
            subProjectBuilds = true
        }
        buildStates {
            buildAddedToQueue = true
        }
        authentication = bearer {
            token = "new-bearer-toke"
            preemptive = true
        }
    })

    object WebHookConfiguration10 : WebHookConfiguration ({
        webHookId = "RootProjectId_WebHook_10"
        template = "legacy-json"
        url = "http://localhost:8111/webhooks/endpoint.html?vcs_test=10"
        buildTypes = allProjectBuilds {
            subProjectBuilds = true
        }
        buildStates {
            buildAddedToQueue = true
        }
        authentication = bearer {
            token = "new-bearer-toke"
            preemptive = true
        }
    })

    object WebHookConfiguration11 : WebHookConfiguration ({
        webHookId = "RootProjectId_WebHook_11"
        template = "legacy-json"
        url = "http://localhost:8111/webhooks/endpoint.html?vcs_test=11"
        buildTypes = allProjectBuilds {
            subProjectBuilds = true
        }
        buildStates {
            buildAddedToQueue = true
        }
        authentication = bearer {
            token = "new-bearer-toke"
            preemptive = true
        }
    })

    object WebHookConfiguration12 : WebHookConfiguration ({
        webHookId = "RootProjectId_WebHook_12"
        template = "legacy-json"
        url = "http://localhost:8111/webhooks/endpoint.html?vcs_test=12"
        buildTypes = allProjectBuilds {
            subProjectBuilds = true
        }
        buildStates {
            buildAddedToQueue = true
        }
        authentication = bearer {
            token = "new-bearer-toke"
            preemptive = true
        }
    })

    object WebHookConfiguration13 : WebHookConfiguration ({
        webHookId = "RootProjectId_WebHook_13"
        template = "legacy-json"
        url = "http://localhost:8111/webhooks/endpoint.html?vcs_test=13"
        buildTypes = allProjectBuilds {
            subProjectBuilds = true
        }
        buildStates {
            buildAddedToQueue = true
        }
        authentication = bearer {
            token = "new-bearer-toke"
            preemptive = true
        }
    })

    object AirTouchJavaTemplate : GitVcsRoot({
        name = "AirTouch Java GitHub Template"
        url = "https://github.com/airtouch-java/%vcs.project.name%.git"
        branch = "main"
    })

    object TcPluginGitHubTemplate : GitVcsRoot({
        name = "tcPlugins GitHub Template"
        url = "https://github.com/tcplugins/%vcs.project.name%.git"
        branch = "refs/heads/master"
    })

    object AirTouchDiscoveryConsole : BuildType({
        name = "AirTouch Discovery Console"

        params {
            param("vcs.project.name", "airtouch-discovery-console")
        }

        vcs {
            root(AirTouchJavaTemplate)
        }

        steps {
            maven {
                id = "Maven2"
                goals = "clean test"
                runnerArgs = "-Dmaven.test.failure.ignore=true"
                jdkHome = "%env.JDK_1_8%"
            }
        }
    })

    object AirTouchUtils : BuildType({
        name = "AirTouch Utils"

        params {
            param("vcs.project.name", "airtouch-utils")
        }

        vcs {
            root(AirTouchJavaTemplate)
        }

        steps {
            maven {
                id = "Maven2"
                goals = "clean test install"
                runnerArgs = "-Dmaven.test.failure.ignore=true"
                jdkHome = "%env.JDK_1_8%"
            }
        }
    })

    object TcChatBot : BuildType({
        name = "tcChatBot"

        params {
            param("vcs.project.name", "tcChatBot")
        }

        vcs {
            root(TcPluginGitHubTemplate)
        }

        steps {
            maven {
                id = "Maven2"
                goals = "clean test"
                runnerArgs = "-Dmaven.test.failure.ignore=true"
            }
        }
    })

    object TcDummyDeb : BuildType({
        name = "tcDummyDeb"

        params {
            param("vcs.project.name", "tcDummyDeb")
        }

        vcs {
            root(TcPluginGitHubTemplate)
        }

        steps {
            script {
                id = "simpleRunner"
                scriptContent = "./build.sh"
            }
        }
    })

    object TcWebHooks : BuildType({
        name = "tcWebHooks"

        params {
            param("vcs.project.name", "tcWebHooks")
        }

        vcs {
            root(TcPluginGitHubTemplate)
        }

        steps {
            maven {
                id = "Maven2"
                goals = "clean test"
                runnerArgs = "-Dmaven.test.failure.ignore=true"
                jdkHome = "%env.JDK_1_8%"
            }
        }
    })
