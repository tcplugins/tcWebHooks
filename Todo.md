
## Todo List

- Require template format for createTempalate (POST). Fail to create if missing. In fact, implement a proper validator
	Don't allow creation of a template is that not a valid format. 
- Display JSON (templated) for templates that are of type jsonTemplate
- Support templates other than JSON
- Convert complex payload fields to JSON for templates (or relevant payload if more than JSON supported). eg, changes, buildsteps
- Add support for specifying templating engine (eg, velocity).
- Find some way to be able to specify parts of complex objects.  eg buildRunners.
- move useNonBranchTemplate checkbox so that it's visible from both templates.
- fix fullConfig link as it looks weird - done
