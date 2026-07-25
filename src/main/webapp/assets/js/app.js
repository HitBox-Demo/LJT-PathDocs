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

  document.querySelectorAll("[data-toggle-sidebar]").forEach((button) => {
    button.addEventListener("click", openSidebar);
  });
  document.querySelectorAll("[data-close-sidebar]").forEach((button) => {
    button.addEventListener("click", closeSidebar);
  });
  backdrop?.addEventListener("click", closeSidebar);

  const currentPath = `${window.location.pathname}${window.location.search}`;
  const navigationLinks = [...document.querySelectorAll(".nav-list a")];
  const exactLink = navigationLinks.find((link) => {
    const target = new URL(link.href, window.location.origin);
    return `${target.pathname}${target.search}` === currentPath;
  });
  const activeLink = exactLink || navigationLinks.find((link) => {
    return new URL(link.href, window.location.origin).pathname === window.location.pathname;
  });

  if (activeLink) {
    activeLink.classList.add("active");
    activeLink.setAttribute("aria-current", "page");
  }

  document.querySelectorAll("[data-demo-user]").forEach((button) => {
    button.addEventListener("click", () => {
      const form = document.querySelector("form[action$='/login']");
      if (!form) return;
      form.querySelector("[name='username']").value = button.dataset.demoUser || "";
      form.querySelector("[name='password']").value = button.dataset.demoPassword || "";
    });
  });

  document.querySelectorAll("[data-file-list]").forEach((input) => {
    input.addEventListener("change", () => {
      const target = document.getElementById(input.dataset.fileList);
      if (!target) return;
      target.replaceChildren();
      [...(input.files || [])].forEach((file) => {
        const item = document.createElement("span");
        item.textContent = `${file.name} (${formatBytes(file.size)})`;
        target.append(item);
      });
    });
  });

  document.querySelectorAll("[data-remove-existing]").forEach((button) => {
    button.addEventListener("click", () => {
      const row = button.closest("[data-existing-file]");
      const checkbox = row?.querySelector(".remove-file-checkbox");
      if (!row || !checkbox) return;

      checkbox.checked = !checkbox.checked;
      row.classList.toggle("marked-for-removal", checkbox.checked);
      button.classList.toggle("is-undo", checkbox.checked);
      button.textContent = checkbox.checked ? "Undo" : "X";
      button.title = checkbox.checked ? "Undo removal" : "Remove file";
      button.setAttribute("aria-label", checkbox.checked ? "Undo file removal" : "Remove file");
    });
  });

  const selectAll = document.querySelector("[data-select-all]");
  selectAll?.addEventListener("change", () => {
    document.querySelectorAll("[data-select-item]").forEach((item) => {
      item.checked = selectAll.checked;
    });
  });

  document.querySelectorAll("form[data-confirm]").forEach((form) => {
    form.addEventListener("submit", (event) => {
      if (!window.confirm(form.dataset.confirm)) event.preventDefault();
    });
  });

  document.querySelectorAll("[data-confirm-click]").forEach((button) => {
    button.addEventListener("click", (event) => {
      if (!window.confirm(button.dataset.confirmClick)) event.preventDefault();
    });
  });

  document.querySelectorAll("[data-require-remarks]").forEach((button) => {
    button.addEventListener("click", (event) => {
      const textarea = button.form?.querySelector("[name='remarks']");
      if (!textarea?.value.trim()) {
        event.preventDefault();
        textarea?.focus();
        window.alert("Enter a reason before returning the document for correction.");
      }
    });
  });

  document.querySelectorAll(".nav-list a").forEach((link) => {
    link.addEventListener("click", (event) => {
      if (event.detail > 0) link.blur();
    });
  });

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
  window.addEventListener("beforeinstallprompt", (event) => {
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

  document.querySelectorAll("[data-image-collector]").forEach(initializeImageCollector);

  function initializeImageCollector(collector) {
    const pickerHost = collector.querySelector("[data-image-picker-host]");
    const inputStore = collector.querySelector("[data-image-input-store]");
    const imageArea = collector.querySelector("[data-image-area]");
    const preview = collector.querySelector("[data-image-preview]");
    const imageCount = collector.querySelector("[data-image-count]");
    const imageSummary = collector.querySelector("[data-image-summary]");
    const clearButton = collector.querySelector("[data-clear-images]");
    let activeInput = collector.querySelector("[data-image-input]");

    if (!pickerHost || !inputStore || !imageArea || !preview || !imageCount || !activeInput) {
      return;
    }

    const pickerId = activeInput.id;
    const pickerLabel = pickerHost.querySelector(`label[for='${pickerId}']`);

    const sourceInputs = () => [...inputStore.querySelectorAll("input[type='file'][name='captureImages']")];

    const selectedImages = () => {
      const result = [];
      sourceInputs().forEach((input) => {
        [...(input.files || [])].forEach((file, fileIndex) => {
          if (file.type.startsWith("image/")) {
            result.push({ input, file, fileIndex });
          }
        });
      });
      return result;
    };

    const createFreshInput = () => {
      const replacement = document.createElement("input");
      replacement.id = pickerId;
      replacement.className = "visually-hidden-file";
      replacement.type = "file";
      replacement.name = "captureImages";
      replacement.accept = "image/*";
      replacement.multiple = true;
      replacement.setAttribute("data-image-input", "");
      replacement.addEventListener("change", handleSelection);
      pickerHost.insertBefore(replacement, pickerLabel || pickerHost.firstChild);
      activeInput = replacement;
    };

    const handleSelection = () => {
      if (!activeInput.files || activeInput.files.length === 0) return;

      const selectedInput = activeInput;
      selectedInput.removeAttribute("id");
      selectedInput.removeAttribute("data-image-input");
      selectedInput.className = "stored-image-input";
      inputStore.append(selectedInput);
      createFreshInput();
      renderImages();
    };

    const removeImage = (sourceInput, removedIndex) => {
      const files = [...(sourceInput.files || [])];
      if (files.length <= 1) {
        sourceInput.remove();
        renderImages();
        return;
      }

      if (typeof DataTransfer === "function") {
        const transfer = new DataTransfer();
        files.forEach((file, index) => {
          if (index !== removedIndex) transfer.items.add(file);
        });
        sourceInput.files = transfer.files;
      } else {
        sourceInput.remove();
      }
      renderImages();
    };

    const renderImages = () => {
      const images = selectedImages();
      preview.replaceChildren();
      imageCount.textContent = String(images.length);
      imageArea.hidden = images.length === 0;
      if (imageSummary) {
        imageSummary.textContent = images.length === 0
          ? "No images selected"
          : `${images.length} image${images.length === 1 ? "" : "s"} selected`;
      }

      images.forEach(({ input, file, fileIndex }, displayIndex) => {
        const item = document.createElement("article");
        item.className = "selected-image-item";

        const thumbnail = document.createElement("img");
        thumbnail.className = "selected-image-thumbnail";
        thumbnail.alt = `Selected page ${displayIndex + 1}`;
        const objectUrl = URL.createObjectURL(file);
        thumbnail.src = objectUrl;
        thumbnail.addEventListener("load", () => URL.revokeObjectURL(objectUrl), { once: true });

        const removeButton = document.createElement("button");
        removeButton.className = "selected-image-remove";
        removeButton.type = "button";
        removeButton.textContent = "X";
        removeButton.setAttribute("aria-label", `Remove ${file.name}`);
        removeButton.addEventListener("click", () => removeImage(input, fileIndex));

        const information = document.createElement("div");
        information.className = "selected-image-information";

        const pageNumber = document.createElement("strong");
        pageNumber.textContent = `Page ${displayIndex + 1}`;
        const filename = document.createElement("span");
        filename.textContent = file.name;
        filename.title = file.name;
        const fileSize = document.createElement("small");
        fileSize.textContent = formatBytes(file.size);

        information.append(pageNumber, filename, fileSize);
        item.append(thumbnail, removeButton, information);
        preview.append(item);
      });
    };

    activeInput.addEventListener("change", handleSelection);
    clearButton?.addEventListener("click", () => {
      sourceInputs().forEach((input) => input.remove());
      renderImages();
    });
    renderImages();
  }

  function formatBytes(bytes) {
    if (!Number.isFinite(bytes) || bytes <= 0) return "0 B";
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1048576) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / 1048576).toFixed(1)} MB`;
  }
})();
