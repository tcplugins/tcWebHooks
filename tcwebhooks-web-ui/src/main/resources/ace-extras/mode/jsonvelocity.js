define(function(require, exports, module) {
"use strict";

var oop = require("../lib/oop");
var HtmlMode = require("./json").Mode;
var JsonVelocityHighlightRules = require("./jsonvelocity_highlight_rules").VelocityHighlightRules;
var FoldMode = require("./folding/velocity").FoldMode;

var Mode = function() {
    JsonMode.call(this);
    this.HighlightRules = JsonVelocityHighlightRules;
    this.foldingRules = new FoldMode();
};
oop.inherits(Mode, HtmlMode);

(function() {
    this.lineCommentStart = "##";
    this.blockComment = {start: "#*", end: "*#"};
    this.$id = "ace/mode/jsonvelocity";
}).call(Mode.prototype);

exports.Mode = Mode;
});