
## Todo List

- Prevent template from being removed if it's in use by a webhook.
- Display JSON (templated) for templates that are of type jsonTemplate
- Support templates other than JSON
- Convert complex payload fields to JSON for templates (or relevant payload if more than JSON supported). eg, changes, buildsteps
- Add support for specifying templating engine (eg, velocity).
- Find some way to be able to specify parts of complex objects.  eg buildRunners.
- Normalise WebHook parameter REST update semantics: align PUT /app/rest/webhooks/parameters/{projectId}/{parameterId} with GET/DELETE locator behaviour (id:...) and add tests/docs to avoid raw-id vs locator ambiguity.

