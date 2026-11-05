/* UaRD native contact dialog: explicit Turnstile lifecycle across PrimeFaces AJAX. */
(function () {
    "use strict";
    if (window.uardContactTurnstile) return;
    var active = null;
    var loading = null;
    var resizeTimer;

    function token(value) {
        var input = document.querySelector('#contactDialog input[id$="turnstileToken"]');
        if (input) input.value = value || "";
    }
    function status(container, message, needsRetry) {
        var output = container.parentNode.querySelector(".uard-contact-verification-status");
        var retry = container.parentNode.querySelector(".uard-contact-verification-retry");
        if (output) {
            output.textContent = message;
            output.classList.toggle("sr-only", !needsRetry);
        }
        if (retry) retry.hidden = !needsRetry;
    }
    function load() {
        if (window.turnstile) return Promise.resolve(window.turnstile);
        if (loading) return loading;
        loading = new Promise(function (resolve, reject) {
            var script = document.createElement("script");
            var timer = window.setTimeout(function () {
                script.remove();
                loading = null;
                reject(new Error("Verification unavailable"));
            }, 15000);
            script.src = "https://challenges.cloudflare.com/turnstile/v0/api.js?render=explicit";
            script.async = true;
            script.onload = function () {
                window.clearTimeout(timer);
                if (window.turnstile) resolve(window.turnstile);
                else { loading = null; reject(new Error("Verification unavailable")); }
            };
            script.onerror = function () {
                window.clearTimeout(timer);
                script.remove();
                loading = null;
                reject(new Error("Verification unavailable"));
            };
            document.head.appendChild(script);
        });
        return loading;
    }
    function close() {
        var previous = active;
        active = null;
        token("");
        if (previous && previous.id !== undefined && window.turnstile) {
            try { window.turnstile.remove(previous.id); } catch (ignore) { /* Detached by JSF. */ }
        }
    }
    function open() {
        close();
        var container = document.querySelector("#contactDialog [data-contact-turnstile]");
        if (!container) return;
        var state = { container: container, size: container.clientWidth < 300 ? "compact" : "flexible" };
        active = state;
        status(container, container.dataset.pending);
        if (!container.dataset.sitekey) {
            status(container, container.dataset.unavailable, true);
            return;
        }
        load().then(function (api) {
            if (active !== state || !container.isConnected) return;
            state.id = api.render(container, {
                sitekey: container.dataset.sitekey,
                language: container.dataset.language,
                action: "contact_form",
                size: state.size,
                theme: "light",
                "response-field": false,
                callback: function (value) {
                    if (active !== state) return;
                    token(value);
                    status(container, container.dataset.verified);
                },
                "expired-callback": function () {
                    if (active !== state) return;
                    token("");
                    status(container, container.dataset.pending);
                    api.reset(state.id);
                },
                "error-callback": function () {
                    if (active !== state) return;
                    token("");
                    status(container, container.dataset.unavailable, true);
                    return true;
                }
            });
        }).catch(function () {
            if (active === state) status(container, container.dataset.unavailable, true);
        });
    }
    // Dataverse 6.12 replaces Dialog.postShow and skips PrimeFaces onShow.
    // Observe only lifecycle-relevant changes; never replace the native resize hook.
    var observedDialog;
    var dialogObserver;
    var scheduled = false;
    function synchronize() {
        scheduled = false;
        var dialog = document.getElementById("contactDialog");
        if (dialog !== observedDialog) {
            if (dialogObserver) dialogObserver.disconnect();
            observedDialog = dialog;
            if (dialog) {
                dialogObserver = new MutationObserver(schedule);
                dialogObserver.observe(dialog, { attributes: true, attributeFilter: ["style", "class"] });
            }
        }
        var container = dialog && dialog.querySelector("[data-contact-turnstile]");
        var visible = dialog && window.getComputedStyle(dialog).display !== "none";
        if (!visible || !container) {
            if (active) close();
        } else if (!active || active.container !== container) {
            open();
        }
    }
    function schedule() {
        if (scheduled) return;
        scheduled = true;
        window.requestAnimationFrame(synchronize);
    }
    new MutationObserver(schedule).observe(document.body, { childList: true, subtree: true });
    schedule();
    window.uardContactTurnstile = {
        open: open,
        close: close,
        retry: open,
        afterSubmit: function (args) {
            if (args && args.validationFailed) open();
            else close();
        }
    };
    window.addEventListener("resize", function () {
        window.clearTimeout(resizeTimer);
        resizeTimer = window.setTimeout(function () {
            if (active && active.container.isConnected
                    && (active.container.clientWidth < 300 ? "compact" : "flexible") !== active.size) open();
        }, 150);
    });
}());
