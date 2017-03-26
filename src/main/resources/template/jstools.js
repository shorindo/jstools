if (!("__jstools__" in window)) {
    window.__jstools__ = (function() {
        var running = true;
        var controller;
        var root = new node(0, null);
        var curr = root;
        var handlers = {
                'enter': [],
                'exit': []
        }
        var now = "performance" in window ?
                function() { return performance.now(); } :
                function() { return (new Date()).getTime(); };

        function node(id, parent) {
            this.id = id;
            this.parent = parent;
            this.args = [];
            this.children = [];
            this.elapsed = -1;
        }
        node.prototype.setArgs = function(args) {
            this.args = [];
            if (!args) {
                return;
            }
            for (var i = 0; i < args.length; i++) {
                var arg = args[i];
                if (typeof arg == 'string') arg = "'" + arg + "'";
                else if (typeof arg == 'object') arg = 'object';
                this.args.push(arg);
            }
        };
        node.prototype.addChild = function(child) {
            this.children.push(child);
        };
        node.prototype.startTimer = function() {
            this.stime = now();
        };
        node.prototype.stopTimer = function() {
            this.elapsed = now() - this.stime;
            delete this.stime;
        };
        node.prototype.clear = function() {
            for (var i = 0; i < this.children.length; i++) {
                this.children[i].clear();
            }
            this.children = [];
        };

        function start() {
            running = true;
            controller.style.background = "red";
        }
        function stop() {
            running = false;
            controller.style.background = "lightgreen";
            report();
            clear();
        }
        function clear() {
            root.clear();
            root = new node(0, null);
            curr = root;
            //localStorage.removeItem('jstools.profile');
        }
        function enter(num, name) {
            if (!running) return;
            var args = arguments.callee.caller.arguments;
            var child = new node(num, curr);
            child.setArgs(args);
            child.startTimer();
            curr.addChild(child);
            curr = child;
            for (var i = 0; i < handlers['enter'].length; i++) {
                handlers['enter'][i](num, name);
            }
        }
        function exit(retval) {
            if (!running) return retval;
            curr.stopTimer();
            curr = curr.parent;
            for (var i = 0; i < handlers['exit'].length; i++) {
                handlers['exit'][i](retval);
            }
            return retval;
        }
        function report() {
            localStorage.setItem('jstools.profile', JSON.stringify(tree()));
            clear();
            window.open(".jstools/analyze.html", "jstools");
        }
        function tree() {
            function dig(node) {
                var result = { id:node.id, elapsed:node.elapsed, args:node.args, children:[]};
                for (var i = 0; i < node.children.length; i++) {
                    result.children.push(dig(node.children[i]));
                }
                return result;
            }
            return dig(root);
        }
        function createControl() {
            var icon = controller = document.createElement("div");
            icon.style.position = "fixed";
            icon.style.width = "10px";
            icon.style.height = "10px";
            icon.style.top = "10px";
            icon.style.right = "10px";
            icon.style.background = "red";
            icon.style.zIndex = 999999;
            icon.onclick = function(evt) {
                running ? stop() : start();
            };
            document.body.appendChild(icon);
        }
        function addHandler(name, fn) {
            handlers[name].push(fn);
        }
        
        window.addEventListener('load', createControl, false);
        return {
            'enter':  enter,
            'exit':   exit,
            'addHandler': addHandler
        }
    })();
}
