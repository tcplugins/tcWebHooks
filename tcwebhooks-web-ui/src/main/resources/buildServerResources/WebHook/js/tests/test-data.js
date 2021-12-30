var restApiDetected = true;
const ProjectBuilds = {
  "templatesAndWebhooks": {
    "registeredTemplates": {
      "templateList": {
        "test01": {
          "description": "Test 01 (JSON Standard template)",
          "templateId": "test01",
          "projectId": "_Root",
          "projectExternalId": "_Root",
          "projectName": "_Root",
          "templateDescription": "Test 01",
          "templateToolTip": "",
          "formatShortName": "jsontemplate",
          "formatDescription": "JSON Standard template",
          "supportedStates": [
            "buildAddedToQueue",
            "buildRemovedFromQueue",
            "buildStarted",
            "changesLoaded",
            "buildInterrupted",
            "beforeBuildFinish",
            "buildSuccessful",
            "buildFailed",
            "buildFixed",
            "buildBroken",
            "responsibilityChanged",
            "buildPinned",
            "buildUnpinned",
            "serviceMessageReceived"
          ],
          "supportedBuildEnumStates": [
            "BUILD_ADDED_TO_QUEUE",
            "BUILD_REMOVED_FROM_QUEUE",
            "BUILD_STARTED",
            "CHANGES_LOADED",
            "BUILD_INTERRUPTED",
            "BEFORE_BUILD_FINISHED",
            "BUILD_SUCCESSFUL",
            "BUILD_FAILED",
            "BUILD_FIXED",
            "BUILD_BROKEN",
            "RESPONSIBILITY_CHANGED",
            "BUILD_PINNED",
            "BUILD_UNPINNED",
            "SERVICE_MESSAGE_RECEIVED"
          ],
          "supportedBranchStates": [
            "buildAddedToQueue",
            "buildRemovedFromQueue",
            "buildStarted",
            "changesLoaded",
            "buildInterrupted",
            "beforeBuildFinish",
            "buildSuccessful",
            "buildFailed",
            "buildFixed",
            "buildBroken",
            "responsibilityChanged",
            "buildPinned",
            "buildUnpinned",
            "serviceMessageReceived"
          ],
          "webhookUsageCount": 1
        },
        "discord.com": {
          "description": "Discord JSON templates (JSON Standard template)",
          "templateId": "discord.com",
          "projectId": "_Root",
          "projectExternalId": "_Root",
          "projectName": "_Root",
          "templateDescription": "Discord JSON templates",
          "templateToolTip": "Supports the discordapp.com JSON webhooks endpoint",
          "formatShortName": "jsontemplate",
          "formatDescription": "JSON Standard template",
          "supportedStates": [
            "buildAddedToQueue",
            "buildRemovedFromQueue",
            "buildStarted",
            "changesLoaded",
            "buildInterrupted",
            "beforeBuildFinish",
            "buildSuccessful",
            "buildFailed",
            "buildFixed",
            "buildBroken",
            "responsibilityChanged",
            "buildPinned",
            "buildUnpinned",
            "serviceMessageReceived"
          ],
          "supportedBuildEnumStates": [
            "BUILD_ADDED_TO_QUEUE",
            "BUILD_REMOVED_FROM_QUEUE",
            "BUILD_STARTED",
            "CHANGES_LOADED",
            "BUILD_INTERRUPTED",
            "BEFORE_BUILD_FINISHED",
            "BUILD_SUCCESSFUL",
            "BUILD_FAILED",
            "BUILD_FIXED",
            "BUILD_BROKEN",
            "RESPONSIBILITY_CHANGED",
            "BUILD_PINNED",
            "BUILD_UNPINNED",
            "SERVICE_MESSAGE_RECEIVED"
          ],
          "supportedBranchStates": [
            "buildAddedToQueue",
            "buildRemovedFromQueue",
            "buildStarted",
            "changesLoaded",
            "buildInterrupted",
            "beforeBuildFinish",
            "buildSuccessful",
            "buildFailed",
            "buildFixed",
            "buildBroken",
            "responsibilityChanged",
            "buildPinned",
            "buildUnpinned",
            "serviceMessageReceived"
          ],
          "webhookUsageCount": 0
        },
        "elasticsearch": {
          "description": "ElasticSearch Document Creation (JSON Standard template)",
          "templateId": "elasticsearch",
          "projectId": "_Root",
          "projectExternalId": "_Root",
          "projectName": "_Root",
          "templateDescription": "ElasticSearch Document Creation",
          "templateToolTip": "Creates document in Elastic Search index",
          "formatShortName": "jsontemplate",
          "formatDescription": "JSON Standard template",
          "supportedStates": [
            "buildAddedToQueue",
            "buildRemovedFromQueue",
            "buildStarted",
            "changesLoaded",
            "buildInterrupted",
            "beforeBuildFinish",
            "buildFinished",
            "buildSuccessful",
            "buildFailed",
            "buildFixed",
            "buildBroken",
            "responsibilityChanged",
            "buildPinned",
            "buildUnpinned",
            "serviceMessageReceived"
          ],
          "supportedBuildEnumStates": [
            "BUILD_ADDED_TO_QUEUE",
            "BUILD_REMOVED_FROM_QUEUE",
            "BUILD_STARTED",
            "CHANGES_LOADED",
            "BUILD_INTERRUPTED",
            "BEFORE_BUILD_FINISHED",
            "BUILD_FINISHED",
            "BUILD_SUCCESSFUL",
            "BUILD_FAILED",
            "BUILD_FIXED",
            "BUILD_BROKEN",
            "RESPONSIBILITY_CHANGED",
            "BUILD_PINNED",
            "BUILD_UNPINNED",
            "SERVICE_MESSAGE_RECEIVED"
          ],
          "supportedBranchStates": [
            "buildAddedToQueue",
            "buildRemovedFromQueue",
            "buildStarted",
            "changesLoaded",
            "buildInterrupted",
            "beforeBuildFinish",
            "buildFinished",
            "buildSuccessful",
            "buildFailed",
            "buildFixed",
            "buildBroken",
            "responsibilityChanged",
            "buildPinned",
            "buildUnpinned",
            "serviceMessageReceived"
          ],
          "webhookUsageCount": 0
        },
        "flowdock": {
          "description": "Flowdock JSON templates (JSON Standard template)",
          "templateId": "flowdock",
          "projectId": "_Root",
          "projectExternalId": "_Root",
          "projectName": "_Root",
          "templateDescription": "Flowdock JSON templates",
          "templateToolTip": "Supports the TeamCity Flowdock JSON integration",
          "formatShortName": "jsontemplate",
          "formatDescription": "JSON Standard template",
          "supportedStates": [
            "buildStarted",
            "buildInterrupted",
            "buildSuccessful",
            "buildFailed",
            "buildFixed",
            "buildBroken",
            "responsibilityChanged",
            "serviceMessageReceived"
          ],
          "supportedBuildEnumStates": [
            "BUILD_STARTED",
            "BUILD_INTERRUPTED",
            "BUILD_SUCCESSFUL",
            "BUILD_FAILED",
            "BUILD_FIXED",
            "BUILD_BROKEN",
            "RESPONSIBILITY_CHANGED",
            "SERVICE_MESSAGE_RECEIVED"
          ],
          "supportedBranchStates": [
            "buildStarted",
            "buildInterrupted",
            "buildSuccessful",
            "buildFailed",
            "buildFixed",
            "buildBroken",
            "responsibilityChanged",
            "serviceMessageReceived"
          ],
          "webhookUsageCount": 0
        },
        "microsoft-teams": {
          "description": "Microsoft Teams (JSON Standard template)",
          "templateId": "microsoft-teams",
          "projectId": "_Root",
          "projectExternalId": "_Root",
          "projectName": "_Root",
          "templateDescription": "Microsoft Teams",
          "templateToolTip": "Post to Microsoft Teams Group Chat Software",
          "formatShortName": "jsontemplate",
          "formatDescription": "JSON Standard template",
          "supportedStates": [
            "buildSuccessful",
            "buildFailed",
            "buildFixed",
            "buildBroken"
          ],
          "supportedBuildEnumStates": [
            "BUILD_SUCCESSFUL",
            "BUILD_FAILED",
            "BUILD_FIXED",
            "BUILD_BROKEN"
          ],
          "supportedBranchStates": [
            "buildSuccessful",
            "buildFailed",
            "buildFixed",
            "buildBroken"
          ],
          "webhookUsageCount": 0
        },
        "microsoft-teams-2": {
          "description": "Microsoft Teams (light) (JSON Standard template)",
          "templateId": "microsoft-teams-2",
          "projectId": "_Root",
          "projectExternalId": "_Root",
          "projectName": "_Root",
          "templateDescription": "Microsoft Teams (light)",
          "templateToolTip": "A template for Microsoft Teams. Has a different look and feel.",
          "formatShortName": "jsontemplate",
          "formatDescription": "JSON Standard template",
          "supportedStates": [
            "buildStarted",
            "buildSuccessful",
            "buildFailed"
          ],
          "supportedBuildEnumStates": [
            "BUILD_STARTED",
            "BUILD_SUCCESSFUL",
            "BUILD_FAILED"
          ],
          "supportedBranchStates": [
            "buildStarted",
            "buildSuccessful",
            "buildFailed"
          ],
          "webhookUsageCount": 0
        },
        "slack.com": {
          "description": "Slack.com JSON templates (JSON Standard template)",
          "templateId": "slack.com",
          "projectId": "_Root",
          "projectExternalId": "_Root",
          "projectName": "_Root",
          "templateDescription": "Slack.com JSON templates",
          "templateToolTip": "Supports the slack.com JSON webhooks endpoint",
          "formatShortName": "jsontemplate",
          "formatDescription": "JSON Standard template",
          "supportedStates": [
            "buildAddedToQueue",
            "buildRemovedFromQueue",
            "buildStarted",
            "changesLoaded",
            "buildInterrupted",
            "beforeBuildFinish",
            "buildSuccessful",
            "buildFailed",
            "buildFixed",
            "buildBroken",
            "responsibilityChanged",
            "buildPinned",
            "buildUnpinned",
            "serviceMessageReceived"
          ],
          "supportedBuildEnumStates": [
            "BUILD_ADDED_TO_QUEUE",
            "BUILD_REMOVED_FROM_QUEUE",
            "BUILD_STARTED",
            "CHANGES_LOADED",
            "BUILD_INTERRUPTED",
            "BEFORE_BUILD_FINISHED",
            "BUILD_SUCCESSFUL",
            "BUILD_FAILED",
            "BUILD_FIXED",
            "BUILD_BROKEN",
            "RESPONSIBILITY_CHANGED",
            "BUILD_PINNED",
            "BUILD_UNPINNED",
            "SERVICE_MESSAGE_RECEIVED"
          ],
          "supportedBranchStates": [
            "buildAddedToQueue",
            "buildRemovedFromQueue",
            "buildStarted",
            "changesLoaded",
            "buildInterrupted",
            "beforeBuildFinish",
            "buildSuccessful",
            "buildFailed",
            "buildFixed",
            "buildBroken",
            "responsibilityChanged",
            "buildPinned",
            "buildUnpinned",
            "serviceMessageReceived"
          ],
          "webhookUsageCount": 0
        },
        "slack.com-compact": {
          "description": "Slack.com Compact Notification (JSON Standard template)",
          "templateId": "slack.com-compact",
          "projectId": "_Root",
          "projectExternalId": "_Root",
          "projectName": "_Root",
          "templateDescription": "Slack.com Compact Notification",
          "templateToolTip": "POSTs a very compact slack.com notification",
          "formatShortName": "jsontemplate",
          "formatDescription": "JSON Standard template",
          "supportedStates": [
            "buildAddedToQueue",
            "buildRemovedFromQueue",
            "buildStarted",
            "changesLoaded",
            "buildInterrupted",
            "beforeBuildFinish",
            "buildSuccessful",
            "buildFailed",
            "buildFixed",
            "buildBroken",
            "serviceMessageReceived"
          ],
          "supportedBuildEnumStates": [
            "BUILD_ADDED_TO_QUEUE",
            "BUILD_REMOVED_FROM_QUEUE",
            "BUILD_STARTED",
            "CHANGES_LOADED",
            "BUILD_INTERRUPTED",
            "BEFORE_BUILD_FINISHED",
            "BUILD_SUCCESSFUL",
            "BUILD_FAILED",
            "BUILD_FIXED",
            "BUILD_BROKEN",
            "SERVICE_MESSAGE_RECEIVED"
          ],
          "supportedBranchStates": [
            "buildAddedToQueue",
            "buildRemovedFromQueue",
            "buildStarted",
            "changesLoaded",
            "buildInterrupted",
            "beforeBuildFinish",
            "buildSuccessful",
            "buildFailed",
            "buildFixed",
            "buildBroken",
            "serviceMessageReceived"
          ],
          "webhookUsageCount": 0
        },
        "legacy-empty": {
          "description": "Legacy Webhook (None)",
          "templateId": "legacy-empty",
          "projectId": "_Root",
          "projectExternalId": "_Root",
          "projectName": "_Root",
          "templateDescription": "Legacy Webhook",
          "templateToolTip": "Send a POST request with no content",
          "formatShortName": "empty",
          "formatDescription": "None",
          "supportedStates": [
            "buildSuccessful",
            "changesLoaded",
            "buildInterrupted",
            "buildPinned",
            "buildUnpinned",
            "buildAddedToQueue",
            "responsibilityChanged",
            "buildFailed",
            "buildFixed",
            "beforeBuildFinish",
            "buildBroken",
            "buildRemovedFromQueue",
            "serviceMessageReceived",
            "buildStarted"
          ],
          "supportedBuildEnumStates": [
            "BUILD_SUCCESSFUL",
            "CHANGES_LOADED",
            "BUILD_INTERRUPTED",
            "BUILD_PINNED",
            "BUILD_UNPINNED",
            "BUILD_ADDED_TO_QUEUE",
            "RESPONSIBILITY_CHANGED",
            "BUILD_FAILED",
            "BUILD_FIXED",
            "BEFORE_BUILD_FINISHED",
            "BUILD_BROKEN",
            "BUILD_REMOVED_FROM_QUEUE",
            "SERVICE_MESSAGE_RECEIVED",
            "BUILD_STARTED"
          ],
          "supportedBranchStates": [
            "buildSuccessful",
            "changesLoaded",
            "buildInterrupted",
            "buildPinned",
            "buildUnpinned",
            "buildAddedToQueue",
            "responsibilityChanged",
            "buildFailed",
            "buildFixed",
            "beforeBuildFinish",
            "buildBroken",
            "buildRemovedFromQueue",
            "serviceMessageReceived",
            "buildStarted"
          ],
          "webhookUsageCount": 0
        },
        "legacy-json": {
          "description": "Legacy Webhook (JSON)",
          "templateId": "legacy-json",
          "projectId": "_Root",
          "projectExternalId": "_Root",
          "projectName": "_Root",
          "templateDescription": "Legacy Webhook",
          "templateToolTip": "Send the payload formatted in JSON",
          "formatShortName": "json",
          "formatDescription": "JSON",
          "supportedStates": [
            "buildSuccessful",
            "changesLoaded",
            "buildInterrupted",
            "buildPinned",
            "buildUnpinned",
            "buildAddedToQueue",
            "responsibilityChanged",
            "buildFailed",
            "buildFixed",
            "beforeBuildFinish",
            "buildBroken",
            "buildRemovedFromQueue",
            "serviceMessageReceived",
            "buildStarted"
          ],
          "supportedBuildEnumStates": [
            "BUILD_SUCCESSFUL",
            "CHANGES_LOADED",
            "BUILD_INTERRUPTED",
            "BUILD_PINNED",
            "BUILD_UNPINNED",
            "BUILD_ADDED_TO_QUEUE",
            "RESPONSIBILITY_CHANGED",
            "BUILD_FAILED",
            "BUILD_FIXED",
            "BEFORE_BUILD_FINISHED",
            "BUILD_BROKEN",
            "BUILD_REMOVED_FROM_QUEUE",
            "SERVICE_MESSAGE_RECEIVED",
            "BUILD_STARTED"
          ],
          "supportedBranchStates": [
            "buildSuccessful",
            "changesLoaded",
            "buildInterrupted",
            "buildPinned",
            "buildUnpinned",
            "buildAddedToQueue",
            "responsibilityChanged",
            "buildFailed",
            "buildFixed",
            "beforeBuildFinish",
            "buildBroken",
            "buildRemovedFromQueue",
            "serviceMessageReceived",
            "buildStarted"
          ],
          "webhookUsageCount": 1
        },
        "legacy-nvpairs": {
          "description": "Legacy Webhook (Name Value Pairs)",
          "templateId": "legacy-nvpairs",
          "projectId": "_Root",
          "projectExternalId": "_Root",
          "projectName": "_Root",
          "templateDescription": "Legacy Webhook",
          "templateToolTip": "Send the payload as a set of Name/Value Pairs (www-urlencoded as if posted from a form)",
          "formatShortName": "nvpairs",
          "formatDescription": "Name Value Pairs",
          "supportedStates": [
            "buildSuccessful",
            "changesLoaded",
            "buildInterrupted",
            "buildPinned",
            "buildUnpinned",
            "buildAddedToQueue",
            "responsibilityChanged",
            "buildFailed",
            "buildFixed",
            "beforeBuildFinish",
            "buildBroken",
            "buildRemovedFromQueue",
            "serviceMessageReceived",
            "buildStarted"
          ],
          "supportedBuildEnumStates": [
            "BUILD_SUCCESSFUL",
            "CHANGES_LOADED",
            "BUILD_INTERRUPTED",
            "BUILD_PINNED",
            "BUILD_UNPINNED",
            "BUILD_ADDED_TO_QUEUE",
            "RESPONSIBILITY_CHANGED",
            "BUILD_FAILED",
            "BUILD_FIXED",
            "BEFORE_BUILD_FINISHED",
            "BUILD_BROKEN",
            "BUILD_REMOVED_FROM_QUEUE",
            "SERVICE_MESSAGE_RECEIVED",
            "BUILD_STARTED"
          ],
          "supportedBranchStates": [
            "buildSuccessful",
            "changesLoaded",
            "buildInterrupted",
            "buildPinned",
            "buildUnpinned",
            "buildAddedToQueue",
            "responsibilityChanged",
            "buildFailed",
            "buildFixed",
            "beforeBuildFinish",
            "buildBroken",
            "buildRemovedFromQueue",
            "serviceMessageReceived",
            "buildStarted"
          ],
          "webhookUsageCount": 0
        },
        "legacy-tailored-json": {
          "description": "Legacy Webhook (Tailored JSON in body)",
          "templateId": "legacy-tailored-json",
          "projectId": "_Root",
          "projectExternalId": "_Root",
          "projectName": "_Root",
          "templateDescription": "Legacy Webhook",
          "templateToolTip": "Send a JSON payload with content specified by parameter named \u0027body\u0027",
          "formatShortName": "tailoredjson",
          "formatDescription": "Tailored JSON in body",
          "supportedStates": [
            "buildSuccessful",
            "changesLoaded",
            "buildInterrupted",
            "buildPinned",
            "buildUnpinned",
            "buildFailed",
            "buildFixed",
            "beforeBuildFinish",
            "buildBroken",
            "serviceMessageReceived",
            "buildStarted"
          ],
          "supportedBuildEnumStates": [
            "BUILD_SUCCESSFUL",
            "CHANGES_LOADED",
            "BUILD_INTERRUPTED",
            "BUILD_PINNED",
            "BUILD_UNPINNED",
            "BUILD_FAILED",
            "BUILD_FIXED",
            "BEFORE_BUILD_FINISHED",
            "BUILD_BROKEN",
            "SERVICE_MESSAGE_RECEIVED",
            "BUILD_STARTED"
          ],
          "supportedBranchStates": [
            "buildSuccessful",
            "changesLoaded",
            "buildInterrupted",
            "buildPinned",
            "buildUnpinned",
            "buildFailed",
            "buildFixed",
            "beforeBuildFinish",
            "buildBroken",
            "serviceMessageReceived",
            "buildStarted"
          ],
          "webhookUsageCount": 0
        },
        "legacy-xml": {
          "description": "Legacy Webhook (XML)",
          "templateId": "legacy-xml",
          "projectId": "_Root",
          "projectExternalId": "_Root",
          "projectName": "_Root",
          "templateDescription": "Legacy Webhook",
          "templateToolTip": "Send the payload formatted in XML",
          "formatShortName": "xml",
          "formatDescription": "XML",
          "supportedStates": [
            "buildSuccessful",
            "changesLoaded",
            "buildInterrupted",
            "buildPinned",
            "buildUnpinned",
            "buildAddedToQueue",
            "responsibilityChanged",
            "buildFailed",
            "buildFixed",
            "beforeBuildFinish",
            "buildBroken",
            "buildRemovedFromQueue",
            "serviceMessageReceived",
            "buildStarted"
          ],
          "supportedBuildEnumStates": [
            "BUILD_SUCCESSFUL",
            "CHANGES_LOADED",
            "BUILD_INTERRUPTED",
            "BUILD_PINNED",
            "BUILD_UNPINNED",
            "BUILD_ADDED_TO_QUEUE",
            "RESPONSIBILITY_CHANGED",
            "BUILD_FAILED",
            "BUILD_FIXED",
            "BEFORE_BUILD_FINISHED",
            "BUILD_BROKEN",
            "BUILD_REMOVED_FROM_QUEUE",
            "SERVICE_MESSAGE_RECEIVED",
            "BUILD_STARTED"
          ],
          "supportedBranchStates": [
            "buildSuccessful",
            "changesLoaded",
            "buildInterrupted",
            "buildPinned",
            "buildUnpinned",
            "buildAddedToQueue",
            "responsibilityChanged",
            "buildFailed",
            "buildFixed",
            "beforeBuildFinish",
            "buildBroken",
            "buildRemovedFromQueue",
            "serviceMessageReceived",
            "buildStarted"
          ],
          "webhookUsageCount": 0
        },
        "statistics-report": {
          "description": "Webhook Statistics (JSON)",
          "templateId": "statistics-report",
          "projectId": "_Root",
          "projectExternalId": "_Root",
          "projectName": "_Root",
          "templateDescription": "Webhook Statistics",
          "templateToolTip": "Assemble Webhook configuration and usage statistics",
          "formatShortName": "json",
          "formatDescription": "JSON",
          "supportedStates": [
            "reportStatistics"
          ],
          "supportedBuildEnumStates": [
            "REPORT_STATISTICS"
          ],
          "supportedBranchStates": [],
          "webhookUsageCount": 0
        }
      }
    },
    "registeredAuthTypes": {
      "ntlm": {
        "description": "NTLM Authentication",
        "parameters": [
          {
            "key": "username",
            "required": true,
            "hidden": false,
            "name": "Username",
            "toolTip": "The user name.  This should not include the domain to authenticate with. For example: \u0027user\u0027 is correct whereas \u0027DOMAIN\u0026#x5c;user\u0027 is not."
          },
          {
            "key": "password",
            "required": true,
            "hidden": true,
            "name": "Password",
            "toolTip": "The password that the webhook should send to authenticate with the webserver."
          },
          {
            "key": "workstation",
            "required": true,
            "hidden": false,
            "name": "Workstation",
            "toolTip": "The workstation the authentication request is originating from. Essentially, the computer name for the TeamCity server."
          },
          {
            "key": "domain",
            "required": true,
            "hidden": false,
            "name": "Domain",
            "toolTip": "The Windows domain to authenticate within."
          }
        ]
      },
      "userpass": {
        "description": "Username/Password Authentication (Basic Auth)",
        "parameters": [
          {
            "key": "username",
            "required": true,
            "hidden": false,
            "name": "Username",
            "toolTip": "The username that the webhook should send to authenticate with the webserver."
          },
          {
            "key": "password",
            "required": true,
            "hidden": true,
            "name": "Password",
            "toolTip": "The password that the webhook should send to authenticate with the webserver."
          },
          {
            "key": "realm",
            "required": false,
            "hidden": false,
            "name": "Realm",
            "toolTip": "The Realm the server must present before this webhook will send credentials. This is ignored if preemptive is enabled, because the webhook does not make a first request to expect a 401 repsonse and to retreive the realm."
          }
        ]
      },
      "bearer": {
        "description": "Bearer Token Authentication (Bearer)",
        "parameters": [
          {
            "key": "bearer",
            "required": true,
            "hidden": false,
            "name": "Bearer Token",
            "toolTip": "The Bearer token that the webhook should send to authenticate with the webserver."
          }
        ]
      }
    }
  }
};

