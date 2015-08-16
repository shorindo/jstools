$(function() {
    var currFunc;
    var summary = [];
    
    function initProfile() {
        var summary = {};

        function calc(data) {
            data.effective = 0;
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
                sum.effective -= data.children[i].effective;
            }
        }

        calc(root);
        var records = [];
        for (var key in summary) {
            var rec = summary[key];
            records.push({
               recid: rec.id,
               name: esc(rec.name),
               count: rec.count,
               elapsed: rec.elapsed,
               effective: rec.effective
            });
        }
        records.sort(function(a, b) {
            return a.effective < b.effective ? 1 :
                (a.effective > b.effective ? -1 : 0);
        });

        var grid = $('#content-profile').w2grid({
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
                if (evt.column === 0 && evt.recid != 0) {
                    currFunc = evt.recid;
                    w2ui.layout.panels[0].tabs.enable('tab-source');
                    w2ui.layout.panels[0].tabs.click('tab-source');
                }
            },
            sortData: [
                { "field": "effective", "direction": "DESC" }
            ],
            records: records
        });
    }

    $('#layout').w2layout({
        name    : 'layout',
        panels  : [
            {
                type: 'main', 
                content:
                    '<div id="content-profile"></div>' +
                    '<div id="content-trace"></div>' +
                    '<div id="content-source"></div>',
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
                            $('#content-profile').css('display', 'block');
                            $('#content-trace').css('display', 'none');
                            $('#content-source').css('display', 'none');
                            break;
                        case 'tab-trace':
                            var o = $(w2ui.layout.el('main'));
                            $('#content-profile').css('display', 'none');
                            $('#content-trace').css('display', 'block');
                            $('#content-source').css('display', 'none');
                            break;
                        case 'tab-source':
                            $('#content-profile').css('display', 'none');
                            $('#content-trace').css('display', 'none');
                            $('#content-source').css('display', 'block');
                            showFunction(currFunc);
                            break;
                        }
                    }
                }
            }
        ]
    });
    function initTrace() {
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
            var target = $(evt.target.parentNode);
            if (target.hasClass('open')) {
                target.removeClass('open');
                target.addClass('close');
            } else {
                target.removeClass('close');
                target.addClass('open');
            }
        }

        $('#content-trace').append(createDom(root));
    }
    
    var root = JSON.parse(localStorage.getItem("jstools.profile"));
    initProfile();
    initTrace();
    
    function onResize() {
        var height = $(window).height();
        $('#layout').css('height', height + 'px');
    }
    
    function esc(text) {
        if (!text) return text;
        return text
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }
    function showFunction(id) {
        function hilit(id) {
            $('pre.line.hilit').removeClass('hilit');
            var obj = $('#line-' + id).addClass('hilit');
            var offsetParent = obj[0].offsetParent;
            var offsetTop = obj[0].offsetTop;
            offsetParent.scrollTop = offsetTop - offsetParent.offsetHeight / 2;
        }
        var f = functionMap.functions[id];
        var fileName = 'sources/' + functionMap.files[f.fileId] + '.html';

        if ($('#content-source').attr("title") != fileName) {
            $.ajax({
                type: "GET",
                url: fileName,
                dataType: "html",
                success: function(html){
                    $("#content-source").html('');
                    $("#content-source").append(html);
                    $("#content-source").attr('title', fileName);
                    hilit(f.row);
                }
            });
        } else {
            hilit(f.row);
        }
        currFunc = id;
    }
    function showSource(evt) {
        currFunc = evt.target.parentNode.id.replace(/^trace\-/, '');
        w2ui.layout.panels[0].tabs.enable('tab-source');
        w2ui.layout.panels[0].tabs.click('tab-source');
    }

    onResize();
    $(window).resize(onResize);
});
