const WH = {
	    SimpleDialog: OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
	    	getContainer: function () {
	    		return $('deleteWebHookDialog');
	    	},

	    	formElement: function () {
	    		return $('deleteWebHookForm');
	    	},

            showDialog: function (title, action, data) {

                // $j("input[id='parameterProjectId']").val(data.projectId);
                // $j("input[id='WebHookParameteraction']").val(action);
                // $j(".dialogTitle").text(title);
                // $j("#editWebHookParameterDialogSubmit").val(action === "addWebhookParameter" ? "Add Parameter" : "Edit Parameter");
                this.resetAndShow(data);
                // this.getWebHookParameterData(data.projectId, data.parameterId, action);
                // $j("#viewRow_" + data.parameterId).animate({
                //     backgroundColor: "#ffffcc"
                // }, 1000 );
            },
            
            cancelDialog: function () {
                this.close();
                // $j("#viewRow_" + $j("#editWebHookParameterForm input[id='parameterId']").val()).animate({
                //     backgroundColor: "#ffffff"
                // }, 500 );
            },

            resetAndShow: function (data) {
                //this.disableAndClearCheckboxes();
                //this.cleanFields(data);
                this.showCentered();
                this.afterShow();
            },
        }))

    };