/* Dashboard realtime: Chart.js + SockJS/STOMP. Không dùng framework, chạy với /dashboard. */
(function () {
  const MAX_POINTS = 20;
  const $ = (id) => document.getElementById(id);
  let filterId = null; // null = tất cả thiết bị

  // ---------- Biểu đồ ----------
  const chart = new Chart($('tempChart'), {
    type: 'line',
    data: {
      labels: [],
      datasets: [
        { label: 'Nhiệt độ (°C)', data: [], borderColor: '#dc3545', tension: 0.3 },
        { label: 'Độ ẩm (%)', data: [], borderColor: '#0d6efd', tension: 0.3 }
      ]
    },
    options: { animation: false, scales: { y: { beginAtZero: false } } }
  });

  function timeLabel(iso) { return new Date(iso).toLocaleTimeString(); }

  function addPoint(p) {
    if (filterId !== null && Number(p.deviceId) !== filterId) return;
    chart.data.labels.push(timeLabel(p.time));
    chart.data.datasets[0].data.push(p.temperature);
    chart.data.datasets[1].data.push(p.humidity);
    if (chart.data.labels.length > MAX_POINTS) {
      chart.data.labels.shift();
      chart.data.datasets.forEach((ds) => ds.data.shift());
    }
    chart.update();
  }

  function loadHistory() {
    const url = '/api/iot/data' + (filterId !== null ? '?deviceId=' + filterId : '');
    fetch(url, { credentials: 'same-origin' })
      .then((r) => (r.ok ? r.json() : []))
      .then((list) => {
        chart.data.labels = list.map((d) => timeLabel(d.recordedAt));
        chart.data.datasets[0].data = list.map((d) => d.temperature);
        chart.data.datasets[1].data = list.map((d) => d.humidity);
        chart.update();
      })
      .catch(() => {});
  }

  // ---------- Thống kê + bảng cảnh báo ----------
  function loadStats() {
    fetch('/api/iot/stats', { credentials: 'same-origin' })
      .then((r) => (r.ok ? r.json() : null))
      .then((s) => {
        if (!s) return;
        $('statDevices').textContent = s.reportingDevices;
        $('statRecords').textContent = s.totalRecords;
        $('statAlerts').textContent = s.totalAlerts;
        $('statThreshold').textContent = s.threshold;
      })
      .catch(() => {});
  }

  function renderAlertRow(a) {
    const body = $('alertBody');
    if (body.firstElementChild && body.firstElementChild.firstElementChild.colSpan === 3) body.innerHTML = '';
    const tr = document.createElement('tr');
    [new Date(a.time || a.createdAt).toLocaleString(), a.deviceId, a.message].forEach((t) => {
      const td = document.createElement('td');
      td.textContent = t; // textContent: an toàn XSS
      tr.appendChild(td);
    });
    body.insertBefore(tr, body.firstChild);
    while (body.children.length > 10) body.removeChild(body.lastChild);
  }

  function loadAlerts() {
    fetch('/api/iot/logs', { credentials: 'same-origin' })
      .then((r) => (r.ok ? r.json() : []))
      .then((logs) => logs.slice().reverse().forEach(renderAlertRow))
      .catch(() => {});
  }

  // ---------- Toast ----------
  function showToast(text) {
    const el = document.createElement('div');
    el.className = 'alert alert-danger shadow mb-2';
    el.textContent = text;
    $('toastArea').appendChild(el);
    setTimeout(() => el.remove(), 5000);
  }

  // ---------- WebSocket ----------
  function setStatus(ok) {
    const b = $('wsStatus');
    b.className = 'badge me-3 ' + (ok ? 'bg-success' : 'bg-danger');
    b.textContent = ok ? 'Realtime: đã kết nối' : 'Mất kết nối, đang thử lại...';
  }

  function connect() {
    const stomp = Stomp.over(new SockJS('/ws'));
    stomp.debug = null; // tắt log STOMP trong console
    stomp.connect({}, () => {
      setStatus(true);
      stomp.subscribe('/topic/data', (m) => { addPoint(JSON.parse(m.body)); loadStats(); });
      stomp.subscribe('/topic/alerts', (m) => {
        const a = JSON.parse(m.body);
        showToast(a.message);
        renderAlertRow(a);
      });
    }, () => {
      setStatus(false);
      setTimeout(connect, 3000); // tự kết nối lại
    });
  }

  $('deviceFilter').addEventListener('change', (e) => {
    const v = e.target.value.trim();
    filterId = v === '' ? null : Number(v);
    loadHistory();
  });

  loadHistory();
  loadStats();
  loadAlerts();
  connect();
})();
