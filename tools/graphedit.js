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
    var nodes = [
           {
               "data": {
                   "id": "A"
               }
           },
           {
               "data": {
                   "id": "B"
               }
           },
           {
               "data": {
                   "id": "C"
               }
           },
           {
               "data": {
                   "id": "D"
               }
           },
           {
               "data": {
                   "id": "E"
               }
           },
           {
               "data": {
                   "id": "F"
               }
           },
           {
               "data": {
                   "id": "G"
               }
           },
           {
               "data": {
                   "id": "H"
               }
           }
       ];
    var edges = [
        {
            "data": {
                "id": "AB",
                "source": "A",
                "target": "B"
            }
        },
        {
            "data": {
                "id": "BC",
                "source": "B",
                "target": "C"
            }
        },
        {
            "data": {
                "id": "BD",
                "source": "B",
                "target": "D"
            }
        },
        {
            "data": {
                "id": "BE",
                "source": "B",
                "target": "E"
            }
        },
        {
            "data": {
                "id": "CF",
                "source": "C",
                "target": "F"
            }
        },
        {
            "data": {
                "id": "CG",
                "source": "C",
                "target": "G"
            }
        },
        {
            "data": {
                "id": "DH",
                "source": "D",
                "target": "H"
            }
        },
        {
            "data": {
                "id": "ED",
                "source": "E",
                "target": "D"
            }
        },
        {
            "data": {
                "id": "FH",
                "source": "F",
                "target": "H"
            }
        },
        {
            "data": {
                "id": "GH",
                "source": "G",
                "target": "H"
            }
        }
    ];

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
                    selector: '.hilit',
                    css: {
                        'background-color': 'red',
                        'line-color': 'red',
                        'target-arrow-color': 'red'
                    }
                }
            ],
            elements: {
                nodes: nodes,
                edges: edges
            },
            layout: {
                name: 'cose',
                animate             : false,
                fit                 : false,
                padding             : 50,
                randomize           : true,
                debug               : false,
            },
            ready: function() {
//                var div = this.container().appendChild(document.createElement('div'));
//                div.id = "cyto-message";
//                div.innerHTML =
//                    "ノード数：" + nodes.length + " 枝数：" + edges.length;
            }
        });
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
