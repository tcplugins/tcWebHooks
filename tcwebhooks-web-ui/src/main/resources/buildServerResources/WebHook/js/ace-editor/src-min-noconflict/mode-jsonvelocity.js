ace.define("ace/mode/json_highlight_rules",["require","exports","module","ace/lib/oop","ace/mode/text_highlight_rules"], function(require, exports, module) {
"use strict";

var oop = require("../lib/oop");
var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;

var JsonHighlightRules = function() {
    this.$rules = {
        "start" : [
            {
                token : "variable", // single line
                regex : '["](?:(?:\\\\.)|(?:[^"\\\\]))*?["]\\s*(?=:)'
            }, {
                token : "string", // single line
                regex : '"',
                next  : "string"
            }, {
                token : "constant.numeric", // hex
                regex : "0[xX][0-9a-fA-F]+\\b"
            }, {
                token : "constant.numeric", // float
                regex : "[+-]?\\d+(?:(?:\\.\\d*)?(?:[eE][+-]?\\d+)?)?\\b"
            }, {
                token : "constant.language.boolean",
                regex : "(?:true|false)\\b"
            }, {
                token : "text", // single quoted strings are not allowed
                regex : "['](?:(?:\\\\.)|(?:[^'\\\\]))*?[']"
            }, {
                token : "comment", // comments are not allowed, but who cares?
                regex : "\\/\\/.*$"
            }, {
                token : "comment.start", // comments are not allowed, but who cares?
                regex : "\\/\\*",
                next  : "comment"
            }, {
                token : "paren.lparen",
                regex : "[[({]"
            }, {
                token : "paren.rparen",
                regex : "[\\])}]"
            }, {
                token : "text",
                regex : "\\s+"
            }
        ],
        "string" : [
            {
                token : "constant.language.escape",
                regex : /\\(?:x[0-9a-fA-F]{2}|u[0-9a-fA-F]{4}|["\\\/bfnrt])/
            }, {
                token : "string",
                regex : '"|$',
                next  : "start"
            }, {
                defaultToken : "string"
            }
        ],
        "comment" : [
            {
                token : "comment.end", // comments are not allowed, but who cares?
                regex : "\\*\\/",
                next  : "start"
            }, {
                defaultToken: "comment"
            }
        ]
    };
    
};

oop.inherits(JsonHighlightRules, TextHighlightRules);

exports.JsonHighlightRules = JsonHighlightRules;
});

ace.define("ace/mode/matching_brace_outdent",["require","exports","module","ace/range"], function(require, exports, module) {
"use strict";

var Range = require("../range").Range;

var MatchingBraceOutdent = function() {};

(function() {

    this.checkOutdent = function(line, input) {
        if (! /^\s+$/.test(line))
            return false;

        return /^\s*\}/.test(input);
    };

    this.autoOutdent = function(doc, row) {
        var line = doc.getLine(row);
        var match = line.match(/^(\s*\})/);

        if (!match) return 0;

        var column = match[1].length;
        var openBracePos = doc.findMatchingBracket({row: row, column: column});

        if (!openBracePos || openBracePos.row == row) return 0;

        var indent = this.$getIndent(doc.getLine(openBracePos.row));
        doc.replace(new Range(row, 0, row, column-1), indent);
    };

    this.$getIndent = function(line) {
        return line.match(/^\s*/)[0];
    };

}).call(MatchingBraceOutdent.prototype);

exports.MatchingBraceOutdent = MatchingBraceOutdent;
});

