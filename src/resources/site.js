function recordPopup(e) {
  var value = e[e.selectedIndex].value;
  e.selectedIndex = 0;
  if (value != "@@NA@@") {
    location.href=value;
  }
}
