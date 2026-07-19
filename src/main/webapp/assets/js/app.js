(() => {
  "use strict";

  const body = document.body;
  const contextPath = body?.dataset.context || "";
  const sidebar = document.querySelector("[data-sidebar]");
  const backdrop = document.querySelector("[data-sidebar-backdrop]");

  const openSidebar = () => {
    sidebar?.classList.add("open");
    if (backdrop) backdrop.hidden = false;
  };

  const closeSidebar = () => {
    sidebar?.classList.remove("open");
    if (backdrop) backdrop.hidden = true;
  };

  document.querySelectorAll("[data-toggle-sidebar]").forEach(button => button.addEventListener("click", openSidebar));
  document.querySelectorAll("[data-close-sidebar]").forEach(button => button.addEventListener("click", closeSidebar));
  backdrop?.addEventListener("click", closeSidebar);

  // Highlight the exact route when possible; fall back to the matching path.
  const currentPath = `${window.location.pathname}${window.location.search}`;
  const navigationLinks = [...document.querySelectorAll(".nav-list a")];
  const exactLink = navigationLinks.find(link => {
    const target = new URL(link.href, window.location.origin);
    return `${target.pathname}${target.search}` === currentPath;
  });
  const activeLink = exactLink || navigationLinks.find(link => new URL(link.href, window.location.origin).pathname === window.location.pathname);
  if (activeLink) {
    activeLink.classList.add("active");
    activeLink.setAttribute("aria-current", "page");
  }

  document.querySelectorAll("[data-demo-user]").forEach(button => button.addEventListener("click", () => {
    const form = document.querySelector("form[action$='/login']");
    if (!form) return;
    form.querySelector("[name='username']").value = button.dataset.demoUser;
    form.querySelector("[name='password']").value = button.dataset.demoPassword;
  }));

  document.querySelectorAll("[data-file-list]").forEach(input => input.addEventListener("change", () => {
    const target = document.getElementById(input.dataset.fileList);
    if (!target) return;
    target.innerHTML = [...input.files]
      .map(file => `<span>• ${escapeHtml(file.name)} (${formatBytes(file.size)})</span>`)
      .join("");
  }));

  const selectAll = document.querySelector("[data-select-all]");
  selectAll?.addEventListener("change", () => {
    document.querySelectorAll("[data-select-item]").forEach(item => {
      item.checked = selectAll.checked;
    });
  });

  document.querySelectorAll("form[data-confirm]").forEach(form => form.addEventListener("submit", event => {
    if (!window.confirm(form.dataset.confirm)) event.preventDefault();
  }));

  document.querySelectorAll("[data-confirm-click]").forEach(button => button.addEventListener("click", event => {
    if (!window.confirm(button.dataset.confirmClick)) event.preventDefault();
  }));

  document.querySelectorAll("[data-require-remarks]").forEach(button => button.addEventListener("click", event => {
    const textarea = button.form?.querySelector("[name='remarks']");
    if (!textarea?.value.trim()) {
      event.preventDefault();
      textarea?.focus();
      window.alert("Enter a reason before returning the document for correction.");
    }
  }));

  const indicator = document.querySelector("[data-online-indicator]");
  const updateOnlineIndicator = () => {
    if (!indicator) return;
    const isOnline = navigator.onLine;
    indicator.textContent = isOnline ? "Online" : "Offline";
    indicator.classList.toggle("is-offline", !isOnline);
  };
  window.addEventListener("online", updateOnlineIndicator);
  window.addEventListener("offline", updateOnlineIndicator);
  updateOnlineIndicator();

  let installPrompt;
  const installButton = document.querySelector("[data-install-app]");
  window.addEventListener("beforeinstallprompt", event => {
    event.preventDefault();
    installPrompt = event;
    if (installButton) installButton.hidden = false;
  });
  installButton?.addEventListener("click", async () => {
    if (!installPrompt) return;
    await installPrompt.prompt();
    await installPrompt.userChoice;
    installPrompt = null;
    installButton.hidden = true;
  });

  if ("serviceWorker" in navigator) {
    window.addEventListener("load", () => {
      navigator.serviceWorker.register(`${contextPath}/service-worker.js`).catch(console.warn);
    });
  }

  function formatBytes(bytes) {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1048576) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / 1048576).toFixed(1)} MB`;
  }

  function escapeHtml(value) {
    const element = document.createElement("div");
    element.textContent = value;
    return element.innerHTML;
  }
})();
