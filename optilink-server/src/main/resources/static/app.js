const state = {
  transfers: [],
  selected: null,
  room: null,
  scan: {
    session: null,
    stream: null,
    active: false,
    busy: false,
    timer: null,
    detector: null,
    submittedPayloads: new Set()
  }
};

const els = {
  health: document.getElementById("health"),
  pageSubtitle: document.getElementById("pageSubtitle"),
  consoleLink: document.getElementById("consoleLink"),
  sendLink: document.getElementById("sendLink"),
  receiveLink: document.getElementById("receiveLink"),
  roomCode: document.getElementById("roomCode"),
  roomStatus: document.getElementById("roomStatus"),
  roomInput: document.getElementById("roomInput"),
  joinRoom: document.getElementById("joinRoom"),
  newRoom: document.getElementById("newRoom"),
  copyRoomLink: document.getElementById("copyRoomLink"),
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
  scanProgress: document.getElementById("scanProgress"),
  scanPercent: document.getElementById("scanPercent"),
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
  const {roomHeader = true, ...fetchOptions} = options;
  const response = await fetch(path, {
    headers: {
      "Content-Type": "application/json",
      ...(roomHeader && state.room ? {"X-OptiLink-Room": state.room.code} : {}),
      ...(fetchOptions.headers || {})
    },
    ...fetchOptions
  });
  const text = await response.text();
  const body = text ? JSON.parse(text) : null;
  if (!response.ok) {
    throw new Error(body?.message || `Request failed: ${response.status}`);
  }
  return body;
}

async function initializeRoom() {
  const queryRoom = new URLSearchParams(window.location.search).get("room");
  const storedRoom = window.sessionStorage.getItem("optilink.room");
  const candidate = (queryRoom || storedRoom || "").trim().toUpperCase();
  if (candidate) {
    try {
      await joinTransferRoom(candidate, false);
      return;
    } catch (error) {
      window.sessionStorage.removeItem("optilink.room");
      els.roomStatus.textContent = error.message;
    }
  }
  if (!document.body.classList.contains("receive-page")) {
    await createTransferRoom();
  }
}

async function createTransferRoom() {
  setRoomBusy(true);
  try {
    const room = await request("/api/rooms", {method: "POST", roomHeader: false});
    setRoom(room);
    await loadTransfers();
  } finally {
    setRoomBusy(false);
  }
}

async function joinTransferRoom(code = els.roomInput.value, reload = true) {
  const normalized = code.trim().toUpperCase();
  if (normalized.length !== 8) {
    throw new Error("Enter the eight-character room code.");
  }
  setRoomBusy(true);
  try {
    const room = await request(`/api/rooms/${encodeURIComponent(normalized)}`, {roomHeader: false});
    setRoom(room);
    if (reload) {
      await loadTransfers();
    }
  } finally {
    setRoomBusy(false);
  }
}

function setRoom(room) {
  state.room = room;
  window.sessionStorage.setItem("optilink.room", room.code);
  els.roomCode.textContent = room.code;
  els.roomInput.value = room.code;
  els.roomStatus.textContent = `Active until ${new Date(room.expiresAt).toLocaleTimeString([], {hour: "2-digit", minute: "2-digit"})}`;
  els.copyRoomLink.disabled = false;
  const roomQuery = `?room=${encodeURIComponent(room.code)}`;
  els.sendLink.href = `/send${roomQuery}`;
  els.receiveLink.href = `/receive${roomQuery}`;
}

function setRoomBusy(busy) {
  els.joinRoom.disabled = busy;
  els.newRoom.disabled = busy;
}

async function copyReceiverLink() {
  if (!state.room) {
    return;
  }
  const url = new URL(state.room.receiveUrl, window.location.origin).href;
  try {
    await navigator.clipboard.writeText(url);
    els.roomStatus.textContent = "Receiver link copied.";
  } catch (_) {
    window.prompt("Receiver link", url);
  }
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
    if (!state.room) {
      await createTransferRoom();
    }
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
    if (!state.room) {
      await createTransferRoom();
    }
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
    state.scan.detector = createBarcodeDetector();
    state.scan.submittedPayloads = new Set();
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
    els.scanOutput.textContent = "Looking for an OptiLink QR frame...";
    state.scan.timer = window.setInterval(captureScanFrame, 320);
  } catch (error) {
    stopScan(false);
    els.scanState.textContent = "Error";
    els.scanOutput.textContent = cameraErrorMessage(error);
  }
}

