navHover = function() {
    var navEls = document.getElementById("menu").getElementsByTagName("ul")[0].getElementsByTagName("li");
    for (var i=0; i<navEls.length; i++) {
        navEls[i].onmouseover=function() {
            this.className+=" navhover";
        }
        navEls[i].onmouseout=function() {
            this.className=this.className.replace(new RegExp(" navhover\\b"), "");
        }
    }
}
if (window.attachEvent) window.attachEvent("onload", navHover);
