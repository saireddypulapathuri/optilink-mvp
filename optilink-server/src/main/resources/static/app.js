const state = {
  transfers: [],
  selected: null,
  scan: {
    session: null,
    stream: null,
    active: false,
    busy: false,
    timer: null
  }
};

const els = {
  health: document.getElementById("health"),
  pageSubtitle: document.getElementById("pageSubtitle"),
  consoleLink: document.getElementById("consoleLink"),
  sendLink: document.getElementById("sendLink"),
  receiveLink: document.getElementById("receiveLink"),
  textMode: document.getElementById("textMode"),
  imageMode: document.getElementById("imageMode"),
  videoMode: document.getElementById("videoMode"),
  fileMode: document.getElementById("fileMode"),
  textForm: document.getElementById("textForm"),
  imageForm: document.getElementById("imageForm"),
  videoForm: document.getElementById("videoForm"),
  fileForm: document.getElementById("fileForm"),
  textPayload: document.getElementById("textPayload"),
  textPassphrase: document.getElementById("textPassphrase"),
  imagePayload: document.getElementById("imagePayload"),
  imagePassphrase: document.getElementById("imagePassphrase"),
  videoPayload: document.getElementById("videoPayload"),
  videoPassphrase: document.getElementById("videoPassphrase"),
  filePayload: document.getElementById("filePayload"),
  filePassphrase: document.getElementById("filePassphrase"),
  createMessage: document.getElementById("createMessage"),
  scanPassphrase: document.getElementById("scanPassphrase"),
  startScan: document.getElementById("startScan"),
  stopScan: document.getElementById("stopScan"),
  scanState: document.getElementById("scanState"),
  cameraPreview: document.getElementById("cameraPreview"),
  scanCanvas: document.getElementById("scanCanvas"),
  cameraEmpty: document.getElementById("cameraEmpty"),
  scanFrames: document.getElementById("scanFrames"),
  scanInvalid: document.getElementById("scanInvalid"),
  scanMissing: document.getElementById("scanMissing"),
  scanRecoveredPreview: document.getElementById("scanRecoveredPreview"),
  scanDownload: document.getElementById("scanDownload"),
  scanOutput: document.getElementById("scanOutput"),
  refreshTransfers: document.getElementById("refreshTransfers"),
  transferList: document.getElementById("transferList"),
  emptyDetail: document.getElementById("emptyDetail"),
  detailBody: document.getElementById("detailBody"),
  detailId: document.getElementById("detailId"),
  detailFrames: document.getElementById("detailFrames"),
  detailBytes: document.getElementById("detailBytes"),
  detailStatus: document.getElementById("detailStatus"),
  manifestLink: document.getElementById("manifestLink"),
  openDisplay: document.getElementById("openDisplay"),
  inspectTransfer: document.getElementById("inspectTransfer"),
  displayPreview: document.getElementById("displayPreview"),
  receivePassphrase: document.getElementById("receivePassphrase"),
  receiveTransfer: document.getElementById("receiveTransfer"),
  mediaPreview: document.getElementById("mediaPreview"),
  downloadRecovered: document.getElementById("downloadRecovered"),
  receiveOutput: document.getElementById("receiveOutput")
};

const modes = ["text", "image", "video", "file"];

function applyRouteMode() {
  const path = window.location.pathname.replace(/\/$/, "") || "/";
  const send = path === "/send";
  const receive = path === "/receive";
  document.body.classList.toggle("send-page", send);
  document.body.classList.toggle("receive-page", receive);
  document.body.classList.toggle("console-page", !send && !receive);
  els.consoleLink.classList.toggle("active", !send && !receive);
  els.sendLink.classList.toggle("active", send);
  els.receiveLink.classList.toggle("active", receive);
  if (send) {
    document.title = "OptiLink Transmit";
    els.pageSubtitle.textContent = "Transmitter: create and beam encrypted QR frames";
    return;
  }
  if (receive) {
    document.title = "OptiLink Receive";
    els.pageSubtitle.textContent = "Receiver: scan the QR beam and recover the payload";
    return;
  }
  document.title = "OptiLink";
  els.pageSubtitle.textContent = "Local optical transfer console";
}

async function request(path, options = {}) {
  const response = await fetch(path, {
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {})
    },
    ...options
  });
  const text = await response.text();
  const body = text ? JSON.parse(text) : null;
  if (!response.ok) {
    throw new Error(body?.message || `Request failed: ${response.status}`);
  }
  return body;
}

