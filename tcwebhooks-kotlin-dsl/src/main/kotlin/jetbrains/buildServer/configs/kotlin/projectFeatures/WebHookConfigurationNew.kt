package jetbrains.buildServer.configs.kotlin.projectFeatures

import jetbrains.buildServer.configs.kotlin.*

/**
 * A description of a class.
 *
 *
 * @see webHookConfigurationNew
 */
@TeamCityDsl
open class WebHookConfigurationNew() : ProjectFeature() {

    init {
        type = "tcWebHook"
    }

    constructor(init: WebHookConfigurationNew.() -> Unit): this() {
        init()
    }

    /**
     * WebHook Id. Must be unique across the whole of teamcity. Suggestion: use a
     * name like 'ProjectIdentifier_WebHook_01'
     */
    var webHookId by stringParameter()

    /**
     * WebHook URL. The URL to which the webhook will be POST'd.
     */
    var url by stringParameter()

    /**
     * The templateId referring to the template this webhook will use.
     */
    var template by stringParameter()

    /**
     * A flag to instruct the webhook to run against all builds in this
     * project. If only selected builds are required, set this to false and
     * add a
     */
    var allProjectBuilds by booleanParameter("allProjectBuildsEnabled")

    /**
     * A flag to instruct the webhook to run against all builds sub-projects of this
     * project. If only selected builds are required, set this to false and
     * add a
     */
    var subProjectBuilds by booleanParameter("subProjectBuildsEnabled")

    /**
     * Build Type references for which this webhook should run.
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
    }
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
