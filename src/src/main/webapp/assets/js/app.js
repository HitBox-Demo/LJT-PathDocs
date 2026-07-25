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

  // Standard file lists (PDF, Word, Excel and general attachments).
  document.querySelectorAll("[data-file-list]").forEach(input => input.addEventListener("change", () => {
    const target = document.getElementById(input.dataset.fileList);
    if (!target) return;
    target.innerHTML = [...input.files]
      .map(file => `<span>${escapeHtml(file.name)} (${formatBytes(file.size)})</span>`)
      .join("");
  }));

  // Multiple image selection with cumulative preview and per-image removal.
  // This also lets mobile users capture one picture, reopen the picker and add another.
  document.querySelectorAll("input[type='file'][data-image-preview]").forEach(input => {
    const preview = document.getElementById(input.dataset.imagePreview);
    if (!preview) return;

    let selectedImages = [];
    let previewUrls = [];

    const fileKey = file => `${file.name}:${file.size}:${file.lastModified}`;

    const syncInputFiles = () => {
      if (typeof DataTransfer === "undefined") return;
      const transfer = new DataTransfer();
      selectedImages.forEach(file => transfer.items.add(file));
      input.files = transfer.files;
    };

    const clearPreviewUrls = () => {
      previewUrls.forEach(url => URL.revokeObjectURL(url));
      previewUrls = [];
    };

    const renderImagePreview = () => {
      clearPreviewUrls();
      preview.innerHTML = "";

      if (!selectedImages.length) {
        preview.classList.remove("has-files");
        return;
      }

      preview.classList.add("has-files");
      selectedImages.forEach((file, index) => {
        const url = URL.createObjectURL(file);
        previewUrls.push(url);

        const item = document.createElement("article");
        item.className = "image-preview-item";
        item.innerHTML = `
          <img src="${url}" alt="Preview ${index + 1}">
          <button class="image-remove-button" type="button" data-remove-image="${index}" aria-label="Remove ${escapeHtml(file.name)}" title="Remove picture">&times;</button>
          <div class="image-preview-copy">
            <strong>Page ${index + 1}</strong>
            <small>${escapeHtml(file.name)} - ${formatBytes(file.size)}</small>
          </div>
        `;
        preview.appendChild(item);
      });
    };

    input.addEventListener("change", () => {
      const existingKeys = new Set(selectedImages.map(fileKey));
      [...input.files]
        .filter(file => file.type.startsWith("image/"))
        .forEach(file => {
          const key = fileKey(file);
          if (!existingKeys.has(key)) {
            selectedImages.push(file);
            existingKeys.add(key);
          }
        });
      syncInputFiles();
      renderImagePreview();
    });

    preview.addEventListener("click", event => {
      const button = event.target.closest("[data-remove-image]");
      if (!button) return;
      const index = Number(button.dataset.removeImage);
      if (!Number.isInteger(index)) return;
      selectedImages.splice(index, 1);
      syncInputFiles();
      renderImagePreview();
    });
  });

  // Existing attachment removal on the correction screen.
  document.querySelectorAll("[data-remove-existing]").forEach(button => {
    button.addEventListener("click", () => {
      const row = button.closest("[data-existing-file]");
      const checkbox = row?.querySelector(".remove-file-checkbox");
      if (!row || !checkbox) return;

      checkbox.checked = !checkbox.checked;
      row.classList.toggle("marked-for-removal", checkbox.checked);
      button.classList.toggle("is-undo", checkbox.checked);
      button.innerHTML = checkbox.checked ? "&#8634;" : "&times;";
      button.title = checkbox.checked ? "Undo removal" : "Remove file";
      button.setAttribute("aria-label", checkbox.checked ? "Undo file removal" : "Remove file");
    });
  });

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

/**
 * Multi-image collector.
 *
 * - Allows several images in one selection.
 * - Keeps previous images when the picker is opened again.
 * - Prevents duplicate files.
 * - Allows individual removal before form submission.
 */
