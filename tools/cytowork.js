var $ = {
    inArray: function (v, arr) {
        return arr.indexOf(v);
    }
}
var cy;

QUnit.config.reorder = false;
//QUnit.config.autostart = false;

function arrayContains(arr, o, f) {
    for (var i = 0; i < arr.length; i++) {
        if (f(arr[i], o)) {
            return true;
        }
    }
    return false;
}

function createGraph(data) {
    var nodes = [];
    var edges = [];
    for (var from in data) {
        if (!arrayContains(nodes, from, function(a, b) { return a.data.id == b; })) {
            nodes.push({
                data: { id:from }
            });
        }
        var adj = data[from];
        for (var i = 0; i < adj.length; i++) {
            var to = adj[i];
            if (!arrayContains(nodes, to, function(a, b) { return a.data.id == b; })) {
                nodes.push({
                    data: { id:to }
                });
            }
            edges.push({
                data: { id: from + to, source:from, target:to }
            })
        }
    }
    
    console.log("nodes:" + JSON.stringify(nodes, null, 4));
    console.log("edges:" + JSON.stringify(edges, null, 4));
    
    var cy = cytoscape({
        container: document.getElementById('cyto-canvas'),
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
            var div = this.container().appendChild(document.createElement('div'));
            div.id = "cyto-message";
            div.innerHTML =
                "ノード数：" + nodes.length + " 枝数：" + edges.length;
        }
    });
    cy.on('tap', function(evt) {
        if (cy === evt.cyTarget) {
            console.log("background");
        } else {
            console.log(evt.cyTarget);
        }
    });
    return cy;
}

function log(msg) {
    var div = document.getElementById('cyto-message');
    if (div) div.innerHTML = msg;
    else console.log(msg);
}

function visit(node, edge) {
    cy.nodes('#' + node).toggleClass('hilit');
    cy.edges('#' + edge).toggleClass('hilit');
}

function rvisit(node, edge) {
    edge = edge[1] + edge[0];
    cy.nodes('#' + node).toggleClass('hilit');
    cy.edges('#' + edge).toggleClass('hilit');
}

/**
 * startノードから到達できるすべてのノードを探索する
 * 
 * @param data ノードデータ
 * @returns 訪れたノードのリスト
 */
function findPathFrom(start, data, visit) {
    var visited = [];
    var find = function(path) {
        var begin = path[path.length - 1];
        var prev = path.length > 1 ? path[path.length - 2] : null;
        var next = data[begin];
        if (visited.indexOf(begin) >= 0) {
            return;
        }
        visited.push(begin);
        visit && visit(begin, prev + begin);

        if (next && next.length > 0) {
            for (var i = 0; i < next.length; i++) {
                if (path.indexOf(next[i]) < 0) {
                    path.push(next[i]);
                    find(path);
                    path.pop();
                }
            }
        }
    };
    find([ start ]);
    return visited;
}

/**
 * goalノードに到達できるすべてのノードを探索する
 * @param goal
 * @param data
 */
function findPathTo(goal, data, visit) {
    //枝の向きを逆にしたデータを作る
    var reverse = {};
    for (var key in data) {
        var list = data[key];
        for (var i = 0; i < list.length; i++) {
            var next = list[i];
            if (!(next in reverse)) {
                reverse[next] = [];
            }
            reverse[next].push(key);
        }
    }
    
    return findPathFrom(goal, reverse, visit);
}

var DATA = {
    '1': {
        'A': []
    },
    '2': {
        'A': ['B']
    },
    '3': {
        'A': ['B'],
        'B': ['C']
    },
    '4': {
        'A': [ 'B', 'C' ],
        'B': [ 'D' ],
        'C': [ 'D' ]
    },
    '6': {
        'A': [ 'B', 'C' ],
        'B': [ 'D', 'E' ],
        'D': [ 'F' ],
        'E': [ 'F' ],
        'C': [ 'F' ]
    },
    '7': {
        'A': [ 'B', 'C' ],
        'B': [ 'D', 'E' ],
        'D': [ 'F' ],
        'E': [ 'F' ],
        'C': [ 'F' ],
        'F': [ 'G' ]
    },
    '8': {
        'A': [ 'B' ],
        'B': [ 'C', 'D', 'E' ],
        'C': [ 'F', 'G' ],
        'D': [ 'H' ],
        'E': [ 'D' ],
        'F': [ 'H' ],
        'G': [ 'H' ]
    }
};

var DELAY = 500;