async function checkHealth() {
  try {
    const health = await request("/health");
    els.health.textContent = health.status;
    els.health.className = "health ok";
  } catch (error) {
    els.health.textContent = "Offline";
    els.health.className = "health bad";
  }
}

function setMode(mode) {
  for (const item of modes) {
    els[`${item}Form`].classList.toggle("hidden", item !== mode);
    els[`${item}Mode`].classList.toggle("active", item === mode);
  }
  els.createMessage.textContent = "";
}

async function loadTransfers() {
  state.transfers = await request("/api/transfers");
  renderTransfers();
  if (!state.selected && state.transfers.length > 0) {
    selectTransfer(state.transfers[0].transferId);
  }
}

function renderTransfers() {
  els.transferList.innerHTML = "";
  if (state.transfers.length === 0) {
    const empty = document.createElement("div");
    empty.className = "empty";
    empty.textContent = "No transfers yet.";
    els.transferList.append(empty);
    return;
  }
  for (const transfer of state.transfers) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = "transfer-item";
    if (state.selected?.transferId === transfer.transferId) {
      button.classList.add("active");
    }
    button.innerHTML = `
      <span class="transfer-title"></span>
      <span class="transfer-meta"></span>
    `;
    button.querySelector(".transfer-title").textContent = transfer.name;
    button.querySelector(".transfer-meta").textContent =
      `${transfer.kind} · ${transfer.frameCount} frame${transfer.frameCount === 1 ? "" : "s"} · ${transfer.plaintextByteCount} bytes`;
    button.addEventListener("click", () => selectTransfer(transfer.transferId));
    els.transferList.append(button);
  }
}

function selectTransfer(transferId) {
  const transfer = state.transfers.find(item => item.transferId === transferId);
  if (!transfer) {
    return;
  }
  state.selected = transfer;
  renderTransfers();
  els.emptyDetail.classList.add("hidden");
  els.detailBody.classList.remove("hidden");
  els.manifestLink.classList.remove("hidden");
  els.detailId.textContent = transfer.transferId;
  els.detailFrames.textContent = transfer.frameCount;
  els.detailBytes.textContent = transfer.plaintextByteCount;
  els.detailStatus.textContent = "Unchecked";
  els.detailStatus.className = "";
  els.manifestLink.href = transfer.manifestUrl;
  els.displayPreview.src = transfer.displayUrl;
  clearRecovered();
}

async function createText(event) {
  event.preventDefault();
  els.createMessage.textContent = "Creating transfer...";
  try {
    const transfer = await request("/api/transfers/text", {
      method: "POST",
      body: JSON.stringify({
        text: els.textPayload.value,
        passphrase: els.textPassphrase.value
      })
    });
    els.textPassphrase.value = "";
    await loadTransfers();
    selectTransfer(transfer.transferId);
    await inspectSelected();
    els.createMessage.textContent = "Transfer ready.";
  } catch (error) {
    els.createMessage.textContent = error.message;
  }
}

async function createBinary(event, mode) {
  event.preventDefault();
  const fileInput = els[`${mode}Payload`];
  const passphraseInput = els[`${mode}Passphrase`];
  const file = fileInput.files[0];
  if (!file) {
    const label = mode === "image" ? "an image" : mode === "video" ? "a video" : "a file";
    els.createMessage.textContent = `Choose ${label}.`;
    return;
  }
  els.createMessage.textContent = "Creating transfer...";
  try {
    const contentBase64 = await fileToBase64(file);
    const transfer = await request(`/api/transfers/${mode}`, {
      method: "POST",
      body: JSON.stringify({
        fileName: file.name,
        contentType: contentTypeFor(file, mode),
        contentBase64,
        passphrase: passphraseInput.value
      })
    });
    passphraseInput.value = "";
    await loadTransfers();
    selectTransfer(transfer.transferId);
    await inspectSelected();
    els.createMessage.textContent = "Transfer ready.";
  } catch (error) {
    els.createMessage.textContent = error.message;
  }
}

