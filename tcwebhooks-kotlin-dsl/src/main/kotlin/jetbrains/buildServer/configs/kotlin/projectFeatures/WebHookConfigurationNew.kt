package jetbrains.buildServer.configs.kotlin.projectFeatures

import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.projectFeatures.WebHookConfigurationNew.Parameter

/**
 * A description of a class.
 *
 *
 * @see webHookConfigurationNew
 */
@TeamCityDsl
open class WebHookConfigurationNew() : ProjectFeature() {

    init {
        type = "tcWebHooks"
    }

    constructor(init: WebHookConfigurationNew.() -> Unit): this() {
        init()
    }

    /**
     * WebHook Id. Must be unique across the whole of teamcity.
     * Suggestion: use a
     * name like 'ProjectIdentifier_WebHook_01'
     */
    var webHookId by stringParameter()

    /**
     * WebHook URL. The URL to which the webhook will be
     * POST'd.
     */
    var url by stringParameter()

    /**
     * The templateId referring to the template this webhook
     * will use.
     */
    var template by stringParameter()

    /**
     * Authentication type to use
     */
    var authentication by compoundParameter<Authentication>()

    sealed class Authentication(value: String? = null): CompoundParam<Authentication>(value) {
        abstract fun validate(consumer: ErrorConsumer)

        class Bearer() : Authentication("bearer") {

            /**
             * Bearer token to use for Bearer Authentication
             */
            var token by stringParameter("bearerToken")

            /**
             * Whether to enable pre-emptive authentication (defaults
             * to true). Without pre-emptive enabled, the webhook will get a 401
             * and then re-send the request with authentication enabled.
             */
            var preemptive by booleanParameter("bearerPreemptive")

            override fun validate(consumer: ErrorConsumer) {
                if (token == null && !hasParam("bearerToken")) {
                    consumer.consumePropertyError("authentication.token", "mandatory 'authentication.token' property is not specified")
                }
            }
        }

        class Basic() : Authentication("basicAuth") {

            /**
             * Username for Basic Authentication
             */
            var username by stringParameter("basicAuthUsername")

            /**
             * Password for Basic Authentication
             */
            var password by stringParameter("basicAuthPassword")

            /**
             * Realm to use for Basic Authentication
             */
            var realm by stringParameter("basicAuthRealm")

            /**
             * Whether to enable pre-emptive authentication (defaults
             * to true). Without pre-emptive enabled, the webhook will get a 401
             * and then re-send the request with authentication enabled.
             */
            var preemptive by booleanParameter("basicAuthPreemptive")

            override fun validate(consumer: ErrorConsumer) {
            }
        }
    }

    fun bearer(init: Authentication.Bearer.() -> Unit = {}) : Authentication.Bearer {
        val result = Authentication.Bearer()
        result.init()
        return result
    }

    fun basic(init: Authentication.Basic.() -> Unit = {}) : Authentication.Basic {
        val result = Authentication.Basic()
        result.init()
        return result
    }

    /**
     * Specifies to which system a status should be published
     */
    var buildTypes by compoundParameter<BuildTypes>()

    sealed class BuildTypes(value: String? = null): CompoundParam<BuildTypes>(value) {
        abstract fun validate(consumer: ErrorConsumer)

        /**
         * Send webhook to all build in project
         */
        class AllProjectBuilds() : BuildTypes("allProjectBuilds") {

            /**
             * A flag to instruct the webhook to run against all
             * builds in subprojects of this project.
             */
            var subProjectBuilds by booleanParameter()

            override fun validate(consumer: ErrorConsumer) {
            }
        }

        class SelectedProjectBuilds() : BuildTypes("selectedProjectBuilds") {

            /**
             * A flag to instruct the webhook to run against all
             * builds in subprojects of this project.
             */
            var subProjectBuilds by booleanParameter()

            /**
             * Comma separated list of buildTypeIds for which this
             * webhook should
             * trigger.
             */
            var buildTypeIds by stringParameter()

