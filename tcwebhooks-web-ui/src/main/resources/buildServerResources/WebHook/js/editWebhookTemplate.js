WebHooksPlugin = {
		
    editBuildEventTemplate: function(data) {
    	WebHooksPlugin.TemplateEditBuildEventDialog.showDialog("Edit Build Event Template", 'editBuildEventTemplate', data);
    },
    copyBuildEventTemplate: function(data) {
    	WebHooksPlugin.TemplateEditBuildEventDialog.showDialog("Copy Build Event Template", 'copyBuildEventTemplate', data);
    },
    addBuildEventTemplate: function(data) {
    	WebHooksPlugin.TemplateEditBuildEventDialog.showDialog("Add Build Event Template", 'addBuildEventTemplate', data);
    },
    copyFilter: function(data) {
    	DebRepoFilterPlugin.RepoEditFilterDialog.showDialog("Copy Artifact Filter", 'copyArtifactFilter', data);
    },
    deleteFilter: function(data) {
    	DebRepoFilterPlugin.RepoDeleteFilterDialog.showDialog("Delete Artifact Filter", 'deleteArtifactFilter', data);
    },
    TemplateEditBuildEventDialog: OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
        getContainer: function () {
            return $('editTemplateDialog');
        },

        formElement: function () {
            return $('editTemplateForm');
        },

        showDialog: function (title, action, data) {
        	
        	this.getWebHookTemplateData(data.templateName, data.templateNumber, action);
        	
            $j("input[id='DebRepoaction']").val(action);
            $j(".dialogTitle").html(title);
            this.cleanFields(data);
            this.cleanErrors();
            this.showCentered();
        },

        cleanFields: function (data) {
            $j("#repoEditFilterForm input[id='debrepo.uuid']").val(data.uuid);
            $j("#repoEditFilterForm input[id='debrepofilter.id']").val(data.id);
            $j(".runnerFormTable input[id='debrepofilter.regex']").val(data.regex);
            $j(".runnerFormTable input[id='debrepofilter.dist']").val(data.dist);
            $j(".runnerFormTable input[id='debrepofilter.component']").val(data.component);
            $j(".runnerFormTable select[id='debrepofilter.buildtypeid']").val(data.build);
            $j("#repoEditFilterForm input[id='projectId']").val(data.projectId);

            this.cleanErrors();
        },

        cleanErrors: function () {
            $j("#repoEditFilterForm .error").remove();
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
        	var next = $j("#ajaxResult").next();
        	if (next != null && next.prop("class") != null && next.prop("class").indexOf('error') > 0) {
        		next.text(message);
        	} else {
        		$j("#ajaxResult").after("<p class='error'>" + message + "</p>");
        	}
        },

        doValidate: function() {
            var errorFound = false;

            var name = $j('input[id="debrepo.name"]');
            if (name.val() == "") {
                this.error(name, "Please set the repository name");
                errorFound = true;
            }

            return !errorFound;
        },
        
		getWebHookTemplateData: function (templateName, buildTemplateId, action) {
			this.disableCheckboxes();
			this.clearEditor();
			this.getTemplateData(templateName, buildTemplateId, action);
		},
		putWebHookTemplateData: function () {
			this.disableCheckboxes();
			this.updateJsonDataFromForm();
			this.putTemplateData();
		},
		postWebHookTemplateData: function () {
			this.disableCheckboxes();
			this.updateJsonDataFromForm();
			this.postTemplateData();
		},
		disableCheckboxes: function () {
			$j("#editTemplateForm input.buildState").prop("disabled", true);
			$j("#editTemplateForm label").addClass("checkboxLooksDisabled");
		},
		enableCheckboxes: function () {
			$j("#editTemplateForm input.buildState").prop("disabled", false);
		},
		updateJsonDataFromForm: function () {
			myJson.templateText.content = editor.getValue();
			myJson.templateText.useTemplateTextForBranch = $j("#editTemplateForm input#useTemplateTextForBranch").is(':checked');
			myJson.branchTemplateText.content = editorBranch.getValue();
			
    		$j(myJson.state).each(function() {
    			//console.log(this.type + " :: "+ this.enabled);
    			this.enabled = $j("#editTemplateForm input[id='" + this.type + "']").prop( "checked");
    		});

		},
		clearEditor: function () {
			editor.session.setValue("Loading...");
			editorBranch.session.setValue("Loading...");
		},
		getTemplateData: function (templateName, buildTemplateId, action) {
			var dialog = this;
    		$j.ajax ({
    			url: window['base_uri'] + '/app/rest/webhooks/templates/id:' + templateName + '/templateItem/' + buildTemplateId + '?fields=$long,useTemplateTextForBranch,href,parentTemplate,content',
    		    type: "GET",
    		    headers : {
    		        'Accept' : 'application/json'
    		    },
    		    success: function (response) {
    				myJson = response;
    				dialog.handleGetSuccess(action);
    		    }
    		});
		}, 
		handleGetSuccess: function (action) {
			$j("#templateHeading").html(myJson.parentTemplate.description);
			this.updateCheckboxes(action);
			this.updateEditor(action);
		},
		putTemplateData: function () {
			var dialog = this;
			$j.ajax ({
				url: myJson.href,
				type: "PUT",
				data: JSON.stringify(myJson),
				dataType: 'json',
				headers : {
					'Content-Type' : 'application/json',
					'Accept' : 'application/json'
				},
				success: function (response) {
					//console.log(response);
					dialog.close();
					$("buildEventTemplatesContainer").refresh();
				},
				error: function (response) {
					console.log(response);
					alert(response);
				}
			});
		}, 
		postTemplateData: function () {
			var dialog = this;
			$j.ajax ({
				url: myJson.parentTemplate.href + "/templateItem",
				type: "POST",
				data: JSON.stringify(myJson),
				dataType: 'json',
				headers : {
					'Content-Type' : 'application/json',
					'Accept' : 'application/json'
				},
				success: function (response) {
					dialog.close();
					$("buildEventTemplatesContainer").refresh();
				},
				error: function (response) {
					console.log(response);
					alert(response);
				}
			});
		}, 
		handlePutSuccess: function () {
			$j("#templateHeading").html(myJson.parentTemplateDescription);
			this.updateCheckboxes();
			this.updateEditor();
		},
		updateCheckboxes: function (action) {
			
        	if (action === 'copyBuildEventTemplate' || action === 'addBuildEventTemplate') {
        		if (myJson.id == 'defaultTemplate') {
            		$j(myJson.state).each(function() {
            			console.log(this.type + " :: "+ this.enabled);
            			$j("#editTemplateForm input[id='" + this.type + "']").prop( "checked", false).prop( "disabled", ! this.enabled);
            			if (this.enabled) {
            				$j("#editTemplateForm td[class='" + this.type + "'] label").removeClass("checkboxLooksDisabled");
            			}
            		});
        		} else {
            		$j(myJson.state).each(function() {
            			console.log(this.type + " :: "+ this.enabled);
            			$j("#editTemplateForm input[id='" + this.type + "']").prop( "checked", false).prop( "disabled", ! this.editable && ! this.enabled);
            			if (this.editable && ! this.enabled) {
            				$j("#editTemplateForm td[class='" + this.type + "'] label").removeClass("checkboxLooksDisabled");
            			}
            		});
        		}
        		myJson.id = '_new';
        	} else {
	    		$j(myJson.state).each(function() {
	    			console.log(this.type + " :: "+ this.enabled);
	    			$j("#editTemplateForm input[id='" + this.type + "']").prop( "checked", this.enabled).prop( "disabled", ! this.editable);
	    			if (this.editable) {
	    				$j("#editTemplateForm td[class='" + this.type + "'] label").removeClass("checkboxLooksDisabled");
	    			}
	    		});
        	}
        	
        	if (action === 'addBuildEventTemplate') {
	    		$j("#editTemplateForm input[id='useTemplateTextForBranch']").prop( "checked", false).prop( "disabled", false);
				$j("label.useTemplateTextForBranch").removeClass("checkboxLooksDisabled");

        	} else {
	    		$j("#editTemplateForm input[id='useTemplateTextForBranch']").prop( "checked", myJson.templateText.useTemplateTextForBranch).prop( "disabled", false);
				$j("label.useTemplateTextForBranch").removeClass("checkboxLooksDisabled");
			}
		},
		updateEditor: function (action) {
			if (action === 'addBuildEventTemplate') {
				editor.session.setValue("");
				editorBranch.session.setValue("");				
			} else {
				console.log(myJson.templateText.content);
				editor.session.setValue(myJson.templateText.content);
				editorBranch.session.setValue(myJson.branchTemplateText.content);
				//editorBranch.setReadOnly(myJson.templateText.useTemplateTextForBranch);
			}
		},
		
		doPost: function() {
			console.log(myJson);
			if (myJson.id == '_new' || myJson.id == '_copy') {
				this.postWebHookTemplateData();
			} else {
				this.putWebHookTemplateData();
			}
			return false;
		},

        xxdoPost: function() {
            this.cleanErrors();

            if (!this.doValidate()) {
                return false;
            }
            var thing = $j(".runnerFormTable input[id='debrepofilter.regex']").val();
            console.log(thing);
            thing = JSON.stringify($j(".runnerFormTable input[id='debrepofilter.regex']").val()).slice(1, -1);
            console.log(thing);

            var parameters = {
                action: $j("#repoEditFilterForm #DebRepoaction").val(),
                "debrepo.filter.id": $j("#repoEditFilterForm input[id='debrepofilter.id']").val(),
                "debrepo.filter.regex": $j(".runnerFormTable input[id='debrepofilter.regex']").val(),
                "debrepo.filter.dist": $j(".runnerFormTable input[id='debrepofilter.dist']").val(),
                "debrepo.filter.component": $j(".runnerFormTable input[id='debrepofilter.component']").val(),
            	"debrepo.filter.buildtypeid": $j(".runnerFormTable select[id='debrepofilter.buildtypeid']").val(),
                "debrepo.uuid": $j("#repoEditFilterForm input[id='debrepo.uuid']").val()
            };

             var dialog = this;

            BS.ajaxRequest(window['base_uri'] + '/admin/webhookTemplateAction.html', {
                parameters: parameters,
                onComplete: function(transport) {
                    var shouldClose = true;
                    if (transport != null && transport.responseXML != null) {
                        var response = transport.responseXML.getElementsByTagName("response");
                        if (response != null && response.length > 0) {
                            var responseTag = response[0];
                            var error = responseTag.getAttribute("error");
                            if (error != null) {
                                shouldClose = false;
                                dialog.ajaxError(error);
                            } else if (responseTag.getAttribute("status") == "OK") {
                                shouldClose = true;
                            } else if (responseTag.firstChild == null) {
                                shouldClose = false;
                                alert("Error: empty response");
                            }
                        }
                    }
                    if (shouldClose) {
                        $("repoBuildTypesContainer").refresh();
                        dialog.close();
                        $("repoRepoInfoContainer").refresh();
                    }
                }
            });

            return false;
        }
    })),
    
    RepoDeleteFilterDialog: OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
    	getContainer: function () {
    		return $('repoDeleteFilterDialog');
    	},
    	
    	formElement: function () {
    		return $('repoDeleteFilterForm');
    	},
    	
    	showDialog: function (title, action, data) {
    		$j("input[id='DebRepoaction']").val(action);
    		$j(".dialogTitle").html(title);
    		this.cleanFields(data);
    		this.cleanErrors();
    		this.showCentered();
    	},
    	
    	cleanFields: function (data) {
    		$j("#repoDeleteFilterForm input[id='debrepo.uuid']").val(data.uuid);
    		$j("#repoDeleteFilterForm input[id='debrepofilter.id']").val(data.id);
    		$j("#repoDeleteFilterForm input[id='debrepofilter.buildtypeid']").val(data.build);
    		
    		this.cleanErrors();
    	},
    	
    	cleanErrors: function () {
    		$j("#repoDeleteFilterForm .error").remove();
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
    		var next = $j("#ajaxDeleteResult").next();
    		if (next != null && next.prop("class") != null && next.prop("class").indexOf('error') > 0) {
    			next.text(message);
    		} else {
    			$j("#ajaxDeleteResult").after("<p class='error'>" + message + "</p>");
    		}
    	},
    	
    	doValidate: function() {
    		var errorFound = false;
    		return !errorFound;
    	},
    	
    	doPost: function() {
    		this.cleanErrors();
    		
    		if (!this.doValidate()) {
    			return false;
    		}
    		
    		var parameters = {
    				action: $j("#repoDeleteFilterForm #DebRepoaction").val(),
    				"debrepo.filter.id": $j("#repoDeleteFilterForm input[id='debrepofilter.id']").val(),
    				"debrepo.filter.buildtypeid": $j("#repoDeleteFilterForm input[id='debrepofilter.buildtypeid']").val(),
    				"debrepo.uuid": $j("#repoDeleteFilterForm input[id='debrepo.uuid']").val()
    		};
    		
    		var dialog = this;
    		
    		BS.ajaxRequest(window['base_uri'] + '/admin/debianRepositoryAction.html', {
    			parameters: parameters,
    			onComplete: function(transport) {
    				var shouldClose = true;
    				if (transport != null && transport.responseXML != null) {
    					var response = transport.responseXML.getElementsByTagName("response");
    					if (response != null && response.length > 0) {
    						var responseTag = response[0];
    						var error = responseTag.getAttribute("error");
    						if (error != null) {
    							shouldClose = false;
    							dialog.ajaxError(error);
    						} else if (responseTag.getAttribute("status") == "OK") {
    							shouldClose = true;
    						} else if (responseTag.firstChild == null) {
    							shouldClose = false;
    							alert("Error: empty response");
    						}
    					}
    				}
    				if (shouldClose) {
    					$("repoBuildTypesContainer").refresh();
    					dialog.close();
    					$("repoRepoInfoContainer").refresh();
    				}
    			}
    		});
    		
    		return false;
    	}
    }))
};
