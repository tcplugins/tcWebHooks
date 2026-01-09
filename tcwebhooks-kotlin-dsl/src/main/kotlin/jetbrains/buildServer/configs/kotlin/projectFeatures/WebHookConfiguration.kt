package jetbrains.buildServer.configs.kotlin.projectFeatures

import jetbrains.buildServer.configs.kotlin.*

/**
 * A description of a class.
 *
 *
 * @see webHookConfiguration
 */
@TeamCityDsl
open class WebHookConfiguration() : ProjectFeature() {

    init {
        type = "tcWebHooks"
    }

    constructor(init: WebHookConfiguration.() -> Unit): this() {
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
     * Show simplified URL in UI and prevent payload from logging secure values.
     */
    @Suppress("unused")
    var hideSecureValues: Boolean? by booleanParameter()

    /**
     * Whether the webhook is enabled. Defaults to true if omitted.
     */
    @Suppress("unused")
    var enabled by booleanParameter()

    /**
     * Authentication type to use
     */
    @Suppress("unused")
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
            @Suppress("unused")
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
            @Suppress("unused")
            var preemptive by booleanParameter("basicAuthPreemptive")

            override fun validate(consumer: ErrorConsumer) {
                if (username == null && !hasParam("basicAuthUsername")) {
                    consumer.consumePropertyError("authentication.username", "mandatory 'authentication.username' property is not specified")
                }
                if (password == null && !hasParam("basicAuthPassword")) {
                    consumer.consumePropertyError("authentication.password", "mandatory 'authentication.password' property is not specified")
                }
            }
        }

        class Ntlm() : Authentication("ntlm") {

            /**
             * Username for NTLM Authentication
             */
            var username by stringParameter("ntlmUsername")

            /**
             * Password for NTLM Authentication
             */
            var password by stringParameter("ntlmPassword")

            /**
             * WorkStation to use for NTLM Authentication
             */
            var workstation by stringParameter("ntlmWorkStation")

            /**
             * Domain to use for NTLM Authentication
             */
            var domain by stringParameter("ntlmDomain")

            /**
             * Whether to enable pre-emptive authentication (defaults
             * to true). Without pre-emptive enabled, the webhook will get a 401
             * and then re-send the request with authentication enabled.
             */
            @Suppress("unused")
            var preemptive by booleanParameter("ntlmPreemptive")

