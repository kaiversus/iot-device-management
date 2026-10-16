const {test} = require('node:test');
const assert = require('node:assert/strict');
const vm = require('node:vm');
const fs = require('node:fs');
const path = require('node:path');
const source = fs.readFileSync(path.join(__dirname, '../../main/resources/static/js/devices.js'), 'utf8');

async function submit(status, body, type = '', redirected = false) {
    let handler, rendered, headers, destination;
    const button = {textContent: 'Save', disabled: false};
    const alert = {hidden: true};
    const form = {action: '/devices', addEventListener: (_, fn) => handler = fn,
        querySelector: selector => selector.startsWith('button') ? button : selector.startsWith('input') ? {value: 'stale'} : alert};
    vm.runInNewContext(source, {
        document: {cookie: 'XSRF-TOKEN=current', querySelector: () => null,
            querySelectorAll: selector => selector.includes('data-upload-form') ? [form] : [],
            open() {}, write(html) {rendered = html;}, close() {}},
        window: {history: {replaceState(_, __, url) {destination = url;}}},
        FormData: class {},
        fetch: async (_, options) => {
            headers = options.headers;
            return {status, redirected, url: 'http://localhost/devices/1',
                headers: {get: () => type}, text: async () => body};
        }
    });
    await handler({preventDefault() {}});
    assert.equal(button.disabled, false);
    assert.equal(button.textContent, 'Save');
    assert.equal(headers['X-XSRF-TOKEN'], 'current');
    return {alert, rendered, destination};
}

for (const [status, message] of [[401, 'đăng nhập'], [403, 'tải lại trang'], [413, '5 MB']]) {
    test(`empty ${status} gives an actionable message`, async () => {
        const {alert, rendered} = await submit(status, '');
        assert.equal(alert.hidden, false);
        assert.ok(alert.textContent.includes(message));
        assert.equal(rendered, undefined);
    });
}
for (const body of ['', '{broken']) {
    test(`invalid JSON response ${JSON.stringify(body)} preserves HTTP error`, async () => {
        const {alert} = await submit(500, body, 'application/json');
        assert.ok(alert.textContent.includes('HTTP 500'));
        assert.ok(!alert.textContent.includes('JSON'));
    });
}
test('JSON validation error is displayed', async () => {
    const {alert} = await submit(409, '{"error":"Serial already exists"}', 'application/json');
    assert.equal(alert.textContent, 'Serial already exists');
});
test('HTML form validation is rendered', async () => {
    const result = await submit(200, '<form>Validation</form>', 'text/html');
    assert.equal(result.rendered, '<form>Validation</form>');
    assert.equal(result.alert.hidden, true);
});
test('successful redirect renders detail and updates URL', async () => {
    const result = await submit(200, '<p>Saved</p>', 'text/html', true);
    assert.equal(result.rendered, '<p>Saved</p>');
    assert.equal(result.destination, 'http://localhost/devices/1');
});
