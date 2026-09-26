package webhook.teamcity.server.rest.docs;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.swagger.converter.ModelConverters;
import io.swagger.annotations.ApiOperation;
import io.swagger.jaxrs.Reader;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.RefModel;
import io.swagger.models.Response;
import io.swagger.models.Info;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.parameters.SerializableParameter;
import io.swagger.models.properties.RefProperty;
import io.swagger.util.Json;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import webhook.teamcity.server.rest.request.Constants;
import webhook.teamcity.server.rest.request.StatisticsRequest;
import webhook.teamcity.server.rest.request.TemplateRequest;
import webhook.teamcity.server.rest.request.TemplateTestRequest;
import webhook.teamcity.server.rest.request.WebHookParametersRequest;
import webhook.teamcity.server.rest.request.WebHooksConfigRequest;
import webhook.teamcity.server.rest.request.WebHooksIndexRequest;
import webhook.teamcity.server.rest.request.WebHooksRequest;

/**
 * Generates Swagger/OpenAPI JSON derived from JAX-RS resources.
 */
public final class OpenApiDocGenerator {

    private OpenApiDocGenerator() {
    }

    public static void main(String[] args) throws Exception {
        Swagger seed = new Swagger();
        Info info = new Info();
        info.setTitle("tcWebHooks REST API");
        info.setVersion("generated");
        info.setDescription("Generated from JAX-RS resource classes in tcwebhooks-rest-api.");
        seed.setInfo(info);
        seed.setBasePath(Constants.API_URL);

        Set<Class<?>> resources = new LinkedHashSet<>(Arrays.asList(
                StatisticsRequest.class,
                TemplateRequest.class,
                TemplateTestRequest.class,
                WebHookParametersRequest.class,
                WebHooksConfigRequest.class,
                WebHooksIndexRequest.class,
                WebHooksRequest.class));

        Reader reader = new Reader(seed);
        enableScanAllResources(reader);

        Swagger swagger = reader.read(resources);
        if (swagger.getPaths() == null || swagger.getPaths().isEmpty()) {
            swagger = buildFromJaxRsAnnotations(seed, resources);
        }
        if (swagger.getPaths() == null || swagger.getPaths().isEmpty()) {
            throw new IllegalStateException("Generated Swagger spec contains no paths. Resource scanning did not discover REST endpoints.");
        }

        Path specFile = args != null && args.length > 0 && args[0] != null && !args[0].trim().isEmpty()
                ? Paths.get(args[0])
                : Paths.get("target", "rest-docs", "swagger.json");
        Files.createDirectories(specFile.getParent());
        Files.write(specFile, Json.pretty(swagger).getBytes(StandardCharsets.UTF_8));

        System.out.println("Generated Swagger spec:");
        System.out.println(" - " + specFile.toAbsolutePath());
    }

