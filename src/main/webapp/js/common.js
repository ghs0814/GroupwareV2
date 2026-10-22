(() => {
  const script = document.currentScript;
  const token = script.dataset.token;
  window.groupwareCsrfToken = token;
  const originalFetch = window.fetch;
  window.fetch = (input, options = {}) => {
    const url = new URL(typeof input === 'string' ? input : input.url, location.href);
    if (url.origin === location.origin) {
      const headers = new Headers(options.headers || (input instanceof Request ? input.headers : undefined));
      headers.set('X-CSRF-Token', token);
      options = {...options, headers};
    }
    return originalFetch(input, options);
  };
  document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('a[href]').forEach(link => {
      const url = new URL(link.href, location.href);
      if (url.origin === location.origin && /\/(logout|boardDelete|commentDelete|cancelReserve|returnProcess)\.do$/.test(url.pathname)) {
        url.searchParams.set('csrfToken', token);
        link.href = url.toString();
      }
    });
    document.querySelectorAll('form').forEach(form => {
      if (form.method.toLowerCase() !== 'post') return;
      const hidden = document.createElement('input');
      hidden.type = 'hidden'; hidden.name = 'csrfToken'; hidden.value = token;
      form.appendChild(hidden);
    });
    if (window.jQuery) $.ajaxSetup({headers: {'X-CSRF-Token': token}});
  });
})();
