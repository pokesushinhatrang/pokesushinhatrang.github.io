// SUSHI GO staff: shows a notification for each push. The push carries no text,
// so the service worker reads the newest open orders from the server with the staff code.
self.addEventListener('install', () => self.skipWaiting());
self.addEventListener('activate', e => e.waitUntil(self.clients.claim()));

async function config() {
  try { const r = await (await caches.open('sg-config')).match('/sg-config'); return r ? r.json() : null } catch (e) { return null }
}

self.addEventListener('push', e => e.waitUntil((async () => {
  let title = 'SUSHI GO · Có đơn mới', body = 'Mở màn hình đơn hàng', tag = 'sg';
  const cfg = await config();
  if (!cfg || !cfg.code) {   // logged out on this phone: stop getting pushes
    try { const sub = await self.registration.pushManager.getSubscription(); if (sub) await sub.unsubscribe() } catch (err) {}
    return;
  }
  if (cfg && cfg.code) {
    try {
      const r = await fetch(`${cfg.api}/staff/latest?venue=${cfg.venue}`, { headers: { 'X-Staff-Key': cfg.code }, cache: 'no-store' });
      const d = await r.json(), o = d.orders && d.orders[0];
      if (o) {
        const first = o.text.split('\n')[0];
        title = (o.kind === 'bill' ? '💳 ' : o.kind === 'waiter' ? '🔔 ' : '🧾 ') + first;
        body = o.text.split('\n').slice(1, 6).join('\n') || 'Mở màn hình đơn hàng';
        tag = 'sg-' + o.id;
      }
    } catch (err) {}
  }
  await self.registration.showNotification(title, { body, tag, renotify: true, requireInteraction: true, icon: 'icon-192.png', badge: 'icon-192.png', vibrate: [300, 100, 300, 100, 300] });
})()));

self.addEventListener('notificationclick', e => {
  e.notification.close();
  e.waitUntil((async () => {
    const all = await self.clients.matchAll({ type: 'window', includeUncontrolled: true });
    const w = all.find(c => c.url.includes('/staff/'));
    if (w) return w.focus();
    return self.clients.openWindow('./');
  })());
});