ace.define("ace/mode/folding/cstyle",["require","exports","module","ace/lib/oop","ace/range","ace/mode/folding/fold_mode"], function(require, exports, module) {
"use strict";

var oop = require("../../lib/oop");
var Range = require("../../range").Range;
var BaseFoldMode = require("./fold_mode").FoldMode;

var FoldMode = exports.FoldMode = function(commentRegex) {
    if (commentRegex) {
        this.foldingStartMarker = new RegExp(
            this.foldingStartMarker.source.replace(/\|[^|]*?$/, "|" + commentRegex.start)
        );
        this.foldingStopMarker = new RegExp(
            this.foldingStopMarker.source.replace(/\|[^|]*?$/, "|" + commentRegex.end)
        );
    }
};
oop.inherits(FoldMode, BaseFoldMode);

(function() {
    
    this.foldingStartMarker = /([\{\[\(])[^\}\]\)]*$|^\s*(\/\*)/;
    this.foldingStopMarker = /^[^\[\{\(]*([\}\]\)])|^[\s\*]*(\*\/)/;
    this.singleLineBlockCommentRe= /^\s*(\/\*).*\*\/\s*$/;
    this.tripleStarBlockCommentRe = /^\s*(\/\*\*\*).*\*\/\s*$/;
    this.startRegionRe = /^\s*(\/\*|\/\/)#?region\b/;
    this._getFoldWidgetBase = this.getFoldWidget;
    this.getFoldWidget = function(session, foldStyle, row) {
        var line = session.getLine(row);
    
        if (this.singleLineBlockCommentRe.test(line)) {
            if (!this.startRegionRe.test(line) && !this.tripleStarBlockCommentRe.test(line))
                return "";
        }
    
        var fw = this._getFoldWidgetBase(session, foldStyle, row);
    
        if (!fw && this.startRegionRe.test(line))
            return "start"; // lineCommentRegionStart
    
        return fw;
    };

    this.getFoldWidgetRange = function(session, foldStyle, row, forceMultiline) {
        var line = session.getLine(row);
        
        if (this.startRegionRe.test(line))
            return this.getCommentRegionBlock(session, line, row);
        
        var match = line.match(this.foldingStartMarker);
        if (match) {
            var i = match.index;

            if (match[1])
                return this.openingBracketBlock(session, match[1], row, i);
                
            var range = session.getCommentFoldRange(row, i + match[0].length, 1);
            
            if (range && !range.isMultiLine()) {
                if (forceMultiline) {
                    range = this.getSectionRange(session, row);
                } else if (foldStyle != "all")
                    range = null;
            }
            
            return range;
        }

        if (foldStyle === "markbegin")
            return;

        var match = line.match(this.foldingStopMarker);
        if (match) {
            var i = match.index + match[0].length;

            if (match[1])
                return this.closingBracketBlock(session, match[1], row, i);

            return session.getCommentFoldRange(row, i, -1);
        }
    };
    
    this.getSectionRange = function(session, row) {
        var line = session.getLine(row);
        var startIndent = line.search(/\S/);
        var startRow = row;
        var startColumn = line.length;
        row = row + 1;
        var endRow = row;
        var maxRow = session.getLength();
        while (++row < maxRow) {
            line = session.getLine(row);
            var indent = line.search(/\S/);
            if (indent === -1)
                continue;
            if  (startIndent > indent)
                break;
            var subRange = this.getFoldWidgetRange(session, "all", row);
            
            if (subRange) {
                if (subRange.start.row <= startRow) {
                    break;
                } else if (subRange.isMultiLine()) {
                    row = subRange.end.row;
                } else if (startIndent == indent) {
                    break;
                }
            }
            endRow = row;
        }
        
        return new Range(startRow, startColumn, endRow, session.getLine(endRow).length);
    };
    this.getCommentRegionBlock = function(session, line, row) {
        var startColumn = line.search(/\s*$/);
        var maxRow = session.getLength();
        var startRow = row;
        
        var re = /^\s*(?:\/\*|\/\/|--)#?(end)?region\b/;
        var depth = 1;
        while (++row < maxRow) {
            line = session.getLine(row);
            var m = re.exec(line);
            if (!m) continue;
            if (m[1]) depth--;
            else depth++;

            if (!depth) break;
        }

        var endRow = row;
        if (endRow > startRow) {
            return new Range(startRow, startColumn, endRow, line.length);
        }
    };

}).call(FoldMode.prototype);

});