    private static Swagger buildFromJaxRsAnnotations(Swagger seed, Set<Class<?>> resources) {
        Swagger swagger = new Swagger();
        swagger.setInfo(seed.getInfo());
        swagger.setBasePath(seed.getBasePath());

        Map<String, io.swagger.models.Path> builtPaths = new LinkedHashMap<>();
        Map<String, Model> definitions = new LinkedHashMap<>();
        for (Class<?> resource : resources) {
            javax.ws.rs.Path classPathAnnotation = resource.getAnnotation(javax.ws.rs.Path.class);
            String classPath = classPathAnnotation == null ? "" : classPathAnnotation.value();

            List<String> classConsumes = mediaTypes(resource.getAnnotation(Consumes.class));
            List<String> classProduces = mediaTypes(resource.getAnnotation(Produces.class));

            for (Method method : resource.getDeclaredMethods()) {
                String httpMethod = resolveHttpMethod(method);
                if (httpMethod == null) {
                    continue;
                }

                javax.ws.rs.Path methodPathAnnotation = method.getAnnotation(javax.ws.rs.Path.class);
                String methodPath = methodPathAnnotation == null ? "" : methodPathAnnotation.value();
                String fullPath = joinPath(classPath, methodPath);

                io.swagger.models.Path pathModel = builtPaths.containsKey(fullPath) ? builtPaths.get(fullPath) : new io.swagger.models.Path();
                Operation operation = new Operation();
                operation.setOperationId(resource.getSimpleName() + "_" + method.getName());
                ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
                if (apiOperation != null && apiOperation.value() != null && !apiOperation.value().trim().isEmpty()) {
                    operation.setSummary(apiOperation.value().trim());
                } else {
                    operation.setSummary(method.getName());
                }
                if (apiOperation != null && apiOperation.notes() != null && !apiOperation.notes().trim().isEmpty()) {
                    operation.setDescription(apiOperation.notes().trim());
                }
                operation.setConsumes(mergeMediaTypes(classConsumes, mediaTypes(method.getAnnotation(Consumes.class))));
                operation.setProduces(mergeMediaTypes(classProduces, mediaTypes(method.getAnnotation(Produces.class))));
                operation.addTag(resource.getSimpleName().replace("Request", ""));
                operation.setParameters(extractParameters(method, fullPath, definitions));

                Response okResponse = new Response().description("OK");
                Class<?> returnType = method.getReturnType();
                if (!isResponseType(returnType) && !isSimpleType(returnType) && returnType != Void.TYPE && returnType != Void.class) {
                    String responseModelName = addModelAndDependencies(returnType, definitions);
                    if (responseModelName != null) {
                        okResponse.setSchema(new RefProperty(responseModelName));
                    }
                }
                operation.response(200, okResponse);

                if ("GET".equals(httpMethod)) {
                    pathModel.setGet(operation);
                } else if ("POST".equals(httpMethod)) {
                    pathModel.setPost(operation);
                } else if ("PUT".equals(httpMethod)) {
                    pathModel.setPut(operation);
                } else if ("DELETE".equals(httpMethod)) {
                    pathModel.setDelete(operation);
                } else if ("PATCH".equals(httpMethod)) {
                    pathModel.setPatch(operation);
                } else if ("HEAD".equals(httpMethod)) {
                    pathModel.setHead(operation);
                } else if ("OPTIONS".equals(httpMethod)) {
                    pathModel.setOptions(operation);
                }

                builtPaths.put(fullPath, pathModel);
            }
        }

        for (Map.Entry<String, io.swagger.models.Path> entry : builtPaths.entrySet()) {
            swagger.path(entry.getKey(), entry.getValue());
        }
        if (!definitions.isEmpty()) {
            swagger.setDefinitions(definitions);
        }
        return swagger;
    }

    private static List<io.swagger.models.parameters.Parameter> extractParameters(Method method, String fullPath, Map<String, Model> definitions) {
        List<io.swagger.models.parameters.Parameter> parameters = new ArrayList<>();
        boolean hasBodyParameter = false;

        for (Parameter parameter : method.getParameters()) {
            if (parameter.getAnnotation(Context.class) != null) {
                continue;
            }

            PathParam pathParam = parameter.getAnnotation(PathParam.class);
            if (pathParam != null) {
                PathParameter p = new PathParameter();
                p.setName(pathParam.value());
                p.setRequired(true);
                p.setType("string");
                parameters.add(p);
                continue;
            }

            QueryParam queryParam = parameter.getAnnotation(QueryParam.class);
            if (queryParam != null) {
                QueryParameter p = new QueryParameter();
                p.setName(queryParam.value());
                p.setRequired(false);
                p.setType("string");
                parameters.add(p);
                continue;
            }

            if (!hasBodyParameter) {
                BodyParameter body = new BodyParameter();
                body.setName("body");
                Class<?> bodyType = parameter.getType();
                if (!isSimpleType(bodyType)) {
                    String bodyModelName = addModelAndDependencies(bodyType, definitions);
                    if (bodyModelName != null) {
                        body.setSchema(new RefModel(bodyModelName));
                    }
                }
                parameters.add(body);
                hasBodyParameter = true;
            }
        }

        ensurePathParametersFromTemplate(parameters, fullPath);
        return parameters;
    }

    private static void ensurePathParametersFromTemplate(List<io.swagger.models.parameters.Parameter> parameters, String fullPath) {
        int idx = 0;
        while (idx < fullPath.length()) {
            int start = fullPath.indexOf('{', idx);
            if (start < 0) {
                return;
            }
            int end = fullPath.indexOf('}', start + 1);
            if (end < 0) {
                return;
            }
            String name = fullPath.substring(start + 1, end);
            if (!hasParameter(parameters, name)) {
                PathParameter p = new PathParameter();
                p.setName(name);
                p.setRequired(true);
                p.setType("string");
                parameters.add(p);
            }
            idx = end + 1;
        }
    }

