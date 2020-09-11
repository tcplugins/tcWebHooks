package webhook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import jetbrains.buildServer.serverSide.SFinishedBuild;
import lombok.Getter;
import webhook.teamcity.BuildState;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.Loggers;
import webhook.teamcity.WebHookExecutionException;
import webhook.teamcity.auth.WebHookAuthConfig;
import webhook.teamcity.auth.WebHookAuthenticator;
import webhook.teamcity.payload.variableresolver.VariableMessageBuilder;
import webhook.teamcity.payload.variableresolver.VariableResolverFactory;
import webhook.teamcity.settings.WebHookFilterConfig;
import webhook.teamcity.settings.WebHookHeaderConfig;


public class WebHookImpl implements WebHook {
	private String proxyHost;
	private Integer proxyPort = 0;
	private String proxyUsername;
	private String proxyPassword;
	private String url;
	private String contentType;
	private String charset;
	private String payload;
	private HttpClient client;
	private boolean enabled = false;
	private String disabledReason = "";
	private List<NameValuePair> params;
	private BuildState states;
	private WebHookAuthenticator authenticator;
	private List<WebHookFilterConfig> filters;
	private WebHookExecutionStats webhookStats;
	private SFinishedBuild previousSFinishedBuild;
	private RequestConfig requestConfig= RequestConfig.custom().build();
	private CredentialsProvider credentialsProvider;
	private List<WebHookHeaderConfig> headers = new ArrayList<>();
	private Map<String, String> resolvedHeaders = new LinkedHashMap<>();
	
	@Getter
	private UUID requestId = UUID.randomUUID();
	private VariableResolverFactory variableResolverFactory;
	
	
	public WebHookImpl (String url, WebHookProxyConfig proxyConfig, HttpClient client){
		this.webhookStats = new WebHookExecutionStats(url);
		this.url = url;
		this.client = client;
		this.params = new ArrayList<>();
		this.setProxy(proxyConfig);
	}

	@Override
	public void setProxy(WebHookProxyConfig proxyConfig) {
		if ((proxyConfig != null) && (proxyConfig.getProxyHost() != null) && (proxyConfig.getProxyPort() != null)){
			this.setProxy(proxyConfig.getProxyHost(), proxyConfig.getProxyPort());
			if (proxyConfig.getCreds() != null){
				getCredentialsProvider().setCredentials(
							new AuthScope(proxyConfig.getProxyHost(), proxyConfig.getProxyPort()), 
							proxyConfig.getCreds()
					);
			}
		}
	}
	
	private CredentialsProvider getCredentialsProvider() {
		if (this.credentialsProvider == null) {
			this.credentialsProvider = new BasicCredentialsProvider();
		}
		return this.credentialsProvider;
	}

	@Override
	public void setProxy(String proxyHost, Integer proxyPort) {
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		if (this.proxyHost.length() > 0 && !this.proxyPort.equals(0)) {
			this.requestConfig = RequestConfig.copy(this.requestConfig).setProxy(new HttpHost(this.proxyHost, this.proxyPort)).build();
		}
	}

	@Override
	public void setProxyUserAndPass(String username, String password){
		this.proxyUsername = username;
		this.proxyPassword = password;
		if (this.proxyUsername.length() > 0 && this.proxyPassword.length() > 0) {
			getCredentialsProvider().setCredentials(
					new AuthScope(this.getProxyHost(), this.getProxyPort()), 
					new UsernamePasswordCredentials(username, password)
			);
		}
	}
	
