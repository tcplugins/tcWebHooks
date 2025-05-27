package jetbrains.buildServer.configs.kotlin.projectFeatures

import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.ui.id

/**
 * A description of a class.
 *
 *
 * @see webHookConfigurationNew
 */
@TeamCityDsl
class WebHookConfigurationNew() : ProjectFeature() {

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
    //var buildTypes by compoundParameter<BuildTypes>()
    sealed class BuildTypes2(
        val feature: WebHookConfigurationNew,
        val allProjectBuilds : Boolean = false,
        var subProjectBuilds: Boolean = false) : Validatable {



        class AllProjectBuilds(feature: WebHookConfigurationNew,): BuildTypes2(feature, allProjectBuilds = true) {
            override fun validate(consumer: ErrorConsumer) {
                TODO("Not yet implemented")
            }

        }
        class SelectedProjectBuilds(feature: WebHookConfigurationNew,): BuildTypes2(feature, allProjectBuilds = false) {
            private val myBuildTypes = mutableSetOf<Id?>()

            override fun validate(consumer: ErrorConsumer) {

            }

            /**
             * 
             */
            fun buildType(myBuildTypeFunction: () -> BuildType) {
                /* We are passed in a function like this...
                    buildType{
                        // Some buildType reference passed to the function
                        BuildType {
                            name = "AirTouch Utils"
                            params {
                                param("vcs.project.name", "airtouch-utils")
                            }
                        }
                    }

                 * Therefore, call the function and add the resulting Id
                 * to the set.
                 */
                myBuildTypes.add(myBuildTypeFunction().id)
            }

            fun buildTypeId(myBuildTypeIdFunction: () -> Id?) {
                myBuildTypes.add(myBuildTypeIdFunction())
            }
        }
    }

    fun allProjectBuilds(init: BuildTypes2.AllProjectBuilds.() -> Unit = {}) : BuildTypes2.AllProjectBuilds {
        val result = BuildTypes2.AllProjectBuilds(this)
        result.init()
        return result
    }

    fun selectedProjectBuilds(init: BuildTypes2.SelectedProjectBuilds.() -> Unit = {}) : BuildTypes2.SelectedProjectBuilds {
        val result = BuildTypes2.SelectedProjectBuilds(this)
        result.init()
        return result
    }

//    sealed class xBuildTypes(value: String? = null): CompoundParam<BuildTypes>(value) {
//        abstract fun validate(consumer: ErrorConsumer)
//
//        /**
//         * Send webhook to all build in project
//         */
//        class AllProjectBuilds() : BuildTypes("allProjectBuilds") {
//
//            var subProjectBuilds by booleanParameter()
//            /**
//             * A flag to instruct the webhook to run against all
//             * builds in subprojects of this project.
//             */
//            val allProjectBuilds = true
//            override fun validate(consumer: ErrorConsumer) {
//            }
//
//            class SelectedProjectBuilds() : BuildTypes("selectedProjectBuilds") {
//                /**
//                 * A flag to instruct the webhook to run against all
//                 * builds in subprojects of this project.
//                 */
//                var subProjectBuilds by booleanParameter()
//
//                var allProjectBuilds by booleanParameter2()
//
//                fun booleanParameter2(customName: String? = null, trueValue: String? = "true", falseValue: String? = "false"): DelegateProvider<Boolean?> {
//                    var thing = booleanParameter()
//
//                    return thing
//                }
//
//
//                /**
//                 * Comma separated list of buildTypeIds for which this
//                 * webhook should
//                 * trigger.
//                 */
//                var buildTypeIds by stringParameter()
//
//                override fun validate(consumer: ErrorConsumer) {
//                    if (buildTypeIds == null && !hasParam("buildTypeIds")) {
//                        consumer.consumePropertyError("buildTypes.buildTypeIds", "mandatory 'buildTypes.buildTypeIds' property is not specified")
//                    }
//                }
//            }
//        }
//
//    }

    /**
     * Send webhook to all builds in project
     */
//    fun allProjectBuilds(init: BuildTypes.AllProjectBuilds.() -> Unit = {}) : BuildTypes.AllProjectBuilds {
//        val result = BuildTypes.AllProjectBuilds()
//        result.init()
//        return result
//    }
//
//    fun selectedProjectBuilds(init: BuildTypes.SelectedProjectBuilds.() -> Unit = {}) : BuildTypes.SelectedProjectBuilds {
//        val result = BuildTypes.SelectedProjectBuilds()
//        result.init()
//        return result
//    }

