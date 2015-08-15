(function() {
    function esc(text) {
        if (!text) return text;
        return text
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }
    function dom(data) {
        var f = functionMap.functions[data.id];
        var node = document.createElement('div');
        node.id = 'trace-' + data.id;
        node.className = 'trace close';
        var icon = node.appendChild(document.createElement('a'));
        icon.className = 'icon';
        icon.onclick = toggle;
        var name = node.appendChild(document.createElement('a'));
        name.title = functionMap.files[f.fileId] + ':' + f.row + ':' + f.col;
        name.appendChild(document.createTextNode(functionMap.functions[data.id].name + '(' + data.args.join(', ') + ')'));
        for (var i = 0; i < data.children.length; i++) {
            node.appendChild(dom(data.children[i]));
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
        document.getElementById('content').appendChild(dom(root));
    });
})();
