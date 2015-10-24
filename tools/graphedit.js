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
var xy = (function() {
    var currType = 'task';

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
                classes: n.json().classes,
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
                classes: e.json().classes,
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
            cy.elements().removeClass("visited");
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
        cy.add({
             group: "nodes",
             position: pos
        })
        .addClass(currType);
    }
    function findPathFromStart(to) {
        var from;
        var matrix = {};
        var reverse = {};
        cy.nodes().forEach(function(n) {
            if (n.hasClass("start")) {
                from = n;
            }
            matrix[n.id()] = [];
            reverse[n.id()] = [];
            n.removeClass("visited");
        });
        cy.edges().forEach(function(e) {
            matrix[e.source().id()].push(e.target().id());
            reverse[e.target().id()].push(e.source().id());
        });
        var path = [from.id()];
        var visited = {};
        var unvisited = {};
        findPath(matrix, from.id(), to.id(), path, visited, unvisited);

        if (to.id() in visited) {
            for (var id in visited) {
                if (visited[id] == visited[to.id()]) {
                    cy.$("#" + id).addClass("visited");
                }
            }
        } else {
            for (var id in unvisited) {
                if (unvisited[id] == unvisited[from.id()]) {
                    cy.$("#" + id).addClass("visited");
                }
            }

            var path = [to.id()];
            var visited = {};
            var unvisited = {};
            findPath(reverse, to.id(), null, path, visited, unvisited);
            for (var id in unvisited) {
                if (unvisited[id] == unvisited[to.id()]) {
                    cy.$("#" + id).addClass("visited");
                }
            }
        }
        
    }
    function findPath(matrix, fromId, toId, path, visited, unvisited) {
        if (fromId == toId) {
            countUp(path, visited);
        } else if (matrix[fromId].length == 0) {
            countUp(path, unvisited);
        } else {
            for (var i = 0; i < matrix[fromId].length; i++) {
                var next = matrix[fromId][i];
                if (path.indexOf(next) >= 0) {
                    continue;
                } else {
                    path.push(next);
                    findPath(matrix, next, toId, path, visited, unvisited);
                    path.pop();
                }
            }
        }
    }
    function countUp(path, visited) {
        for (var i = 0; i < path.length; i++) {
            var p = path[i];
            if (p in visited) {
                visited[p] = visited[p] + 1;
            } else {
                visited[p] = 1;
            }
        }
    }
    function printPath(path) {
        var s = "";
        var sep = "";
        for (var i = 0; i < path.length; i++) {
            s += sep + path[i];
            sep = "->";
        }
        return s;
    }
    function init() {
        cy = cytoscape({
            container: document.getElementById('graphedit'),
            style: [
                {
                    selector: 'node',
                    css: {
                        content: 'data(id)',
                        'text-valign': 'center',
                        'text-halign': 'center',
                        color: 'white',
                        shape: 'roundrectangle',
                        width: 60,
                        height: 40
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
                },
                {
                    selector: 'node.task',
                },
                {
                    selector: 'node.start',
                    css: {
                        content: '',
                        shape: 'ellipse',
                        width: 28,
                        height: 28,
                        'border-color': 'gray',
                        'border-width': '2',
                        'background-color': 'gray'
                    }
                },
                {
                    selector: 'node.end',
                    css: {
                        content: '',
                        shape: 'ellipse',
                        width: 26,
                        height: 26,
                        'border-color': 'gray',
                        'border-width': '4',
                        'background-color': 'white'
                    }
                },
                {
                    selector: 'node.xor',
                    css: {
                        content: 'x',
                        shape: 'diamond',
                        width: 30,
                        height: 30
                    }
                },
                {
                    selector: 'node.or',
                    css: {
                        content: 'o',
                        shape: 'diamond',
                        width: 30,
                        height: 30
                    }
                },
                {
                    selector: 'node.and',
                    css: {
                        content: '+',
                        shape: 'diamond',
                        width: 30,
                        height: 30
                    }
                },
                {
                    selector: 'node.complex',
                    css: {
                        content: '*',
                        shape: 'diamond',
                        width: 30,
                        height: 30
                    }
                },
                {
                    selector: 'node.join',
                    css: {
                        content: '',
                        shape: 'diamond',
                        width: 30,
                        height: 30
                    }
                },
                {
                    selector: 'node.visited',
                    css: {
                        'color': 'black',
                        'background-color': '#FFEEEE'
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
            if (target === cy) {
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
                } else {
                    findPathFromStart(target);
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
    
    function setType(type) {
        currType = type;
    }
    
    window.addEventListener('load', onResize);
    window.addEventListener('load', init);
    window.addEventListener('resize', onResize);
    
    return {
        'setType': setType
    };
})();
