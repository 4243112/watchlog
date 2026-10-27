const state = { works: [], services: [], histories: [], user: null, authMode: 'login' };
const $ = (selector, root = document) => root.querySelector(selector);
const $$ = (selector, root = document) => [...root.querySelectorAll(selector)];

const labels = {
  MOVIE: '映画', DRAMA: 'ドラマ',
  SUBSCRIPTION: '見放題', RENTAL: 'レンタル', PURCHASE: '購入',
  UNWATCHED: '未視聴', WATCHING: '視聴中', WATCHED: '視聴済み'
};

async function api(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: { ...(options.body ? { 'Content-Type': 'application/json' } : {}), ...options.headers }
  });
  if (!response.ok) {
    let error = { message: '通信に失敗しました' };
    try { error = await response.json(); } catch (_) {}
    throw Object.assign(new Error(error.message), error);
  }
  return response.status === 204 ? null : response.json();
}

function escapeHtml(value = '') {
  return String(value).replace(/[&<>'"]/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[char]));
}

function cleanUrl(value) {
  if (!value) return '';
  try { const url = new URL(value); return ['http:', 'https:'].includes(url.protocol) ? url.href : ''; }
  catch (_) { return ''; }
}

function toast(message, error = false) {
  const el = $('#toast');
  el.textContent = message;
  el.className = `toast show${error ? ' error' : ''}`;
  clearTimeout(toast.timer);
  toast.timer = setTimeout(() => el.className = 'toast', 3200);
}

async function initialize() {
  try {
    const [works, services, user] = await Promise.all([
      api('/api/works'), api('/api/streaming-services'), api('/api/auth/me').catch(() => null)
    ]);
    state.works = works;
    state.services = services;
    state.user = user;
    if (user) await loadHistories();
    renderAll();
  } catch (error) { toast(error.message, true); }
}

async function refreshCatalog() {
  [state.works, state.services] = await Promise.all([api('/api/works'), api('/api/streaming-services')]);
  renderAll();
}

async function loadHistories() {
  state.histories = state.user ? await api(`/api/users/${state.user.id}/viewing-histories`) : [];
}

function renderAll() {
  renderAccount();
  renderFilters();
  renderWorks();
  renderServices();
  renderHistories();
  $('#workCount').textContent = state.works.length;
  $('#serviceCount').textContent = state.services.length;
  $('#watchedCount').textContent = state.user ? state.histories.filter(h => h.status === 'WATCHED').length : '—';
}

function renderAccount() {
  $('#accountArea').innerHTML = state.user ? `
    <div class="user-chip"><span class="user-avatar">${escapeHtml(state.user.name.charAt(0))}</span>
      <span class="user-meta"><strong>${escapeHtml(state.user.name)}</strong><span>ログイン中</span></span></div>
    <button class="button ghost" data-action="logout">ログアウト</button>` : `
    <button class="button ghost" data-action="open-auth">ログイン</button>
    <button class="button primary" data-action="open-register">はじめる</button>`;
}

function renderFilters() {
  const current = $('#yearFilter').value;
  const years = [...new Set(state.works.map(w => w.releaseYear))].sort((a, b) => b - a);
  $('#yearFilter').innerHTML = '<option value="">すべての公開年</option>' + years.map(y => `<option value="${y}">${y}年</option>`).join('');
  $('#yearFilter').value = years.includes(Number(current)) ? current : '';
}

function filteredWorks() {
  const query = $('#searchInput').value.trim().toLowerCase();
  const type = $('#typeFilter').value;
  const year = $('#yearFilter').value;
  return state.works.filter(work => {
    const haystack = [work.title, work.genre, work.director, work.description].filter(Boolean).join(' ').toLowerCase();
    return (!query || haystack.includes(query)) && (!type || work.type === type) && (!year || String(work.releaseYear) === year);
  });
}

function renderWorks() {
  const works = filteredWorks();
  const tones = ['#7057ff', '#e65e44', '#168c83', '#ca8b2f', '#4f71bd', '#93509a'];
  $('#workGrid').innerHTML = works.map((work, index) => {
    const image = cleanUrl(work.imageUrl);
    const initials = [...work.title].slice(0, 2).join('');
    const services = work.streamingServices.slice(0, 3).map(s => `<span class="service-dot" title="${escapeHtml(s.name)}">${escapeHtml(s.name.slice(0, 1))}</span>`).join('');
    return `<article class="work-card" tabindex="0" data-action="work-detail" data-id="${work.id}">
      <div class="poster">
        ${image ? `<img src="${escapeHtml(image)}" alt="${escapeHtml(work.title)}のポスター">` : `<div class="poster-placeholder" style="--tone:${tones[index % tones.length]}">${escapeHtml(initials)}</div>`}
        <span class="type-badge">${labels[work.type]}</span><div class="service-dots">${services}</div>
      </div>
      <div class="card-copy"><h3>${escapeHtml(work.title)}</h3><div class="card-meta"><span>${work.releaseYear}</span><span>•</span><span>${escapeHtml(work.genre)}</span></div><p class="card-description">${escapeHtml(work.description)}</p></div>
    </article>`;
  }).join('');
  $('#workEmpty').classList.toggle('hidden', works.length !== 0);
}

function renderHistories() {
  const gate = $('#historyGate');
  const list = $('#historyList');
  const summary = $('#historySummary');
  if (!state.user) {
    gate.classList.remove('hidden'); list.innerHTML = ''; summary.innerHTML = ''; return;
  }
  gate.classList.add('hidden');
  const count = status => state.histories.filter(h => h.status === status).length;
  summary.innerHTML = `<div><strong>${state.histories.length}</strong><span>すべての記録</span></div><div><strong>${count('WATCHING')}</strong><span>視聴中</span></div><div><strong>${count('WATCHED')}</strong><span>視聴済み</span></div>`;
  list.innerHTML = state.histories.length ? state.histories.map(history => {
    const image = cleanUrl(history.work.imageUrl);
    const stars = history.rating ? '★'.repeat(history.rating) + '☆'.repeat(5 - history.rating) : '評価なし';
    return `<article class="history-row">
      <div class="history-thumb">${image ? `<img src="${escapeHtml(image)}" alt="">` : escapeHtml(history.work.title.charAt(0))}</div>
      <div class="history-main"><h3>${escapeHtml(history.work.title)}</h3><span class="status-pill ${history.status}">${labels[history.status]}</span><p>${escapeHtml(history.comment || 'まだ感想はありません')}</p></div>
      <div class="stars">${stars}</div>
      <div class="row-actions"><button class="icon-button" data-action="edit-history" data-id="${history.id}" aria-label="編集">✎</button><button class="icon-button" data-action="delete-history" data-id="${history.id}" aria-label="削除">×</button></div>
    </article>`;
  }).join('') : '<div class="empty-state"><div>◇</div><h3>最初の視聴記録を残そう</h3><p>作品ライブラリから気になる作品を選んでください。</p></div>';
}

function renderServices() {
  $('#serviceGrid').innerHTML = state.services.length ? state.services.map(service => {
    const logo = cleanUrl(service.logoUrl);
    return `<article class="service-card"><div class="service-logo">${logo ? `<img src="${escapeHtml(logo)}" alt="">` : escapeHtml(service.name.charAt(0))}</div>
      <div class="service-info"><h3>${escapeHtml(service.name)}</h3><a href="${escapeHtml(cleanUrl(service.serviceUrl))}" target="_blank" rel="noopener">${escapeHtml(service.serviceUrl)}</a></div>
      <div class="service-actions"><button class="icon-button" data-action="edit-service" data-id="${service.id}" aria-label="編集">✎</button><button class="icon-button" data-action="delete-service" data-id="${service.id}" aria-label="削除">×</button></div></article>`;
  }).join('') : '<div class="empty-state"><div>▻</div><h3>配信サービスがありません</h3><p>作品の配信先を登録してください。</p></div>';
}

function showView(name) {
  $$('.view').forEach(view => view.classList.toggle('active', view.id === `${name}View`));
  $$('.nav-item').forEach(item => item.classList.toggle('active', item.dataset.view === name));
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function openAuth(mode = 'login') {
  state.authMode = mode;
  const register = mode === 'register';
  $('#authKicker').textContent = register ? 'JOIN WATCHLOG' : 'WELCOME BACK';
  $('#authTitle').textContent = register ? '記録をはじめよう' : 'おかえりなさい';
  $('#authCopy').textContent = register ? 'あなただけの視聴ログを、今日から。' : '視聴記録の続きをはじめましょう。';
  $('#nameField').classList.toggle('hidden', !register);
  $('#nameField input').required = register;
  $('#authForm button').textContent = register ? 'アカウントを作成' : 'ログイン';
  $('#authSwitch').textContent = register ? 'アカウントをお持ちの方' : 'はじめての方はこちら';
  $('#authForm').reset();
  $('#authDialog').showModal();
}

async function submitAuth(event) {
  event.preventDefault();
  const data = Object.fromEntries(new FormData(event.currentTarget));
  try {
    state.user = await api(`/api/auth/${state.authMode === 'register' ? 'register' : 'login'}`, { method: 'POST', body: JSON.stringify(data) });
    await loadHistories();
    $('#authDialog').close();
    renderAll();
    toast(`${state.user.name}さん、ようこそ`);
  } catch (error) { toast(error.message, true); }
}

function workForm(work = null) {
  const edit = Boolean(work);
  $('#formContent').innerHTML = `<button class="dialog-close" data-action="close-dialog" aria-label="閉じる">×</button>
    <div class="dialog-head"><span class="kicker">${edit ? 'EDIT TITLE' : 'NEW TITLE'}</span><h2>${edit ? '作品を編集' : '作品を登録'}</h2><p class="dialog-copy">作品情報はあとからいつでも更新できます。</p></div>
    <form id="workForm" class="stack-form">
      <label>作品名<input name="title" maxlength="200" required value="${escapeHtml(work?.title || '')}"></label>
      <label>あらすじ<textarea name="description" maxlength="4000" required>${escapeHtml(work?.description || '')}</textarea></label>
      <div class="form-row"><label>作品種別<select name="type"><option value="MOVIE" ${work?.type === 'MOVIE' ? 'selected' : ''}>映画</option><option value="DRAMA" ${work?.type === 'DRAMA' ? 'selected' : ''}>ドラマ</option></select></label>
      <label>公開年<input name="releaseYear" type="number" min="1000" max="9999" required value="${work?.releaseYear || new Date().getFullYear()}"></label></div>
      <div class="form-row"><label>ジャンル<input name="genre" maxlength="100" required value="${escapeHtml(work?.genre || '')}"></label><label>監督名（任意）<input name="director" maxlength="150" value="${escapeHtml(work?.director || '')}"></label></div>
      <label>ポスター画像URL（任意）<input name="imageUrl" type="url" value="${escapeHtml(work?.imageUrl || '')}" placeholder="https://example.com/poster.jpg"></label>
      <div class="form-actions"><button type="button" class="button ghost" data-action="close-dialog">キャンセル</button><button class="button primary" type="submit">${edit ? '変更を保存' : '作品を登録'}</button></div>
    </form>`;
  $('#workForm').addEventListener('submit', async event => {
    event.preventDefault(); const data = Object.fromEntries(new FormData(event.currentTarget)); data.releaseYear = Number(data.releaseYear);
    try { await api(edit ? `/api/works/${work.id}` : '/api/works', { method: edit ? 'PUT' : 'POST', body: JSON.stringify(data) }); $('#formDialog').close(); await refreshCatalog(); toast(edit ? '作品を更新しました' : '作品を登録しました'); }
    catch (error) { toast(error.message, true); }
  });
  $('#formDialog').showModal();
}

function historyForm(history = null, selectedWorkId = null) {
  if (!state.user) { openAuth(); return; }
  const edit = Boolean(history);
  const workId = history?.work.id || selectedWorkId || state.works[0]?.id;
  if (!workId) { toast('先に作品を登録してください', true); return; }
  $('#formContent').innerHTML = `<button class="dialog-close" data-action="close-dialog" aria-label="閉じる">×</button>
    <div class="dialog-head"><span class="kicker">MY JOURNAL</span><h2>${edit ? '視聴記録を編集' : '視聴記録を追加'}</h2></div>
    <form id="historyForm" class="stack-form">
      ${edit ? `<label>作品<input disabled value="${escapeHtml(history.work.title)}"></label>` : `<label>作品<select name="workId">${state.works.map(w => `<option value="${w.id}" ${w.id === Number(workId) ? 'selected' : ''}>${escapeHtml(w.title)}</option>`).join('')}</select></label>`}
      <div class="form-row"><label>視聴状態<select name="status"><option value="UNWATCHED" ${history?.status === 'UNWATCHED' ? 'selected' : ''}>未視聴</option><option value="WATCHING" ${history?.status === 'WATCHING' ? 'selected' : ''}>視聴中</option><option value="WATCHED" ${!history || history?.status === 'WATCHED' ? 'selected' : ''}>視聴済み</option></select></label>
      <label>視聴日<input name="watchedAt" type="date" value="${history?.watchedAt || ''}"></label></div>
      <label>評価（1〜5）<select name="rating"><option value="">評価なし</option>${[1,2,3,4,5].map(n => `<option value="${n}" ${history?.rating === n ? 'selected' : ''}>${'★'.repeat(n)}${'☆'.repeat(5-n)}</option>`).join('')}</select></label>
      <label>感想・メモ<textarea name="comment" maxlength="2000" placeholder="心に残った場面や感想を自由に…">${escapeHtml(history?.comment || '')}</textarea></label>
      <div class="form-actions"><button type="button" class="button ghost" data-action="close-dialog">キャンセル</button><button class="button primary" type="submit">${edit ? '変更を保存' : '記録を追加'}</button></div>
    </form>`;
  $('#historyForm').addEventListener('submit', async event => {
    event.preventDefault(); const data = Object.fromEntries(new FormData(event.currentTarget));
    if (!edit) data.workId = Number(data.workId); data.rating = data.rating ? Number(data.rating) : null; data.watchedAt ||= null;
    try { await api(edit ? `/api/users/${state.user.id}/viewing-histories/${history.id}` : `/api/users/${state.user.id}/viewing-histories`, { method: edit ? 'PATCH' : 'POST', body: JSON.stringify(data) }); $('#formDialog').close(); await loadHistories(); renderAll(); toast(edit ? '視聴記録を更新しました' : '視聴記録を追加しました'); }
    catch (error) { toast(error.message, true); }
  });
  $('#formDialog').showModal();
}

function serviceForm(service = null) {
  const edit = Boolean(service);
  $('#formContent').innerHTML = `<button class="dialog-close" data-action="close-dialog" aria-label="閉じる">×</button>
    <div class="dialog-head"><span class="kicker">STREAMING</span><h2>${edit ? 'サービスを編集' : 'サービスを登録'}</h2></div>
    <form id="serviceForm" class="stack-form"><label>サービス名<input name="name" required maxlength="100" value="${escapeHtml(service?.name || '')}"></label><label>サービスURL<input name="serviceUrl" type="url" required value="${escapeHtml(service?.serviceUrl || '')}"></label><label>ロゴ画像URL（任意）<input name="logoUrl" type="url" value="${escapeHtml(service?.logoUrl || '')}"></label><div class="form-actions"><button type="button" class="button ghost" data-action="close-dialog">キャンセル</button><button class="button primary" type="submit">保存</button></div></form>`;
  $('#serviceForm').addEventListener('submit', async event => {
    event.preventDefault(); const data = Object.fromEntries(new FormData(event.currentTarget));
    try { await api(edit ? `/api/streaming-services/${service.id}` : '/api/streaming-services', { method: edit ? 'PUT' : 'POST', body: JSON.stringify(data) }); $('#formDialog').close(); await refreshCatalog(); toast('配信サービスを保存しました'); }
    catch (error) { toast(error.message, true); }
  });
  $('#formDialog').showModal();
}

function showWorkDetail(work) {
  const image = cleanUrl(work.imageUrl);
  const unlinked = state.services.filter(service => !work.streamingServices.some(item => item.streamingServiceId === service.id));
  const optionRows = work.streamingServices.length ? work.streamingServices.map(option => `<div class="availability-row"><div class="service-logo">${escapeHtml(option.name.charAt(0))}</div><div><strong>${escapeHtml(option.name)}</strong><br><span>${labels[option.availabilityType]}</span></div>${option.viewingUrl ? `<a href="${escapeHtml(cleanUrl(option.viewingUrl))}" target="_blank" rel="noopener">視聴する ↗</a>` : ''}<button class="icon-button" data-action="delete-availability" data-work-id="${work.id}" data-id="${option.id}" aria-label="紐づけ解除">×</button></div>`).join('') : '<p class="dialog-copy">配信情報はまだ登録されていません。</p>';
  $('#detailContent').innerHTML = `<button class="dialog-close" data-action="close-dialog" aria-label="閉じる">×</button><div class="detail-layout"><div class="detail-visual">${image ? `<img src="${escapeHtml(image)}" alt="${escapeHtml(work.title)}">` : `<div class="poster-placeholder">${escapeHtml([...work.title].slice(0,2).join(''))}</div>`}</div><div class="detail-body"><span class="kicker">${labels[work.type]}</span><h2>${escapeHtml(work.title)}</h2><div class="detail-meta">${work.releaseYear} ・ ${escapeHtml(work.genre)}${work.director ? ` ・ ${escapeHtml(work.director)}監督` : ''}</div><p class="detail-description">${escapeHtml(work.description)}</p><div class="detail-actions"><button class="button primary" data-action="record-work" data-id="${work.id}">視聴記録に追加</button><button class="button soft" data-action="edit-work" data-id="${work.id}">作品を編集</button><button class="button danger" data-action="delete-work" data-id="${work.id}">削除</button></div><h3 class="subheading">視聴できるサービス</h3><div class="availability-list">${optionRows}</div>${unlinked.length ? `<form id="availabilityForm" class="add-availability"><select name="streamingServiceId">${unlinked.map(service => `<option value="${service.id}">${escapeHtml(service.name)}</option>`).join('')}</select><select name="availabilityType"><option value="SUBSCRIPTION">見放題</option><option value="RENTAL">レンタル</option><option value="PURCHASE">購入</option></select><button class="button ghost" type="submit">＋ 追加</button></form>` : ''}</div></div>`;
  $('#availabilityForm')?.addEventListener('submit', async event => {
    event.preventDefault(); const data = Object.fromEntries(new FormData(event.currentTarget)); data.streamingServiceId = Number(data.streamingServiceId); data.viewingUrl = state.services.find(s => s.id === data.streamingServiceId)?.serviceUrl || null;
    try { await api(`/api/works/${work.id}/streaming-services`, { method: 'POST', body: JSON.stringify(data) }); await refreshCatalog(); showWorkDetail(state.works.find(w => w.id === work.id)); toast('配信先を追加しました'); }
    catch (error) { toast(error.message, true); }
  });
  $('#detailDialog').showModal();
}

async function handleAction(action, target) {
  const id = Number(target.dataset.id);
  if (action === 'open-auth') return openAuth('login');
  if (action === 'open-register') return openAuth('register');
  if (action === 'close-dialog') return target.closest('dialog').close();
  if (action === 'new-work') return workForm();
  if (action === 'new-history') return historyForm();
  if (action === 'new-service') return serviceForm();
  if (action === 'work-detail') return showWorkDetail(state.works.find(w => w.id === id));
  if (action === 'edit-work') { $('#detailDialog').close(); return workForm(state.works.find(w => w.id === id)); }
  if (action === 'record-work') { $('#detailDialog').close(); return historyForm(null, id); }
  if (action === 'edit-history') return historyForm(state.histories.find(h => h.id === id));
  if (action === 'edit-service') return serviceForm(state.services.find(s => s.id === id));
  try {
    if (action === 'logout') { await api('/api/auth/logout', { method: 'POST' }); state.user = null; state.histories = []; renderAll(); toast('ログアウトしました'); }
    if (action === 'delete-work' && confirm('この作品と関連する視聴記録を削除しますか？')) { await api(`/api/works/${id}`, { method: 'DELETE' }); $('#detailDialog').close(); await refreshCatalog(); if (state.user) await loadHistories(); renderAll(); toast('作品を削除しました'); }
    if (action === 'delete-history' && confirm('この視聴記録を削除しますか？')) { await api(`/api/users/${state.user.id}/viewing-histories/${id}`, { method: 'DELETE' }); await loadHistories(); renderAll(); toast('視聴記録を削除しました'); }
    if (action === 'delete-service' && confirm('このサービスと作品の紐づけを削除しますか？')) { await api(`/api/streaming-services/${id}`, { method: 'DELETE' }); await refreshCatalog(); toast('配信サービスを削除しました'); }
    if (action === 'delete-availability' && confirm('この配信情報を解除しますか？')) { const workId = Number(target.dataset.workId); await api(`/api/works/${workId}/streaming-services/${id}`, { method: 'DELETE' }); await refreshCatalog(); showWorkDetail(state.works.find(w => w.id === workId)); toast('配信情報を解除しました'); }
  } catch (error) { toast(error.message, true); }
}

document.addEventListener('click', event => {
  const viewTarget = event.target.closest('[data-view]');
  if (viewTarget) { event.preventDefault(); showView(viewTarget.dataset.view); }
  const actionTarget = event.target.closest('[data-action]');
  if (actionTarget) { event.preventDefault(); handleAction(actionTarget.dataset.action, actionTarget); }
});
document.addEventListener('keydown', event => { if (event.key === 'Enter' && event.target.matches('.work-card')) handleAction('work-detail', event.target); });
$('#searchInput').addEventListener('input', renderWorks);
$('#typeFilter').addEventListener('change', renderWorks);
$('#yearFilter').addEventListener('change', renderWorks);
$('#authForm').addEventListener('submit', submitAuth);
$('#authSwitch').addEventListener('click', () => openAuth(state.authMode === 'login' ? 'register' : 'login'));
$$('dialog').forEach(dialog => dialog.addEventListener('click', event => { if (event.target === dialog) dialog.close(); }));
initialize();
