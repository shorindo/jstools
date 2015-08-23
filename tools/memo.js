(function() {
var editor, printable, menu, menu_bar, menu_search, menu_list;
var curr;
window.onload = function() {
    editor = document.getElementById("editor");
    printable = document.getElementById("printable");
    menu = document.getElementById("menu");
    menu_bar = document.getElementById("menu-bar");
    menu_search = document.getElementById("menu-search");
    menu_list = document.getElementById("menu-list");
    resize();
    show_memo_list();
    show_last_memo();
    editor.focus();
    document.getElementById("menu-tab").onclick = function() {
        toggle_menu();
    };
    editor.onblur = function(evt) {
        save_memo();
    };
    document.getElementById("menu-add").onclick = function() {
        create_memo();
    };
};

window.onresize = resize;

function toggle_menu() {
    var menu = document.getElementById("menu");
    var clazz = menu.getAttribute("class");
    if (clazz && (' ' + clazz + ' ').indexOf(' open ') >= 0) {
      menu.removeAttribute('class');
    } else {
      menu.setAttribute('class', 'open');
    }
}

function create_memo() {
    var key = (new Date()).getTime();
    editor.value = printable.textContent = "";
    editor.setAttribute("key", key);
    editor.focus();
}

function save_memo() {
    var key = editor.getAttribute("key");
    if (!editor.value) {
        localStorage.removeItem(key);
        show_memo_list();
    } else if (editor.value != localStorage.getItem(key)) {
        var newkey = (new Date()).getTime();
        editor.setAttribute("key", newkey);
        localStorage.removeItem(key);
        localStorage.setItem(newkey, editor.value);
        printable.textContent = editor.value;
        show_memo_list();
    }
}

function open_memo(key) {
    editor.setAttribute("key", key);
    editor.value = printable.textContent = localStorage.getItem(key);
    editor.focus();
    curr = key;
}

function list_memo() {
    var keys = [];
    for (var i = 0; i < localStorage.length; i++) {
        var key = localStorage.key(i);
        if (key.match(/^\d+$/)) {
            keys.push(parseInt(key));
        } 
    }
    keys.sort(function(a,b){ return a < b ? 1: (a == b ? 0 : -1) });
    return keys;
}

function show_last_memo() {
    var keys = list_memo();
    if (keys.length > 0) {
        var key = keys[0];
        editor.value = printable.textContent = localStorage.getItem(key);
        editor.setAttribute("key", key);
        curr = key;
    }
}

function show_memo_list() {
    console.log("show_memo_list");
    var keys = list_memo();
    menu_list.innerHTML = "";
    for (var i = 0; i < keys.length; i++) {
        var item = menu_list.appendChild(document.createElement("div"));
        item.setAttribute("id", "list-" + keys[i]);
        item.setAttribute("class", "item");
        item.setAttribute("key", keys[i]);
        item.innerHTML = localStorage.getItem(keys[i])
            .substring(0, 80)
            .replace(/&/g,'&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;');
        item.onclick = (function(key) {
            return function(evt) {
                var prev = document.getElementById("list-" + curr);
                if (prev) {
                    prev.setAttribute("class", "item");
                }
                var node = evt.target;
                node.setAttribute("class", node.getAttribute("class") + " selected");
                open_memo(key);
            };
        })(keys[i]);
    }
}

function resize() {
    editor.style.minHeight = window.innerHeight + "px";
    //menu_search.style.width = (menu_bar.offsetWidth - 30) + "px";
    menu_list.style.height = (window.innerHeight - 20) + "px";
}
})();

