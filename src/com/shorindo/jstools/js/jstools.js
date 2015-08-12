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
            this.children = [];
            this.elapsed = -1;
        }
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
        function init() {
            root = new node(0, null);
            curr = root;
            localStorage.removeItem('jstools.profile');
            
        }
        function enter(num) {
            var child = new node(num, curr);
            child.start();
            curr.addChild(child);
            curr = child;
        }
        function exit() {
            curr.stop();
            curr = curr.parent;
        }
        function report() {
            localStorage.setItem('jstools.profile', JSON.stringify(this.tree()));
            var w = window.open("", "profile");
            var d = w.document.documentElement;
            d.innerHTML = "hoge";
        }
        function tree() {
            function dig(node) {
                var result = { id:node.id, elapsed:node.elapsed, children:[]};
                for (var i = 0; i < node.children.length; i++) {
                    result.children.push(dig(node.children[i]));
                }
                return result;
            }
            return dig(root);
        }
        return {
            'init':   init,
            'enter':  enter,
            'exit':   exit,
            'tree':   tree,
            'report': report
        }
    })();
}
