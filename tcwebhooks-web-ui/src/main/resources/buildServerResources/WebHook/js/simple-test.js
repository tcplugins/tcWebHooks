const WHTest = {
    showEditDialog: function(data, tab) {
        this.EditDialog.showDialog("Edit Web Hook", 'updateWebHook', data, tab);
    },

    EditDialog: OO.extend(WH.SimpleDialog, {
        afterShow: function() {
            alert("Showing somehing!");
        } 
    })
}