function contentTypeFor(file, mode) {
  if (file.type) {
    return file.type;
  }
  const lower = file.name.toLowerCase();
  const extensionTypes = [
    [".png", "image/png"],
    [".jpg", "image/jpeg"],
    [".jpeg", "image/jpeg"],
    [".gif", "image/gif"],
    [".webp", "image/webp"],
    [".svg", "image/svg+xml"],
    [".mp4", "video/mp4"],
    [".m4v", "video/mp4"],
    [".webm", "video/webm"],
    [".mov", "video/quicktime"],
    [".ogv", "video/ogg"]
  ];
  const match = extensionTypes.find(([extension]) => lower.endsWith(extension));
  if (match) {
    return match[1];
  }
  if (mode === "image") {
    return "image/png";
  }
  if (mode === "video") {
    return "video/mp4";
  }
  return "application/octet-stream";
}

async function fileToBase64(file) {
  const buffer = await file.arrayBuffer();
  const bytes = new Uint8Array(buffer);
  let binary = "";
  const chunkSize = 8192;
  for (let index = 0; index < bytes.length; index += chunkSize) {
    binary += String.fromCharCode(...bytes.subarray(index, index + chunkSize));
  }
  return btoa(binary);
}

async function inspectSelected() {
  if (!state.selected) {
    return;
  }
  const inspection = await request(state.selected.inspectUrl);
  if (inspection.complete) {
    els.detailStatus.textContent = "Complete";
    els.detailStatus.className = "status-good";
    return;
  }
  if (inspection.invalidFrames > 0) {
    els.detailStatus.textContent = `${inspection.invalidFrames} invalid`;
    els.detailStatus.className = "status-bad";
    return;
  }
  els.detailStatus.textContent = `Missing ${inspection.missingFrameNumbers.join(", ")}`;
  els.detailStatus.className = "status-warn";
}

async function receiveSelected() {
  if (!state.selected) {
    return;
  }
  clearRecovered();
  els.receiveOutput.textContent = "Receiving...";
  try {
    const response = await request(state.selected.receiveUrl, {
      method: "POST",
      body: JSON.stringify({passphrase: els.receivePassphrase.value})
    });
    els.receivePassphrase.value = "";
    renderRecoveredPayload(response);
  } catch (error) {
    els.receiveOutput.textContent = error.message;
  }
}

function clearRecovered() {
  els.mediaPreview.replaceChildren();
  els.mediaPreview.classList.add("hidden");
  els.downloadRecovered.classList.add("hidden");
  els.downloadRecovered.removeAttribute("href");
  els.downloadRecovered.removeAttribute("download");
  els.receiveOutput.textContent = "";
}

function renderRecoveredPayload(response) {
  renderPayload(response, els.mediaPreview, els.downloadRecovered, els.receiveOutput);
}

function renderPayload(response, previewElement, downloadElement, outputElement) {
  const contentType = response.contentType || "application/octet-stream";
  const dataUrl = response.dataUrl || `data:${contentType};base64,${response.base64}`;
  const name = response.name || state.selected?.name || "optilink-payload";
  const isImage = contentType.startsWith("image/");
  const isVideo = contentType.startsWith("video/");

  previewElement.replaceChildren();
  previewElement.classList.add("hidden");
  downloadElement.href = dataUrl;
  downloadElement.download = name;
  downloadElement.classList.remove("hidden");

  if (isImage || isVideo) {
    const media = document.createElement(isImage ? "img" : "video");
    media.src = dataUrl;
    if (isImage) {
      media.alt = name;
    } else {
      media.controls = true;
      media.preload = "metadata";
    }
    previewElement.append(media);
    previewElement.classList.remove("hidden");
    outputElement.textContent = `Recovered ${name}\n${contentType} · ${response.byteCount} bytes`;
    return;
  }

  if (response.utf8Text) {
    outputElement.textContent = response.text;
    return;
  }
  outputElement.textContent = `Base64 (${response.byteCount} bytes)\n${response.base64}`;
}

async function startScan() {
  clearScanRecovered();
  els.scanState.textContent = "Starting";
  try {
    const session = await request("/api/scans", {
      method: "POST",
      body: JSON.stringify({passphrase: els.scanPassphrase.value})
    });
    state.scan.session = session;
    renderScan(session);
    const stream = await navigator.mediaDevices.getUserMedia({
      video: {
        facingMode: "environment",
        width: {ideal: 1280},
        height: {ideal: 720}
      },
      audio: false
    });
    state.scan.stream = stream;
    state.scan.active = true;
    els.cameraPreview.srcObject = stream;
    await els.cameraPreview.play();
    els.cameraEmpty.classList.add("hidden");
    els.startScan.disabled = true;
    els.stopScan.disabled = false;
    els.scanState.textContent = "Scanning";
    state.scan.timer = window.setInterval(captureScanFrame, 520);
  } catch (error) {
    stopScan(false);
    els.scanState.textContent = "Error";
    els.scanOutput.textContent = error.message;
  }
}

