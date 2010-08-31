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
function createStats(altClass, footerClass, items) {
    var table = document.getElementById("stats");
    var insertFunction = removeToInsertLater(table);
    var vals = items.vals;
    for (var v in vals) {
        var tr = table.insertRow(table.rows.length);
        
        var th = document.createElement("th");
        if (vals[v].f == null)
            th.appendChild(document.createTextNode(vals[v].k));
        else {
            var a = document.createElement("a");
            a.setAttribute("href", items.directory + vals[v].f + ".html#" + vals[v].i);
            if (items.prefix != null)
                a.setAttribute("title", items.prefix + vals[v].k);
            a.appendChild(document.createTextNode(vals[v].k));
            th.appendChild(a);
        }
        tr.appendChild(th);
        
        var td = document.createElement("td");
        td.appendChild(document.createTextNode(vals[v].v));
        tr.appendChild(td);
        
        td = document.createElement("td");
        var p = vals[v].v / items.total * 100.0;
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
    td.appendChild(document.createTextNode(items.total));
    tr.appendChild(td);
    
    td = document.createElement("th");
    td.appendChild(document.createTextNode(""));
    tr.appendChild(td);
    
    tr.className = footerClass;
    insertFunction();
}
function createShowStats(altClass, footerClass, items) {
    var table = document.getElementById("stats");
    var insertFunction = removeToInsertLater(table);
    
    var rows = items.shows;
    for (var r in rows) {
        var row = rows[r];
        
        var tr = table.insertRow(table.rows.length);
        
        var td = document.createElement("td");
        var a = document.createElement("a");
        a.setAttribute("href", items.directory + row.y + ".html");
        if (items.title != null)
            a.setAttribute("title", "'" + row.y + "' " + items.title);
        a.appendChild(document.createTextNode(row.y));
        td.appendChild(a);
        tr.appendChild(td);

        for (var t in row.s) {
            var c = row.s[t];
            var td = document.createElement("td");
            if (c == 0 || t > 12)
                td.appendChild(document.createTextNode(c));
            else {
                var a = document.createElement("a");
                a.setAttribute("href", items.directory + row.y + ".html#" + items.months[t].ms);
                if (items.prefix != null)
                    a.setAttribute("title", items.prefix + " " + items.months[t].m + ", " + row.y);
                a.appendChild(document.createTextNode(c));
                td.appendChild(a);
            }
            tr.appendChild(td);
        }
        
        if (r % 2 != 0)
            tr.className = altClass;
    }
    
    var tr = table.insertRow(table.rows.length);
    for (var t in items.totals) {
        var total = items.totals[t];

        var td = document.createElement("th");
        td.appendChild(document.createTextNode(total));
        tr.appendChild(td);
    }
    tr.className = footerClass;
    
    insertFunction();
}
