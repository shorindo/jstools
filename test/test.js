/*
 * 
 */
QUnit.config.reorder = false;
QUnit.test("initial root", function(assert) {
    __jstools__.clear();
    var tree = __jstools__.tree();
    assert.equal(tree.id, 0);
    assert.equal(tree.parent, null);
    assert.equal(tree.children.length, 0);
});

QUnit.test("enter/exit single call", function(assert) {
    __jstools__.clear();
    function foo() {
        __jstools__.enter(1);
        __jstools__.exit();
    }
    foo();
    var tree = __jstools__.tree();
    assert.equal(tree.children[0].id, 1);
});

QUnit.test("enter/exit sub call", function(assert) {
    __jstools__.clear();
    function foo() {
        __jstools__.enter(1);
        bar();
        __jstools__.exit();
    }
    function bar() {
        __jstools__.enter(2);
        __jstools__.exit();
    }
    foo();
    var tree = __jstools__.tree();
    assert.equal(tree.children[0].id, 1);
    assert.equal(tree.children[0].children[0].id, 2);
});

QUnit.test("enter/exit multiple call", function(assert) {
    __jstools__.clear();
    function foo() {
        __jstools__.enter(1);
        __jstools__.exit();
    }
    function bar() {
        __jstools__.enter(2);
        __jstools__.exit();
    }
    foo();
    bar();
    var tree = __jstools__.tree();
    console.log(JSON.stringify(tree, null, 4));
    assert.equal(tree.children[0].id, 1);
    assert.equal(tree.children[1].id, 2);
});

QUnit.test('enter/exit recursive call', function(assert) {
    __jstools__.clear();
    function recurs(count) {
        __jstools__.enter(1);
        if (count > 0)
            recurs(--count);
        __jstools__.exit();
    }
    recurs(3);
    var tree = __jstools__.tree();
    console.log(JSON.stringify(tree, null, 4));
    assert.equal(tree.children[0].id, 1);
    assert.equal(tree.children[0].children[0].id, 1);
    assert.equal(tree.children[0].children[0].children[0].id, 1);
});
