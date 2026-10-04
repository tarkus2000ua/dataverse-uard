(function () {
    "use strict";
    var home = document.getElementById("dv-home");
    if (!home) return;
    // The JSF shell owns the locale. Never read browser preferences or localStorage.
    var code = (document.documentElement.lang || "uk").toLowerCase().split(/[-_]/)[0];
    var locale = code === "ua" || code === "uk" ? "uk" : code === "en" ? "en" : "uk";
    var translations = JSON.parse(document.getElementById("dv-home-translations").textContent);
    var strings = translations[locale];
    home.lang = locale;
    home.querySelectorAll("[data-home-ui]").forEach(function (element) {
        element.textContent = strings.ui[element.getAttribute("data-home-ui")];
    });
    home.querySelectorAll("[data-home-content]").forEach(function (element) {
        element.textContent = strings.content[element.getAttribute("data-home-content")];
    });
    document.getElementById("dv-home-query").placeholder = strings.ui.searchPlaceholder;
    // Native GET submission handles encoding, Enter and navigation to Dataverse search.

    var list = document.getElementById("dv-collections-list");
    var status = document.getElementById("dv-collections-status");
    var controls = document.getElementById("dv-collections-controls");
    var previous = document.getElementById("dv-collections-previous");
    var next = document.getElementById("dv-collections-next");
    var retry = document.getElementById("dv-collections-retry");
    var position = document.getElementById("dv-collections-position");
    function format(key, values) {
        return strings.ui[key].replace(/\{(\w+)\}/g, function (_, name) { return values[name]; });
    }
    async function readApi(path) {
        var controller = new AbortController();
        var timeout = setTimeout(function () { controller.abort(); }, 10000);
        try {
            // Public content only: never embed a token or depend on the visitor's privileges.
            var response = await fetch(path, {credentials: "omit", signal: controller.signal});
            if (!response.ok) throw new Error("Collection API request failed");
            var payload = await response.json();
            if (payload.status !== "OK") throw new Error("Invalid collection API response");
            return payload.data;
        } finally { clearTimeout(timeout); }
    }
    function updatePosition() {
        if (list.hidden) return;
        var bounds = list.getBoundingClientRect();
        var visible = Array.from(list.children).map(function (card, index) {
            var rect = card.getBoundingClientRect();
            return rect.right > bounds.left + 2 && rect.left < bounds.right - 2 ? index + 1 : 0;
        }).filter(Boolean);
        previous.disabled = list.scrollLeft <= 2;
        next.disabled = list.scrollLeft + list.clientWidth >= list.scrollWidth - 2;
        position.textContent = format("collectionsPosition", {
            start: visible[0] || 1, end: visible[visible.length - 1] || 1, count: list.children.length
        });
    }
    function scrollCards(strip, direction) {
        if (!strip.children.length) return;
        var padding = parseFloat(getComputedStyle(strip).paddingLeft) || 0;
        var bounds = strip.getBoundingClientRect();
        var starts = Array.from(strip.children).map(function (card) {
            return card.getBoundingClientRect().left - bounds.left - strip.clientLeft + strip.scrollLeft - padding;
        });
        var nearest = starts.reduce(function (best, value, index) {
            return Math.abs(value - strip.scrollLeft) < Math.abs(starts[best] - strip.scrollLeft) ? index : best;
        }, 0);
        var pitch = starts.length > 1 ? starts[1] - starts[0] : strip.clientWidth;
        var step = Math.max(1, Math.floor(strip.clientWidth * 0.8 / pitch));
        var index = Math.max(0, Math.min(starts.length - 1, nearest + direction * step));
        var maximum = Math.max(0, strip.scrollWidth - strip.clientWidth);
        var target = index === 0 ? 0 : Math.min(maximum, starts[index]);
        strip.scrollTo({left: target,
            behavior: window.matchMedia("(prefers-reduced-motion: reduce)").matches ? "auto" : "smooth"});
    }
    function move(direction) { scrollCards(list, direction); }
    previous.addEventListener("click", function () { move(-1); });
    next.addEventListener("click", function () { move(1); });
    list.addEventListener("scroll", updatePosition, {passive: true});
    window.addEventListener("resize", updatePosition);
    // Native Tab navigation also brings each focused card into view.
    list.addEventListener("keydown", function (event) {
        if (event.target !== list) return;
        if (event.key === "ArrowRight" || event.key === "ArrowLeft") {
            event.preventDefault(); move(event.key === "ArrowRight" ? 1 : -1);
        }
    });
    async function loadCollections() {
        retry.hidden = true;
        list.hidden = true;
        controls.hidden = true;
        list.replaceChildren();
        status.classList.remove("dv-home__visually-hidden");
        status.textContent = strings.ui.collectionsLoading;
        list.setAttribute("aria-busy", "true");
        try {
            var aliases = await readApi("/api/dataverses/:root/featured");
            if (!Array.isArray(aliases) || aliases.some(function (alias) { return typeof alias !== "string"; })) {
                throw new Error("Invalid featured list");
            }
            aliases = Array.from(new Set(aliases));
            if (!aliases.length) { status.textContent = strings.ui.collectionsEmpty; return; }
            // Promise.all preserves the curator's order even when requests finish out of order.
            var results = await Promise.all(aliases.map(async function (alias) {
                try {
                    var collection = await readApi("/api/dataverses/" + encodeURIComponent(alias));
                    if (!collection || collection.isReleased !== true || typeof collection.name !== "string") return null;
                    return {alias: alias, name: collection.name};
                } catch (_) { return null; }
            }));
            var collections = results.filter(Boolean);
            if (!collections.length) throw new Error("No featured collections available");
            collections.forEach(function (collection) {
                var item = document.createElement("li");
                var link = document.createElement("a");
                link.href = "/dataverse/" + encodeURIComponent(collection.alias);
                var icon = document.createElement("span");
                icon.className = "dv-home__collection-icon icon-dataverse";
                icon.setAttribute("aria-hidden", "true");
                var name = document.createElement("span");
                name.textContent = collection.name;
                link.append(icon, name);
                item.append(link);
                list.append(item);
            });
            list.hidden = false;
            controls.hidden = false;
            list.scrollLeft = 0;
            var partial = collections.length !== aliases.length;
            status.textContent = partial ? strings.ui.collectionsPartial : format("collectionsLoaded", {count: collections.length});
            status.classList.toggle("dv-home__visually-hidden", !partial);
            retry.hidden = !partial;
            updatePosition();
        } catch (_) {
            status.textContent = strings.ui.collectionsError;
            retry.hidden = false;
        } finally { list.setAttribute("aria-busy", "false"); }
    }
    retry.addEventListener("click", loadCollections);
    loadCollections();

    home.querySelectorAll("[data-knowledge-area]").forEach(function (element) {
        var letter = element.getAttribute("data-knowledge-area");
        element.textContent = letter + " " + strings.knowledgeAreas[letter];
    });
    home.querySelectorAll("[data-cluster]").forEach(function (element) {
        element.textContent = strings.clusters[element.getAttribute("data-cluster")];
    });
    function initializeStaticStrip(prefix) {
        var stripList = document.getElementById(prefix + "-list");
        var stripControls = document.getElementById(prefix + "-controls");
        var stripPrevious = stripControls.querySelector('[data-scroll-direction="previous"]');
        var stripNext = stripControls.querySelector('[data-scroll-direction="next"]');
        stripControls.hidden = false;
        function updateStripPosition() {
            var bounds = stripList.getBoundingClientRect();
            var visible = Array.from(stripList.children).map(function (card, index) {
                var rect = card.getBoundingClientRect();
                return rect.right > bounds.left + 2 && rect.left < bounds.right - 2 ? index + 1 : 0;
            }).filter(Boolean);
            stripPrevious.disabled = stripList.scrollLeft <= 2;
            stripNext.disabled = stripList.scrollLeft + stripList.clientWidth >= stripList.scrollWidth - 2;
            document.getElementById(prefix + "-position").textContent = format("collectionsPosition", {
                start: visible[0] || 1, end: visible[visible.length - 1] || 1, count: stripList.children.length
            });
        }
        function moveStrip(direction) {
            scrollCards(stripList, direction);
        }
        stripPrevious.addEventListener("click", function () { moveStrip(-1); });
        stripNext.addEventListener("click", function () { moveStrip(1); });
        stripList.addEventListener("scroll", updateStripPosition, {passive: true});
        window.addEventListener("resize", updateStripPosition);
        stripList.addEventListener("keydown", function (event) {
            if (event.target === stripList && (event.key === "ArrowRight" || event.key === "ArrowLeft")) {
                event.preventDefault(); moveStrip(event.key === "ArrowRight" ? 1 : -1);
            }
        });
        updateStripPosition();
    }
    initializeStaticStrip("dv-knowledge");
    initializeStaticStrip("dv-clusters");

})();
