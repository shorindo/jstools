(function() {
    var summary = {};
    function esc(text) {
        if (!text) return text;
        return text
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }
    function calc(data) {
        var sum = summary[data.id];
        if (data.id in summary) {
            sum.count++;
            sum.elapsed += data.elapsed;
        } else {
            sum = summary[data.id] = {
                    id: data.id,
                    fileName: functionMap.files[functionMap.functions[data.id].fileId],
                    name: functionMap.functions[data.id].name,
                    count: 1,
                    elapsed: data.elapsed,
                    effective: data.elapsed
                };
        }
        for (var i = 0; i < data.children.length; i++) {
            calc(data.children[i]);
        }
        for (var i = 0; i < data.children.length; i++) {
            sum.effective -= data.children[i].elapsed;
        }
    }
    function show() {
        var array = [];
        for (var key in summary) {
            array.push(summary[key]);
        }
        array.sort(function(a, b) {
            return a.elapsed > b.elapsed ? -1 : (a.elapsed < b.elapsed ? 1 : 0);
        });
        var html = '<table>';
        for (var i = 0; i < array.length; i++) {
            var v = array[i];
            var h = '<tr><td><a target="source" href="sources/' +
                v.fileName + '.html#line-' + functionMap.functions[v.id].row +
                '">' + esc(v.name) + '</a></td><td>' + v.count + '</td><td>' +
                v.elapsed + '</td><td>' + v.effective + '</td></tr>';
            html += h;
        }
        html += '</table>';
        document.getElementById('content').innerHTML = html;
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
        calc(root);
        show();
    });
})();