var webHook01 = {
  "url": "http://localhost:8111/webhooks/endpoint.html",
  "id": "500198782",
  "projectId": "TcPlugins",
  "enabled": true,
  "template": "test01",
  "webUrl": "http://localhost:8111/webhooks/index.html?projectId=TcPlugins",
  "href": "/app/rest/webhooks/configurations/TcPlugins/id:500198782",
  "buildState": [
    {
      "type": "buildAddedToQueue",
      "enabled": true
    },
    {
      "type": "buildRemovedFromQueue",
      "enabled": true
    },
    {
      "type": "buildStarted",
      "enabled": true
    },
    {
      "type": "changesLoaded",
      "enabled": true
    },
    {
      "type": "buildInterrupted",
      "enabled": true
    },
    {
      "type": "beforeBuildFinish",
      "enabled": true
    },
    {
      "type": "buildFinished",
      "enabled": true
    },
    {
      "type": "buildSuccessful",
      "enabled": true
    },
    {
      "type": "buildFailed",
      "enabled": true
    },
    {
      "type": "responsibilityChanged",
      "enabled": true
    },
    {
      "type": "buildPinned",
      "enabled": true
    },
    {
      "type": "buildUnpinned",
      "enabled": true
    },
    {
      "type": "serviceMessageReceived",
      "enabled": true
    }
  ],
  "buildTypes": {
    "allEnabled": true,
    "subProjectsEnabled": true,
    "id": []
  },
  "parameters": {
    "count": 0,
    "parameter": [],
    "href": "/app/rest/webhooks/configurations/TcPlugins/id:500198782/parameters"
  },
  "filters": {
    "count": 0,
    "filter": [],
    "href": "/app/rest/webhooks/configurations/TcPlugins/id:500198782/filters"
  },
  "headers": {
    "count": 0,
    "header": [],
    "href": "/app/rest/webhooks/configurations/TcPlugins/id:500198782/headers"
  }
};