            override fun validate(consumer: ErrorConsumer) {
                if (username == null && !hasParam("ntlmUsername")) {
                    consumer.consumePropertyError("authentication.username", "mandatory 'authentication.username' property is not specified")
                }
                if (password == null && !hasParam("ntlmPassword")) {
                    consumer.consumePropertyError("authentication.password", "mandatory 'authentication.password' property is not specified")
                }
                if (workstation == null && !hasParam("ntlmWorkStation")) {
                    consumer.consumePropertyError("authentication.workstation", "mandatory 'authentication.workstation' property is not specified")
                }
                if (domain == null && !hasParam("ntlmDomain")) {
                    consumer.consumePropertyError("authentication.domain", "mandatory 'authentication.domain' property is not specified")
                }
            }
        }
    }

    @Suppress("unused")
    fun bearer(init: Authentication.Bearer.() -> Unit = {}) : Authentication.Bearer {
        val result = Authentication.Bearer()
        result.init()
        return result
    }

    @Suppress("unused")
    fun basic(init: Authentication.Basic.() -> Unit = {}) : Authentication.Basic {
        val result = Authentication.Basic()
        result.init()
        return result
    }

    @Suppress("unused")
    fun ntlm(init: Authentication.Ntlm.() -> Unit = {}) : Authentication.Ntlm {
        val result = Authentication.Ntlm()
        result.init()
        return result
    }

    /**
     * Specifies to which system a status should be published
     */
    sealed class BuildTypes(
        @Suppress("unused")
        val feature: WebHookConfiguration,
        @Suppress("unused")
        val allProjectBuilds: Boolean = false,
        @Suppress("unused")
        var subProjectBuilds: Boolean = false
    ) : Validatable {
        
        abstract fun addToParams()


        class AllProjectBuilds(feature: WebHookConfiguration) : BuildTypes(feature, allProjectBuilds = true) {
            override fun addToParams() {
                this.feature.param("buildTypes" , "allProjectBuilds")
                subProjectBuilds.let { this.feature.param("subProjectBuilds" , subProjectBuilds.toString()) }
            }

            override fun validate(consumer: ErrorConsumer) {
                //TODO: add validation here
            }
        }

        class SelectedProjectBuilds(feature: WebHookConfiguration) :
            BuildTypes(feature, allProjectBuilds = false) {
            private val myBuildTypes = mutableSetOf<String>()
            override fun addToParams() {
                this.feature.param("buildTypes" , "selectedProjectBuilds")
                this.feature.param("buildTypeIds" , myBuildTypes.joinToString(", "))
                this.feature.param("subProjectBuilds" , subProjectBuilds.toString())
            }

            override fun validate(consumer: ErrorConsumer) {
                //TODO: add validation here
            }

            /**
             *
             */
            fun buildType(myBuildTypeFunction: () -> BuildType) {
                /** We are passed in a function like this...
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
                myBuildTypes.add(myBuildTypeFunction().id.toString())
            }
            fun buildType(myBuildType: BuildType) {
                myBuildTypes.add(myBuildType.id.toString())
            }

            fun buildTypeId(myBuildTypeId: String) {
                myBuildTypes.add(myBuildTypeId)
            }

            fun buildTypeId(myBuildTypeIdFunction: () -> String) {
                myBuildTypes.add(myBuildTypeIdFunction())
            }
        }
    }

    fun allProjectBuilds(init: BuildTypes.AllProjectBuilds.() -> Unit = {}) : BuildTypes.AllProjectBuilds {
        val result = BuildTypes.AllProjectBuilds(this)
        result.init()
        result.addToParams()
        return result
    }

    fun selectedProjectBuilds(init: BuildTypes.SelectedProjectBuilds.() -> Unit = {}) : BuildTypes.SelectedProjectBuilds {
        val result = BuildTypes.SelectedProjectBuilds(this)
        result.init()
        result.addToParams()
        return result
    }

    class BuildStates(
        private val feature: WebHookConfiguration, init: BuildStates.() -> Unit,
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
    private var triggerFilters: TriggerFilters? = null

    /**
     * Build Type references for which this webhook should
     * run.
     */
    //var buildType by stringParameter()
    var buildTypes: BuildTypes? = null

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
        if (buildStates == null && !hasParam("buildStates")) {
            consumer.consumePropertyError("buildStates", "mandatory 'buildStates' property is not specified")
        }

        buildTypes?.validate(consumer)
        parameters?.validate(consumer)
        headers?.validate(consumer)
        triggerFilters?.validate(consumer)
    }

    class Headers(val feature: WebHookConfiguration, init: Headers.() -> Unit) : SingleValue<String>("header"), Validatable {
        init {
            init()
        }

        fun header(name: String, value: String) {
            addValue(name, value)
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
        } else {
            for ((headerCounter, key) in headers.myValues.keys.withIndex()) {
                headers.feature.param("header_${headerCounter}_name", key)
                headers.myValues[key]?.let { headers.feature.param("header_${headerCounter}_value", it) }
            }
        }
        this.headers = headers
        return headers
    }

    class Parameters(val feature: WebHookConfiguration, init: Parameters.() -> Unit) : SingleValue<jetbrains.buildServer.configs.kotlin.projectFeatures.WebHookConfiguration.Parameters.Parameter>("parameter"), Validatable {
        init {
            init()
        }

        fun parameter(name: String, value: String) {
            parameter(name, value, null, null, null, null as String?)
        }

        fun parameter(
            name: String,
            value: String,
            secure: Boolean? = null,
            includedInLegacyPayloads: Boolean? = null,
            forceResolveTeamCityVariable: Boolean? = null,
            templateEngine: String? = null
            ) {
            addValue(name, Parameter(
                    name,
                    value,
                    secure,
                    includedInLegacyPayloads,
                    forceResolveTeamCityVariable,
                    templateEngine
                )
            )
        }

        override fun validate(consumer: ErrorConsumer) {
            super.validate(consumer)
            for (p in this.myValues.values) {
                p.templateEngine?.let {
                    if (!TemplateEngine.asStrings().contains(p.templateEngine.toString())) {
                        consumer.consumePropertyError("parameters", "templateEngine must be one of " + TemplateEngine.asStrings())
                    }
                }
            }
        }

        class Parameter(
            var name: String,
            var value: String,
            var secure: Boolean?,
            var includedInLegacyPayloads: Boolean?,
            var forceResolveTeamCityVariable: Boolean?,
            var templateEngine: String?
        )
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
     *            templateEngine = "VELOCITY"
     *        )
     *    }
     */
    fun parameters(init: Parameters.() -> Unit): Parameters {
        val parameters = Parameters(this, init)
        if (this.parameters != null) {
            parameters.alreadySeen = true
        } else {
            for ((parameterCounter, p) in parameters.myValues.values.withIndex()) {
                parameters.feature.param("parameter_${parameterCounter}_name", p.name)
                parameters.feature.param("parameter_${parameterCounter}_value", p.value)
                p.secure?.let { parameters.feature.param("parameter_${parameterCounter}_secure", p.secure.toString()) }
                p.includedInLegacyPayloads?.let {
                    parameters.feature.param(
                        "parameter_${parameterCounter}_includedInLegacyPayloads",
                        p.includedInLegacyPayloads.toString()
                    )
                }
                p.forceResolveTeamCityVariable?.let {
                    parameters.feature.param(
                        "parameter_${parameterCounter}_forceResolveTeamCityVariable",
                        p.forceResolveTeamCityVariable.toString()
                    )
                }
                p.templateEngine?.let {
                    parameters.feature.param(
                        "parameter_${parameterCounter}_templateEngine",
                        p.templateEngine.toString()
                    )
                }
            }
        }
        this.parameters = parameters
        return parameters
    }

    class TriggerFilters(val feature: WebHookConfiguration, init: TriggerFilters.() -> Unit) : SingleValue<jetbrains.buildServer.configs.kotlin.projectFeatures.WebHookConfiguration.TriggerFilters.TriggerFilter>("triggerFilter"), Validatable {
        init {
            init()
        }

        /**
         * Define a trigger filter and default to enabled.
         */
        fun triggerFilter(value: String, regex: String) {
            triggerFilter(value, regex, true)
        }

        /**
         * Define a trigger filter
         */
        fun triggerFilter(
            value: String,
            regex: String,
            enabled: Boolean,
            ) {
            addValue(value, TriggerFilter(
                value,
                regex,
                enabled)
            )
        }

        class TriggerFilter(
            var value: String,
            var regex: String,
            var enabled: Boolean?,
        )
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
     *            templateEngine = "VELOCITY"
     *        )
     *    }
     */
    fun triggerFilters(init: TriggerFilters.() -> Unit): TriggerFilters {
        val triggerFilters = TriggerFilters(this, init)
        if (this.triggerFilters != null) {
            triggerFilters.alreadySeen = true
        } else {
            for ((triggerFilterCounter, p) in triggerFilters.myValues.values.withIndex()) {
                triggerFilters.feature.param("triggerFilter_${triggerFilterCounter}_value", p.value)
                triggerFilters.feature.param("triggerFilter_${triggerFilterCounter}_regex", p.regex)
                triggerFilters.feature.param("triggerFilter_${triggerFilterCounter}_enabled", p.enabled.toString())
            }
        }
        this.triggerFilters = triggerFilters
        return triggerFilters
    }

    enum class TemplateEngine {
        STANDARD, VELOCITY;
        companion object {
            fun asStrings(): List<String> {
                return values().map(TemplateEngine::toString)
            }
        }
    }

    open class SingleValue<T>(nameType:String) : Validatable {
        var alreadySeen = false
        var myNameType = nameType
        var mySeenKeys = mutableMapOf<String,Int>()
        var myValues = mutableMapOf<String,T>()

        fun addValue(name:String, value:T) {
            this.mySeenKeys.putIfAbsent(name, 0)
            this.mySeenKeys[name] = mySeenKeys[name]!! + 1
            this.myValues[name] = value
        }

        override fun validate(consumer: ErrorConsumer) {
            if (alreadySeen) {
                consumer.consumePropertyError("${myNameType}s", "${myNameType}s function was called more than once.")
            }
            for (k in this.mySeenKeys.keys) {
                if (this.mySeenKeys[k]!! > 1) {
                    val count = this.mySeenKeys[k]
                    consumer.consumePropertyError("${myNameType}_${k}", "${myNameType} '${myNameType}s.${k}' is declared ${count} times")
                }
            }
        }
    }

}

/**
 * A description of a called function.
 *
 *
 * @see WebHookConfiguration
 */
fun ProjectFeatures.webHookConfiguration(init: WebHookConfiguration.() -> Unit): WebHookConfiguration {
    val result = WebHookConfiguration(init)
    feature(result)
    return result
}

fun ProjectFeatures.webHookConfiguration(webhookConfig: WebHookConfiguration): WebHookConfiguration {
    feature(webhookConfig)
    return webhookConfig
}

