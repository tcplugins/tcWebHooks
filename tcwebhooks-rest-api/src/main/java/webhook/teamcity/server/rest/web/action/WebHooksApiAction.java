/*******************************************************************************
 *
 *  Copyright 2017 Net Wolf UK
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 *  
 *******************************************************************************/
package webhook.teamcity.server.rest.web.action;

import static webhook.teamcity.server.rest.web.WebHookRestApiActionController.*;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.util.StringUtil;

public abstract class WebHooksApiAction {

	public WebHooksApiAction() {
		super();
	}

	public String getParameterAsStringOrNull(HttpServletRequest request, String paramName, String errorMessage) throws MissingPathException {
		String returnValue = StringUtil.nullIfEmpty(request.getParameter(paramName));
		if (returnValue == null || "".equals(returnValue.trim())) {
			throw new MissingPathException(errorMessage);
		}
		return returnValue;
	}
	
	public abstract String getApiAction();

	public boolean canProcess(@NotNull HttpServletRequest request) {
		return getApiAction().equals(request.getParameter(ACTION_TYPE));
	}
	
	@SuppressWarnings("serial")
	public class MissingPathException extends Exception {
		public MissingPathException(String message) {
			super(message);
		}
	}

}