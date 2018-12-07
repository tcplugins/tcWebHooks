define(function(require, exports, module) {
"use strict";

var oop = require("../lib/oop");
var JsonMode = require("./json").Mode;
var JsonVelocityHighlightRules = require("./jsonvelocity_highlight_rules").JsonVelocityHighlightRules;
var FoldMode = require("./folding/velocity").FoldMode;

var Mode = function() {
    JsonMode.call(this);
    this.HighlightRules = JsonVelocityHighlightRules;
    this.foldingRules = new FoldMode();
};
oop.inherits(Mode, JsonMode);

(function() {
    this.lineCommentStart = "##";
    this.blockComment = {start: "#*", end: "*#"};
    this.$id = "ace/mode/jsonvelocity";
}).call(Mode.prototype);

exports.Mode = Mode;
});