var webHook02 = {
  "url": "http://localhost:8111/webhooks/endpoint.html",
  "id": "500198783",
  "projectId": "TcPlugins",
  "enabled": true,
  "template": "test01",
  "webUrl": "http://localhost:8111/webhooks/index.html?projectId=TcPlugins",
  "href": "/app/rest/webhooks/configurations/TcPlugins/id:500198783",
  "buildState": [
    {
      "type": "buildBroken",
      "enabled": true
    },
    {
      "type": "buildFixed",
      "enabled": true
    }
  ],
  "buildTypes": {
    "allEnabled": true,
    "subProjectsEnabled": true,
    "id": []
  },
  "parameters": {
    "count": 0,
    "parameter": [],
    "href": "/app/rest/webhooks/configurations/TcPlugins/id:500198783/parameters"
  },
  "filters": {
    "count": 0,
    "filter": [],
    "href": "/app/rest/webhooks/configurations/TcPlugins/id:500198783/filters"
  },
  "headers": {
    "count": 0,
    "header": [],
    "href": "/app/rest/webhooks/configurations/TcPlugins/id:500198783/headers"
  }
};

var webHook03 = {
  "url": "http://localhost:8111/webhooks/endpoint.html",
  "id": "500198784",
  "projectId": "TcPlugins",
  "enabled": true,
  "template": "test01",
  "webUrl": "http://localhost:8111/webhooks/index.html?projectId=TcPlugins",
  "href": "/app/rest/webhooks/configurations/TcPlugins/id:500198784",
  "buildState": [
    {
      "type": "buildBroken",
      "enabled": true
    },
    {
      "type": "buildFixed",
      "enabled": true
    }
  ],
  "buildTypes": {
    "allEnabled": true,
    "subProjectsEnabled": true,
    "id": []
  },
  "parameters": {
    "count": 1,
    "parameter": [],
    "href": "/app/rest/webhooks/configurations/TcPlugins/id:500198784/parameters"
  },
  "filters": {
    "count": 0,
    "filter": [],
    "href": "/app/rest/webhooks/configurations/TcPlugins/id:500198784/filters"
  },
  "headers": {
    "count": 0,
    "header": [],
    "href": "/app/rest/webhooks/configurations/TcPlugins/id:500198784/headers"
  }
};

