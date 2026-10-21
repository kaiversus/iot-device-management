(() => {
document.querySelectorAll('form[data-confirm]').forEach(form => {
    form.addEventListener('submit', event => {
        if (!window.confirm(form.dataset.confirm)) event.preventDefault();
    });
});
const imageInput = document.querySelector('#image');
if (imageInput) {
    const preview = document.querySelector('#imagePreview');
    let previewUrl;
    imageInput.addEventListener('change', () => {
        if (previewUrl) URL.revokeObjectURL(previewUrl);
        imageInput.setCustomValidity('');
        const file = imageInput.files[0];
        preview.hidden = true;
        if (!file) return;
        if (file.size > 5 * 1024 * 1024) {
            imageInput.setCustomValidity('Ảnh không được vượt quá 5 MB.');
            imageInput.reportValidity();
            return;
        }
        previewUrl = URL.createObjectURL(file);
        preview.src = previewUrl;
        preview.hidden = false;
    });
}
// Check CSRF before the multipart body is parsed by MVC.
document.querySelectorAll('form[data-upload-form]').forEach(form => {
    form.addEventListener('submit', async event => {
        event.preventDefault();
        const button = form.querySelector('button[type="submit"]');
        const originalLabel = button.textContent;
        const alert = form.querySelector('[data-upload-error]');
        alert.hidden = true;
        button.disabled = true; button.textContent = 'Đang lưu…';
        const headers = {};
        const token = form.querySelector('input[name="_csrf"]');
        const csrfCookie = document.cookie.split('; ').find(cookie => cookie.startsWith('XSRF-TOKEN='));
        const csrfToken = csrfCookie ? decodeURIComponent(csrfCookie.slice('XSRF-TOKEN='.length)) : token?.value;
        if (csrfToken) headers['X-XSRF-TOKEN'] = csrfToken;
        try {
            const response = await fetch(form.action, {method: 'POST', body: new FormData(form), headers, credentials: 'same-origin'});
            if (response.status === 401) {
                throw new Error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại rồi lưu thiết bị.');
            }
            if (response.status === 403) {
                throw new Error('Phiên làm việc đã thay đổi hoặc bạn không có quyền lưu thiết bị. Vui lòng tải lại trang.');
            }
            if (response.status === 413) {
                throw new Error('Ảnh vượt quá giới hạn 5 MB. Vui lòng chọn ảnh nhỏ hơn.');
            }
            const body = await response.text();
            if ((response.headers.get('content-type') || '').includes('text/html')) {
                if (!body.trim()) throw new Error('Máy chủ trả về trang rỗng. Vui lòng kiểm tra danh sách thiết bị trước khi thử lại.');
                // The redirect GET already consumed the flash message; display that response once.
                if (response.redirected) window.history.replaceState(null, '', response.url);
                document.open(); document.write(body); document.close(); return;
            }
            let message = 'Không thể lưu thiết bị (HTTP ' + response.status + '). Vui lòng kiểm tra danh sách thiết bị trước khi thử lại.';
            if (body.trim() && (response.headers.get('content-type') || '').includes('application/json')) {
                try {
                    const data = JSON.parse(body);
                    if (typeof data?.error === 'string') message = data.error;
                } catch { /* Keep the useful fallback for empty or malformed server responses. */ }
            }
            throw new Error(message);
        } catch (error) {
            alert.textContent = error.message || 'Không thể kết nối máy chủ.'; alert.hidden = false;
        } finally { button.disabled = false; button.textContent = originalLabel; }
    });
});
})();
