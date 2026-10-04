(function () {
    "use strict";
    var footer = document.getElementById("uard-footer");
    if (!footer) return;
    var code = (document.documentElement.lang || "ua").toLowerCase().split(/[-_]/)[0];
    var locale = code === "en" ? "en" : "uk";
    var strings = JSON.parse(document.getElementById("uard-footer-translations").textContent)[locale];
    footer.lang = locale;
    footer.querySelectorAll("[data-footer-text]").forEach(function (node) {
        node.textContent = strings[node.getAttribute("data-footer-text")];
    });
    // The native template emits an orphan NBSP after its scripts. Remove only
    // whitespace-only body text containing that spacer, not visible content.
    function removeTrailingSpacer() {
        Array.from(document.body.childNodes).forEach(function (node) {
            if (node.nodeType === 3 && node.textContent.includes("\u00a0") && !node.textContent.trim()) {
                node.remove();
            }
        });
    }
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", removeTrailingSpacer, {once: true});
    } else { removeTrailingSpacer(); }
    // Reuse the native JSF support action and its existing dialog initialization.
    var support = document.querySelector('#navbarFixed [id$="headerSupportLink"]');
    var button = footer.querySelector(".uard-footer__support");
    if (support) {
        button.hidden = false;
        button.addEventListener("click", function () { support.click(); });
    }
})();
