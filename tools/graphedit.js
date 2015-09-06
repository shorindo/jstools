/*
 * Copyright 2015 Shorindo, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var cy;
(function() {
    function Dialog() {
        var pane, frame, okButton, cancelButton;
        var self = this;
        self.open = function(opt) {
            opt = opt ? opt : {};
            pane = document.getElementById('dialog-pane');
            frame = document.getElementById('dialog-frame');
            okButton = document.getElementById('dialog-ok');
            cancelButton = document.getElementById('dialog-cancel');
            if (opt.title) head.innerHTML = opt.title;
            okButton.onclick = function() {
                opt.ok && opt.ok();
                self.close();
            };
            cancelButton.onclick = self.close;
            pane.style.display = 'block';
            frame.style.display = 'block';
        };
        self.close = function() {
            pane.style.display = 'none';
            frame.style.display = 'none';
        };
    }
    var dialog = new Dialog();

    function save() {
        var result = {
            nodes: [],
            edges: []
        };
        cy.nodes().forEach(function(n) {
            result.nodes.push({
                group: 'nodes',
                id: n.id(),
                position: {
                    x: n.position('x') + cy.pan('x'),
                    y: n.position('y') + cy.pan('y')
                }
            });
        });
        cy.edges().forEach(function(e) {
            result.edges.push({
                group: 'edges',
                id: e.id(),
                data: {
                    source: e.source().id(),
                    target: e.target().id()
                }
            });
        });
        //console.log(JSON.stringify(result, null, 4));
        localStorage.setItem("graphedit", JSON.stringify(result));
    }
    function load() {
        var data = localStorage.getItem('graphedit');
        if (data) {
            data = JSON.parse(data);
            //console.log(JSON.stringify(data, null, 4));
            for (var i = 0; i < data.nodes.length; i++) {
                cy.add(data.nodes[i]);
            }
            for (var i = 0; i < data.edges.length; i++) {
                cy.add(data.edges[i]);
            }
        }
    }
    function toggle_menu() {
        var menu = document.getElementById("menu");
        var clazz = menu.getAttribute("class");
        if (clazz && (' ' + clazz + ' ').indexOf(' open ') >= 0) {
          menu.removeAttribute('class');
        } else {
          menu.setAttribute('class', 'open');
        }
    }
    function create_node(pos) {
//        dialog.open({
//            ok: function() {
                cy.add({
                    group: "nodes",
                    name: 'name',
                    href: 'http://shorindo.com',
                    label: 'label',
                    position: pos
                });
//            }
//        });
    }
    function init() {
        cy = cytoscape({
            container: document.getElementById('graphedit'),
            style: [
                {
                    selector: 'node',
                    css: {
                        'content': 'data(id)',
                        'text-valign': 'center',
                        'text-halign': 'center',
                        'color': 'white'
                    }
                },
                {
                    selector: 'edge',
                    css: {
                        'target-arrow-shape': 'triangle',
                        'line-color': 'black',
                        'target-arrow-color': 'black'
                    }
                },
                {
                    selector: ':selected',
                    css: {
                        'background-color': 'lime',
                        'line-color': 'lime',
                        'target-arrow-color': 'lime',
                        'source-arrow-color': 'lime',
                        'color': 'black'
                    }
                }
            ],
            elements: {
                nodes: [],
                edges: []
            },
            layout: {
                name: 'preset',
                fit: false
            }
        });
        cy.on('tap', function(evt) {
            var target = evt.cyTarget;
            if (target == cy) {
                create_node(evt.cyPosition);
            } else if (target.group() == 'nodes'){
                if (evt.originalEvent.ctrlKey) {
                    cy.nodes(':selected').forEach(function(source) {
                        cy.add({
                            group: 'edges',
                            data: { source: source.id(), target: target.id() }
                        });
                    });
                } else if (target.selected()) {
                    // for override default action
                    setTimeout(function() {
                        target.unselect();
                    }, 100);
                }
            }
        });
        window.addEventListener('keydown', function(evt) {
            switch(evt.key) {
            case 'Delete':
                cy.elements(':selected').remove();
                break;
            case 'Escape':
                cy.elements(':selected').unselect();
                break;
            case 's':
                if (evt.ctrlKey) {
                    evt.preventDefault();
                    evt.stopPropagation();
                    save();
                    return false;
                }
                break;
            }
        });
        document.getElementById('menu-tab').addEventListener('click', toggle_menu);
        load();
    }
    
    function onResize() {
        var canvas = document.getElementById('graphedit');
        canvas.style.width = window.innerWidth + "px";
        canvas.style.height = window.innerHeight + "px";
    }
    
    window.addEventListener('load', onResize);
    window.addEventListener('load', init);
    window.addEventListener('resize', onResize);
})();