            override fun validate(consumer: ErrorConsumer) {
                if (buildTypeIds == null && !hasParam("buildTypeIds")) {
                    consumer.consumePropertyError("buildTypes.buildTypeIds", "mandatory 'buildTypes.buildTypeIds' property is not specified")
                }
            }
        }
    }

    /**
     * Send webhook to all builds in project
     */
    fun allProjectBuilds(init: BuildTypes.AllProjectBuilds.() -> Unit = {}) : BuildTypes.AllProjectBuilds {
        val result = BuildTypes.AllProjectBuilds()
        result.init()
        return result
    }

    fun selectedProjectBuilds(init: BuildTypes.SelectedProjectBuilds.() -> Unit = {}) : BuildTypes.SelectedProjectBuilds {
        val result = BuildTypes.SelectedProjectBuilds()
        result.init()
        return result
    }

    /**
     * The build states for which this webhook should trigger.
     */
    var buildStates by compoundParameter<BuildStates>()

    sealed class BuildStates(value: String? = null): CompoundParam<BuildStates>(value) {
        /**
         * Pass in the build states required. Any omitted build
         * states will default to disabled.
         */
        class EnabledBuildStates() : BuildStates("enabledBuildStates") {

            /**
             * Trigger this webhook when the build is added to the queue.
             */
            var buildAddedToQueue by booleanParameter(trueValue = "enabled", falseValue = "disabled")

            /**
             * Trigger this webhook when the build is removed from the queue by a user.
             */
            var buildRemovedFromQueue by booleanParameter(trueValue = "enabled", falseValue = "disabled")

        }
    }

    /**
     * Pass in the build states required. Any omitted build
     * states will default to disabled.
     */
    fun enabledBuildStates(init: BuildStates.EnabledBuildStates.() -> Unit = {}) : BuildStates.EnabledBuildStates {
        val result = BuildStates.EnabledBuildStates()
        result.init()
        return result
    }

    /**
     * Build Type references for which this webhook should
     * run.
     */
    var buildType by stringParameter()

    override fun validate(consumer: ErrorConsumer) {
        super.validate(consumer)
        if (webHookId == null && !hasParam("webHookId")) {
            consumer.consumePropertyError("webHookId", "mandatory 'webHookId' property is not specified")
        }
        if (url == null && !hasParam("url")) {
            consumer.consumePropertyError("url", "mandatory 'url' property is not specified")
        }
        if (template == null && !hasParam("template")) {
            consumer.consumePropertyError("template", "mandatory 'template' property is not specified")
        }
        authentication?.validate(consumer)
        if (buildTypes == null && !hasParam("buildTypes")) {
            consumer.consumePropertyError("buildTypes", "mandatory 'buildTypes' property is not specified")
        }
        buildTypes?.validate(consumer)
        if (buildStates == null && !hasParam("buildStates")) {
            consumer.consumePropertyError("buildStates", "mandatory 'buildStates' property is not specified")
        }
    }

//    fun header(name: String, value: String) : Header {
//        val h = Header()
//        h.name = name
//        h.value = value
//        return h
//    }

    class Headers(val feature: WebHookConfigurationNew, init: Headers.() -> Unit) {
        val myHeaders = mutableMapOf<String,String>()
        init {
            init()
        }

        fun header(name: String, value: String) {
            // TODO: Throw exception if header added twice
            myHeaders[name] = value
            //this.feature.param(name, value)
        }

//        fun header(init: Header.() -> Unit = {}) {
//            val result = Header()
//            result.init()
//            if (result.name != null && result.value != null) {
//                this.feature.param(result.name, result.value)
//            }
//        }
    }

    fun headers(init: Headers.() -> Unit): Headers {
        val headers = Headers(this, init)
        for((headerCounter, key) in headers.myHeaders.keys.withIndex()) {
            headers.feature.param("header_${headerCounter}_name", key)
            headers.myHeaders[key]?.let { headers.feature.param("header_${headerCounter}_value", it) }
        }
        return headers
    }

