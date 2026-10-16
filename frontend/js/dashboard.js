(function () {
  const chartCanvas = document.getElementById("sensorChart");
  const ctx = chartCanvas.getContext("2d");
  const deviceList = document.getElementById("deviceList");
  const allDevicesBtn = document.getElementById("allDevicesBtn");
  const connectionStatus = document.getElementById("connectionStatus");
  const lastUpdated = document.getElementById("lastUpdated");
  let chartTooltip = document.getElementById("chartTooltip");
  let chartData = [];
  let deviceState = window.IOT.devices.map((device) => ({ ...device }));
  let chartArea = null;
  let hoveredPointIndex = null;
  const DEVICE_LOADING_DELAY_MS = 2000;

  function scaledColor(value, min, max, rgb, invert = false) {
    const rawRatio = Math.max(0, Math.min(1, (value - min) / Math.max(max - min, 1)));
    const ratio = invert ? 1 - rawRatio : rawRatio;
    const alpha = 0.22 + ratio * 0.78;
    return `rgba(${rgb}, ${alpha.toFixed(2)})`;
  }

  function setMetricDotColors(data) {
    document.querySelector(".metric-card.temp")?.style.setProperty(
      "--metric-dot-color",
      scaledColor(data.temperature, 0, 50, "240, 77, 58")
    );
    document.querySelector(".metric-card.light")?.style.setProperty(
      "--metric-dot-color",
      scaledColor(data.luminosity, 0, 800, "217, 165, 20", true)
    );
    document.querySelector(".metric-card.humidity")?.style.setProperty(
      "--metric-dot-color",
      scaledColor(data.humidity, 0, 100, "47, 127, 222")
    );
  }

  function setMetric(latest) {
    const data = window.IOT.normalizeLatestSensor(latest);
    document.getElementById("temperatureValue").textContent = `${data.temperature.toFixed(1)}°C`;
    document.getElementById("humidityValue").textContent = `${data.humidity.toFixed(1)}%`;
    document.getElementById("luminosityValue").textContent = `${data.luminosity.toFixed(0)} Lux`;
    lastUpdated.textContent = `Cập nhật: ${window.IOT.formatTime(data.timestamp)}`;
    setMetricDotColors(data);
  }

  function normalizeChartPoint(point) {
    const normalized = window.IOT.normalizeLatestSensor(point);
    return {
      time: point.time || new Date(normalized.timestamp).toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit", second: "2-digit" }),
      temperature: normalized.temperature,
      humidity: normalized.humidity,
      luminosity: normalized.luminosity
    };
  }

  function normalizeDevice(device, index) {
    return {
      id: device.id ?? device.deviceId ?? index + 1,
      code: device.code || device.deviceCode || device.name,
      name: device.name || device.deviceName || `Device ${index + 1}`,
      status: device.status || device.state || "OFF"
    };
  }

  function resizeCanvas() {
    const box = chartCanvas.getBoundingClientRect();
    const ratio = window.devicePixelRatio || 1;
    chartCanvas.width = Math.floor(box.width * ratio);
    chartCanvas.height = Math.floor(box.height * ratio);
    ctx.setTransform(ratio, 0, 0, ratio, 0, 0);
  }

  function drawLine(points, color, key, min, max, area) {
    ctx.beginPath();
    points.forEach((point, index) => {
      const x = area.x + (index / Math.max(points.length - 1, 1)) * area.w;
      const y = area.y + area.h - ((point[key] - min) / Math.max(max - min, 1)) * area.h;
      if (index === 0) ctx.moveTo(x, y);
      else ctx.lineTo(x, y);
    });
    ctx.strokeStyle = color;
    ctx.lineWidth = 3;
    ctx.lineJoin = "round";
    ctx.stroke();
  }

  function pointPosition(point, index, key, min, max, area) {
    return {
      x: area.x + (index / Math.max(chartData.length - 1, 1)) * area.w,
      y: area.y + area.h - ((point[key] - min) / Math.max(max - min, 1)) * area.h
    };
  }

  function drawHoverGuide(area) {
    if (hoveredPointIndex === null || !chartData[hoveredPointIndex]) return;
    const point = chartData[hoveredPointIndex];
    const x = area.x + (hoveredPointIndex / Math.max(chartData.length - 1, 1)) * area.w;
    const markers = [
      ["temperature", "#e24a3b", 0, 100],
      ["humidity", "#2878d9", 0, 100],
      ["luminosity", "#d9a91f", 0, 800]
    ];

    ctx.save();
    ctx.strokeStyle = "rgba(17, 24, 39, 0.2)";
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(x, area.y);
    ctx.lineTo(x, area.y + area.h);
    ctx.stroke();

    markers.forEach(([key, color, min, max]) => {
      const position = pointPosition(point, hoveredPointIndex, key, min, max, area);
      ctx.beginPath();
      ctx.arc(position.x, position.y, 5, 0, Math.PI * 2);
      ctx.fillStyle = color;
      ctx.fill();
      ctx.lineWidth = 3;
      ctx.strokeStyle = "#ffffff";
      ctx.stroke();
    });

    const boxW = 250;
    const boxH = 142;
    const boxX = Math.min(Math.max(area.x + 8, x + 18), area.x + area.w - boxW - 8);
    const boxY = Math.max(area.y + 8, Math.min(area.y + area.h - boxH - 8, area.y + 30));
    ctx.fillStyle = "#111827";
    ctx.beginPath();
    ctx.roundRect(boxX, boxY, boxW, boxH, 8);
    ctx.fill();

    ctx.font = "800 16px Menlo, Consolas, monospace";
    ctx.fillStyle = "#ffffff";
    ctx.fillText(point.time, boxX + 18, boxY + 28);
    ctx.fillStyle = "#e24a3b";
    ctx.fillText(`Nhiệt độ (°C) : ${point.temperature.toFixed(1)}`, boxX + 18, boxY + 62);
    ctx.fillStyle = "#d9a91f";
    ctx.fillText(`Ánh sáng (Lux): ${point.luminosity.toFixed(0)}`, boxX + 18, boxY + 96);
    ctx.fillStyle = "#2878d9";
    ctx.fillText(`Độ ẩm (%)    : ${point.humidity.toFixed(1)}`, boxX + 18, boxY + 128);
    ctx.restore();
  }

  function drawChart() {
    resizeCanvas();
    const width = chartCanvas.clientWidth;
    const height = chartCanvas.clientHeight;
    const area = { x: 48, y: 22, w: width - 78, h: height - 72 };
    chartArea = area;
    ctx.clearRect(0, 0, width, height);

    ctx.strokeStyle = "#d8e1ea";
    ctx.lineWidth = 1;
    ctx.fillStyle = "#667585";
    ctx.font = "12px Arial";
    for (let i = 0; i <= 4; i += 1) {
      const y = area.y + (area.h / 4) * i;
      ctx.beginPath();
      ctx.moveTo(area.x, y);
      ctx.lineTo(area.x + area.w, y);
      ctx.stroke();
      ctx.fillText(`${100 - i * 25}`, 12, y + 4);
    }

    if (!chartData.length) {
      ctx.fillText("Chưa có dữ liệu biểu đồ", area.x + 12, area.y + 30);
      return;
    }

    drawLine(chartData, "#e24a3b", "temperature", 0, 100, area);
    drawLine(chartData, "#2878d9", "humidity", 0, 100, area);
    drawLine(chartData, "#d9a91f", "luminosity", 0, 800, area);
    drawHoverGuide(area);

    const step = Math.ceil(chartData.length / 5);
    chartData.forEach((point, index) => {
      if (index % step !== 0 && index !== chartData.length - 1) return;
      const x = area.x + (index / Math.max(chartData.length - 1, 1)) * area.w;
      ctx.fillStyle = "#667585";
      ctx.fillText(point.time, Math.max(8, x - 24), height - 22);
    });
	  }

	  function renderTooltip(index, event) {
    if (!chartData[index]) return;
    if (!chartTooltip) {
      chartTooltip = document.createElement("div");
      chartTooltip.id = "chartTooltip";
      chartTooltip.className = "chart-tooltip";
      chartCanvas.parentElement.appendChild(chartTooltip);
    }
    const point = chartData[index];
    const panelBox = chartCanvas.parentElement.getBoundingClientRect();
    const tooltipWidth = 230;
    const tooltipHeight = 152;
    const cursorX = event.clientX - panelBox.left;
    const cursorY = event.clientY - panelBox.top;
    const left = Math.min(Math.max(12, cursorX + 18), panelBox.width - tooltipWidth - 12);
    const top = Math.min(Math.max(12, cursorY - tooltipHeight - 14), panelBox.height - tooltipHeight - 12);

    chartTooltip.innerHTML = `
      <div class="tooltip-time">${point.time}</div>
      <div class="tooltip-temp">Nhiệt độ (°C) : ${point.temperature.toFixed(1)}</div>
      <div class="tooltip-light">Ánh sáng (Lux) : ${point.luminosity.toFixed(0)}</div>
      <div class="tooltip-humidity">Độ ẩm (%) : ${point.humidity.toFixed(1)}</div>
    `;
    chartTooltip.style.left = `${left}px`;
    chartTooltip.style.top = `${top}px`;
    chartTooltip.classList.add("is-visible");
  }

  function handleChartMove(event) {
    if (!chartArea || !chartData.length) return;
    const box = chartCanvas.getBoundingClientRect();
    const x = event.clientX - box.left;
    const relative = (x - chartArea.x) / Math.max(chartArea.w, 1);
    const index = Math.round(relative * (chartData.length - 1));
    const nextIndex = Math.max(0, Math.min(chartData.length - 1, index));
    hoveredPointIndex = nextIndex;
    drawChart();
    renderTooltip(nextIndex, event);
  }

  function hideChartTooltip() {
    hoveredPointIndex = null;
    chartTooltip?.classList.remove("is-visible");
    drawChart();
  }

  function renderDevices() {
    deviceList.innerHTML = "";
    deviceState.forEach((device) => {
      const item = document.createElement("div");
      item.className = "device-item";
      item.innerHTML = `
        <div>
          <h3>LED ${device.name}</h3>
          <p>${window.IOT.statusBadge(device.loading ? "Loading" : device.status)}</p>
        </div>
        <button class="toggle-btn ${device.status === "ON" ? "on" : ""} ${device.loading ? "loading" : ""}" type="button" aria-label="Toggle LED ${device.name}"></button>
      `;
      const button = item.querySelector("button");
      button.disabled = Boolean(device.loading);
      button.addEventListener("click", () => controlDevice(device.id));
      deviceList.appendChild(item);
    });
    if (allDevicesBtn) {
      const allOn = deviceState.length > 0 && deviceState.every((device) => device.status === "ON");
      const anyLoading = deviceState.some((device) => device.loading);
      allDevicesBtn.classList.toggle("is-on", allOn);
      allDevicesBtn.textContent = allOn ? "ALL OFF" : "ALL ON";
      allDevicesBtn.disabled = anyLoading;
    }
  }

  async function controlDevice(deviceId) {
    const device = deviceState.find((item) => item.id === deviceId);
    const previous = device.status;
    const action = previous === "ON" ? "OFF" : "ON";
    device.loading = true;
    renderDevices();

    const timeout = new Promise((_, reject) => {
      window.setTimeout(() => reject(new Error("Thiết bị không phản hồi")), window.IOT.config.DEVICE_TIMEOUT_MS);
    });

    try {
      const request = window.IOT.safeApi("/api/devices/control", {
        method: "POST",
        body: JSON.stringify({ deviceId, action })
      }, () => ({
        actionId: Date.now(),
        deviceId,
        deviceName: device.name,
        status: action,
        timestamp: new Date().toISOString()
      }));
      const [result] = await Promise.all([
        Promise.race([request, timeout]),
        new Promise((resolve) => window.setTimeout(resolve, DEVICE_LOADING_DELAY_MS))
      ]);
      device.status = result.status && result.status !== "Loading" ? result.status : action;
    } catch (error) {
      device.status = previous;
      connectionStatus.textContent = "Thiết bị không phản hồi";
      connectionStatus.style.color = "#9c2525";
      connectionStatus.style.background = "rgba(214, 69, 69, 0.14)";
    } finally {
      device.loading = false;
      renderDevices();
    }
  }

  async function controlAllDevices() {
    const turnOn = deviceState.some((device) => device.status !== "ON");
    const targetDevices = deviceState.filter((device) => (
      (turnOn && device.status !== "ON") || (!turnOn && device.status === "ON")
    ));
    await Promise.all(targetDevices.map((device) => controlDevice(device.id)));
  }

  async function loadInitial() {
    const devices = await window.IOT.safeApi("/api/devices", {}, () => window.IOT.devices);
    const deviceContent = Array.isArray(devices?.content) ? devices.content : devices;
    deviceState = (Array.isArray(deviceContent) && deviceContent.length ? deviceContent : window.IOT.devices).map(normalizeDevice);

    const rawChartData = await window.IOT.safeApi("/api/sensors/chart-data?limit=20", {}, () => window.IOT.mockChartData(20));
    chartData = (Array.isArray(rawChartData) ? rawChartData : []).map(normalizeChartPoint);
    const latest = await window.IOT.safeApi("/api/sensors/latest", {}, window.IOT.mockLatest);
    setMetric(latest);
    drawChart();
    renderDevices();
    connectionStatus.textContent = "Đang hoạt động";
  }

  async function refreshLatest() {
    const latest = await window.IOT.safeApi("/api/sensors/latest", {}, window.IOT.mockLatest);
    const point = normalizeChartPoint(latest);
    setMetric(latest);
    chartData.push(point);
    chartData = chartData.slice(-20);
    drawChart();
  }

  window.addEventListener("resize", drawChart);
  document.addEventListener("DOMContentLoaded", () => {
    loadInitial();
    chartCanvas.addEventListener("mousemove", handleChartMove);
    chartCanvas.addEventListener("mouseleave", hideChartTooltip);
    allDevicesBtn?.addEventListener("click", controlAllDevices);
    window.setInterval(refreshLatest, window.IOT.config.SENSOR_REFRESH_MS);
  });
})();
