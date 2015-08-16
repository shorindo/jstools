$(function() {
    var currFunc;
    var grid = $().w2grid({
        name: 'profile',
        show: {
            toolbar: false,
            footer: true
        },
        columns: [
            { field: 'name', caption: 'name', size: '100%', sortable: true },
            { field: 'count', caption: 'count', size: '80px', sortable: true, render:'int' },
            { field: 'elapsed', caption: 'elapsed', size: '150px', sortable: true, render:'float:3' },
            { field: 'effective', caption: 'effective', size: '150px', sortable: true, render:'float:3' }
        ],
        onClick: function(evt) {
            console.log(evt);
            if (evt.column === 0 && evt.recid != 0) {
                currFunc = evt.recid;
                w2ui.layout.panels[0].tabs.enable('tab-source');
                w2ui.layout.panels[0].tabs.click('tab-source');
            }
        }
    });

    $('#layout').w2layout({
        name    : 'layout',
        panels  : [
            {
                type: 'main', 
                tabs: {
                    active: 'tab-profile',
                    tabs: [
                        { id: 'tab-profile', caption: 'profile' },
                        { id: 'tab-trace', caption: 'trace' },
                        { id: 'tab-source', caption: 'source', disabled: true }
                    ],
                    onClick: function (id, data) {
                        switch(id) {
                        case 'tab-profile':
                            w2ui.layout.content('main', grid);
                            break;
                        case 'tab-trace':
                            w2ui.layout.content('main', '');
                            break;
                        case 'tab-source':
                            showFunction(currFunc);
                            break;
                        }
                    }
                }
            }
        ],
        onRender: function(evt) {
            this.content('main', grid);
        }
    });
    
    function onResize() {
        var height = $(window).height();
        $('#layout').css('height', height + 'px');
    }
    
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
        for (var i = 0; i < array.length; i++) {
            var v = array[i];
            grid.add({
               recid: v.id,
               name: esc(v.name),
               count: v.count,
               elapsed: v.elapsed,
               effective: v.effective
            });
        }
    }
    function showFunction(id) {
        var f = functionMap.functions[id];
        var fileName = 'sources/' + functionMap.files[f.fileId] + '.html#line-' + f.row;
        w2ui.layout.load('main', fileName, null, function(evt) {
            var obj = $('#line-' + f.row).css('background', 'yellow');
            var offsetParent = obj[0].offsetParent;
            var offsetTop = obj[0].offsetTop;
            offsetParent.scrollTop = offsetTop - offsetParent.offsetHeight / 2;
            console.log(offsetParent.offsetHeight);
        });
        currFunc = id;
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

    var root = JSON.parse(localStorage.getItem("jstools.profile"));
    calc(root);
    show();
    
    onResize();
    $(window).resize(onResize);
});