	@Override
	public void post() throws IOException {
		if ((this.enabled) && (!this.getExecutionStats().isErrored())){
			HttpPost httppost = new HttpPost(this.url);
			HttpClientContext context = HttpClientContext.create();
			httppost.addHeader("X-tcwebhooks-request-id", this.getExecutionStats().getTrackingIdAsString());
			if (   this.payload != null && this.payload.length() > 0 
				&& this.contentType != null && this.contentType.length() > 0){
				httppost.setEntity(new StringEntity(this.payload, this.contentType, this.charset));
			} else if ( ! this.params.isEmpty()){
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(this.params, Consts.UTF_8);
				httppost.setEntity(entity);
			}
			if(authenticator != null){
				authenticator.addAuthentication(getCredentialsProvider(), context, url);
				requestConfig = RequestConfig.copy(requestConfig).setTargetPreferredAuthSchemes(Arrays.asList(authenticator.getWwwAuthenticateChallengePrefix())).build();
			}
			
			for (Map.Entry<String, String> header : this.resolvedHeaders.entrySet()) {
				httppost.addHeader(header.getKey(), header.getValue());
			}
			
			if (this.credentialsProvider != null) {
				context.setCredentialsProvider(getCredentialsProvider());
			}

			context.setRequestConfig(requestConfig);
			
			this.webhookStats.setUrl(this.url);
			HttpResponse httpResponse = null;
		    try {
		    	this.webhookStats.setRequestStarting();
		    	Loggers.SERVER.debug("WebHookImpl::  Connect timeout(millis): " + this.requestConfig.getConnectTimeout());
		    	Loggers.SERVER.debug("WebHookImpl:: Response timeout(millis): " + this.requestConfig.getSocketTimeout());
		    	httpResponse = client.execute(httppost, context);
		        this.webhookStats.setRequestCompleted(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
		        this.webhookStats.setResponseHeaders(httpResponse.getAllHeaders());
		        
		        if (Loggers.SERVER.isDebugEnabled()) {
		        	// Log headers
		        	Loggers.SERVER.debug("WebHookImpl::  --- Begin Server Response Headers ---");
		        	for (Header header : httpResponse.getAllHeaders()) {
		        		Loggers.SERVER.debug( header.getName() + " : " + header.getValue() );
		        	}
		        	Loggers.SERVER.debug("WebHookImpl::  --- End Server Response Headers ---");
		        	
		        	// Log body
		        	Loggers.SERVER.debug("WebHookImpl::  --- Begin Server Response Body ---");
			        try {
			        	HttpEntity responseEntity = httpResponse.getEntity();
				        if(responseEntity != null) {
				        	Loggers.SERVER.debug( EntityUtils.toString(responseEntity) );
			        	} else {
			        		Loggers.SERVER.debug("Unable to parse response body. responseEntity is null");
			        	}
			        } catch (IOException ex) {
			        	Loggers.SERVER.debug("Unable to parse response body:" + ex.getMessage());
			        }
			        Loggers.SERVER.debug("WebHookImpl::  --- End Server Response Body ---");
		        }
		    } finally {
		        httppost.releaseConnection();
		        this.webhookStats.setTeardownCompleted();
		        if (this.client instanceof CloseableHttpClient) {
		        	((CloseableHttpClient)this.client).close();
		        }
		        if (httpResponse != null && httpResponse instanceof CloseableHttpResponse) {
		        	((CloseableHttpResponse)httpResponse).close();
		        }
		    }   
		}
	}

	@Override
	public Integer getStatus(){
		return this.getExecutionStats().getStatusCode();
	}
	
	@Override
	public String getProxyHost() {
		return proxyHost;
	}

	@Override
	public int getProxyPort() {
		return proxyPort;
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public void setUrl(String url) {
		this.url = url;
		this.getExecutionStats().setUrl(url);
	}
	
	@Override
	public String getParameterisedUrl(){
		return this.url +  this.parametersAsQueryString();
	}

	@Override
	public String parametersAsQueryString(){
		StringBuilder s = new StringBuilder("");
		for (NameValuePair nv : this.params){
			s.append("&").append(nv.getName()).append("=").append(nv.getValue()); 
		}
		if (s.length() > 0 ){
			return "?" + s.substring(1);
		}
		return s.toString();
	}
	
	@Override
	public void addParam(String key, String value){
		this.params.add(new BasicNameValuePair(key, value));
	}

	@Override
	public void addParams(List<NameValuePair> paramsList){
		for (NameValuePair i : paramsList){
			this.params.add(i); 
		}		
	}
	
	@Override
	public void addParams(Map<String,String> paramsList){
		for (Entry<String,String> entry : paramsList.entrySet()){
			addParam(entry.getKey(), entry.getValue()); 
		}		
	}
	
	
	@Override
	public String getParam(String key){
		for (NameValuePair nv :this.params){
			if (nv.getName().equals(key)){
				return nv.getValue();
			}
		}		
		return "";
	}

	@Override
	public Boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
		this.getExecutionStats().setEnabled(enabled);
	}
	

	@Override
	public void setEnabledForBuildState(BuildStateEnum buildState, boolean enabled) {
		this.setEnabled(enabled);
		this.getExecutionStats().setBuildState(buildState);
		if (!enabled) {
			this.getExecutionStats().setStatusCode(WebHookExecutionException.WEBHOOK_DISABLED_INFO_CODE);
			this.getExecutionStats().setStatusReason("WebHook not enabled for buildState '" + buildState.getShortName() + "'");
		}
	}

	@Override
	public void setEnabled(String enabled){
		if ("true".equalsIgnoreCase(enabled)){
			this.setEnabled(true);
		} else {
			this.setEnabled(false);
		}
	}

	@Override
	public Boolean isErrored() {
		return this.getExecutionStats().isErrored();
	}

	@Override
	public void setErrored(Boolean errored) {
		this.getExecutionStats().setErrored(errored);
	}

	@Override
	public String getErrorReason() {
		return this.getExecutionStats().getStatusReason();
	}

	@Override
	public void setErrorReason(String errorReason) {
		this.getExecutionStats().setStatusReason(errorReason);
	}

	@Override
	public String getProxyUsername() {
		return proxyUsername;
	}

	@Override
	public void setProxyUsername(String proxyUsername) {
		this.proxyUsername = proxyUsername;
	}

	@Override
	public String getProxyPassword() {
		return proxyPassword;
	}

	@Override
	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}