    private static boolean hasParameter(List<io.swagger.models.parameters.Parameter> parameters, String name) {
        for (io.swagger.models.parameters.Parameter parameter : parameters) {
            if (parameter instanceof SerializableParameter) {
                SerializableParameter serializable = (SerializableParameter) parameter;
                if (name.equals(serializable.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String resolveHttpMethod(Method method) {
        if (method.getAnnotation(GET.class) != null) {
            return "GET";
        }
        if (method.getAnnotation(POST.class) != null) {
            return "POST";
        }
        if (method.getAnnotation(PUT.class) != null) {
            return "PUT";
        }
        if (method.getAnnotation(DELETE.class) != null) {
            return "DELETE";
        }
        if (method.getAnnotation(PATCH.class) != null) {
            return "PATCH";
        }
        if (method.getAnnotation(HEAD.class) != null) {
            return "HEAD";
        }
        if (method.getAnnotation(OPTIONS.class) != null) {
            return "OPTIONS";
        }
        return null;
    }

    private static List<String> mediaTypes(Consumes annotation) {
        if (annotation == null || annotation.value() == null || annotation.value().length == 0) {
            return Collections.emptyList();
        }
        return Arrays.asList(annotation.value());
    }

    private static List<String> mediaTypes(Produces annotation) {
        if (annotation == null || annotation.value() == null || annotation.value().length == 0) {
            return Collections.emptyList();
        }
        return Arrays.asList(annotation.value());
    }

    private static List<String> mergeMediaTypes(List<String> classLevel, List<String> methodLevel) {
        if (!methodLevel.isEmpty()) {
            return methodLevel;
        }
        if (!classLevel.isEmpty()) {
            return classLevel;
        }
        return Collections.emptyList();
    }

    private static String joinPath(String classPath, String methodPath) {
        String c = sanitizePathPart(classPath);
        String m = sanitizePathPart(methodPath);
        if (c.isEmpty() && m.isEmpty()) {
            return "/";
        }
        if (m.isEmpty()) {
            return c;
        }
        if (c.isEmpty()) {
            return m;
        }
        return c + m;
    }

    private static String sanitizePathPart(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        String p = value.trim();
        if (!p.startsWith("/")) {
            p = "/" + p;
        }
        while (p.contains("//")) {
            p = p.replace("//", "/");
        }
        if (p.length() > 1 && p.endsWith("/")) {
            p = p.substring(0, p.length() - 1);
        }
        return p;
    }

    private static void enableScanAllResources(Reader reader) {
        String[] methodNames = {"setScanAllResources", "setReadAllResources"};
        for (String methodName : methodNames) {
            try {
                Method m = Reader.class.getMethod(methodName, boolean.class);
                m.invoke(reader, true);
                return;
            } catch (ReflectiveOperationException ignored) {
                // Try the next known method name.
            }
        }
    }

    private static boolean isResponseType(Class<?> type) {
        return type != null && javax.ws.rs.core.Response.class.isAssignableFrom(type);
    }

    private static boolean isSimpleType(Class<?> type) {
        if (type == null) {
            return true;
        }
        if (type.isPrimitive() || type.isEnum()) {
            return true;
        }
        if (Number.class.isAssignableFrom(type)
                || CharSequence.class.isAssignableFrom(type)
                || Boolean.class == type
                || Character.class == type
                || java.util.Date.class.isAssignableFrom(type)
                || java.time.temporal.Temporal.class.isAssignableFrom(type)) {
            return true;
        }
        Package p = type.getPackage();
        if (p != null && p.getName().startsWith("java.")) {
            return true;
        }
        return Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type);
    }

    private static String addModelAndDependencies(Class<?> type, Map<String, Model> definitions) {
        if (type == null || isSimpleType(type)) {
            return null;
        }
        Map<String, Model> converted = ModelConverters.getInstance().read(type);
        if (converted == null || converted.isEmpty()) {
            return null;
        }

        for (Map.Entry<String, Model> entry : converted.entrySet()) {
            if (!definitions.containsKey(entry.getKey())) {
                definitions.put(entry.getKey(), entry.getValue());
            }
        }

        String preferred = type.getSimpleName();
        if (converted.containsKey(preferred)) {
            return preferred;
        }
        return converted.keySet().iterator().next();
    }
}
