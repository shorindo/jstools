if (!("__jstools__" in window)) {
    window.__jstools__ = (function() {
        var root = new node(0, null);
        var curr = root;
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
        node.prototype.start = function() {
            this.stime = now();
        };
        node.prototype.stop = function() {
            this.elapsed = now() - this.stime;
            delete this.stime;
        };

        function clear() {
            root = new node(0, null);
            curr = root;
            //localStorage.removeItem('jstools.profile');
        }
        function enter(num) {
            var args = arguments.callee.caller.arguments;
            var child = new node(num, curr);
            child.setArgs(args);
            child.start();
            curr.addChild(child);
            curr = child;
        }
        function exit(retval) {
            curr.stop();
            curr = curr.parent;
            return retval;
        }
        function report() {
            localStorage.setItem('jstools.profile', JSON.stringify(this.tree()));
            this.clear();
            window.open(".jstools/trace.html", "trace");
            //window.open(".jstools/profile.html", "profile");
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
            var icon = document.createElement("div");
            icon.style.position = "fixed";
            icon.style.width = "10px";
            icon.style.height = "10px";
            icon.style.top = "10px";
            icon.style.right = "10px";
            icon.style.background = "red";
            icon.onclick = function(evt) {
                __jstools__.report();
            };
            document.body.appendChild(icon);
        }
        
        window.addEventListener('load', createControl, false);
        return {
            'clear':  clear,
            'enter':  enter,
            'exit':   exit,
            'tree':   tree,
            'report': report
        }
    })();
}