var webHook04 = {
  "url": "http://localhost:8111/webhooks/endpoint.html",
  "id": "306243139",
  "projectId": "TcPlugins",
  "enabled": true,
  "template": "legacy-json",
  "webUrl": "http://localhost:8111/webhooks/index.html?projectId=TcPlugins",
  "href": "/app/rest/webhooks/configurations/TcPlugins/id:306243139",
  "buildState": [
    { "type": "buildStarted", "enabled": true },
    { "type": "changesLoaded", "enabled": true },
    { "type": "buildInterrupted", "enabled": true },
    { "type": "beforeBuildFinish", "enabled": true },
    { "type": "buildFinished", "enabled": true },
    { "type": "buildSuccessful", "enabled": true },
    { "type": "buildFailed", "enabled": true },
    { "type": "responsibilityChanged", "enabled": true }
  ],
  "buildTypes": {
    "allEnabled": false,
    "subProjectsEnabled": false,
    "id": [
      "Project_MyBuildId02"
    ]
  },
  "parameters": {
    "count": 2,
    "parameter": [
      {
        "id": "1",
        "name": "color",
        "value": "red",
        "secure": false,
        "includedInLegacyPayloads": true,
        "templateEngine": "STANDARD",
        "href": "/app/rest/webhooks/configurations/TcPlugins/id:306243139/parameters/id:1"
      },
      {
        "id": "2",
        "name": "notify",
        "value": "1",
        "secure": true,
        "includedInLegacyPayloads": false,
        "templateEngine": "VELOCITY",
        "href": "/app/rest/webhooks/configurations/TcPlugins/id:306243139/parameters/id:2"
      }
    ],
    "href": "/app/rest/webhooks/configurations/TcPlugins/id:306243139/parameters"
  },
  "filters": {
    "count": 3,
    "filter": [
      {
        "id": 1,
        "value": "${branchDisplayName}",
        "regex": "^master$",
        "enabled": true,
        "href": "/app/rest/webhooks/configurations/TcPlugins/id:306243139/filters/id:1"
      },
      {
        "id": 2,
        "value": "${buildInternalTypeId}",
        "regex": "^bt\\d$",
        "enabled": true,
        "href": "/app/rest/webhooks/configurations/TcPlugins/id:306243139/filters/id:2"
      },
      {
        "id": 3,
        "value": "${foo}",
        "regex": "^bar$",
        "enabled": true,
        "href": "/app/rest/webhooks/configurations/TcPlugins/id:306243139/filters/id:3"
      }
    ],
    "href": "/app/rest/webhooks/configurations/TcPlugins/id:306243139/filters"
  },
  "headers": {
    "count": 1,
    "header": [
      {
        "id": 1,
        "name": "my-header-showing-build-name",
        "value": "${buildName}",
        "href": "/app/rest/webhooks/configurations/TcPlugins/id:306243139/headers/id:1"
      }
    ],
    "href": "/app/rest/webhooks/configurations/TcPlugins/id:306243139/headers"
  }
};