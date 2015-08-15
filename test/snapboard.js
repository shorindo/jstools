var snapboard = {
Pad : (function() {
    //private consntant variables
    const MODE_INIT = 0;
    const MODE_TRANSLATE = 1;
    const MODE_SCALE = 2;
    const MODE_PATH = 10;
    const MODE_LINE = 11;
    const MODE_RECT = 12;
    const MODE_CIRCLE = 13;
    const MODE_ELIPSE = 14;
    const MODE_POLYLINE = 15;
    const MODE_POLYGON = 16;
    const MODE_TEXT = 17;
    const MODE_ERASE = 30;
    const COLORS = [
        '#000000', '#FF0000', '#00FF00', '#0000FF', '#FFFF00', '#FF00FF', '#00FFFF'
    ];
    
    window.onerror = debug;

    //private properties and functions
    var pad, canvas, menu;
    var useTouch = "ontouchstart" in window ? true : false;
    var event = {
          start : useTouch ? 'touchstart' : 'mousedown',
          move  : useTouch ? 'touchmove'  : 'mousemove',
          end   : useTouch ? 'touchend'   : 'mouseup'
    };
    var settings = {
        stroke : COLORS[0],
        strokeOpacity : 0.5,
        strokeWidth : "5px",
        strokeLinejoin : "round",
        strokeLinecap : "round",
        fill : "none",
        background : "white"
    };

    var here = function(fn) {
        return fn.toString()
            .replace(/^.*?\/\*\s*[\r\n]+([\s\S]*)\*\/[\s\S]+/, "$1");
    };

    var getOffset = function(node) {
        var offset = {x:0, y:0};
        while (node) {
            offset.x += node.offsetLeft;
            offset.y += node.offsetTop;
            node = node.offsetParent;
        }
        return offset;
    };

    var fixEvent = function(evt) {
        if (!evt.target) evt.target = evt.srcElement;
        if (useTouch && evt.touches[0]) {
            evt.pageX = evt.touches[0].pageX;
            evt.pageY = evt.touches[0].pageY;
            var offset = getOffset(evt.target);
            evt.absX = evt.touches[0].pageX - offset.x;
            evt.absY = evt.touches[0].pageY - offset.y;
        } else {
            var offset = getOffset(evt.target);
            evt.absX = evt.layerX;
            evt.absY = evt.layerY;
        }
        return evt;
    };
    
    var transform = function(point) {
        //console.log("transform(" + point.x + "," + point.y + ")");
        var m = settings.matrix;
        var tx = m.a * point.x + m.c * point.y + m.e * 1.0;
        var ty = m.b * point.x + m.d * point.y + m.f * 1.0;
        return {x:tx, y:ty};
    };
    
    var inverse = function(point) {
        var m = settings.matrix.inverse();
        var tx = m.a * point.x + m.c * point.y + m.e * 1.0;
        var ty = m.b * point.x + m.d * point.y + m.f * 1.0;
        return {x:tx, y:ty};
    }

    var setStyle = function(style) {
        style.stroke = settings.stroke;
        style.strokeOpacity = settings.strokeOpacity;
        style.strokeWidth = settings.strokeWidth;
        style.strokeLinecap = settings.strokeLinecap;
        style.strokeLinejoin = settings.strokeLinejoin;
        style.fill = settings.fill;
    }

    var trace = function(msg) {
        //console.log("[T]" + msg);
    };
    
    var debug = function() {
        var msg = "";
        var sep = "";
        for (var i = 0; i < arguments.length; i++) {
            msg += sep + (typeof arguments[i] == 'object' ? JSON.stringify(arguments[i]) : arguments[i]);
            sep = ", ";
        }
        console.log(msg);
        var c = document.getElementById("console");
        if (c) {
            var line = document.createElement("div");
            line.appendChild(document.createTextNode(msg));
            c.appendChild(line);
            c.scrollTop = c.scrollHeight;
        }
    }

    var Class = function() {};
    Class.extend = function(opt) {
        var self = this;
        var fn = (function() {
            if (opt.hasOwnProperty('constructor'))
                return opt.constructor;
            else
                return function(){ return self.apply(this, opt); };
        })();
        for (var key in this.prototype) {
            fn.prototype[key] = this.prototype[key];
        }
        for (var key in opt) {
            fn.prototype[key] = opt[key];
        }
        fn.extend = Class.extend;
        return fn;
    };

    var Shape = Class.extend({
        onStart  : function(x, y) {},
        onFinish : function(x, y) {
            pad.save();
        },
        addShape : function(shape) {
            this.shapeList.push(shape);
            this.dom.appendChild(shape.dom);
        },
        removeShape : function(shape) {},
        importFromDOM : function(node) {
            trace("importFromDOM");
            if (node.nodeName != this.dom.nodeName) {
                return;
            }
            if (node.hasAttributes()) {
                for (var i = 0; i < node.attributes.length; i++) {
                    var item = node.attributes.item(i);
                    this.dom.setAttribute(item.name, item.value);
                }
            }
            for (var i = 0; i < node.childNodes.length; i++) {
                var child = node.childNodes.item(i);
                switch(child.nodeName) {
                case 'g': //TODO
                    canvas.importFromDOM(child);
                    for (var j = 0; j < canvas.dom.transform.animVal.length; j++) {
                        var item = canvas.dom.transform.animVal.getItem(j);
                        settings.matrix = canvas.dom.transform.animVal.getItem(j).matrix;
                    }
                    if (!settings.matrix) {
                        try {
                            settings.matrix = canvas.dom.ownerSVGDocument.createSVGMatrix();
                            debug(settings.matrix);
                        } catch (e) {
                            debug(e);
                        }
                    }
                    break;
                case 'path':
                    var path = new Path(0, 0);
                    path.importFromDOM(child);
                    this.addShape(path);
                    break;
                case 'line':
                    var line = new Line(0, 0);
                    line.importFromDOM(child);
                    this.addShape(line);
                case 'rect':
                    var rect = new Rect(0, 0);
                    rect.importFromDOM(child);
                    this.addShape(rect);
                case 'circle':
                    var circle = new Circle(0, 0);
                    circle.importFromDOM(child);
                    this.addShape(circle);
                case 'text':
                    var text = new Text(0, 0);
                    text.importFromDOM(child);
                    var lines = "";
                    var sep = "";
                    for (var j = 0; j < child.childNodes.length; j++) {
                        var tspan = child.childNodes.item(j);
                        if (tspan.nodeName == 'tspan') {
                            lines += sep + tspan.textContent;
                            sep = "<BR>";
                        }
                    }
                    text.setText(lines);
                    this.addShape(text);
                }
            }
        },
        exportToXML : function() {
            var node = this.dom;
            var result = "<" + node.nodeName;
            if (node.hasAttributes()) {
                for (var i = 0; i < node.attributes.length; i++) {
                    var item = node.attributes.item(i);
                    result += ' ' + item.name + '="' + item.value + '"';
                }
            }
            if (this.shapeList && this.shapeList.length > 0) {
                result += ">";
                for (var i = 0; i < this.shapeList.length; i++) {
                    var child = this.shapeList[i];
                    result += child.exportToXML();
                }
                result += "</" + node.nodeName + ">";
            } else {
                result += "/>";
            }
            return result;
        }
    });

    var Path = Shape.extend({
        constructor : function(x, y) {
            this.shapeList = [];
            this.dom = document.createElementNS("http://www.w3.org/2000/svg", "path");
            setStyle(this.dom.style);
            var p = inverse({x:x, y:y});
            this.moveTo(p.x, p.y);
            this.lineTo(p.x, p.y);
            this.ox = p.x;
            this.oy = p.y;
        },
        onStart : function(x, y) {
            var self = this;
            var draw = function(evt) {
                evt = fixEvent(evt);
                var point = inverse({x:evt.absX, y:evt.absY});
                self.lineTo(point.x, point.y, 5)
            };
            var finish = function(evt) {
                self.onFinish();
                document.removeEventListener(event.move, draw);
                document.removeEventListener(event.end, finish);
            };
            document.addEventListener(event.move, draw);
            document.addEventListener(event.end, finish);
        },
        moveTo : function(x, y) {
            var item = this.dom.createSVGPathSegMovetoAbs(x, y);
            this.dom.pathSegList.appendItem(item);
            this.ox = x;
            this.oy = y;
        },
        lineTo : function(x, y, accuracy) {
            accuracy = accuracy || 0;
            if (Math.sqrt(Math.pow(this.ox - x, 2) + Math.pow(this.oy - y, 2)) >= accuracy) {
                var seg = this.dom.createSVGPathSegLinetoAbs(x, y);
                this.dom.pathSegList.appendItem(seg);
                this.ox = x;
                this.oy = y;
            }
        }
    });

    var Line = Shape.extend({
        constructor : function(x, y) {
            this.shapeList = [];
            this.dom = document.createElementNS("http://www.w3.org/2000/svg", "line");
            setStyle(this.dom.style);
            var p = inverse({x:x, y:y});
            this.dom.setAttribute("x1", p.x);
            this.dom.setAttribute("y1", p.y);
            this.dom.setAttribute("x2", p.x);
            this.dom.setAttribute("y2", p.y);
            this.lineTo(p.x, p.y);
            this.ox = p.x;
            this.oy = p.y;
        },
        onStart : function(x, y) {
            var self = this;
            var draw = function(evt) {
                evt = fixEvent(evt);
                var point = inverse({x:evt.absX, y:evt.absY});
                self.lineTo(point.x, point.y, 5)
            };
            var finish = function(evt) {
                self.onFinish();
                document.removeEventListener(event.move, draw);
                document.removeEventListener(event.end, finish);
            };
            document.addEventListener(event.move, draw);
            document.addEventListener(event.end, finish);
        },
        lineTo : function(x, y, accuracy) {
            accuracy = accuracy || 0;
            if (Math.sqrt(Math.pow(this.ox - x, 2) + Math.pow(this.oy - y, 2)) >= accuracy) {
                this.dom.setAttribute("x2", x);
                this.dom.setAttribute("y2", y);
                this.ox = x;
                this.oy = y;
            }
        }
    });

    var Polyline = Shape.extend({
        constructor : function(x, y) {
            this.shapeList = [];
            this.dom = document.createElementNS("http://www.w3.org/2000/svg", "polyline");
            setStyle(this.dom.style);
            var p = inverse({x:x, y:y});
            this.dom.setAttribute("points", p.x + "," + p.y);
            this.points = [{x:p.x, y:p.y}, {x:p.x, y:p.y}];
            this.ox = p.x;
            this.oy = p.y;
        },
        onStart : function(x, y) {
            var self = this;
            var draw = function(evt) {
                evt = fixEvent(evt);
                var point = inverse({x:evt.absX, y:evt.absY});
                self.moveTo(point.x, point.y, 5)
            };
            var mark = function(evt) {
                evt = fixEvent(evt);
                var point = inverse({x:evt.absX, y:evt.absY});
                self.points.push({x:point.x, y:point.y});
                self.moveTo(point.x, point.y, 5)
            };
            document.addEventListener(event.move, draw);
            document.addEventListener(event.end, mark);
            return function(evt) {
                console.log(evt);
                return false;
            };
        },
        moveTo : function(x, y, accuracy) {
            accuracy = accuracy || 0;
            if (Math.sqrt(Math.pow(this.ox - x, 2) + Math.pow(this.oy - y, 2)) >= accuracy) {
                var p = this.points[0];
                var value = p.x + "," + p.y;
                for (var i = 1; i < this.points.length; i++) {
                    p = this.points[i];
                    value += " " + p.x + "," + p.y;
                }
                value += " " + x + "," + y;
                this.dom.setAttribute("points", value);
                this.ox = x;
                this.oy = y;
            }
        }
    });

    var Rect = Shape.extend({
        constructor : function(x, y) {
            this.shapeList = [];
            this.dom = document.createElementNS("http://www.w3.org/2000/svg", "rect");
            setStyle(this.dom.style);
            var p = inverse({x:x, y:y});
            this.dom.setAttribute("x", p.x);
            this.dom.setAttribute("y", p.y);
            this.dom.setAttribute("width", 0);
            this.dom.setAttribute("height", 0);
            this.sx = p.x;
            this.sy = p.y;
            this.ox = p.x;
            this.oy = p.y;
        },
        onStart : function(x, y) {
            var self = this;
            var draw = function(evt) {
                evt = fixEvent(evt);
                var point = inverse({x:evt.absX, y:evt.absY});
                self.rectTo(point.x, point.y, 5)
            };
            var finish = function(evt) {
                self.onFinish();
                document.removeEventListener(event.move, draw);
                document.removeEventListener(event.end, finish);
            };
            document.addEventListener(event.move, draw);
            document.addEventListener(event.end, finish);
        },
        rectTo : function(x, y, accuracy) {
            accuracy = accuracy || 0;
            if (Math.sqrt(Math.pow(this.ox - x, 2) + Math.pow(this.oy - y, 2)) >= accuracy) {
                var w = x - this.sx;
                var h = y - this.sy;
                if (w < 0) {
                    w = -w;
                    this.dom.setAttribute("x", x);
                    this.ox = this.sx;
                } else {
                    this.ox = x;
                }
                if (h < 0) {
                    h = -h;
                    this.dom.setAttribute("y", y);
                    this.oy = this.sy;
                } else {
                    this.oy = y;
                }
                this.dom.setAttribute("width", w);
                this.dom.setAttribute("height", h);
            }
        }
    });

    var Circle = Shape.extend({
        constructor : function(x, y) {
            this.shapeList = [];
            this.dom = document.createElementNS("http://www.w3.org/2000/svg", "circle");
            setStyle(this.dom.style);
            var p = inverse({x:x, y:y});
            this.dom.setAttribute("cx", p.x);
            this.dom.setAttribute("cy", p.y);
            this.dom.setAttribute("r", 0);
            this.ox = p.x;
            this.oy = p.y;
        },
        onStart : function(x, y) {
            var self = this;
            var draw = function(evt) {
                evt = fixEvent(evt);
                var point = inverse({x:evt.absX, y:evt.absY});
                self.circleTo(point.x, point.y, 5)
            };
            var finish = function(evt) {
                self.onFinish();
                document.removeEventListener(event.move, draw);
                document.removeEventListener(event.end, finish);
            };
            document.addEventListener(event.move, draw);
            document.addEventListener(event.end, finish);
        },
        circleTo : function(x, y, accuracy) {
            accuracy = accuracy || 0;
            var radius = Math.sqrt(Math.pow(this.ox - x, 2) + Math.pow(this.oy - y, 2));
            if (radius >= accuracy) {
                this.dom.setAttribute("r", radius);
            }
        }
    });
    
    var Text = Shape.extend({
        constructor : function(x, y) {
            this.shapeList = [];
            this.dom = document.createElementNS("http://www.w3.org/2000/svg", "text");
            this.dom.style.strokeWidth = "0px";
            this.dom.style.opacity = "1";
            this.dom.style.fill = "black";
            var p = inverse({x:x, y:y});
            this.dom.setAttribute("x", p.x);
            this.dom.setAttribute("y", p.y);
            this.ox = p.x;
            this.oy = p.y;
            //var text = this.dom.appendChild(document.createTextNode("Text"));
        },
        onStart : function(x, y) {
            var self = this;
            var editor = document.createElement("div");
            editor.contentEditable = true;
            editor.style.position = "absolute";
            editor.style.left = x + "px";
            editor.style.top = y + "px";
            editor.innerHTML = "";
            document.body.appendChild(editor);
            editor.focus();
            var finish = function(evt) {
                self.setText(editor.innerHTML);
                editor.parentNode.removeChild(editor);
                document.removeEventListener(event.start, finish);
                self.onFinish();
            };
            editor.addEventListener('blur', finish);
            editor.addEventListener('keydown', function(evt) {
                switch(evt.keyCode) {
                case 13:
                case 27: finish(); // ESC
                }
            });
            //document.addEventListener(event.start, finish);
        },
        setText : function(text) {
            var self = this;
            var lines = text.split(/<BR>/i);
            var style = window.getComputedStyle(document.body);
            var lineHeight = style.lineHeight;
            for (var i = 0; i < lines.length; i++) {
                var tspan = document.createElementNS("http://www.w3.org/2000/svg", "tspan");
                tspan.setAttribute("x", self.dom.getAttribute("x"));
                tspan.setAttribute("dy", lineHeight);
                tspan.style.lineHeight = lineHeight;
                tspan.appendChild(document.createTextNode(lines[i]));
                self.dom.appendChild(tspan);
            }
        }
    });

    var Eraser = Path.extend({
        constructor : function(x, y) {
            this.shapeList = [];
            this.dom = document.createElementNS("http://www.w3.org/2000/svg", "path");
            this.dom.style.stroke = "white";
            this.dom.style.strokeOpacity = 1;
            this.dom.style.strokeWidth = "20px";
            this.dom.style.fill = settings.fill;
            this.dom.style.strokeLinecap = settings.strokeLinecap;
            this.dom.style.strokeLinejoin = settings.strokeLinejoin;
            var p = inverse({x:x, y:y});
            this.moveTo(p.x, p.y);
            this.lineTo(p.x, p.y);
            this.ox = p.x;
            this.oy = p.y;
        }
    });

    var Translator = Class.extend({
        constructor : function() { return this; },
        onStart : function(evt) {
            var self = this;
            //debug("Translator.onStart()", typeof this);
            if (evt.touches && evt.touches.length > 1) {
                self.sp1 = {x:evt.touches[0].pageX, y:evt.touches[0].pageY};
                self.sp2 = {x:evt.touches[1].pageX, y:evt.touches[1].pageY};
                debug("pinch!", self.sp1);
            }
            var op = evt.touches ?
                    inverse({x:evt.touches[0].pageX, y:evt.touches[0].pageY}) :
                    inverse({x:evt.absX, y:evt.absY});
            var move = function(evt) {
                evt = fixEvent(evt);
                if (evt.touches && evt.touches.length > 1) {
                    var p1 = {x:evt.touches[0].pageX, y:evt.touches[0].pageY};
                    var p2 = {x:evt.touches[1].pageX, y:evt.touches[1].pageY};
                    var center = inverse({x:(self.sp1.x + self.sp2.x) / 2.0, y:(self.sp1.y + self.sp2.y) / 2.0});
                    var ol = Math.sqrt(Math.pow(self.sp1.x - self.sp2.x, 2) + Math.pow(self.sp1.y - self.sp2.y, 2));
                    var nl = Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
                    var scale = nl / ol;
                    canvas.scale(center, {x:scale, y:scale});
                    self.sp1 = p1;
                    self.sp2 = p2;
                    return;
                }
                var np = inverse({x:evt.absX, y:evt.absY});
                var dx = np.x - op.x;
                var dy = np.y - op.y;
                canvas.translate(dx, dy);
                ox = evt.absX;
                oy = evt.absY;
            };
            var finish = function(evt) {
                evt = fixEvent(evt);
                self.onFinish();
                document.removeEventListener(event.move, move);
                document.removeEventListener(event.end, finish);
                pad.dom.style.cursor = "default";
            };
            //FIXME
            if (evt.shiftKey) {
                var sx = 0.8;
                var sy = 0.8;
                canvas.scale(inverse({x:evt.absX, y:evt.absY}), {x:sx, y:sy});
            } else if (evt.altKey) {
                var sx = 1.2;
                var sy = 1.2;
                canvas.scale(inverse({x:evt.absX, y:evt.absY}), {x:sx, y:sy});
            } else {
                document.addEventListener(event.move, move);
                document.addEventListener(event.end, finish);
                pad.dom.style.cursor = "move";
            }
        },
        onFinish : function() {
            //debug("Translator.onFinish()");
            pad.save();
        }
    });

    var Canvas = Shape.extend({
        mode : MODE_TRANSLATE,
        constructor : function(parent) {
            trace("Canvas");
            var self = this;
            var hook;
            this.shapeList = [];
            this.parent = parent;
            this.shapeList = [];
            this.dom = parent.appendChild(document.createElementNS("http://www.w3.org/2000/svg", "g"));
            this.dom.id = "canvas-pane";
            var start = function(evt) {
                evt = fixEvent(evt);
                evt.preventDefault();
                evt.stopPropagation();
                if (hook && !hook(evt)) {
                    return;
                }
                switch(self.mode) {
                case MODE_PATH:
                    var shape = new Path(evt.absX, evt.absY);
                    self.addShape(shape);
                    hook = shape.onStart(evt.absX, evt.absY);
                    break;
                case MODE_LINE:
                    var shape = new Line(evt.absX, evt.absY);
                    self.addShape(shape);
                    hook = shape.onStart(evt.absX, evt.absY);
                    break;
                case MODE_POLYLINE:
                    var shape = new Polyline(evt.absX, evt.absY);
                    self.addShape(shape);
                    hook = shape.onStart(evt.absX, evt.absY);
                    break;
                case MODE_RECT:
                    var shape = new Rect(evt.absX, evt.absY);
                    self.addShape(shape);
                    hook = shape.onStart(evt.absX, evt.absY);
                    break;
                case MODE_CIRCLE:
                    var shape = new Circle(evt.absX, evt.absY);
                    self.addShape(shape);
                    hook = shape.onStart(evt.absX, evt.absY);
                    break;
                case MODE_TEXT:
                    var shape = new Text(evt.absX, evt.absY);
                    self.addShape(shape);
                    hook = shape.onStart(evt.absX, evt.absY);
                    break;
                case MODE_ERASE:
                    var shape = new Eraser(evt.absX, evt.absY);
                    self.addShape(shape);
                    hook = shape.onStart(evt.absX, evt.absY);
                    break;
                case MODE_TRANSLATE:
                    var trans = new Translator();
                    hook = trans.onStart(evt);
                }
                return false;
            };
            parent.addEventListener(event.start, start);
            return this;
        },
        clear : function() {
            trace("clear()");
            this.dom.setAttribute("transform", "matrix(1,0,0,1,0,0)");
            settings.matrix = this.dom.transform.animVal.getItem(0).matrix;
            this.shapeList = [];
            for (var i = this.dom.childNodes.length - 1; i >= 0; i--) {
                this.dom.removeChild(this.dom.childNodes.item(i));
            }
        },
        undo : function() {
            if (this.shapeList.length > 0) {
                var shape = this.shapeList.pop();
                shape.dom.parentNode.removeChild(shape.dom);
            }
        },
        translate : function(dx, dy) {
            var m = settings.matrix = settings.matrix.translate(dx, dy);
            this.dom.setAttribute("transform", "matrix(" + m.a + "," + m.b + "," + m.c + "," + m.d + "," + m.e + "," + m.f + ")");
        },
        scale : function(center, scale) {
            //debug("scale(" + center.x + "," + center.y + "," + scale.x + "," + scale.y + ")");
            var m = settings.matrix.translate(center.x, center.y);
            m = settings.matrix = m.scale(scale.x, scale.y);
            m = settings.matrix = m.translate(-center.x, -center.y);
            this.dom.setAttribute("transform", "matrix(" + m.a + "," + m.b + "," + m.c + "," + m.d + "," + m.e + "," + m.f + ")");
        }
    });

    var Menu = Class.extend({
        constructor : function(parent) {
            var parentNode = ((!parent || parent === window) ? document.body : parent);
            table = parentNode.appendChild(document.createElement("table"));
            table.style.borderCollapse = "collapse";
            table.style.position = "fixed";
            table.style.left = "50%";
            table.style.marginLeft = "-80px";
            table.style.bottom = "5px";
            table.style.fontFamily = "ionicons";
            table.style.fontSize = "24pt";
            this.frame = table.appendChild(document.createElement('tr'));
            this.add("&#xf1ee;", "undo", function(evt) {
                evt.preventDefault();
                canvas.undo();
            });
            this.add("&#xf263;", "tool", function(evt) {
                evt.preventDefault();
                if (canvas.mode == MODE_TRANSLATE) {
                    canvas.mode = MODE_PATH;
                    evt.target.innerHTML = "&#xf208;";
                } else if (canvas.mode == MODE_PATH) {
                    canvas.mode = MODE_LINE;
                    evt.target.innerHTML = "L";
                } else if (canvas.mode == MODE_LINE) {
                    canvas.mode = MODE_POLYLINE;
                    evt.target.innerHTML = "P";
                } else if (canvas.mode == MODE_POLYLINE) {
                    canvas.mode = MODE_RECT;
                    evt.target.innerHTML = "R";
                } else if (canvas.mode == MODE_RECT) {
                    canvas.mode = MODE_CIRCLE;
                    evt.target.innerHTML = "C";
                } else if (canvas.mode == MODE_CIRCLE) {
                    canvas.mode = MODE_TEXT;
                    evt.target.innerHTML = "T";
                } else if (canvas.mode == MODE_TEXT) {
                    canvas.mode = MODE_ERASE;
                    evt.target.innerHTML = "□";
                } else if (canvas.mode == MODE_ERASE) {
                    canvas.mode = MODE_TRANSLATE;
                    evt.target.innerHTML = "&#xf263;";
                }
                return false;
            });
            this.add('<span id="stroke-color">■</span>', "color", function(evt) {
                evt.preventDefault();
                var index = (COLORS.indexOf(settings.stroke) + 1) % COLORS.length;
                settings.stroke = COLORS[index];
                document.getElementById("stroke-color").style.color = settings.stroke;
                document.getElementById("stroke-color").style.opacity = settings.strokeOpacity;
            });
            //this.add("&#xf2ad;");
            //this.add("&#xf1ec;");
            this.add("&#xf216;", "new document", function(evt) {
                evt.preventDefault();
                pad.create();
            });
            return this;
        },
        add : function(icon, title, fn) {
            var menu = this.frame.appendChild(document.createElement("td"));
            menu.style.width = "40px";
            menu.style.height = "40px";
            menu.style.border = "1px solid gray";
            menu.style.textAlign = "center";
            menu.style.cursor = "pointer";
            menu.title = title;
            menu.innerHTML = icon;
            menu.onclick = fn;
        }
    });

    return Shape.extend({
        settings : settings,
        constructor : function(parent) {
            var self = this;
            this.shapeList = [];
            this.dom = document.createElementNS("http://www.w3.org/2000/svg", "svg");
            settings.matrix = this.dom.createSVGMatrix();
            if (!parent) parent = window;
            if (parent === window) {
                parent.document.body.appendChild(this.dom);
                with(this.dom.style) {
                    position = "absolute";
                    left = "0px";
                    top = "0px";
                };
            } else {
                parent.appendChild(this.dom);
                with(this.dom.style) {
                    position = "relative";
                    left = "0px";
                    top = "0px";
                };
            }

            canvas = new Canvas(this.dom);
            this.addShape(canvas);
            menu = new Menu(parent);
            return pad = this;
        },
        create : function() {
            trace("create()");
            canvas.clear();
            var id = this.dom.id = "GP:" + (new Date()).getTime();
            this.save();
        },
        load : function(key) {
            trace("load(" + key +")");
            canvas.clear();
            try {
                var xml = localStorage.getItem(key);
                var parser = new DOMParser();
                var doc = parser.parseFromString(xml,"text/xml");
                this.importFromDOM(doc.documentElement);
            } catch (e) {
                console.log(e);
            }
        },
        save : function() {
            trace("save()");
            try {
                var key = this.dom.id;
                this.dom.style.cursor = 'default';
                localStorage.setItem(key, this.toSVG(this.dom));
            } catch (e) {
                console.log(e);
            }
        },
        list : function() {
            var keys = [];
            try {
                for (var key in localStorage) {
                    if (key.match(/^GP:\d+$/)) {
                        keys.push(key);
                    }
                }
                keys.sort(function(a,b){ return a < b; });
            } catch (e) {
                console.log(e);
            }
            return keys;
        },
        sync : function() {
        },
        toSVG : function(node) {
            if (node.nodeType == Node.ELEMENT_NODE) {
                var result = "<" + node.nodeName;
                if (node.hasAttributes()) {
                    for (var i = 0; i < node.attributes.length; i++) {
                        var item = node.attributes.item(i);
                        result += ' ' + item.name + '="' + item.value + '"';
                    }
                }
                if (node.hasChildNodes) {
                    result += ">";
                    for (var i = 0; i < node.childNodes.length; i++) {
                        var child = node.childNodes.item(i);
                        result += this.toSVG(child);
                    }
                    result += "</" + node.nodeName + ">";
                } else {
                    result += "/>";
                }
                return result;
            } else if (node.nodeType == Node.TEXT_NODE) {
                return node.nodeValue;
            } else {
                return "";
            }
        }
    });
})()
};

var pad;
window.addEventListener("load", function() {
    pad = new snapboard.Pad(document.getElementById("snapboard"));
    var keys = pad.list();
    if (keys.length > 0) {
        pad.load(keys[0]);
    } else {
        pad.create();
    }
});