    class BuildStates(
        private val feature: WebHookConfigurationNew, init: BuildStates.() -> Unit,
        ) {
        init {
            init()
        }

        private fun enabledAsSting(boolValue: Boolean): String {
            return if (boolValue) "enabled" else "disabled"
        }

        fun addToParams() {
            buildAddedToQueue?.let { this.feature.param("buildAddedToQueue" , enabledAsSting(buildAddedToQueue!!)) }
            buildRemovedFromQueue?.let { this.feature.param("buildRemovedFromQueue" , enabledAsSting(buildRemovedFromQueue!!)) }
            buildStarted?.let { this.feature.param("buildStarted" , enabledAsSting(buildStarted!!)) }
            changesLoaded?.let { this.feature.param("changesLoaded" , enabledAsSting(changesLoaded!!)) }
            buildSuccessful?.let { this.feature.param("buildSuccessful" , enabledAsSting(buildSuccessful!!)) }
            buildFixed?.let { this.feature.param("buildFixed" , enabledAsSting(buildFixed!!)) }
            buildFailed?.let { this.feature.param("buildFailed" , enabledAsSting(buildFailed!!)) }
            buildBroken?.let { this.feature.param("buildBroken" , enabledAsSting(buildBroken!!)) }
            buildPinned?.let { this.feature.param("buildPinned" , enabledAsSting(buildPinned!!)) }
            buildUnpinned?.let { this.feature.param("buildUnpinned" , enabledAsSting(buildUnpinned!!)) }
            testsMuted?.let { this.feature.param("testsMuted" , enabledAsSting(testsMuted!!)) }
            testsUnmuted?.let { this.feature.param("testsUnmuted" , enabledAsSting(testsUnmuted!!)) }
            responsibilityChanged?.let { this.feature.param("responsibilityChanged" , enabledAsSting(responsibilityChanged!!)) }
            serviceMessageReceived?.let { this.feature.param("serviceMessageReceived" , enabledAsSting(serviceMessageReceived!!)) }
            reportStatistics?.let { this.feature.param("reportStatistics" , enabledAsSting(reportStatistics!!)) }
        }

        /**
         *  Triggered when a build is added to the queue.
         */
        var buildAddedToQueue: Boolean? = null
        /**
         * Triggered when a build is removed from the queue by a user.
         */
        var buildRemovedFromQueue: Boolean? = null
        /**
         * Triggered when a build starts.
         * Note: changes and other variables may not be initialised at this point.
         */
        var buildStarted: Boolean? = null
        /**
         * Triggered after the build has started and VCS changes have been assembled.
         */
        var changesLoaded: Boolean? = null
        /**
         * Triggered every time a build successfully completes.
         */
        var buildSuccessful: Boolean? = null
        /**
         * Triggered the first time a build succeeds after previously failing
         */
        var buildFixed: Boolean? = null
        /**
         * Every time a build fails
         */
        var buildFailed: Boolean? = null
        /**
         * The first time a build fails after previously succeeding
         */
        var buildBroken: Boolean? = null
        /**
         * A pin is added to a build by a user
         */
        var buildPinned: Boolean? = null
        /**
         * A pin is removed from a build by a user
         */
        var buildUnpinned: Boolean? = null
        /**
         * Tests are muted on a build
         */
        var testsMuted: Boolean? = null
        /**
         * Tests are unmuted on a build
         */
        var testsUnmuted: Boolean? = null
        /**
         * The Responsibility of resolving build issues is changed
         */
        var responsibilityChanged: Boolean? = null
        /**
         * A webhook service message is generated by the build
         */
        var serviceMessageReceived: Boolean? = null
        /**
         * A statistics report request has been received
         */
        var reportStatistics: Boolean? = null
    }

    /**
     * Function to define the build states for which this webhook should be triggered.
     *
     * Example Code:
     *
     *     buildStates {
     *        buildAddedToQueue = true
     *        buildRemovedFromQueue = true
     *        testsMuted = true
     *        testsUnmuted = false
     *        buildPinned = true
     *        buildFixed = true
     *     }
     */
    fun buildStates(init: BuildStates.() -> Unit = {}): BuildStates {
        val buildStates = BuildStates(this, init)
        this.buildStates = buildStates
        this.buildStates!!.addToParams()
        return buildStates
    }

    private var buildStates: BuildStates? = null
    private var parameters: Parameters? = null
    private var headers: Headers? = null

    /**
     * Build Type references for which this webhook should
     * run.
     */
    //var buildType by stringParameter()
    var buildTypes: BuildTypes2? = null

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
        parameters?.validate(consumer)
        headers?.validate(consumer)
    }