    class Parameters(val feature: WebHookConfigurationNew, init: Parameters.() -> Unit) {
        val myParameters = mutableMapOf<String,Parameter>()
        init {
            init()
        }

        fun parameter(name: String, value: String) {
            // TODO: Throw exception if parameter added twice
            myParameters[name] = Parameter(
                name,
                value,
                secure = null,
                includedInLegacyPayloads = null,
                forceResolveTeamCityVariable = null,
                templateEngine = null
            )
        }
        fun parameter(
            name: String,
            value: String,
            secure: Boolean? = null,
            includedInLegacyPayloads: Boolean? = null,
            forceResolveTeamCityVariable: Boolean? = null,
            templateEngine: TemplateEngine? = null
            ) {
            // TODO: Throw exception if parameter added twice
            myParameters[name] = Parameter(
                name,
                value,
                secure,
                includedInLegacyPayloads,
                forceResolveTeamCityVariable,
                templateEngine
            )
        }

    }

    fun parameters(init: Parameters.() -> Unit): Parameters {
        val parameters = Parameters(this, init)
        for((parameterCounter, p) in parameters.myParameters.values.withIndex()) {

            parameters.feature.param("parameter_${parameterCounter}_name", p.name)
            parameters.feature.param("parameter_${parameterCounter}_value", p.value)
            p.secure?.let { parameters.feature.param("parameter_${parameterCounter}_secure", p.secure.toString()) }
            p.includedInLegacyPayloads?.let { parameters.feature.param("parameter_${parameterCounter}_includedInLegacyPayloads", p.includedInLegacyPayloads.toString()) }
            p.forceResolveTeamCityVariable?.let { parameters.feature.param("parameter_${parameterCounter}_forceResolveTeamCityVariable", p.forceResolveTeamCityVariable.toString()) }
            p.templateEngine?.let { parameters.feature.param("parameter_${parameterCounter}_templateEngine", p.templateEngine.toString()) }
        }
        return parameters
    }




    class Parameter(
        var name: String,
        var value: String,
        var secure: Boolean?,
        var includedInLegacyPayloads: Boolean?,
        var forceResolveTeamCityVariable: Boolean?,
        var templateEngine: TemplateEngine?
        ) {
    }

    enum class TemplateEngine {
        STANDARD, VELOCITY
    }
//    fun headers(init: Headers.() -> Unit): Headers {
//        val headers = Headers(this, init)
//        headers.myHeaders.forEach { n, v -> headers.feature.param(n,v) }
//
//        return headers
//    }

//    class Headers {
//        constructor Headers() {
//
//        }
//        fun header(name: String, value:String) : Header {
//            // TODO find existing headers to get index, then add
//            param(name, value)
//            val result = Header()
//            result.name = name
//            result.value = value
//            return result
//        }
//
//        fun headers(init: Header.() -> Unit) : Headers {
//            val h = Header()
//            h.init()
//            headers.add(h)
//            return this
//        }
//    }



//    fun header(init: Header.() -> Unit = {}) : Header {
//
//        val result = Header()
//        result.init()
//        return result
//    }



    /*
        <param name="header_0_name" value="x-token" />
        <param name="header_0_value" value="some-token-value" />

        <param name="triggerFilter_0_value value="${buildInternalTypeId}" />
        <param name="triggerFilter_0_regex value="^bt\d$" />
        <param name="triggerFilter_0_enabled value="true" />
     */

//    fun headers(header: () -> Header) {
//
//        //var hs = Headers.headers(header.invoke())
//        //hs.add(header.invoke())
//        //return hs
//    }
}

/**
 * A description of a called function.
 *
 *
 * @see WebHookConfigurationNew
 */
fun ProjectFeatures.webHookConfigurationNew(init: WebHookConfigurationNew.() -> Unit): WebHookConfigurationNew {
    val result = WebHookConfigurationNew(init)
    feature(result)
    return result
}
