WebHooksPlugin = {
	handleAjaxError: function(dialog, response) {
		dialog.cleanErrors();
		if (response.status === 422 || response.status === 400) {
			if (response.responseJSON.errored) {
				$j.each(response.responseJSON.errors, function(index, errorMsg){
					dialog.ajaxError(errorMsg)
				});
			}
		} else if (response.status === 403) {
			alert("You are not permissioned to perform this operation. Message is: " + response.responseText);
		} else {
			console.log("----- begin webhooks AJAX error response -----")
			console.log(response);
			console.log("----- end webhooks AJAX error response -----")
			alert("An unexpected error occured. Please see your browser's javascript console.");
		}
	},
    addTemplate: function(data) {
    	if (!restApiDetected) {
    		WebHooksPlugin.NoRestApiDialog.showDialog();
    	} else {
    		WebHooksPlugin.AddTemplateDialog.showDialog("Add New Template", 'addTemplate');
    	}
    },
    importTemplate: function(templateId) {
    	if (!restApiDetected) {
    		WebHooksPlugin.NoRestApiDialog.showDialog();
    	} else {
    		WebHooksPlugin.ImportTemplateDialog.showDialog("Import Template", 'importTemplate', templateId);
    	}
    },
    AddTemplateDialog: OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
    	getContainer: function () {
    		return $('addTemplateDialog');
    	},

    	formElement: function () {
    		return $('addTemplateForm');
    	},

    	showDialog: function (title, action, data) {
    		$j("input[id='WebhookTemplateaction']").val(action);
    		$j(".dialogTitle").text(title);
    		$j("#addTemplateForm #addTemplateDialogSubmit").hide();
    		$j("#addTemplateForm #addTemplateDialogExpand").show();
    		$j("#addTemplateForm .templateDetails").hide();
    		$j("#addTemplateForm .templateShort").show();
    		this.cleanFields(data);
    		this.cleanErrors();
    		this.showCentered();
    	},

    	expandDialog: function (title, action, data) {
    		$j("#addTemplateForm #addTemplateDialogExpand").hide();
    		$j("#addTemplateForm #addTemplateDialogSubmit").show();
    		$j("#addTemplateForm .templateShort").hide();
    		$j("#addTemplateForm .templateDetails").show();
    		this.showCentered();
    		this.loadProjectList();
    	},
    	
    	loadProjectList: function () {
    		$j("#templateDialogProjectSelect").append($j('<option></option>').val(null).text("Loading project list..."))
			$j.ajax ({
				url: window['base_uri'] + '/app/rest/projects',
				type: "GET",
				headers : {
					'Accept' : 'application/json'
				},
				success: function (response) {
					var myselect = $j('#templateDialogProjectSelect');
					var myselect2 = $j('#templateImportDialogProjectSelect');
					
					myselect.empty().append( $j('<option></option>').val(null).text("Choose a Project...") );
					myselect2.empty().append( $j('<option></option>').val(null).text("Choose a Project...") );
					$j(response.project).each(function(index, project) {
						if (project.id === '_Root') {
							myselect.append( $j('<option></option>').val(project.id).text(project.id) );
							myselect2.append( $j('<option></option>').val(project.id).text(project.id) );
						} else {
							myselect.append( $j('<option></option>').val(project.id).text(project.name) );
							myselect2.append( $j('<option></option>').val(project.id).text(project.name) );
						}
					});
				},
				error: function (response) {
					WebHooksPlugin.handleAjaxError(dialog, response);
				}
			});
    	},    	

    	cleanFields: function (data) {
    		this.cleanErrors();
    	},

    	cleanErrors: function () {
    		$j("#addTemplateForm .error").remove();
    	},

    	error: function($element, message) {
    		var next = $element.next();
    		if (next != null && next.prop("class") != null && next.prop("class").indexOf('error') > 0) {
    			next.text(message);
    		} else {
    			$element.after("<p class='error'>" + message + "</p>");
    		}
    	},

    	ajaxError: function(message) {
    		var next = $j("#ajaxTemplateAddResult").next();
    		if (next != null && next.prop("class") != null && next.prop("class").indexOf('error') > 0) {
    			next.text(message);
    		} else {
    			$j("#ajaxTemplateAddResult").after("<p class='error'>" + message + "</p>");
    		}
    	},

    	doPost: function() {
    		this.cleanErrors();

    		var dialog = this;
    		var myJsonContent = {
    				id : $j("#addTemplateForm input[id='template.id']").val(),
					description : $j("#addTemplateForm input[id='template.description']").val(),
					toolTip : $j("#addTemplateForm input[id='template.tooltip']").val(),
					rank : $j("#addTemplateForm input[id='template.rank']").val(),
					preferredDateFormat : $j("#addTemplateForm input[id='template.dateFormat']").val(),
					format : $j("#addTemplateForm select#payloadFormat").val()
    		};

    		$j.ajax ({
    			url: window['base_uri'] + '/app/rest/webhooks/templates/' + $j("#addTemplateForm select#templateDialogProjectSelect").val(),
    			type: "POST",
				data: JSON.stringify(myJsonContent),
				dataType: 'json',
    			headers : {
    				'Content-Type' : 'application/json',
    				'Accept' : 'application/json'
    			},
    			success: function (response) {
    				dialog.close();
    				window.location = window['base_uri'] + '/webhooks/template.html?template=' + myJsonContent.id;
    			},
    			error: function (response) {
					console.log(response);
					WebHooksPlugin.handleAjaxError(dialog, response);
    			}
    		});

    		return false;
    	}
    })),
    ImportTemplateDialog: OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
    	getContainer: function () {
    		return $('importTemplateDialog');
    	},

    	formElement: function () {
    		return $('importTemplateForm');
    	},

    	showDialog: function (title, action, data) {
    		dialog = this;

    		$j(".dialogTitle").html(title);
    		$j(".fs-upload-target").removeClass("fs-upload-dragover")
    							   .addClass("fs-upload-dragexit")
    							   .css("display", "block");
    		$j("#fs-uploaded").css("display", "none");
    		$j("#template-parse-error").css("visibility", "hidden");
    		this.showCentered();
    	},

    	handleFiles: function (files) {
			// let's just work with one file
			var file = files[0];
			var reader = new FileReader();

			if (!file.type.startsWith('application/json')){
				console.log("Refusing to parse file of mime type:" + file.type);
				$j("#template-parse-error").css("visibility", "visible");
				return;
			}
			reader.onload = function(e) {
				var my_template = JSON.parse (e.target.result);
				if (WebHooksPlugin.ImportTemplateDialog.validateTemplate(my_template)) {
					templateJson = my_template;
					$j("#template-parse-error").css("visibility", "hidden");
					WebHooksPlugin.ImportTemplateDialog.printTemplateData(my_template);
					WebHooksPlugin.ImportTemplateDialog.printTemplateStatus(my_template);
					$j("#importTemplateDialogSubmit").css("visibility", "visible");
				} else {
					$j("#template-parse-error").css("display", "block")
											   .css("visibility", "visible");
				}
			}

			reader.readAsText(file);
    	},

    	printTemplateStatus: function (template) {
			$j.ajax ({
				url: window['base_uri'] + '/app/rest/webhooks/templates/id:' + template.id,
				type: "GET",
				headers : {
					'Accept' : 'application/json'
				},
				success: function (response) {
					$j("#fs-uploaded .template-status")
						.html("A template with the same ID already exists.");

					if (response.status === "USER_OVERRIDDEN") {
						$j("#fs-uploaded .template-status")
							.append("<p>The existing template has status 'USER_OVERRIDDEN'. " +
									"This means there is a bundled template with user applied changes.");
					} else if (response.status === "USER_DEFINED") {
						$j("#fs-uploaded .template-status")
							.append("<p>The existing template has status 'USER_DEFINED'. " +
									"This means there is an existing template which has been " +
									"previously created or imported.");
					} else if (response.status === "PROVIDED") {
						$j("#fs-uploaded .template-status")
							.append("<p>The existing template has status 'PROVIDED'. " +
									"This means there is an existing bundled template. " +
									"Future updates to this bundled template will be overriden " +
									"by this template.");
					}
					$j("#fs-uploaded .template-status")
						.append("<p>Importing this template will overwite the existing one, " +
								"and any previous changes will be lost. Any existing webhooks " +
								"using this template will use the new template.");
				},
				error: function (xhr, ajaxOptions, thrownError) {
					if (xhr.status == 403) {
						$j("#fs-uploaded .template-status").html("A template with this ID already exists in a project you are not permissioned to see.");
					}  else if (xhr.status == 404) {
							$j("#fs-uploaded .template-status").html("No template with this ID exists. Importing this template will create a new template.");
					} else {
						console.log(xhr);
						console.log(ajaxOptions);
						console.log(thrownError);
					}
				}
			});
    	},

    	validateTemplate: function (template) {
    		return (
    				typeof template.id !== 'undefined'
    			&&  typeof template.format !== 'undefined' 
    			&&  typeof template.rank !== 'undefined'
    				);
    	},

    	printTemplateData: function (template) {
    		$j("#fs-uploaded .template-id").html(template.id);
    		$j("#fs-uploaded .template-description").html(template.description);
    		$j("#fs-uploaded .template-payload-format").html(template.format);
    		$j("#fs-uploaded .template-status").html("checking...");
    		$j(".fs-upload-target").css("display", "none");
    		$j("#fs-uploaded").css("display", "block");
    	},

    	doPost: function() {
    		this.cleanErrors();

    		var dialog = this;
    		var templateId = templateJson.id;
    		templateJson.projectId = $j("#importTemplateForm select#templateImportDialogProjectSelect").val();

			$j.ajax ({
				url: window['base_uri'] + '/app/rest/webhooks/templates/id:' + templateId,
				type: "GET",
				headers : {
					'Accept' : 'application/json'
				},
				success: function (response) {
					dialog.sendTemplate(dialog, templateJson, "PUT", window['base_uri'] + '/app/rest/webhooks/templates/id:' + templateId);
				},
				error: function (xhr, ajaxOptions, thrownError) {
					if (xhr.status == 403) {
						dialog.ajaxError(xhr.responseText);
					} else if (xhr.status == 404) {
						dialog.sendTemplate(dialog, templateJson, "POST", window['base_uri'] + '/app/rest/webhooks/templates/' + $j("#importTemplateForm select#templateImportDialogProjectSelect").val());
					} else {
						console.log(xhr);
						console.log(ajaxOptions);
						console.log(thrownError);
					}
				}
			});

    		return false;
    	},

    	cleanFields: function (data) {
    		this.cleanErrors();
    	},

    	cleanErrors: function () {
    		$j("#importTemplateForm .error").remove();
    	},

    	error: function($element, message) {
    		var next = $element.next();
    		if (next != null && next.prop("class") != null && next.prop("class").indexOf('error') > 0) {
    			next.text(message);
    		} else {
    			$element.after("<p class='error'>" + message + "</p>");
    		}
    	},

    	ajaxError: function(message) {
    		var next = $j("#ajaxTemplateImportResult").next();
    		if (next != null && next.prop("class") != null && next.prop("class").indexOf('error') > 0) {
    			next.text(message);
    		} else {
    			$j("#ajaxTemplateImportResult").after("<p class='error'>" + message + "</p>");
    		}
    	},

    	sendTemplate: function(dialog, template, method, url) {
    		$j.ajax ({
    			url: url,
    			type: method,
				data: JSON.stringify(template),
				dataType: 'json',
    			headers : {
    				'Content-Type' : 'application/json',
    				'Accept' : 'application/json'
    			},
    			success: function (response) {
    				dialog.close();
    				window.location = window['base_uri'] + '/webhooks/template.html?template=' + template.id;
    			},
    			error: function (xhr, ajaxOptions, thrownError) {
    				if (xhr.status == 403)  {
    					dialog.ajaxError("You are not permissioned to perform this operation. <br> " + xhr.responseText);
    				} else if (xhr.status == 405)  {
    					dialog.ajaxError("Please choose a valid Project.");
    				} else {
	    				console.log(xhr);
	    				WebHooksPlugin.handleAjaxError(dialog, xhr);
    				}
    			}
    		});

    	}
    }))
};


$j(function() {
	const dropzone = document.querySelector(".fs-upload-target");

	dropzone.addEventListener("dragover", function(e) {
		e.preventDefault();
		e.stopPropagation();
		e.dataTransfer.dropEffect = "copy";
	}, false);

	dropzone.addEventListener("dragenter", function(e) {
		e.preventDefault();
		e.stopPropagation();
		$j(".fs-upload-target").removeClass("fs-upload-dragexit").addClass("fs-upload-dragover");
	}, false);

	dropzone.addEventListener("dragexit", function(e) {
		e.preventDefault();
		e.stopPropagation();
		$j(".fs-upload-target").removeClass("fs-upload-dragover").addClass("fs-upload-dragexit");

	}, false);

	dropzone.addEventListener("drop", function(e) {
		e.preventDefault();
		e.stopPropagation();

		const dt = e.dataTransfer;
		const files = dt.files;

		WebHooksPlugin.ImportTemplateDialog.handleFiles(files);

	}, false);

	const fileElem = document.getElementById("fs-upload-input");

	dropzone.addEventListener("click", function (e) {
	    if (fileElem) {
	        fileElem.click();
	    }
	}, false);

	//Load the project list on page load.
	WebHooksPlugin.AddTemplateDialog.loadProjectList();
});