QUnit.asyncTest('findPathFrom():1 nodes', function(assert) {
    var data = DATA['1'];
    cy = createGraph(data);
    var result = findPathFrom('A', data, visit);
    assert.equal(result.length, 1);
    setTimeout(QUnit.start, DELAY);
});

QUnit.asyncTest('findPathFrom():2 nodes', function(assert) {
    var data = DATA['2'];
    cy = createGraph(data);
    var result = findPathFrom('A', data, visit);
    assert.equal(result.length, 2);
    setTimeout(QUnit.start, DELAY);
});

QUnit.asyncTest('findPathFrom():3 nodes', function(assert) {
    var data = DATA['3'];
    cy = createGraph(data);
    var result = findPathFrom('A', data, visit);
    assert.equal(result.length, 3);
    setTimeout(QUnit.start, DELAY);
});

QUnit.asyncTest('findPathFrom():4 nodes', function(assert) {
    var data = DATA['4'];
    cy = createGraph(data);
    var result = findPathFrom('A', data, visit);
    assert.equal(result.length, 4);
    setTimeout(QUnit.start, DELAY);
});

QUnit.asyncTest('findPathFrom():6 nodes', function(assert) {
    var data = DATA['6'];
    cy = createGraph(data);
    var result = findPathFrom('A', data, visit);
    assert.equal(result.length, 6);
    setTimeout(QUnit.start, DELAY);
});

QUnit.asyncTest('findPathFrom():7 nodes', function(assert) {
    var data = DATA['7'];
    cy = createGraph(data);
    var result = findPathFrom('A', data, visit);
    assert.equal(result.length, 7);
    setTimeout(QUnit.start, DELAY);
});

QUnit.asyncTest('findPathFrom():8 nodes', function(assert) {
    var data = DATA['8'];
    cy = createGraph(data);
    var result = findPathFrom('A', data, visit);
    assert.equal(result.length, 8);
    setTimeout(QUnit.start, DELAY);
});

// findPathTo

QUnit.asyncTest('findPathTo():1 nodes', function(assert) {
    var data = DATA['1'];
    cy = createGraph(data);
    var result = findPathTo('A', data, rvisit);
    assert.equal(result.length, 1);
    setTimeout(QUnit.start, DELAY);
});

QUnit.asyncTest('findPathTo():2 nodes', function(assert) {
    var data = DATA['2'];
    cy = createGraph(data);
    var result = findPathTo('B', data, rvisit);
    assert.equal(result.length, 2);
    setTimeout(QUnit.start, DELAY);
});

QUnit.asyncTest('findPathTo():3 nodes', function(assert) {
    var data = DATA['3'];
    cy = createGraph(data);
    var result = findPathTo('C', data, rvisit);
    assert.equal(result.length, 3);
    setTimeout(QUnit.start, DELAY);
});

QUnit.asyncTest('findPathTo():4 nodes', function(assert) {
    var data = DATA['4'];
    cy = createGraph(data);
    var result = findPathTo('D', data, rvisit);
    assert.equal(result.length, 4);
    setTimeout(QUnit.start, DELAY);
});

QUnit.asyncTest('findPathTo():6 nodes', function(assert) {
    var data = DATA['6'];
    cy = createGraph(data);
    var result = findPathTo('F', data, rvisit);
    assert.equal(result.length, 6);
    setTimeout(QUnit.start, DELAY);
});

QUnit.asyncTest('findPathTo():7 nodes', function(assert) {
    var data = DATA['7'];
    cy = createGraph(data);
    var result = findPathTo('G', data, rvisit);
    assert.equal(result.length, 7);
    setTimeout(QUnit.start, DELAY);
});

QUnit.asyncTest('findPathTo():8 nodes', function(assert) {
    var data = DATA['8'];
    cy = createGraph(data);
    var result = findPathTo('H', data, rvisit);
    assert.equal(result.length, 8);
    setTimeout(QUnit.start, DELAY);
});

QUnit.asyncTest('random nodes', function(assert) {
    var seed = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    var n = Math.floor(Math.random() * seed.length);
    var data = {};
    for (var i = 0; i < n; i++) {
        var key = seed.charAt(i);
        var val = [];
        for (var j = 0; j < n; j++) {
            if (i != j && Math.random() > 0.9) {
                val.push(seed.charAt(j));
            }
        }
        data[key] = val;
    }
    cy = createGraph(data);
    var result = findPathFrom('A', data, visit);
    assert.equal(result.length, n);
    
    setTimeout(QUnit.start, DELAY);
});