function stopScan(updateText = true) {
  state.scan.active = false;
  state.scan.busy = false;
  if (state.scan.timer) {
    window.clearInterval(state.scan.timer);
    state.scan.timer = null;
  }
  if (state.scan.stream) {
    for (const track of state.scan.stream.getTracks()) {
      track.stop();
    }
    state.scan.stream = null;
  }
  els.cameraPreview.removeAttribute("src");
  els.cameraPreview.srcObject = null;
  els.cameraEmpty.classList.remove("hidden");
  els.startScan.disabled = false;
  els.stopScan.disabled = true;
  if (updateText) {
    els.scanState.textContent = "Stopped";
  }
}

async function captureScanFrame() {
  if (!state.scan.active || state.scan.busy || !state.scan.session || els.cameraPreview.readyState < 2) {
    return;
  }
  state.scan.busy = true;
  try {
    const canvas = els.scanCanvas;
    const video = els.cameraPreview;
    const width = video.videoWidth;
    const height = video.videoHeight;
    if (!width || !height) {
      return;
    }
    canvas.width = width;
    canvas.height = height;
    canvas.getContext("2d").drawImage(video, 0, 0, width, height);
    const imageBase64 = canvas.toDataURL("image/png");
    const session = await request(`/api/scans/${state.scan.session.sessionId}/frames`, {
      method: "POST",
      body: JSON.stringify({imageBase64})
    });
    state.scan.session = session;
    renderScan(session);
    if (session.complete) {
      stopScan(false);
      await receiveScan();
    }
  } catch (error) {
    els.scanOutput.textContent = error.message;
  } finally {
    state.scan.busy = false;
  }
}

async function receiveScan() {
  if (!state.scan.session) {
    return;
  }
  els.scanState.textContent = "Complete";
  const response = await request(state.scan.session.receiveUrl, {method: "POST"});
  renderPayload(response, els.scanRecoveredPreview, els.scanDownload, els.scanOutput);
}

function renderScan(session) {
  const expected = session.expectedFrames || 0;
  els.scanFrames.textContent = `${session.acceptedFrames} / ${expected}`;
  els.scanInvalid.textContent = session.invalidFrames;
  els.scanMissing.textContent = session.missingFrameNumbers?.length
    ? session.missingFrameNumbers.join(", ")
    : "-";
  if (session.complete) {
    els.scanState.textContent = "Complete";
  }
}

function clearScanRecovered() {
  els.scanRecoveredPreview.replaceChildren();
  els.scanRecoveredPreview.classList.add("hidden");
  els.scanDownload.classList.add("hidden");
  els.scanDownload.removeAttribute("href");
  els.scanDownload.removeAttribute("download");
  els.scanOutput.textContent = "";
}

els.textMode.addEventListener("click", () => setMode("text"));
els.imageMode.addEventListener("click", () => setMode("image"));
els.videoMode.addEventListener("click", () => setMode("video"));
els.fileMode.addEventListener("click", () => setMode("file"));
els.textForm.addEventListener("submit", createText);
els.imageForm.addEventListener("submit", event => createBinary(event, "image"));
els.videoForm.addEventListener("submit", event => createBinary(event, "video"));
els.fileForm.addEventListener("submit", event => createBinary(event, "file"));
els.refreshTransfers.addEventListener("click", loadTransfers);
els.openDisplay.addEventListener("click", () => {
  if (state.selected) {
    window.open(state.selected.displayUrl, "_blank", "noopener");
  }
});
els.inspectTransfer.addEventListener("click", () => {
  inspectSelected().catch(error => {
    els.detailStatus.textContent = error.message;
    els.detailStatus.className = "status-bad";
  });
});
els.receiveTransfer.addEventListener("click", receiveSelected);
els.startScan.addEventListener("click", startScan);
els.stopScan.addEventListener("click", () => stopScan());

checkHealth();
applyRouteMode();
loadTransfers().catch(error => {
  els.transferList.innerHTML = "";
  const empty = document.createElement("div");
  empty.className = "empty";
  empty.textContent = error.message;
  els.transferList.append(empty);
});