document.querySelectorAll("[data-image-collector]").forEach((collector) => {
    const input = collector.querySelector("[data-image-input]");
    const imageArea = collector.querySelector("[data-image-area]");
    const preview = collector.querySelector("[data-image-preview]");
    const count = collector.querySelector("[data-image-count]");
    const clearButton = collector.querySelector("[data-clear-images]");

    if (!input || !imageArea || !preview || !count) {
        return;
    }

    let selectedFiles = [];

    const fileKey = (file) => {
        return [
            file.name,
            file.size,
            file.lastModified
        ].join("-");
    };

    const formatBytes = (bytes) => {
        if (!bytes) {
            return "0 KB";
        }

        const kilobytes = bytes / 1024;

        if (kilobytes < 1024) {
            return `${kilobytes.toFixed(1)} KB`;
        }

        return `${(kilobytes / 1024).toFixed(1)} MB`;
    };

    const synchronizeInput = () => {
        const transfer = new DataTransfer();

        selectedFiles.forEach((file) => {
            transfer.items.add(file);
        });

        input.files = transfer.files;
    };

    const renderImages = () => {
        preview.replaceChildren();

        count.textContent = String(selectedFiles.length);
        imageArea.hidden = selectedFiles.length === 0;

        selectedFiles.forEach((file, index) => {
            const item = document.createElement("article");
            item.className = "selected-image-item";

            const image = document.createElement("img");
            image.className = "selected-image-thumbnail";
            image.alt = `Selected page ${index + 1}`;

            const objectUrl = URL.createObjectURL(file);
            image.src = objectUrl;

            image.addEventListener(
                "load",
                () => URL.revokeObjectURL(objectUrl),
                { once: true }
            );

            const removeButton = document.createElement("button");
            removeButton.type = "button";
            removeButton.className = "selected-image-remove";
            removeButton.textContent = "X";
            removeButton.setAttribute(
                "aria-label",
                `Remove ${file.name}`
            );

            removeButton.addEventListener("click", () => {
                selectedFiles.splice(index, 1);
                synchronizeInput();
                renderImages();
            });

            const information = document.createElement("div");
            information.className = "selected-image-information";

            const pageNumber = document.createElement("strong");
            pageNumber.textContent = `Page ${index + 1}`;

            const fileName = document.createElement("span");
            fileName.textContent = file.name;
            fileName.title = file.name;

            const fileSize = document.createElement("small");
            fileSize.textContent = formatBytes(file.size);

            information.append(
                pageNumber,
                fileName,
                fileSize
            );

            item.append(
                image,
                removeButton,
                information
            );

            preview.append(item);
        });
    };

    input.addEventListener("change", () => {
        const incomingFiles = Array.from(input.files || []);
        const existingKeys = new Set(
            selectedFiles.map(fileKey)
        );

        incomingFiles.forEach((file) => {
            if (!file.type.startsWith("image/")) {
                return;
            }

            const key = fileKey(file);

            if (!existingKeys.has(key)) {
                selectedFiles.push(file);
                existingKeys.add(key);
            }
        });

        synchronizeInput();
        renderImages();
    });

    clearButton?.addEventListener("click", () => {
        selectedFiles = [];
        synchronizeInput();
        renderImages();
    });

    renderImages();
});

/**
 * Prevents repeated form submission from double-clicking,
 * slow connections or impatient repeated clicks.
 */
document.querySelectorAll("form[data-submit-once]").forEach((form) => {
    let submissionStarted = false;

    form.addEventListener("submit", (event) => {
        if (submissionStarted) {
            event.preventDefault();
            return;
        }

        /*
         * Preserve the clicked submit button's name and value.
         *
         * Disabled buttons are not submitted by the browser,
         * so we copy action=submit or action=draft into a
         * hidden input before disabling the buttons.
         */
        const submitter = event.submitter;

        if (submitter?.name) {
            const existingAction = form.querySelector(
                `input[type="hidden"][data-submitter-value][name="${submitter.name}"]`
            );

            existingAction?.remove();

            const hiddenAction = document.createElement("input");
            hiddenAction.type = "hidden";
            hiddenAction.name = submitter.name;
            hiddenAction.value = submitter.value;
            hiddenAction.dataset.submitterValue = "true";

            form.append(hiddenAction);
        }

        submissionStarted = true;
        form.setAttribute("aria-busy", "true");
        form.classList.add("is-submitting");

        form.querySelectorAll(
            'button[type="submit"], input[type="submit"]'
        ).forEach((button) => {
            button.disabled = true;
            button.setAttribute("aria-disabled", "true");

            if (button === submitter && button.tagName === "BUTTON") {
                button.dataset.originalText = button.textContent;
                button.textContent = "Processing...";
            }
        });
    });
});