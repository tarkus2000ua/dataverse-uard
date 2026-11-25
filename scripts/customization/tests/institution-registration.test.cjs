// Client behavior tests run in Node without submitting a real form or contacting SMTP.
const { test } = require('node:test');
const assert = require('node:assert/strict');
const { readFileSync } = require('node:fs');
const { resolve } = require('node:path');
const vm = require('node:vm');
const source = readFileSync(resolve(__dirname, '../../../src/main/webapp/resources/js/uard/institution-registration.js'), 'utf8');

function setup(fetch) {
    const button = { disabled: false, textContent: 'Submit' };
    const feedback = { hidden: true, focus() { this.focused = true; } };
    const success = { hidden: true, focus() { this.focused = true; } };
    const reference = {};
    const email = { value: 'kept@example.invalid', focus() { this.focused = true; }, setAttribute() {}, removeAttribute() {} };
    const form = {
        hidden: false, action: '/institution-registration-submit', valid: true,
        dataset: { submit: 'Submit', sending: 'Sending', error: 'Retry safely' },
        elements: { token: { value: 'same-token' }, namedItem: key => key === 'email' ? email : null },
        querySelector: () => button, querySelectorAll: () => [],
        reportValidity() { return this.valid; },
        addEventListener(type, handler) { this.submit = () => handler({ preventDefault() {} }); }
    };
    const elements = { institutionRegistration: form, 'institution-registration-feedback': feedback,
        'institution-registration-success': success, 'institution-registration-reference': reference };
    vm.runInNewContext(source, { document: { getElementById: key => elements[key] }, fetch, FormData: class {} });
    return { form, button, feedback, success, reference, email };
}

test('success shows the saved reference and removes the form from interaction', async () => {
    const state = setup(async () => ({ ok: true, json: async () => ({ ok: true, reference: 'saved-id' }) }));
    await state.form.submit();
    assert.equal(state.form.hidden, true);
    assert.equal(state.success.hidden, false);
    assert.equal(state.success.focused, true);
    assert.equal(state.reference.textContent, 'saved-id');
});
test('server validation preserves entered data and focuses the invalid field', async () => {
    const state = setup(async () => ({ ok: false, json: async () => ({ ok: false, field: 'email', message: 'Correct email' }) }));
    await state.form.submit();
    assert.equal(state.form.hidden, false);
    assert.equal(state.email.value, 'kept@example.invalid');
    assert.equal(state.email.focused, true);
    assert.equal(state.feedback.textContent, 'Correct email');
    assert.equal(state.button.disabled, false);
});
test('a lost response permits retry with the same idempotency token', async () => {
    const tokens = [];
    const state = setup(async (url, options) => {
        tokens.push(options.headers['X-Uard-Token']);
        if (tokens.length === 1) throw new Error('connection lost');
        return { ok: true, json: async () => ({ ok: true, reference: 'saved-id' }) };
    });
    await state.form.submit();
    assert.equal(state.form.hidden, false);
    assert.equal(state.feedback.textContent, 'Retry safely');
    await state.form.submit();
    assert.deepEqual(tokens, ['same-token', 'same-token']);
    assert.equal(state.success.hidden, false);
});
test('double clicks cannot send simultaneous requests', async () => {
    let release, calls = 0;
    const state = setup(() => { calls++; return new Promise(resolve => { release = resolve; }); });
    const pending = state.form.submit();
    await state.form.submit();
    assert.equal(calls, 1);
    assert.equal(state.button.disabled, true);
    release({ ok: true, json: async () => ({ ok: true, reference: 'saved-id' }) });
    await pending;
});
test('native invalid controls prevent a request', async () => {
    let calls = 0;
    const state = setup(async () => { calls++; });
    state.form.valid = false;
    await state.form.submit();
    assert.equal(calls, 0);
    assert.equal(state.button.disabled, false);
});
