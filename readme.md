

# tcWebHooks - A TeamCity plugin to send webhooks for build events

tcWebHooks is a TeamCity plugin which sends webhook HTTP POST requests to a destination of your choice.
WebHook POSTs for events are a great way to use an event driven architecture to get updates in other systems, rather than polling teamcity for build state changes.  

Typical use cases include:
- Posting messages to chat systems
- Informing systems of events that should trigger an action. eg, deployment, packaging, etc
- Controlling hardware like a lava lamp, build bunny, etc.
- Anything else that would like to know about an event, rather than having to poll.

## Features

### Standard features
- WebUI for [creating webhooks](https://github.com/tcplugins/tcWebHooks/wiki/Creating-a-WebHook "Creating a WebHook") 
- A customizable webhook body via various different means (templates, build properties, custom parameters)
- Can be triggered from many different events in the build lifecycle
- [WebHook Templates](https://github.com/tcplugins/tcWebHooks/wiki/WebHook-Templates-:-An-Introduction), for specification of a payload format once and re-use across multiple webhook configurations
- A set of templates for common services. Eg, Slack and MatterMost, Flowdock, ElasticSearch
- A standard list of [variables](https://github.com/tcplugins/tcWebHooks/wiki/Example-Webhook-output "Example Webhook output") which can be used for assembling a custom payload (eg, `${buildStatusUrl}`), as well as access to the TeamCity build properties (eg, `${teamcity.agent.jvm.os.name}`).

### Advanced Features
- Support for [using a proxy](https://github.com/tcplugins/tcWebHooks/wiki/Using-a-proxy-server "Using a proxy server")  for outbound requests.
- Regular Expression [Filter support](https://github.com/tcplugins/tcWebHooks/wiki/Applying-Filtering-Criteria-To-WebHook-Execution "Applying Filtering Criteria To WebHook Execution") , which allows conditionally controlling an event based on text in the webhook properties 
- A [REST API](https://github.com/tcplugins/tcWebHooks/wiki/WebHooks-REST-API "WebHooks REST API")  for creating/modifying webhook templates (with a plan to support creating/editing webhooks in the future)
- Import and export of webhook templates for sharing via REST API.
- Control of [HTTP timeouts](https://github.com/tcplugins/tcWebHooks/wiki/Adjusting-HTTP-timeouts-on-a-webhook "Adjusting HTTP timeouts on a webhook") 


## Screenshots
#### WebHook Creation/Editing screen
![Screenshot showing the "Add New WebHook" screen](https://raw.githubusercontent.com/tcplugins/tcWebHooks/master/docs/images/Screenshot_AddWebHook_04_ConfigureUrlFormatAndEvents.png "Add New WebHook") 

#### WebHook Template Payload Editing screen
![Screenshot showing the "Edit Build Event Template" screen](https://raw.githubusercontent.com/tcplugins/tcWebHooks/master/docs/images/Screenshot_Templates_EditBuildEventTemplate.png "Edit Build Event Template") 

#### Error logging screen in Admin Tab
![Screenshot showing the "WebHooks Admin tab"](https://raw.githubusercontent.com/tcplugins/tcWebHooks/master/docs/images/Screenshot_AdminTab_StatsAndErrors.png "WebHooks Admin Tab in TeamCity administration section") 
