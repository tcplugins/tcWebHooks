## Template XML files

The files in this directory are used as inputs into "Spring" templates. See `webhook.teamcity.payload.template.AbstractXmlBasedWebHookTemplate`
A SpringTemplate is a way of "bundling" a template in tcWebHooks, but the configuration of the template is read from an XML file.

The benefit of this method is that one can edit the template in the tcWebHooks template editing UI, and then export it via the REST API and save it to an XML file.

#### Saving an XML version of a template

Get the ID of the template, and then load a URL based on that into your browser (or from curl or similar).

eg, `http://teamcity:8111/app/rest/webhooks/templates/id:slack.com/rawConfig`

Appending `/rawConfig` gets a version of the template as it looks in the `webhooks-template.xml` file. This is what the `AbstractXmlBasedWebHookTemplate` reads to get the template configuration, and then registers as a SpringTemplate.

