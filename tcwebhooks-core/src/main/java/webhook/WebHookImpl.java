package webhook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import jetbrains.buildServer.log.Loggers;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.velocity.context.Context;

import webhook.teamcity.BuildState;
import webhook.teamcity.auth.WebHookAuthenticator;
import webhook.teamcity.payload.util.TemplateMatcher.VariableResolver;
import webhook.teamcity.payload.util.VariableMessageBuilder;
import webhook.teamcity.settings.WebHookFilterConfig;


public class WebHookImpl implements WebHook {
	private String proxyHost;
	private Integer proxyPort = 0;
	private String proxyUsername;
	private String proxyPassword;
	private String url;
	private String content;
	private String contentType;
	private String charset;
	private String payload;
	private Integer resultCode;
	private HttpClient client;
	private String filename = "";
	private Boolean enabled = false;
	private Boolean errored = false;
	private String errorReason = "";
	private String disabledReason = "";
	private List<NameValuePair> params;
	private BuildState states;
	private WebHookAuthenticator authenticator;
	private List<WebHookFilterConfig> filters;
	
	
	public WebHookImpl(){
		this.client = new HttpClient();
		this.params = new ArrayList<>();
	}
	
	protected WebHookImpl(HttpClient client){
		this.client = client;
		this.params = new ArrayList<>();
	}
	
	public WebHookImpl(String url, HttpClient client){
		this.url = url;
		this.client = client;
		this.params = new ArrayList<>();
	}
	
	public WebHookImpl (String url, String proxyHost, String proxyPort, HttpClient client){
		this.url = url;
		this.client = client;
		this.params = new ArrayList<>();
		if (proxyPort.length() != 0) {
			try {
				this.proxyPort = Integer.parseInt(proxyPort);
			} catch (NumberFormatException ex){
				ex.printStackTrace();
			}
		}
		this.setProxy(proxyHost, this.proxyPort);
	}
	
	public WebHookImpl (String url, String proxyHost, Integer proxyPort, HttpClient client){
		this.url = url;
		this.client = client;
		this.params = new ArrayList<>();
		this.setProxy(proxyHost, proxyPort);
	}
	
	public WebHookImpl (String url, WebHookProxyConfig proxyConfig, HttpClient client){
		this.url = url;
		this.client = client;
		this.params = new ArrayList<>();
		setProxy(proxyConfig);
	}

	@Override
	public void setProxy(WebHookProxyConfig proxyConfig) {
		if ((proxyConfig != null) && (proxyConfig.getProxyHost() != null) && (proxyConfig.getProxyPort() != null)){
			this.setProxy(proxyConfig.getProxyHost(), proxyConfig.getProxyPort());
			if (proxyConfig.getCreds() != null){
				this.client.getState().setProxyCredentials(AuthScope.ANY, proxyConfig.getCreds());
			}
		}
	}
	
	@Override
	public void setProxy(String proxyHost, Integer proxyPort) {
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		if (this.proxyHost.length() > 0 && !this.proxyPort.equals(0)) {
			this.client.getHostConfiguration().setProxy(this.proxyHost, this.proxyPort);
		}
	}

	@Override
	public void setProxyUserAndPass(String username, String password){
		this.proxyUsername = username;
		this.proxyPassword = password;
		if (this.proxyUsername.length() > 0 && this.proxyPassword.length() > 0) {
			this.client.getState().setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
		}
	}
	
	@Override
	public void post() throws FileNotFoundException, IOException{
		if ((this.enabled) && (!this.errored)){
			PostMethod httppost = new PostMethod(this.url);
			if (this.filename.length() > 0){
				File file = new File(this.filename);
			    httppost.setRequestEntity(new InputStreamRequestEntity(new FileInputStream(file)));
			    httppost.setContentChunked(true);
			}
			if (   this.payload != null && this.payload.length() > 0 
				&& this.contentType != null && this.contentType.length() > 0){
				httppost.setRequestEntity(new StringRequestEntity(this.payload, this.contentType, this.charset));
			} else if (this.params.size() > 0){
				NameValuePair[] paramsArray = this.params.toArray(new NameValuePair[this.params.size()]);
				httppost.setRequestBody(paramsArray);
			}
			if(authenticator != null){
				authenticator.addAuthentication(httppost, client, url);
			}
				
		    try {
		        client.executeMethod(httppost);
		        this.resultCode = httppost.getStatusCode();
		        this.content = httppost.getResponseBodyAsString();
		    } finally {
		        httppost.releaseConnection();
		    }   
		}
	}

	@Override
	public Integer getStatus(){
		return this.resultCode;
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
	}
	
	@Override
	public String getParameterisedUrl(){
		return this.url +  this.parametersAsQueryString();
	}

	@Override
	public String parametersAsQueryString(){
		String s = "";
		for (NameValuePair nv : this.params){
			s += "&" + nv.getName() + "=" + nv.getValue(); 
		}
		if (s.length() > 0 ){
			return "?" + s.substring(1);
		}
		return s;
	}
	
	@Override
	public void addParam(String key, String value){
		this.params.add(new NameValuePair(key, value));
	}

	@Override
	public void addParams(List<NameValuePair> paramsList){
		for (NameValuePair i : paramsList){
			this.params.add(i); 
		}		
	}
	
	@Override
	public void addParams(Map<String,String> paramsList){
		for (String key : paramsList.keySet()){
			addParam(key, paramsList.get(key)); 
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
	public void setFilename(String filename) {
		this.filename = filename;
	}

	@Override
	public String getFilename() {
		return filename;
	}

	@Override
	public String getContent() {
		return content;
	}

	@Override
	public Boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public void setEnabled(String enabled){
		if (enabled.toLowerCase().equals("true")){
			this.enabled = true;
		} else {
			this.enabled = false;
		}
	}

	@Override
	public Boolean isErrored() {
		return errored;
	}

	@Override
	public void setErrored(Boolean errored) {
		this.errored = errored;
	}

	@Override
	public String getErrorReason() {
		return errorReason;
	}

	@Override
	public void setErrorReason(String errorReason) {
		this.errorReason = errorReason;
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
	public void addFilter(WebHookFilterConfig filter){
		if (this.filters == null){
			this.filters = new ArrayList<>();
		}
		this.filters.add(filter);
	}

	@Override
	public boolean checkFilters(Context variableResolver) {
		if (this.filters == null){
			return true;
		}
		
		for (WebHookFilterConfig filter : this.filters){
			
			/* If this filter is disabled, skip it */
			if (!filter.isEnabled()){
				continue;
			}
			
			/* Otherwise, parse it and test it */
			String variable = VariableMessageBuilder.create(filter.getValue(), variableResolver).build();
			Pattern p = filter.getPattern();
			if (!p.matcher(variable).matches()){
				this.disabledReason = "Filter mismatch: " + filter.getValue() + " (" + variable + ") does not match using regex " + filter.getRegex();
				this.enabled = false;
				return false;
			} {
				if (Loggers.SERVER.isDebugEnabled()) Loggers.SERVER.debug("WebHookImpl: Filter match found: " + filter.getValue() + " (" + variable + ") matches using regex " + filter.getRegex() );
			}
			
		}
		return true;
	}
	
	@Override
	public String getDisabledReason() {
		return disabledReason;
	}

}