//    fun header(name: String, value: String) : Header {
//        val h = Header()
//        h.name = name
//        h.value = value
//        return h
//    }

    fun buildTypes(allProjectBuilds: () -> Unit, subProjectBuilds: Boolean) : BuildTypes {
        var buildTypes = BuildTypes(this, true, subProjectBuilds)
        return buildTypes
    }
    fun buildTypes(init: BuildTypes.() -> Unit, subProjectBuilds: Boolean) : BuildTypes {
        var buildTypes = BuildTypes(this, false, subProjectBuilds)
        return buildTypes
    }

    class BuildTypes(
        var feature: WebHookConfigurationNew,
        val allProjectBuilds : Boolean = false,
        val subProjectBuilds: Boolean = false
    ) : Validatable {

        private val myBuildTypes = mutableSetOf<BuildType>()

        fun buildType(buildType: BuildType) {
            myBuildTypes.add(buildType)
        }

        override fun validate(consumer: ErrorConsumer) {
            consumer.consumePropertyError("headers", "headers function was called more than once.")
        }
    }

    class Headers(val feature: WebHookConfigurationNew, init: Headers.() -> Unit) : Validatable {
        var alreadySeen = false
        val myHeaders = mutableMapOf<String,String>()
        init {
            init()
        }

        fun header(name: String, value: String) {
            // TODO: Throw exception if header added twice
            myHeaders[name] = value
            //this.feature.param(name, value)
        }

        override fun validate(consumer: ErrorConsumer) {
            if (alreadySeen) {
                consumer.consumePropertyError("headers", "headers function was called more than once.")
            }
        }
    }

    /**
     *  Defines headers for a webhook (optional).
     *
     *    headers {
     *        header(name = "foo1", value = "bar1")
     *        header(name = "foo2", value = "bar2")
     *        header("foo3", "bar3")
     *        header(
     *            name = "foo4",
     *            value = "bar4"
     *        )
     *    }
     */
    fun headers(init: Headers.() -> Unit): Headers {
        val headers = Headers(this, init)
        if (this.headers != null) {
            headers.alreadySeen = true
        }
        for((headerCounter, key) in headers.myHeaders.keys.withIndex()) {
            headers.feature.param("header_${headerCounter}_name", key)
            headers.myHeaders[key]?.let { headers.feature.param("header_${headerCounter}_value", it) }
        }
        this.headers = headers
        return headers
    }

    class Parameters(val feature: WebHookConfigurationNew, init: Parameters.() -> Unit) : Validatable {
        var alreadySeen = false
        val myParameters = mutableMapOf<String,Parameter>()
        init {
            init()
        }

        fun parameter(name: String, value: String) {
            // TODO: Throw exception if parameter added twice
            parameter(name, value, null, null, null, null)
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

        override fun validate(consumer: ErrorConsumer) {
            if (alreadySeen) {
                consumer.consumePropertyError("parameters", "parameters function was called more than once.")
            }
        }
    }

    /**
     *  Defines parameters for a webhook (optional).
     *
     *    parameters {
     *        parameter("fooParam1", "barParam1")
     *        parameter(
     *            name = "fooParam2",
     *            value = "barParam2",
     *            secure = true,
     *            forceResolveTeamCityVariable = true,
     *            includedInLegacyPayloads = true,
     *            templateEngine = WebHookConfigurationNew.TemplateEngine.VELOCITY
     *        )
     *    }
     */
    fun parameters(init: Parameters.() -> Unit): Parameters {
        val parameters = Parameters(this, init)
        if (this.parameters != null) {
            parameters.alreadySeen = true
        }
        for((parameterCounter, p) in parameters.myParameters.values.withIndex()) {
            parameters.feature.param("parameter_${parameterCounter}_name", p.name)
            parameters.feature.param("parameter_${parameterCounter}_value", p.value)
            p.secure?.let { parameters.feature.param("parameter_${parameterCounter}_secure", p.secure.toString()) }
            p.includedInLegacyPayloads?.let { parameters.feature.param("parameter_${parameterCounter}_includedInLegacyPayloads", p.includedInLegacyPayloads.toString()) }
            p.forceResolveTeamCityVariable?.let { parameters.feature.param("parameter_${parameterCounter}_forceResolveTeamCityVariable", p.forceResolveTeamCityVariable.toString()) }
            p.templateEngine?.let { parameters.feature.param("parameter_${parameterCounter}_templateEngine", p.templateEngine.toString()) }
        }
        this.parameters = parameters
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

