package webhook;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class WebHookCollection {
	private Map<Integer,WebHook> webHooks;
	private static final Integer SYSTEM = 0;
	private static final Integer WEBHOOK = 1;
	private static final Integer WEBHOOK_ID = 2;
	private static final Integer WEBHOOK_KEY = 3;
	private static final Integer WEBHOOK_PARAMETER_ID = 4;
	private static final Integer WEBHOOK_PARAMETER_KEY = 5;
	private Map <String, String> origParams; 
	
	public WebHookCollection(Map <String, String> params) {
		webHooks = new HashMap<>();
		this.origParams = params;
		this.parseParams(params);
	}
	
	private String getValue(String paramKey) throws WebHookParameterReferenceException {
		if (this.origParams.containsKey(paramKey)){
			String value = this.origParams.get(paramKey);
			if (value.startsWith("%") && value.endsWith("%")){
				return this.getValue(value.substring(1,value.length() - 1));
			} 
			return value;
		} else {
			throw new WebHookParameterReferenceException(paramKey);
		}
	}
	private void parseParams(Map <String, String> params) {
		//webHooks.add(new WebHook("blah"));
        for (Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<String,String> entry = (Map.Entry<String,String>) iterator.next();
            String name = (String)entry.getKey();
            String val = (String)entry.getValue();
            System.out.println(name + " .. " + val);
            String tokens[] = name.toLowerCase().split("\\.");
            // First check if it's one of our tokens.
            if ((tokens[SYSTEM].equals("system")) && (tokens[WEBHOOK].equals("webhook")) 
            		&& (this.canConvertToInt(tokens[WEBHOOK_ID]))) {
            	// Check if we have already created a webhook instance
            	if (webHooks.containsKey(this.convertToInt(tokens[WEBHOOK_ID]))){
            		if (tokens[WEBHOOK_KEY].equals("url")){
            			webHooks.get(this.convertToInt(tokens[WEBHOOK_ID])).setUrl(val);
//            		} else if ((tokens[WEBHOOK_KEY].equals("bitmask"))
//            				&& (this.canConvertToInt(val))){
//                		webHooks.get(this.convertToInt(tokens[WEBHOOK_ID])).setTriggerStateBitMask(this.convertToInt(val));            			
            		} else if (tokens[WEBHOOK_KEY].equals("enabled")){
            			webHooks.get(this.convertToInt(tokens[WEBHOOK_ID])).setEnabled(val);
            		} else if (tokens[WEBHOOK_KEY].equals("parameter") 
            				&& (this.canConvertToInt(tokens[WEBHOOK_PARAMETER_ID]))
            				&& (tokens[WEBHOOK_PARAMETER_KEY].equals("name")))
            		{
            			try {
							String myVal = this.getValue("system.webhook." + tokens[WEBHOOK_ID] + ".parameter." 
									+ tokens[WEBHOOK_PARAMETER_ID] + ".value");

							webHooks.get(this.convertToInt(tokens[WEBHOOK_ID])).addParam(val, myVal);
						} catch (WebHookParameterReferenceException e) {
							webHooks.get(this.convertToInt(tokens[WEBHOOK_ID])).setErrored(true);
							webHooks.get(this.convertToInt(tokens[WEBHOOK_ID])).setErrorReason(
									"WebHook Listener: The configured webhook parameter (" 
									+ name + ") references an alternate non-existant parameter");
						}

            		}
            	} else {
	            	if (tokens[WEBHOOK_KEY].equals("url")){
	            		WebHook wh = new WebHookImpl(val);
	            		this.webHooks.put(this.convertToInt(tokens[WEBHOOK_ID]), wh);
//            		} else if ((tokens[WEBHOOK_KEY].equals("bitmask"))
//            				&& (this.canConvertToInt(val))){
//            			WebHook wh = new WebHookImpl();
//                		wh.setTriggerStateBitMask(this.convertToInt(val)); 	 
//                		this.webHooks.put(this.convertToInt(tokens[WEBHOOK_ID]), wh);
	            	} else if (tokens[WEBHOOK_KEY].equals("enabled")){
	            		WebHook wh = new WebHookImpl();
            			wh.setEnabled(true);
            			this.webHooks.put(this.convertToInt(tokens[WEBHOOK_ID]), wh);
            		} else if (tokens[WEBHOOK_KEY].equals("parameter") 
            				&& (this.canConvertToInt(tokens[WEBHOOK_PARAMETER_ID]))
            				&& (tokens[WEBHOOK_PARAMETER_KEY].equals("name")))
            		{
            			try {
							String myVal = this.getValue("system.webhook." + tokens[WEBHOOK_ID] + ".parameter." 
									+ tokens[WEBHOOK_PARAMETER_ID] + ".value");
							WebHook wh = new WebHookImpl();
							wh.addParam(val, myVal);
							this.webHooks.put(this.convertToInt(tokens[WEBHOOK_ID]), wh);
							
						} catch (WebHookParameterReferenceException e) {
							WebHook wh = new WebHookImpl();
							wh.setErrored(true);
							wh.setErrorReason("WebHook Listener: The configured webhook parameter (" 
									+ name + ") references an alternate non-existant parameter");
							this.webHooks.put(this.convertToInt(tokens[WEBHOOK_ID]), wh);
						}
            		}            			
            	}
            }
        }
	}
	
	public Map<Integer,WebHook> getWebHooks(){
		return this.webHooks;
	}
	
	public Collection<WebHook> getWebHooksAsCollection(){
		return this.webHooks.values();
	}

	private Boolean canConvertToInt(String s){
		try{
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException e){
			return false;
		}
	}
	
	private Integer convertToInt(String s){
		try{
			int myInt = Integer.parseInt(s);
			return myInt;
		} catch (NumberFormatException e){
			return null;
		}		
	}
}
