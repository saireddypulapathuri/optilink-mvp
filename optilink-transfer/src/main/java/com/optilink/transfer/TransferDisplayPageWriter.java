package com.optilink.transfer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Writes a local browser page that loops generated QR frames.
 */
public final class TransferDisplayPageWriter {

    /**
     * Writes the display page.
     *
     * @param outputDirectory output directory
     * @param frameFiles QR frame image file names
     * @return written page path
     * @throws IOException when writing fails
     */
    public Path write(Path outputDirectory, List<String> frameFiles) throws IOException {
        Objects.requireNonNull(outputDirectory, "outputDirectory");
        List<String> files = List.copyOf(frameFiles);
        Path page = outputDirectory.resolve("index.html");
        Files.writeString(page, html(files), StandardCharsets.UTF_8);
        return page;
    }

    private String html(List<String> frameFiles) {
        return """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>OptiLink Beam</title>
                  <style>
                    :root{color-scheme:dark;--bg:#070b10;--panel:#101820;--ink:#f7fbff;--muted:#9eabb6;--line:#26323e;--accent:#37d67a;--blue:#55a7ff}
                    *{box-sizing:border-box}
                    html,body{height:100%;margin:0;background:var(--bg);color:var(--ink);font-family:Inter,ui-sans-serif,system-ui,-apple-system,BlinkMacSystemFont,"Segoe UI",sans-serif}
                    body{overflow:hidden}
                    main{min-height:100%;display:grid;grid-template-rows:auto minmax(0,1fr) auto;gap:14px;padding:18px}
                    header,footer{display:flex;align-items:center;justify-content:space-between;gap:12px}
                    h1{margin:0;font-size:18px;line-height:1.1}
                    .sub{margin-top:4px;color:var(--muted);font-size:13px}
                    .pill{min-width:92px;padding:7px 10px;border:1px solid rgba(55,214,122,.45);border-radius:999px;color:var(--accent);text-align:center;font-size:12px;font-weight:800}
                    .stage{min-height:0;display:grid;place-items:center;border:1px solid var(--line);border-radius:8px;background:#fff}
                    img{width:min(86vmin,780px);height:min(86vmin,780px);image-rendering:pixelated}
                    .meter{display:grid;gap:8px;width:min(100%,760px)}
                    progress{width:100%;height:10px;overflow:hidden;border:0;border-radius:999px;background:#1b2630}
                    progress::-webkit-progress-bar{background:#1b2630}
                    progress::-webkit-progress-value{background:linear-gradient(90deg,var(--accent),var(--blue))}
                    .controls{display:flex;align-items:center;justify-content:space-between;gap:10px;width:100%;color:var(--muted);font-size:13px}
                    .buttons{display:flex;gap:8px}
                    button{min-height:34px;padding:0 12px;border:1px solid var(--line);border-radius:6px;background:#111c26;color:var(--ink);font:inherit;font-size:13px;font-weight:800}
                    button.active{border-color:rgba(85,167,255,.65);color:var(--blue)}
                    @media(max-width:720px){main{padding:10px}header{align-items:flex-start}.stage{border-radius:6px}img{width:min(94vmin,720px);height:min(94vmin,720px)}footer{align-items:stretch;flex-direction:column}.controls{align-items:flex-start;flex-direction:column}}
                  </style>
                </head>
                <body>
                  <main>
                    <header>
                      <div>
                        <h1>OptiLink Beam</h1>
                        <div class="sub">Point the receiver camera at this screen</div>
                      </div>
                      <div class="pill">Broadcasting</div>
                    </header>
                    <section class="stage" aria-label="QR stream">
                      <img id="frame" alt="OptiLink QR frame">
                    </section>
                    <footer>
                      <div class="meter">
                        <progress id="progress" max="100" value="0"></progress>
                        <div class="controls">
                          <span id="counter"></span>
                          <span id="rate"></span>
                        </div>
                      </div>
                      <div class="buttons" aria-label="Frame speed">
                        <button data-period="950" type="button">Slow</button>
                        <button data-period="650" class="active" type="button">Normal</button>
                        <button data-period="420" type="button">Fast</button>
                      </div>
                    </footer>
                  </main>
                  <script>
                    const frames = __FRAMES__;
                    let index = 0;
                    let period = 650;
                    let timer;
                    const image = document.getElementById("frame");
                    const counter = document.getElementById("counter");
                    const progress = document.getElementById("progress");
                    const rate = document.getElementById("rate");
                    const buttons = Array.from(document.querySelectorAll("button[data-period]"));
                    function show() {
                      image.src = frames[index];
                      counter.textContent = `Frame ${index + 1} of ${frames.length}`;
                      progress.value = ((index + 1) / frames.length) * 100;
                      rate.textContent = `${Math.round(1000 / period)} frames/sec`;
                      index = (index + 1) % frames.length;
                    }
                    function schedule() {
                      clearInterval(timer);
                      show();
                      timer = setInterval(show, period);
                    }
                    buttons.forEach(button => button.addEventListener("click", () => {
                      period = Number(button.dataset.period);
                      buttons.forEach(item => item.classList.toggle("active", item === button));
                      schedule();
                    }));
                    schedule();
                  </script>
                </body>
                </html>
                """.replace("__FRAMES__", jsonArray(frameFiles));
    }

    private String jsonArray(List<String> frameFiles) {
        return frameFiles.stream()
                .map(file -> "\"" + file + "\"")
                .collect(java.util.stream.Collectors.joining(",", "[", "]"));
    }
}
