(function() {
    function esc(text) {
        if (!text) return text;
        return text
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }
    function createDom(data) {
        var f = functionMap.functions[data.id];
        var node = document.createElement('div');
        node.id = 'trace-' + data.id;
        node.className = 'trace close';
        var icon = node.appendChild(document.createElement('a'));
        icon.className = 'icon';
        icon.onclick = toggle;
        var file = functionMap.files[f.fileId];
        var name = node.appendChild(document.createElement('a'));
        name.appendChild(document.createTextNode(functionMap.functions[data.id].name + '(' + data.args.join(', ') + ')'));
        if (file) {
            name.title = file + ':' + f.row + ':' + f.col;
            name.onclick = showSource;
        }
        for (var i = 0; i < data.children.length; i++) {
            node.appendChild(createDom(data.children[i]));
        }
        return node;
    }
    function toggle(evt) {
        var target = evt.target.parentNode;
        if (hasClass(target, 'open')) {
            removeClass(target, 'open');
            addClass(target, 'close');
        } else {
            removeClass(target, 'close');
            addClass(target, 'open');
        }
    }
    function showSource(evt) {
        var p = evt.target.title.split(/:/);
        window.open("sources/" + p[0] + ".html#line-" + p[1], "source");
    }
    function hasClass(node, name) {
        if (!node.className) {
            return false;
        } else {
            return (' ' + node.className + ' ').includes(' ' + name + ' ');
        }
    }
    function removeClass(node, name) {
        if (hasClass(node, name)) {
            var parts = node.className.trim().split(/ +/);
            parts.splice(parts.indexOf(name), 1);
            node.className = parts.join(' ');
        }
    }
    function addClass(node, name) {
        if (!hasClass(node, name)) {
            node.className = node.className ?
                    node.className + ' ' + name :
                    name;
        }
    }
    window.addEventListener('load', function() {
        var root = JSON.parse(localStorage.getItem("jstools.profile"));
        document.getElementById('content').appendChild(createDom(root));
    });
})();