function cameraErrorMessage(error) {
  if (!window.isSecureContext) {
    return "Camera access requires HTTPS. Open the hosted receiver URL directly.";
  }
  if (!navigator.mediaDevices?.getUserMedia) {
    return "This browser does not provide camera access. Use current Safari or Chrome.";
  }
  if (error?.name === "NotAllowedError") {
    return "Camera permission is blocked. Allow camera access for this site, then tap Start Camera again.";
  }
  if (error?.name === "NotFoundError") {
    return "No usable camera was found on this device.";
  }
  return error.message || "Unable to start the camera.";
}

function stopScan(updateText = true) {
  state.scan.active = false;
  state.scan.busy = false;
  state.scan.detector = null;
  state.scan.submittedPayloads = new Set();
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
    const context = drawCameraSquare();
    if (!context) {
      return;
    }
    const qrPayload = await detectQrPayload(context);
    if (qrPayload && state.scan.submittedPayloads.has(qrPayload)) {
      return;
    }
    const payload = qrPayload
      ? {qrPayload}
      : {imageBase64: els.scanCanvas.toDataURL("image/jpeg", 0.9)};
    const session = await request(`/api/scans/${state.scan.session.sessionId}/frames`, {
      method: "POST",
      body: JSON.stringify(payload)
    });
    if (qrPayload) {
      state.scan.submittedPayloads.add(qrPayload);
    }
    state.scan.session = session;
    renderScan(session);
    if (!session.complete) {
      els.scanOutput.textContent = qrPayload
        ? `Received ${session.acceptedFrames} of ${session.expectedFrames || "?"} QR frames.`
        : "Looking for an OptiLink QR frame...";
    }
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

function createBarcodeDetector() {
  if (!("BarcodeDetector" in window)) {
    return null;
  }
  try {
    return new window.BarcodeDetector({formats: ["qr_code"]});
  } catch (_) {
    return null;
  }
}

function drawCameraSquare() {
  const video = els.cameraPreview;
  const sourceWidth = video.videoWidth;
  const sourceHeight = video.videoHeight;
  if (!sourceWidth || !sourceHeight) {
    return null;
  }
  const sourceSize = Math.min(sourceWidth, sourceHeight);
  const targetSize = Math.min(960, sourceSize);
  const sourceX = Math.floor((sourceWidth - sourceSize) / 2);
  const sourceY = Math.floor((sourceHeight - sourceSize) / 2);
  const canvas = els.scanCanvas;
  canvas.width = targetSize;
  canvas.height = targetSize;
  const context = canvas.getContext("2d", {willReadFrequently: true});
  context.drawImage(video, sourceX, sourceY, sourceSize, sourceSize, 0, 0, targetSize, targetSize);
  return context;
}

async function detectQrPayload(context) {
  if (state.scan.detector) {
    const codes = await state.scan.detector.detect(els.scanCanvas);
    const value = codes.find(code => code.rawValue?.startsWith("OPTL1:"))?.rawValue;
    if (value) {
      return value;
    }
  }
  if (typeof window.jsQR !== "function") {
    return null;
  }
  const canvas = els.scanCanvas;
  const imageData = context.getImageData(0, 0, canvas.width, canvas.height);
  const result = window.jsQR(imageData.data, canvas.width, canvas.height, {
    inversionAttempts: "attemptBoth"
  });
  return result?.data?.startsWith("OPTL1:") ? result.data : null;
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
  const percentage = expected ? Math.min(100, Math.round((session.acceptedFrames / expected) * 100)) : 0;
  els.scanFrames.textContent = `${session.acceptedFrames} / ${expected}`;
  els.scanProgress.value = percentage;
  els.scanPercent.textContent = `${percentage}%`;
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
  els.scanProgress.value = 0;
  els.scanPercent.textContent = "0%";
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
els.joinRoom.addEventListener("click", () => {
  joinTransferRoom().catch(error => {
    els.roomStatus.textContent = error.message;
  });
});
els.newRoom.addEventListener("click", () => {
  createTransferRoom().catch(error => {
    els.roomStatus.textContent = error.message;
  });
});
els.copyRoomLink.addEventListener("click", copyReceiverLink);
els.roomInput.addEventListener("input", () => {
  els.roomInput.value = els.roomInput.value.toUpperCase().replace(/[^23456789A-HJ-NP-Z]/g, "");
});
els.roomInput.addEventListener("keydown", event => {
  if (event.key === "Enter") {
    event.preventDefault();
    els.joinRoom.click();
  }
});

checkHealth();
applyRouteMode();
initializeRoom()
  .then(loadTransfers)
  .catch(error => {
    els.transferList.innerHTML = "";
    const empty = document.createElement("div");
    empty.className = "empty";
    empty.textContent = error.message;
    els.transferList.append(empty);
  });
