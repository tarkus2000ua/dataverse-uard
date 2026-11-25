(function () {
    'use strict';
    var form = document.getElementById('institutionRegistration');
    if (!form) return;
    var button = form.querySelector('button[type="submit"]');
    var feedback = document.getElementById('institution-registration-feedback');
    var success = document.getElementById('institution-registration-success');
    var sending = false;
    form.addEventListener('submit', async function (event) {
        event.preventDefault();
        if (sending || !form.reportValidity()) return;
        sending = true;
        button.disabled = true;
        button.textContent = form.dataset.sending;
        feedback.hidden = true;
        form.querySelectorAll('[aria-invalid]').forEach(function (field) { field.removeAttribute('aria-invalid'); });
        try {
            var response = await fetch(form.action, {
                method: 'POST', credentials: 'same-origin',
                headers: { 'X-Uard-Token': form.elements.token.value, 'Accept': 'application/json' },
                body: new FormData(form)
            });
            var result = await response.json();
            if (!response.ok || !result.ok) {
                feedback.textContent = result.message || form.dataset.error;
                feedback.hidden = false;
                var field = result.field && form.elements.namedItem(result.field);
                if (field) { field.setAttribute('aria-invalid', 'true'); field.focus(); }
                else feedback.focus();
                return;
            }
            document.getElementById('institution-registration-reference').textContent = result.reference;
            form.hidden = true;
            success.hidden = false;
            success.focus();
        } catch (error) {
            feedback.textContent = form.dataset.error;
            feedback.hidden = false;
            feedback.focus();
        } finally {
            sending = false;
            button.disabled = false;
            button.textContent = form.dataset.submit;
        }
    });
}());
