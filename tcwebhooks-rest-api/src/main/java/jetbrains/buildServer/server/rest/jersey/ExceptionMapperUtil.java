/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.server.rest.jersey;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.web.util.WebUtil;
import org.apache.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: Yegor Yarko
 * Date: 30.03.2009
 */
public class ExceptionMapperUtil {
  protected static final Logger LOG = Logger.getInstance(ExceptionMapperUtil.class.getName());
  public static final String REST_INCLUDE_EXCEPTION_STACKTRACE_PROPERTY = "rest.response.debug.includeExceptionStacktrace";
  public static final String REST_INCLUDE_REQUEST_DETAILS_INTO_ERRORS = "rest.log.includeRequestDetails";
  protected static final String INCLUDE_STACKTRACE_REQUST_PARAMETER = "includeStacktrace";

  @Context private UriInfo myUriInfo;
  @Context private HttpServletRequest myRequest;

  protected Response reportError(@NotNull final Response.Status responseStatus, @NotNull final Exception e, @Nullable final String message) {
    return reportError(responseStatus.getStatusCode(), e, message, false);
  }

  protected Response reportError(final int statusCode, @NotNull final Exception e, @Nullable final String message, final boolean isInternalError) {
    return processRestErrorResponse(statusCode, e, message, isInternalError, myRequest);
  }

  public static Response processRestErrorResponse(final int statusCode,
                                                  @Nullable final Throwable e,
                                                  @Nullable final String message,
                                                  final boolean isInternalError,
                                                  @NotNull final HttpServletRequest request) {
    final String responseText = getResponseTextAndLogRestErrorErrorMessage(statusCode, e, message, isInternalError, Level.WARN, request);
    Response.ResponseBuilder builder = Response.status(statusCode);
    builder.type("text/plain");
    builder.entity(responseText);
    return builder.build();
  }

  public static String getResponseTextAndLogRestErrorErrorMessage(final int statusCode,
                                                                  @Nullable final Throwable e,
                                                                  @Nullable final String message,
                                                                  final boolean isInternalError,
                                                                  final Level level,
                                                                  @NotNull final HttpServletRequest request) {
    Response.Status status = null;
    try {
      status = Response.Status.fromStatusCode(statusCode);
    } catch (Error error) {
      LOG.warn("Critical error encountered while reporting an error", error);
    }
    StringBuffer responseText = new StringBuffer();
    if (statusCode >= 500){
      responseText.append("Error has occurred during request processing");
    } else {
      responseText.append("Responding with error");
    }
    responseText.append(", status code: ").append(Integer.toString(statusCode));
    if (status != null) responseText.append(" (").append(status.toString()).append(")");
    responseText.append(".");

    //provide user-friendly message on missing or wrong Content-Type header
    if (statusCode == 415) { //Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode()
      //todo: response with supported content-types instead
      responseText.append("\nMake sure you have supplied correct 'Content-Type' header.");
    } else if (statusCode == 406) { //Response.Status.NOT_ACCEPTABLE.getStatusCode()
      //todo: response with supported "accept" header values instead
      responseText.append("\nMake sure you have supplied correct 'Accept' header.");
    } else {
      responseText.append("\nDetails: ");
      responseText.append(getMessageWithCauses(e));
      if (message != null) responseText.append("\n").append(message);
    }
    String result = responseText.toString();
    final String singleLineMessage = StringUtil.replace(StringUtil.replace(result, ".\n", ". "), "\n", ". ");
    String logMessage;
    if (TeamCityProperties.getBooleanOrTrue(REST_INCLUDE_REQUEST_DETAILS_INTO_ERRORS)){
      logMessage = singleLineMessage + " Request: " + WebUtil.getRequestDump(request) + ".";
    }else{
      logMessage = singleLineMessage + " URL: " + WebUtil.getRequestUrl(request) + ".";
    }

    if (isInternalError && !isCommonExternalError(e)) {
      logMessage(LOG, level, logMessage, e);
    } else {
      logMessage(LOG, level, logMessage);
      LOG.debug(logMessage, e);
    }

    final String includeStacktrace = TeamCityProperties.getProperty(REST_INCLUDE_EXCEPTION_STACKTRACE_PROPERTY, "false");
    if (e != null && !"false".equals(includeStacktrace)) {
      if ( "true".equals(request.getParameter(INCLUDE_STACKTRACE_REQUST_PARAMETER)) ||
          (!StringUtil.isEmpty(includeStacktrace) &&
           ("true".equals(includeStacktrace) || "any".equals(includeStacktrace) || String.valueOf(statusCode).startsWith(includeStacktrace)))) {
        StringWriter sw = new StringWriter();
        sw.write("\n\n");
        e.printStackTrace(new PrintWriter(sw));
        sw.write("\nThe stacktrace is included as '" + REST_INCLUDE_EXCEPTION_STACKTRACE_PROPERTY + "' internal property or " +
                 "'" + INCLUDE_STACKTRACE_REQUST_PARAMETER + "' request parameter is set.");
        result += sw.toString();
      }
    }
    return result;
  }