	@Override
	public String getPayload() {
		return payload;
	}

	@Override
	public void setPayload(String payloadContent) {
		this.payload = payloadContent;
	}

	@Override
	public void setContentType(String contentType) {
		this.contentType = contentType;

	}

	@Override
	public void setCharset(String charset) {
		this.charset = charset;
	}

	@Override
	public BuildState getBuildStates() {
		return states;
	}

	@Override
	public void setBuildStates(BuildState states) {
		this.states = states;
	}

	@Override
	public void setAuthentication(WebHookAuthenticator authenticator) {
		this.authenticator = authenticator;
	}
	
	@Override
	public void resolveAuthenticationParameters(VariableMessageBuilder variableMessageBuilder) {
		if (this.authenticator != null) {
			WebHookAuthConfig newConfig = this.authenticator.getWebHookAuthConfig().copy();
			for (Entry<String, String> e : newConfig.getParameters().entrySet()) {
				String value = variableMessageBuilder.build(e.getValue());
				newConfig.getParameters().put(e.getKey(), value);
			}
			this.authenticator.setWebHookAuthConfig(newConfig);
		}
	}
	
	@Override
	public void addFilter(WebHookFilterConfig filter){
		if (this.filters == null){
			this.filters = new ArrayList<>();
		}
		this.filters.add(filter);
	}

	@Override
	public boolean checkFilters(VariableMessageBuilder variableMessageBuilder) {
		if (this.filters == null){
			return true;
		}
		
		for (WebHookFilterConfig filter : this.filters){
			
			/* If this filter is disabled, skip it */
			if (!filter.isEnabled()){
				continue;
			}
			
			/* Otherwise, parse it and test it */
			String variable = variableMessageBuilder.build(filter.getValue());
			Pattern p = filter.getPattern();
			if (!p.matcher(variable).matches()){
				this.disabledReason = "Filter mismatch: " + filter.getValue() + " (" + variable + ") does not match using regex " + filter.getRegex();
				this.setEnabled(false);
				this.getExecutionStats().setStatusCode(WebHookExecutionException.WEBHOOK_DISABLED_BY_FILTER_INFO_CODE);
				this.getExecutionStats().setStatusReason(disabledReason);
				return false;
			} else {
				if (Loggers.SERVER.isDebugEnabled()) {
					Loggers.SERVER.debug("WebHookImpl: Filter match found: " + filter.getValue() + " (" + variable + ") matches using regex " + filter.getRegex() );
				}
			}
			
		}
		return true;
	}
	
	@Override
	public void addHeaders(List<WebHookHeaderConfig> headers) {
		this.headers.addAll(headers);
	}
	
	@Override
	public void resolveHeaders(VariableMessageBuilder variableMessageBuilder) {
		for (WebHookHeaderConfig header : this.headers ) {
			String headerName = variableMessageBuilder.build(header.getName());
			String headerValue = variableMessageBuilder.build(header.getValue());
			resolvedHeaders.put(headerName, headerValue);
		}
	}
	
	@Override
	public String getDisabledReason() {
		return disabledReason;
	}

	@Override
	public WebHookExecutionStats getExecutionStats() {
		return this.webhookStats;
	}

	@Override
	public SFinishedBuild getPreviousNonPersonalBuild() {
		return this.previousSFinishedBuild;
	}

	@Override
	public void setPreviousNonPersonalBuild(SFinishedBuild localSFinishedBuild) {
		this.previousSFinishedBuild = localSFinishedBuild;
		
	}

	@Override
	public void setConnectionTimeOut(int httpConnectionTimeout) {
		this.requestConfig = RequestConfig.copy(this.requestConfig).setConnectTimeout(httpConnectionTimeout * 1000).build();
	}

	@Override
	public void setResponseTimeOut(int httpResponseTimeout) {
		this.requestConfig = RequestConfig.copy(this.requestConfig).setSocketTimeout(httpResponseTimeout * 1000).build();
	}

	@Override
	public VariableResolverFactory getVariableResolverFactory() {
		return this.variableResolverFactory;
	}

	@Override
	public void setVariableResolverFactory(VariableResolverFactory variableResolverFactory) {
		this.variableResolverFactory = variableResolverFactory;		
	}
	
}
