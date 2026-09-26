<%@ page isELIgnored="true" %>
<%
String serverRootUrl = (String)request.getAttribute("serverRootUrl");
if (serverRootUrl == null) {
  serverRootUrl = "";
}
String specUrl = (String)request.getAttribute("specUrl");
if (specUrl == null) {
  specUrl = "swagger.json";
}
%>
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>tcWebHooks REST API documentation</title>
  <style>
    :root {
      --ink: #1f2937;
      --muted: #64748b;
      --line: #d8e0ea;
      --paper: #ffffff;
      --wash: #f3f6fb;
      --brand: #0a3b66;
      --brand-2: #1f7a8c;
      --get: #116149;
      --post: #0d5a9c;
      --put: #8c4c08;
      --delete: #8b1e3f;
    }

    * { box-sizing: border-box; }

    body {
      margin: 0;
      font-family: "Segoe UI", Tahoma, sans-serif;
      color: var(--ink);
      background:
        radial-gradient(circle at 20% -10%, rgba(31, 122, 140, 0.22), transparent 35%),
        radial-gradient(circle at 90% 0%, rgba(10, 59, 102, 0.20), transparent 30%),
        var(--wash);
    }

    html {
      scroll-behavior: smooth;
    }

    .hero {
      padding: 26px 30px;
      color: #fff;
      background: linear-gradient(120deg, var(--brand), var(--brand-2));
      border-bottom: 1px solid rgba(255, 255, 255, 0.22);
    }

    .hero h1 {
      margin: 0;
      font-size: 27px;
      letter-spacing: 0.2px;
    }

    .hero p {
      margin: 9px 0 0;
      max-width: 880px;
      opacity: 0.94;
      line-height: 1.4;
    }

    .layout {
      display: grid;
      grid-template-columns: 1fr 320px;
      gap: 20px;
      max-width: 1340px;
      margin: 18px auto 28px;
      padding: 0 18px;
    }

    .sidebar {
      position: sticky;
      top: 12px;
      align-self: start;
      background: var(--paper);
      border: 1px solid var(--line);
      border-radius: 12px;
      overflow: hidden;
      order: 2;
    }

    .sidebar h2 {
      margin: 0;
      padding: 14px 16px;
      font-size: 13px;
      text-transform: uppercase;
      letter-spacing: 0.08em;
      color: var(--muted);
      background: #f9fbfe;
      border-bottom: 1px solid var(--line);
    }

    .meta {
      padding: 12px 16px;
      border-bottom: 1px solid var(--line);
      font-size: 13px;
      color: var(--muted);
      line-height: 1.55;
    }

    .main {
      display: grid;
      gap: 12px;
      align-content: start;
      order: 1;
    }

    .tree-wrap {
      max-height: calc(100vh - 130px);
      overflow: auto;
      padding: 8px 10px 12px;
    }

    .tree-root,
    .tree-root ul {
      list-style: none;
      margin: 0;
      padding: 0;
    }

    .tree-root ul {
      margin-left: 6px;
      border-left: 1px dashed #d0dae8;
      padding-left: 4px;
    }

    .tree-root details {
      margin: 0;
      padding: 0;
    }

    .tree-folder {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      font-size: 12px;
      color: #475569;
      cursor: pointer;
      user-select: none;
      padding: 3px 0;
      list-style: none;
    }

    .tree-folder::-webkit-details-marker {
      display: none;
    }

    .tree-folder::before {
      content: "\25B8";
      color: #94a3b8;
      font-size: 11px;
      transform: translateY(-1px);
    }

    details[open] > .tree-folder::before {
      content: "\25BE";
    }

    .tree-segment {
      font-family: Consolas, "Courier New", monospace;
      color: #0f172a;
    }

    .tree-endpoints {
      display: grid;
      gap: 3px;
      margin: 1px 0 3px;
    }

    .tree-link {
      display: grid;
      grid-template-columns: auto 1fr;
      align-items: center;
      gap: 6px;
      text-decoration: none;
      border: 1px solid transparent;
      border-radius: 8px;
      padding: 4px 5px;
      font-size: 12px;
      line-height: 1.3;
      color: #1e293b;
    }

    .tree-base {
      margin: 3px 0 6px;
      padding: 5px 7px;
      border: 1px solid #cbd5e1;
      border-radius: 7px;
      background: #f8fafc;
      font-family: Consolas, "Courier New", monospace;
      font-size: 11px;
      color: #334155;
    }

    .tree-text {
      min-width: 0;
    }

    .tree-link.is-active {
      background: linear-gradient(90deg, #dbeafe, #eff6ff);
      border-color: #60a5fa;
      box-shadow: inset 0 0 0 1px rgba(37, 99, 235, 0.28), 0 0 0 1px rgba(191, 219, 254, 0.7);
    }

    .tree-link.is-active .tree-path,
    .tree-link.is-active .tree-summary {
      color: #0f172a;
    }

    .tree-link:hover {
      background: #eef5ff;
      border-color: #c8ddf8;
    }

    .tree-method {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      min-width: 46px;
      padding: 2px 8px;
      border-radius: 999px;
      font-size: 10px;
      font-weight: 700;
      letter-spacing: 0.04em;
      color: #fff;
      text-transform: uppercase;
    }

    .tree-path {
      display: block;
      color: #334155;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      font-family: Consolas, "Courier New", monospace;
      font-size: 11px;
    }

    .tree-summary {
      display: block;
      color: #64748b;
      font-size: 11px;
      margin-top: 2px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .schema-link {
      color: #1d4ed8;
      text-decoration: none;
      border-bottom: 1px dotted #93c5fd;
    }

    .schema-link:hover {
      color: #1e40af;
      border-bottom-color: #1e40af;
    }

    .models-block {
      margin-top: 12px;
      padding-top: 10px;
      border-top: 1px solid var(--line);
    }

    .models-title {
      font-size: 11px;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.08em;
      color: var(--muted);
      margin-bottom: 6px;
    }

    .method-group {
      background: var(--paper);
      border: 1px solid var(--line);
      border-radius: 12px;
      overflow: hidden;
    }

    .method-group h3 {
      margin: 0;
      padding: 12px 16px;
      font-size: 14px;
      letter-spacing: 0.02em;
      text-transform: uppercase;
      color: var(--muted);
      background: #f9fbfe;
      border-bottom: 1px solid var(--line);
    }

    .endpoint {
      border-bottom: 1px solid var(--line);
      padding: 14px 16px 16px;
    }

    .endpoint:target {
      background: linear-gradient(180deg, #f8fbff, #f0f7ff);
      box-shadow: inset 0 0 0 2px rgba(59, 130, 246, 0.48), 0 0 0 2px rgba(191, 219, 254, 0.65);
      scroll-margin-top: 12px;
    }

    .endpoint:last-child {
      border-bottom: none;
    }

    .endpoint-header {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      align-items: center;
      margin-bottom: 7px;
    }

    .chip {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      min-width: 58px;
      padding: 3px 10px;
      border-radius: 999px;
      font-size: 12px;
      font-weight: 700;
      color: #fff;
      text-transform: uppercase;
      letter-spacing: 0.04em;
    }

    .GET { background: var(--get); }
    .POST { background: var(--post); }
    .PUT { background: var(--put); }
    .DELETE { background: var(--delete); }

    .path {
      font-family: Consolas, "Courier New", monospace;
      font-size: 13px;
      color: #0f172a;
    }

    .summary {
      font-weight: 600;
      margin-bottom: 3px;
    }

    .description {
      color: var(--muted);
      font-size: 14px;
      line-height: 1.45;
    }

    .narrative {
      margin-top: 10px;
      padding: 10px 11px;
      border: 1px solid #dbe6f3;
      border-radius: 8px;
      background: #f8fbff;
      font-size: 13px;
      color: #334155;
      line-height: 1.45;
    }

    .narrative strong {
      color: #0f172a;
    }

    .list-tight {
      margin: 6px 0 0;
      padding-left: 17px;
    }

    .list-tight li {
      margin: 2px 0;
    }

    .section-label {
      margin-top: 12px;
      font-size: 11px;
      letter-spacing: 0.08em;
      text-transform: uppercase;
      color: var(--muted);
      font-weight: 700;
    }

    table {
      width: 100%;
      border-collapse: collapse;
      margin-top: 8px;
      border: 1px solid var(--line);
      border-radius: 8px;
      overflow: hidden;
      font-size: 13px;
    }

    th, td {
      border-bottom: 1px solid var(--line);
      padding: 8px 9px;
      text-align: left;
      vertical-align: top;
    }

    th {
      background: #f8fbff;
      color: #334155;
      font-weight: 600;
    }

    tr:last-child td {
      border-bottom: none;
    }

    .pill {
      display: inline-block;
      padding: 1px 6px;
      border-radius: 999px;
      border: 1px solid #cbd5e1;
      background: #f8fafc;
      color: #334155;
      font-size: 11px;
      font-weight: 700;
      letter-spacing: 0.02em;
    }

    .detail-grid {
      margin-top: 10px;
      display: grid;
      grid-template-columns: 1fr;
      gap: 10px;
    }

    .detail-block {
      border: 1px solid var(--line);
      border-radius: 8px;
      overflow: hidden;
      background: #fcfdff;
    }

    .detail-head {
      background: #f4f8ff;
      padding: 8px 10px;
      font-size: 12px;
      font-weight: 600;
      color: #334155;
      border-bottom: 1px solid var(--line);
    }

    pre {
      margin: 0;
      padding: 11px 12px;
      overflow: auto;
      font-size: 12px;
      line-height: 1.45;
      background: #0b1220;
      color: #d7e3ff;
      font-family: Consolas, "Courier New", monospace;
    }

    .schema-note {
      padding: 8px 10px;
      font-size: 12px;
      color: #475569;
      border-top: 1px solid var(--line);
      background: #f8fbff;
    }

    .meta-row {
      margin-top: 10px;
      display: flex;
      flex-wrap: wrap;
      gap: 14px;
      font-size: 12px;
      color: var(--muted);
    }

    .meta-block code {
      background: #f1f5f9;
      border: 1px solid #dbe4ef;
      border-radius: 6px;
      padding: 1px 5px;
      color: #0f172a;
    }

    @media (max-width: 980px) {
      .layout { grid-template-columns: 1fr; }
      .sidebar {
        position: static;
        order: 2;
      }
      .main {
        order: 1;
      }
      .tree-wrap {
        max-height: none;
      }
    }
  </style>
</head>
<body>
  <div class="hero">
    <h1>tcWebHooks REST API documentation</h1>
    <p>Docs-first reference view generated from the OpenAPI specification. Designed for clear reading and operational use, not API-console interaction.</p>
  </div>

  <div class="layout">
    <aside class="sidebar">
      <h2>Reference And Tree</h2>
      <div class="meta" id="specMeta">Loading spec...</div>
      <div class="tree-wrap">
        <div class="description" id="treeStatus">Loading tree...</div>
        <ul class="tree-root" id="treeRoot"></ul>
        <div class="models-block">
          <div class="models-title">Models</div>
          <div class="description" id="modelStatus">Loading models...</div>
          <ul class="tree-root" id="modelRoot"></ul>
        </div>
      </div>
    </aside>
    <main class="main" id="content"></main>
  </div>

  <script>
    const SERVER_ROOT_URL = "<%= serverRootUrl %>";
    const SPEC_URL = "<%= specUrl %>";

    function asArray(value) {
      return Array.isArray(value) ? value : [];
    }

    function methodOrder(m) {
      const order = { GET: 1, POST: 2, PUT: 3, DELETE: 4, PATCH: 5 };
      return order[m] || 99;
    }

    function slug(input) {
      return input.toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/(^-|-$)/g, "");
    }

    function escapeHtml(value) {
      return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/\"/g, "&quot;")
        .replace(/'/g, "&#39;");
    }

    function prettyJson(value) {
      return escapeHtml(JSON.stringify(value, null, 2));
    }

    function isLikelyMethodName(value) {
      return typeof value === "string" && /^[a-z][A-Za-z0-9]*$/.test(value);
    }

    function humanizeIdentifier(value) {
      if (!value) {
        return "";
      }
      return String(value)
        .replace(/([a-z0-9])([A-Z])/g, "$1 $2")
        .replace(/[_-]+/g, " ")
        .replace(/\s+/g, " ")
        .trim()
        .replace(/^./, c => c.toUpperCase());
    }

    function readableOperationSummary(method, path, operation) {
      const summary = operation && operation.summary ? String(operation.summary).trim() : "";
      if (summary) {
        return isLikelyMethodName(summary) ? humanizeIdentifier(summary) : summary;
      }
      return `${method} ${path}`;
    }

    function readableOperationDescription(method, path, operation) {
      const description = operation && operation.description ? String(operation.description).trim() : "";
      if (description) {
        return description;
      }
      const summary = readableOperationSummary(method, path, operation);
      return `${summary} for ${path}.`; 
    }

    function refName(ref) {
      if (!ref) {
        return "";
      }
      const parts = ref.split("/");
      return parts[parts.length - 1] || ref;
    }

    function getSchemaMap(spec) {
      return spec.definitions || (spec.components && spec.components.schemas) || {};
    }

    function collectSchemaRefsFromSchema(schema, refs) {
      if (!schema || typeof schema !== "object") {
        return;
      }
      if (schema.$ref) {
        refs.add(refName(schema.$ref));
      }
      if (schema.items) {
        collectSchemaRefsFromSchema(schema.items, refs);
      }
      if (schema.properties) {
        Object.keys(schema.properties).forEach(key => {
          collectSchemaRefsFromSchema(schema.properties[key], refs);
        });
      }
      if (schema.additionalProperties && typeof schema.additionalProperties === "object") {
        collectSchemaRefsFromSchema(schema.additionalProperties, refs);
      }
      if (schema.allOf) {
        asArray(schema.allOf).forEach(s => collectSchemaRefsFromSchema(s, refs));
      }
      if (schema.oneOf) {
        asArray(schema.oneOf).forEach(s => collectSchemaRefsFromSchema(s, refs));
      }
      if (schema.anyOf) {
        asArray(schema.anyOf).forEach(s => collectSchemaRefsFromSchema(s, refs));
      }
    }

    function collectReferencedModelNames(spec, endpoints) {
      const refs = new Set();
      endpoints.forEach(endpoint => {
        const operation = endpoint.operation || {};
        asArray(operation.parameters).forEach(param => {
          if (param && param.schema) {
            collectSchemaRefsFromSchema(param.schema, refs);
          }
          if (param && param.content) {
            Object.keys(param.content).forEach(contentType => {
              const media = param.content[contentType] || {};
              collectSchemaRefsFromSchema(media.schema, refs);
            });
          }
        });

        Object.keys(operation.responses || {}).forEach(code => {
          const response = operation.responses[code] || {};
          if (response.schema) {
            collectSchemaRefsFromSchema(response.schema, refs);
          }
          if (response.content) {
            Object.keys(response.content).forEach(contentType => {
              const media = response.content[contentType] || {};
              collectSchemaRefsFromSchema(media.schema, refs);
            });
          }
        });

        if (operation.requestBody && operation.requestBody.content) {
          Object.keys(operation.requestBody.content).forEach(contentType => {
            const media = operation.requestBody.content[contentType] || {};
            collectSchemaRefsFromSchema(media.schema, refs);
          });
        }
      });
      return Array.from(refs).sort((a, b) => a.localeCompare(b));
    }

    function getModelEntries(spec, endpoints) {
      const defs = getSchemaMap(spec);
      const defNames = Object.keys(defs);
      if (defNames.length) {
        return defNames.sort((a, b) => a.localeCompare(b)).map(name => ({ name: name, def: defs[name], resolved: true }));
      }

      return collectReferencedModelNames(spec, endpoints).map(name => ({
        name: name,
        def: defs[name] || null,
        resolved: !!defs[name]
      }));
    }

    function resolveRefSchema(spec, schema) {
      if (!schema || !schema.$ref) {
        return null;
      }
      const schemas = getSchemaMap(spec);
      const name = refName(schema.$ref);
      return schemas && schemas[name] ? schemas[name] : null;
    }

    function schemaLabel(schema) {
      if (!schema) {
        return "none";
      }
      if (schema.$ref) {
        return "#/definitions/" + refName(schema.$ref);
      }
      if (schema.type === "array" && schema.items) {
        if (schema.items.$ref) {
          return "array<" + refName(schema.items.$ref) + ">";
        }
        return "array<" + (schema.items.type || "object") + ">";
      }
      return schema.type || "object";
    }

    function modelAnchor(name) {
      return `model-${slug(name)}`;
    }

    function schemaLabelHtml(schema) {
      if (!schema) {
        return "none";
      }
      if (schema.$ref) {
        const name = refName(schema.$ref);
        return `<a class="schema-link" href="#${modelAnchor(name)}">#/definitions/${escapeHtml(name)}</a>`;
      }
      if (schema.type === "array" && schema.items) {
        if (schema.items.$ref) {
          const name = refName(schema.items.$ref);
          return `array&lt;<a class="schema-link" href="#${modelAnchor(name)}">${escapeHtml(name)}</a>&gt;`;
        }
        return `array&lt;${escapeHtml(schema.items.type || "object")}&gt;`;
      }
      return escapeHtml(schema.type || "object");
    }

    function definitionForSchema(spec, schema) {
      if (!schema) {
        return null;
      }
      if (schema.$ref) {
        return resolveRefSchema(spec, schema);
      }
      if (schema.type === "array" && schema.items && schema.items.$ref) {
        return resolveRefSchema(spec, schema.items);
      }
      return null;
    }

    function renderSchemaFieldsTable(spec, schema) {
      const def = definitionForSchema(spec, schema);
      if (!def || !def.properties) {
        return "";
      }

      function inferExampleValue(name, prop) {
        if (!prop) {
          return "";
        }
        if (prop.example !== undefined) {
          return typeof prop.example === "string" ? prop.example : JSON.stringify(prop.example);
        }
        if (Array.isArray(prop.enum) && prop.enum.length) {
          return String(prop.enum[0]);
        }
        if (prop.$ref) {
          return "{...}";
        }
        if (prop.type === "array") {
          if (prop.items && prop.items.$ref) {
            return "[{...}]";
          }
          if (prop.items && prop.items.type) {
            const itemType = prop.items.type;
            if (itemType === "string") {
              return "[\"value\"]";
            }
            if (itemType === "integer") {
              return "[123]";
            }
            if (itemType === "number") {
              return "[12.34]";
            }
            if (itemType === "boolean") {
              return "[true]";
            }
          }
          return "[]";
        }

        const n = String(name || "").toLowerCase();
        if (prop.type === "string") {
          if (n.indexOf("url") >= 0 || n.indexOf("href") >= 0) return "https://example.local/path";
          if (n.indexOf("id") >= 0) return "example-id";
          if (n.indexOf("name") >= 0) return "Example Name";
          if (n.indexOf("date") >= 0 || n.indexOf("time") >= 0) return "2026-01-01T12:00:00Z";
          return "value";
        }
        if (prop.type === "integer") return "123";
        if (prop.type === "number") return "12.34";
        if (prop.type === "boolean") return "true";
        if (prop.type === "object") return "{...}";
        return "";
      }

      const required = new Set(asArray(def.required));
      const rows = Object.entries(def.properties).map(([name, prop]) => {
        const typeHtml = prop.$ref
          ? `<a class="schema-link" href="#${modelAnchor(refName(prop.$ref))}">${escapeHtml(refName(prop.$ref))}</a>`
          : prop.type === "array" && prop.items && prop.items.$ref
            ? `array&lt;<a class="schema-link" href="#${modelAnchor(refName(prop.items.$ref))}">${escapeHtml(refName(prop.items.$ref))}</a>&gt;`
            : escapeHtml(prop.type === "array" && prop.items ? `array<${prop.items.type || "object"}>` : (prop.type || "object"));

        const example = inferExampleValue(name, prop);
        const description = prop.description || "No field description in generated spec.";

        return `
          <tr>
            <td><strong>${escapeHtml(name)}</strong></td>
            <td><code>${typeHtml}</code></td>
            <td>${required.has(name) ? "required" : "optional"}</td>
            <td>${escapeHtml(description)}</td>
            <td>${escapeHtml(example)}</td>
          </tr>`;
      }).join("");

      return `
        <div class="section-label">Schema Fields (${escapeHtml(refName((schema && schema.$ref) || "")) || "inline"})</div>
        <table>
          <thead>
            <tr>
              <th>Field</th>
              <th>Type</th>
              <th>Required</th>
              <th>Description</th>
              <th>Example</th>
            </tr>
          </thead>
          <tbody>${rows}</tbody>
        </table>`;
    }

    function getBodyParam(operation) {
      return asArray(operation.parameters).find(param => param.in === "body") || null;
    }

    function bodyExample(spec, operation) {
      const body = getBodyParam(operation);
      if (!body) {
        return null;
      }
      if (body["x-examples"]) {
        const first = Object.keys(body["x-examples"])[0];
        if (first) {
          return body["x-examples"][first];
        }
      }
      if (body.schema && body.schema.example) {
        return body.schema.example;
      }
      const resolved = resolveRefSchema(spec, body.schema);
      if (resolved && resolved.example) {
        return resolved.example;
      }
      return null;
    }

    function responseExample(spec, response) {
      if (!response) {
        return null;
      }
      if (response.examples) {
        const first = Object.keys(response.examples)[0];
        if (first) {
          return response.examples[first];
        }
      }
      if (response.schema && response.schema.example) {
        return response.schema.example;
      }
      const resolved = resolveRefSchema(spec, response.schema);
      if (resolved && resolved.example) {
        return resolved.example;
      }
      return null;
    }

    function renderParamRows(params) {
      if (!params.length) {
        return "<div class=\"description\">No parameters.</div>";
      }
      const rows = params.map(param => {
        const type = param.schema ? schemaLabel(param.schema) : (param.type || "string");
        const required = param.required ? "required" : "optional";
        const example = param.example !== undefined ? String(param.example) : "";
        return `
          <tr>
            <td><strong>${escapeHtml(param.name || "")}</strong></td>
            <td><span class="pill">${escapeHtml(param.in || "")}</span></td>
            <td><code>${escapeHtml(type)}</code></td>
            <td>${escapeHtml(required)}</td>
            <td>${escapeHtml(param.description || "")}</td>
            <td>${escapeHtml(example)}</td>
          </tr>`;
      }).join("");

      return `
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>In</th>
              <th>Type</th>
              <th>Required</th>
              <th>Description</th>
              <th>Example</th>
            </tr>
          </thead>
          <tbody>${rows}</tbody>
        </table>`;
    }

    function stripBasePath(path, basePath) {
      const p = path || "/";
      const base = basePath || "";
      if (!base || base === "/") {
        return p;
      }
      if (p === base) {
        return "/";
      }
      if (p.indexOf(base + "/") === 0) {
        return p.substring(base.length) || "/";
      }
      return p;
    }

    function buildEndpointTree(endpoints, basePath) {
      const root = { children: new Map(), endpoints: [] };
      endpoints.forEach(endpoint => {
        endpoint.treePath = stripBasePath(endpoint.path, basePath);
        const parts = endpoint.treePath.split("/").filter(Boolean);
        if (!parts.length) {
          root.endpoints.push(endpoint);
          return;
        }

        let node = root;
        parts.forEach(part => {
          if (!node.children.has(part)) {
            node.children.set(part, { name: part, children: new Map(), endpoints: [] });
          }
          node = node.children.get(part);
        });
        node.endpoints.push(endpoint);
      });
      return root;
    }

    function treeEndpointLink(endpoint) {
      const target = slug(endpoint.method + " " + endpoint.path);
      const summary = readableOperationSummary(endpoint.method, endpoint.path, endpoint.operation);
      const displayPath = endpoint.treePath || endpoint.path || "/";
      return `
        <a class="tree-link" href="#${target}" title="${escapeHtml(endpoint.operation.summary || endpoint.path)}">
          <span class="tree-method ${endpoint.method}">${endpoint.method}</span>
          <span class="tree-text">
            <span class="tree-path">${escapeHtml(displayPath)}</span>
            <span class="tree-summary">${escapeHtml(summary)}</span>
          </span>
        </a>`;
    }

    function renderTreeNode(node) {
      const children = Array.from(node.children.values()).sort((a, b) => a.name.localeCompare(b.name));
      const endpointLinks = node.endpoints
        .slice()
        .sort((a, b) => methodOrder(a.method) - methodOrder(b.method))
        .map(treeEndpointLink)
        .join("");

      const childrenHtml = children.map(child => {
        return `
          <li>
            <details open>
              <summary class="tree-folder"><span class="tree-segment">${escapeHtml(child.name)}</span></summary>
              ${renderTreeNode(child)}
            </details>
          </li>`;
      }).join("");

      return `
        ${endpointLinks ? `<div class="tree-endpoints">${endpointLinks}</div>` : ""}
        ${childrenHtml ? `<ul>${childrenHtml}</ul>` : ""}`;
    }

    function renderEndpointTree(spec, endpoints) {
      const treeRoot = document.getElementById("treeRoot");
      const treeStatus = document.getElementById("treeStatus");
      const tree = buildEndpointTree(endpoints, spec.basePath || "");
      const baseLine = spec.basePath
        ? `<li><div class="tree-base">${escapeHtml(spec.basePath)}</div></li>`
        : "";
      treeRoot.innerHTML = `${baseLine}${renderTreeNode(tree)}`;
      treeStatus.textContent = `${endpoints.length} endpoint operations`;
    }

    function renderModels(spec, endpoints) {
      const modelRoot = document.getElementById("modelRoot");
      const modelStatus = document.getElementById("modelStatus");
      const models = getModelEntries(spec, endpoints);

      modelStatus.textContent = `${models.length} models`;
      modelRoot.innerHTML = models.map(model => {
        const def = model.def || {};
        const name = model.name;
        const fieldCount = def.properties ? Object.keys(def.properties).length : 0;
        const summary = def.description || (model.resolved ? `Auto-generated model with ${fieldCount} fields` : "Referenced model (schema not embedded)");
        return `<li>
          <a class="tree-link" href="#${modelAnchor(name)}" title="${escapeHtml(summary)}">
            <span class="tree-method" style="background:#334155;">MODEL</span>
            <span class="tree-text">
              <span class="tree-path">${escapeHtml(name)}</span>
              <span class="tree-summary">${escapeHtml(summary)}</span>
            </span>
          </a>
        </li>`;
      }).join("");
    }

    function renderModelsSection(spec, endpoints) {
      const models = getModelEntries(spec, endpoints);
      if (!models.length) {
        return "";
      }

      const blocks = models.map(model => {
        const def = model.def || {};
        const name = model.name;
        const fieldCount = def.properties ? Object.keys(def.properties).length : 0;
        const description = def.description || (model.resolved
          ? `Auto-generated schema for ${name} with ${fieldCount} fields. Source annotations did not provide a richer description.`
          : "Schema details were not embedded in this spec.");
        return `
          <article class="endpoint" id="${modelAnchor(name)}">
            <div class="endpoint-header">
              <span class="chip" style="background:#334155;">MODEL</span>
              <div class="path">${escapeHtml(name)}</div>
            </div>
            <div class="description">${escapeHtml(description)}</div>
            ${model.resolved ? renderSchemaFieldsTable(spec, { $ref: `#/definitions/${name}` }) : ""}
          </article>`;
      }).join("");

      return `<section class="method-group"><h3>Models</h3>${blocks}</section>`;
    }

    function renderResponses(spec, responses) {
      const codes = Object.keys(responses || {}).sort();
      if (!codes.length) {
        return "<div class=\"description\">No responses documented.</div>";
      }
      return codes.map(code => {
        const response = responses[code] || {};
        const schema = response.schema;
        const example = responseExample(spec, response);
        return `
          <div class="detail-block">
            <div class="detail-head">HTTP ${escapeHtml(code)} - ${escapeHtml(response.description || "")}</div>
            <div class="schema-note">Schema: <code>${schemaLabelHtml(schema)}</code></div>
            ${example ? `<pre>${prettyJson(example)}</pre>` : `<div class="schema-note">No example provided.</div>`}
            ${renderSchemaFieldsTable(spec, schema)}
          </div>`;
      }).join("");
    }

    function buildCurl(method, fullPath, body) {
      const serverRoot = SERVER_ROOT_URL || "https://teamcity.example.com";
      const lines = [
        `curl -X ${method} \\`,
        `  '${serverRoot}${fullPath}' \\`,
        `  -H 'Authorization: Bearer <token>' \\`,
        `  -H 'Accept: application/json'`
      ];

      if (body) {
        lines.push("  -H 'Content-Type: application/json' \\");
        lines.push("  -d '" + JSON.stringify(body).replace(/'/g, "'\\''") + "'");
      }
      return lines.join("\n");
    }

    function buildHttpRequest(method, fullPath, body) {
      const serverRoot = SERVER_ROOT_URL || "https://teamcity.example.com";
      const host = serverRoot.replace(/^https?:\/\//, "");
      let text = `${method} ${fullPath} HTTP/1.1\nHost: ${host}\nAuthorization: Bearer <token>\nAccept: application/json`;
      if (body) {
        text += "\nContent-Type: application/json\n\n" + JSON.stringify(body, null, 2);
      }
      return text;
    }

    function buildHttpResponse(code, body) {
      let text = `HTTP/1.1 ${code}`;
      if (body) {
        text += "\nContent-Type: application/json\n\n" + JSON.stringify(body, null, 2);
      }
      return text;
    }

    function buildOperationPath(spec, path) {
      const p = path || "/";
      const base = spec.basePath || "";
      if (!base || base === "/") {
        return p;
      }
      if (p === base || p.indexOf(base + "/") === 0) {
        return p;
      }
      return base + p;
    }

    function renderNarrative(operation) {
      const purpose = operation["x-purpose"] || operation.description || "";
      const notes = asArray(operation["x-notes"]);
      if (!purpose && !notes.length) {
        return "";
      }

      const notesHtml = notes.length
        ? `<ul class="list-tight">${notes.map(note => `<li>${escapeHtml(note)}</li>`).join("")}</ul>`
        : "";

      return `
        <div class="narrative">
          ${purpose ? `<div><strong>Purpose:</strong> ${escapeHtml(purpose)}</div>` : ""}
          ${notes.length ? `<div style="margin-top:6px;"><strong>Notes:</strong>${notesHtml}</div>` : ""}
        </div>`;
    }

    function createEndpointCard(spec, path, method, operation) {
      const id = slug(method + " " + path);
      const responses = Object.keys(operation.responses || {}).sort();
      const params = asArray(operation.parameters);
      const nonBodyParams = params.filter(param => param.in !== "body");
      const body = getBodyParam(operation);
      const requestExample = bodyExample(spec, operation);
      const primaryResponseCode = Object.keys(operation.responses || {}).sort()[0];
      const primaryResponse = (operation.responses || {})[primaryResponseCode] || {};
      const primaryResponseExample = responseExample(spec, primaryResponse);
      const fullPath = buildOperationPath(spec, path);
      const summary = readableOperationSummary(method, path, operation);
      const description = readableOperationDescription(method, path, operation);

      return `
        <article class="endpoint" id="${id}">
          <div class="endpoint-header">
            <span class="chip ${method}">${method}</span>
            <div class="path">${path}</div>
          </div>
          <div class="summary">${escapeHtml(summary)}</div>
          <div class="description">${escapeHtml(description)}</div>
          ${renderNarrative(operation)}
          <div class="meta-row">
            <div class="meta-block">Responses: ${responses.map(code => `<code>${code}</code>`).join(" ") || "none"}</div>
            <div class="meta-block">Params: <code>${params.length}</code></div>
          </div>

          <div class="section-label">Parameters</div>
          ${renderParamRows(nonBodyParams)}

          ${body ? `
          <div class="section-label">Request Body</div>
          <div class="detail-grid">
            <div class="detail-block">
              <div class="detail-head">Body Schema</div>
              <div class="schema-note"><code>${schemaLabelHtml(body.schema)}</code> ${escapeHtml(body.description || "")}</div>
              ${requestExample ? `<pre>${prettyJson(requestExample)}</pre>` : `<div class="schema-note">No request example provided.</div>`}
            </div>
          </div>` : ""}

          <div class="section-label">Responses</div>
          <div class="detail-grid">
            ${renderResponses(spec, operation.responses || {})}
          </div>

          <div class="section-label">Examples</div>
          <div class="detail-grid">
            <div class="detail-block">
              <div class="detail-head">cURL</div>
              <pre>${escapeHtml(buildCurl(method, fullPath, requestExample))}</pre>
            </div>
            <div class="detail-block">
              <div class="detail-head">HTTP Request</div>
              <pre>${escapeHtml(buildHttpRequest(method, fullPath, requestExample))}</pre>
            </div>
            <div class="detail-block">
              <div class="detail-head">HTTP Response (${escapeHtml(primaryResponseCode || "n/a")})</div>
              <pre>${escapeHtml(buildHttpResponse(primaryResponseCode || "200", primaryResponseExample))}</pre>
            </div>
          </div>

        </article>
      `;
    }

    function render(spec) {
      const content = document.getElementById("content");
      const specMeta = document.getElementById("specMeta");

      const endpoints = [];
      Object.entries(spec.paths || {}).forEach(([path, operations]) => {
        Object.entries(operations || {}).forEach(([method, operation]) => {
          const upper = method.toUpperCase();
          if (["GET", "POST", "PUT", "DELETE", "PATCH"].includes(upper)) {
            endpoints.push({ path, method: upper, operation: operation || {} });
          }
        });
      });

      endpoints.sort((a, b) => {
        if (a.path === b.path) {
          return methodOrder(a.method) - methodOrder(b.method);
        }
        return a.path.localeCompare(b.path);
      });

      specMeta.innerHTML = `
        <div><strong>${spec.info && spec.info.title ? spec.info.title : "REST API"}</strong></div>
        <div>Version: ${spec.info && spec.info.version ? spec.info.version : "n/a"}</div>
        <div>Base path: <code>${spec.basePath || "/"}</code></div>
        <div>Endpoints: ${endpoints.length}</div>
        <div>Auth: <code>Authorization: Bearer &lt;token&gt;</code></div>
      `;

      content.innerHTML = `<section class="method-group"><h3>Endpoints</h3>${endpoints
        .map(ep => createEndpointCard(spec, ep.path, ep.method, ep.operation))
        .join("")}</section>${renderModelsSection(spec, endpoints)}`;

      renderEndpointTree(spec, endpoints);
      renderModels(spec, endpoints);
      syncHashHighlight();
    }

    function syncHashHighlight() {
      const hash = window.location.hash;
      const navLinks = document.querySelectorAll('.tree-link');
      navLinks.forEach(link => {
        link.classList.remove('is-active');
      });

      if (!hash) {
        return;
      }

      const selector = `.tree-link[href="${hash.replace(/"/g, '\\"')}"]`;
      const active = document.querySelector(selector);
      if (!active) {
        return;
      }

      active.classList.add('is-active');
      let parent = active.parentElement;
      while (parent) {
        if (parent.tagName && parent.tagName.toLowerCase() === 'details') {
          parent.open = true;
        }
        parent = parent.parentElement;
      }
    }

    window.addEventListener('hashchange', syncHashHighlight);

    fetch(SPEC_URL)
      .then(response => response.json())
      .then(render)
      .catch(error => {
        document.getElementById("specMeta").textContent = "Failed to load spec file.";
        document.getElementById("treeStatus").textContent = "Failed to load endpoint tree.";
        document.getElementById("modelStatus").textContent = "Failed to load models.";
        document.getElementById("content").innerHTML = `<section class="method-group"><h3>Error</h3><article class="endpoint"><div class="summary">Cannot render API reference</div><div class="description">${String(error)}</div></article></section>`;
      });
  </script>
</body>
</html>