ace.define("ace/mode/json",["require","exports","module","ace/lib/oop","ace/mode/text","ace/mode/json_highlight_rules","ace/mode/matching_brace_outdent","ace/mode/behaviour/cstyle","ace/mode/folding/cstyle","ace/worker/worker_client"], function(require, exports, module) {
"use strict";

var oop = require("../lib/oop");
var TextMode = require("./text").Mode;
var HighlightRules = require("./json_highlight_rules").JsonHighlightRules;
var MatchingBraceOutdent = require("./matching_brace_outdent").MatchingBraceOutdent;
var CstyleBehaviour = require("./behaviour/cstyle").CstyleBehaviour;
var CStyleFoldMode = require("./folding/cstyle").FoldMode;
var WorkerClient = require("../worker/worker_client").WorkerClient;

var Mode = function() {
    this.HighlightRules = HighlightRules;
    this.$outdent = new MatchingBraceOutdent();
    this.$behaviour = new CstyleBehaviour();
    this.foldingRules = new CStyleFoldMode();
};
oop.inherits(Mode, TextMode);

(function() {

    this.getNextLineIndent = function(state, line, tab) {
        var indent = this.$getIndent(line);

        if (state == "start") {
            var match = line.match(/^.*[\{\(\[]\s*$/);
            if (match) {
                indent += tab;
            }
        }

        return indent;
    };

    this.checkOutdent = function(state, line, input) {
        return this.$outdent.checkOutdent(line, input);
    };

    this.autoOutdent = function(state, doc, row) {
        this.$outdent.autoOutdent(doc, row);
    };

    this.createWorker = function(session) {
        var worker = new WorkerClient(["ace"], "ace/mode/json_worker", "JsonWorker");
        worker.attachToDocument(session.getDocument());

        worker.on("annotate", function(e) {
            session.setAnnotations(e.data);
        });

        worker.on("terminate", function() {
            session.clearAnnotations();
        });

        return worker;
    };


    this.$id = "ace/mode/json";
}).call(Mode.prototype);

exports.Mode = Mode;
});

ace.define("ace/mode/jsonvelocity_highlight_rules",["require","exports","module","ace/lib/oop","ace/lib/lang","ace/mode/text_highlight_rules","ace/mode/json_highlight_rules"], function(require, exports, module) {
"use strict";

var oop = require("../lib/oop");
var lang = require("../lib/lang");
var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;
var JsonHighlightRules = require("./json_highlight_rules").JsonHighlightRules;

var JsonVelocityHighlightRules = function() {
    JsonHighlightRules.call(this);

    var builtinConstants = lang.arrayToMap(
        ('true|false|null').split('|')
    );

    var builtinFunctions = lang.arrayToMap(
        ("_DateTool|_DisplayTool|_EscapeTool|_FieldTool|_MathTool|_NumberTool|_SerializerTool|_SortTool|_StringTool|_XPathTool").split('|')
    );

    var builtinVariables = lang.arrayToMap(
        ('$contentRoot|$foreach').split('|')
    );

    var keywords = lang.arrayToMap(
        ("#set|#macro|#include|#parse|" +
        "#if|#elseif|#else|#foreach|" +
        "#break|#end|#stop"
        ).split('|')
    );

    this.$rules.start.push(
        {
            token : "comment",
            regex : "##.*$"
        },{
            token : "comment.block", // multi line comment
            regex : "#\\*",
            next : "vm_comment"
        }, {
            token : "string.regexp",
            regex : "[/](?:(?:\\[(?:\\\\]|[^\\]])+\\])|(?:\\\\/|[^\\]/]))*[/]\\w*\\s*(?=[).,;]|$)"
        }, {
            token : "string", // single line
            regex : '["](?:(?:\\\\.)|(?:[^"\\\\]))*?["]'
        }, {
            token : "string", // single line
            regex : "['](?:(?:\\\\.)|(?:[^'\\\\]))*?[']"
        }, {
            token : "constant.numeric", // hex
            regex : "0[xX][0-9a-fA-F]+\\b"
        }, {
            token : "constant.numeric", // float
            regex : "[+-]?\\d+(?:(?:\\.\\d*)?(?:[eE][+-]?\\d+)?)?\\b"
        }, {
            token : "constant.language.boolean",
            regex : "(?:true|false)\\b"
        }, {
            token : function(value) {
                if (keywords.hasOwnProperty(value))
                    return "keyword";
                else if (builtinConstants.hasOwnProperty(value))
                    return "constant.language";
                else if (builtinVariables.hasOwnProperty(value))
                    return "variable.language";
                else if (builtinFunctions.hasOwnProperty(value) || builtinFunctions.hasOwnProperty(value.substring(1)))
                    return "support.function";
                else if (value == "debugger")
                    return "invalid.deprecated";
                else
                    if(value.match(/^(\$[a-zA-Z_][a-zA-Z0-9_]*)$/))
                        return "variable";
                    return "identifier";
            },
            regex : "[a-zA-Z$#][a-zA-Z0-9_]*\\b"
        }, {
            token : "keyword.operator",
            regex : "!|&|\\*|\\-|\\+|=|!=|<=|>=|<|>|&&|\\|\\|"
        }, {
            token : "lparen",
            regex : "[[({]"
        }, {
            token : "rparen",
            regex : "[\\])}]"
        }, {
            token : "text",
            regex : "\\s+"
        }
    );

    this.$rules["vm_comment"] = [
        {
            token : "comment", // closing comment
            regex : "\\*#|-->",
            next : "start"
        }, {
            defaultToken: "comment"
        }
    ];

    this.$rules["vm_start"] = [
        {
            token: "variable",
            regex: "}",
            next: "pop"
        }, {
            token : "string.regexp",
            regex : "[/](?:(?:\\[(?:\\\\]|[^\\]])+\\])|(?:\\\\/|[^\\]/]))*[/]\\w*\\s*(?=[).,;]|$)"
        }, {
            token : "string", // single line
            regex : '["](?:(?:\\\\.)|(?:[^"\\\\]))*?["]'
        }, {
            token : "string", // single line
            regex : "['](?:(?:\\\\.)|(?:[^'\\\\]))*?[']"
        }, {
            token : "constant.numeric", // hex
            regex : "0[xX][0-9a-fA-F]+\\b"
        }, {
            token : "constant.numeric", // float
            regex : "[+-]?\\d+(?:(?:\\.\\d*)?(?:[eE][+-]?\\d+)?)?\\b"
        }, {
            token : "constant.language.boolean",
            regex : "(?:true|false)\\b"
        }, {
            token : function(value) {
                if (keywords.hasOwnProperty(value))
                    return "keyword";
                else if (builtinConstants.hasOwnProperty(value))
                    return "constant.language";
                else if (builtinVariables.hasOwnProperty(value))
                    return "variable.language";
                else if (builtinFunctions.hasOwnProperty(value) || builtinFunctions.hasOwnProperty(value.substring(1)))
                    return "support.function";
                else if (value == "debugger")
                    return "invalid.deprecated";
                else
                    if(value.match(/^(\$[a-zA-Z_$][a-zA-Z0-9_]*)$/))
                        return "variable";
                    return "identifier";
            },
            regex : "[a-zA-Z_$][a-zA-Z0-9_$]*\\b"
        }, {
            token : "keyword.operator",
            regex : "!|&|\\*|\\-|\\+|=|!=|<=|>=|<|>|&&|\\|\\|"
        }, {
            token : "lparen",
            regex : "[[({]"
        }, {
            token : "rparen",
            regex : "[\\])}]"
        }, {
            token : "text",
            regex : "\\s+"
        }
    ];

    for (var i in this.$rules) {
        this.$rules[i].unshift({
            token: "variable",
            regex: "\\${",
            push: "vm_start"
        });
    }

    this.normalizeRules();
};

oop.inherits(JsonVelocityHighlightRules, TextHighlightRules);

exports.JsonVelocityHighlightRules = JsonVelocityHighlightRules;
});

ace.define("ace/mode/folding/velocity",["require","exports","module","ace/lib/oop","ace/mode/folding/fold_mode","ace/range"], function(require, exports, module) {
"use strict";

var oop = require("../../lib/oop");
var BaseFoldMode = require("./fold_mode").FoldMode;
var Range = require("../../range").Range;

var FoldMode = exports.FoldMode = function() {};
oop.inherits(FoldMode, BaseFoldMode);

(function() {

    this.getFoldWidgetRange = function(session, foldStyle, row) {
        var range = this.indentationBlock(session, row);
        if (range)
            return range;

        var re = /\S/;
        var line = session.getLine(row);
        var startLevel = line.search(re);
        if (startLevel == -1 || line[startLevel] != "##")
            return;

        var startColumn = line.length;
        var maxRow = session.getLength();
        var startRow = row;
        var endRow = row;

        while (++row < maxRow) {
            line = session.getLine(row);
            var level = line.search(re);

            if (level == -1)
                continue;

            if (line[level] != "##")
                break;

            endRow = row;
        }

        if (endRow > startRow) {
            var endColumn = session.getLine(endRow).length;
            return new Range(startRow, startColumn, endRow, endColumn);
        }
    };
    this.getFoldWidget = function(session, foldStyle, row) {
        var line = session.getLine(row);
        var indent = line.search(/\S/);
        var next = session.getLine(row + 1);
        var prev = session.getLine(row - 1);
        var prevIndent = prev.search(/\S/);
        var nextIndent = next.search(/\S/);

        if (indent == -1) {
            session.foldWidgets[row - 1] = prevIndent!= -1 && prevIndent < nextIndent ? "start" : "";
            return "";
        }
        if (prevIndent == -1) {
            if (indent == nextIndent && line[indent] == "##" && next[indent] == "##") {
                session.foldWidgets[row - 1] = "";
                session.foldWidgets[row + 1] = "";
                return "start";
            }
        } else if (prevIndent == indent && line[indent] == "##" && prev[indent] == "##") {
            if (session.getLine(row - 2).search(/\S/) == -1) {
                session.foldWidgets[row - 1] = "start";
                session.foldWidgets[row + 1] = "";
                return "";
            }
        }

        if (prevIndent!= -1 && prevIndent < indent)
            session.foldWidgets[row - 1] = "start";
        else
            session.foldWidgets[row - 1] = "";

        if (indent < nextIndent)
            return "start";
        else
            return "";
    };

}).call(FoldMode.prototype);

});

ace.define("ace/mode/jsonvelocity",["require","exports","module","ace/lib/oop","ace/mode/json","ace/mode/jsonvelocity_highlight_rules","ace/mode/folding/velocity"], function(require, exports, module) {
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
});                (function() {
                    ace.require(["ace/mode/jsonvelocity"], function(m) {
                        if (typeof module == "object" && typeof exports == "object" && module) {
                            module.exports = m;
                        }
                    });
                })();