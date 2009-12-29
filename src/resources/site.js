function recordPopup(e) {
  var value = e[e.selectedIndex].value;
  e.selectedIndex = 0;
  if (value != "@@NA@@") {
    location.href=value;
  }
}
function removeToInsertLater(element) {
    var parentNode = element.parentNode;
    var nextSibling = element.nextSibling;
    parentNode.removeChild(element);
    return function () {
        if (nextSibling)
            parentNode.insertBefore(element, nextSibling);
        else
            parentNode.appendChild(element);
    };
}
function createStats(altClass, footerClass, vals, total, title) {
    var table = document.getElementById("stats");
    var insertFunction = removeToInsertLater(table);
    for (var v in vals) {
        var tr = table.insertRow(table.rows.length);
        
        var th = document.createElement("th");
        if (vals[v].l == null)
            th.appendChild(document.createTextNode(vals[v].k));
        else {
            var a = document.createElement("a");
            a.setAttribute("href", vals[v].l);
            if (title != null)
                a.setAttribute("title", title + vals[v].k);
            a.appendChild(document.createTextNode(vals[v].k));
            th.appendChild(a);
        }
        tr.appendChild(th);
        
        var td = document.createElement("td");
        td.appendChild(document.createTextNode(vals[v].v));
        tr.appendChild(td);
        
        td = document.createElement("td");
        var p = vals[v].v / total * 100.0;
        td.appendChild(document.createTextNode(p.toFixed(2)));
        tr.appendChild(td);
        
        if (v % 2 != 0)
            tr.className = altClass;
    }
    var tr = table.insertRow(table.rows.length);
    
    var th = document.createElement("th");
    th.appendChild(document.createTextNode(vals.length));
    tr.appendChild(th);
    
    var td = document.createElement("th");
    td.appendChild(document.createTextNode(total));
    tr.appendChild(td);
    
    td = document.createElement("th");
    td.appendChild(document.createTextNode(""));
    tr.appendChild(td);
    
    tr.className = footerClass;
    insertFunction();
}