  public static boolean isCommonExternalError(@Nullable Throwable e) {
    if (e == null) return false;
    final String message = jetbrains.buildServer.util.StringUtil.emptyIfNull(e.getMessage());
    if (e.getClass().getName().endsWith(".IllegalStateException") && message.equals("Cannot call sendError() after the response has been committed")){
      //Jersey 1.19 (as opposed to Jersey 1.16) reports this error in case of ClientAbortException
      return true;
    }
    if (e.getClass().getName().endsWith(".IllegalStateException") && message.equals("getOutputStream() has already been called for this response")){
      //this is thrown on attempt to report error in already written response in APIController.reportRestErrorResponse()
      return true;
    }
    while (true) {
      if (e.getClass().getName().endsWith(".ClientAbortException")) return true;
      final Throwable cause = e.getCause();
      if (cause == null || cause == e) break;
      e = cause;
    }
    return false;
  }

  private static void logMessage(final Logger log, final Level level, final String message) {
    if (level.isGreaterOrEqual(Level.ERROR)) {
      log.error(message);
    } else if (level.isGreaterOrEqual(Level.WARN)) {
      log.warn(message);
    } else if (level.isGreaterOrEqual(Level.INFO)) {
      log.info(message);
    }
  }

  private static void logMessage(final Logger log, final Level level, final String message, final Throwable e) {
    if (level.isGreaterOrEqual(Level.ERROR)) {
      log.error(message, e);
    } else if (level.isGreaterOrEqual(Level.WARN)) {
      log.warn(message, e);
    } else if (level.isGreaterOrEqual(Level.INFO)) {
      log.info(message, e);
    }
  }

  public static String getMessageWithCauses(@Nullable Throwable e) {
    if (e == null){
      return "";
    }
    final String message = e.getMessage();
    String result = e.getClass().getName() + (message != null ? ": " + message : "");
    result = addKnownExceptionsData(e, result);
    result += appendCauseInfo(e);
    return result;
  }

  public static String addKnownExceptionsData(final Throwable e, String result) {
//    if (e instanceof Errors.ErrorMessagesException) { //error message does not contain details
//      final List<Errors.ErrorMessage> messages = ((Errors.ErrorMessagesException)e).messages;
//      if (messages != null) {
//        try {
//          final Field field = Errors.ErrorMessage.class.getDeclaredField("message");
//          field.setAccessible(true);
//          result += " (messages: ";
//          for (Errors.ErrorMessage errorMessage : messages) {
//            // the data is not accessible otherwise
//            result += "\"" + field.get(errorMessage) + "\",";
//          }
//          result += ")";
//        } catch (NoSuchFieldException e1) {
//          //ignore
//        } catch (IllegalAccessException e1) {
//          //ignore
//        }
//      }
//    }
    return result;
  }

  private static String appendCauseInfo(final Throwable e) {
    final Throwable cause = e.getCause();
    if (cause != null && cause != e) {
      final String message = e.getMessage();
      final String causeMessage = cause.getMessage();
      if (message != null && causeMessage != null && message.contains(causeMessage)){
        //skip cause
        return appendCauseInfo(cause);
      }
      return  ", caused by: " + getMessageWithCauses(cause);
    }
    return "";
  }
}
