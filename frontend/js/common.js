(function () {
  const config = window.APP_CONFIG;

  const mockProfile = {
    fullName: "Lê Quốc Tuấn",
    role: "Student, Software Engineering",
    studentId: "B23DCCN885",
    className: "D23CNPM02",
    institution: "PTIT (Học viện CNBCVT)",
    aboutMe: "Xin chào! Tôi là Lê Quốc Tuấn, sinh viên chuyên ngành Công nghệ phần mềm. Đồ án tập trung xây dựng hệ thống IoT giám sát nhiệt độ, độ ẩm, ánh sáng và điều khiển thiết bị qua Web.",
    contact: {
      phone: "0869728340",
      email: "tuanthanhmc123@gmail.com",
      address: "Hà Nội, Việt Nam"
    },
    resources: {
      githubLink: "https://github.com/tuanthanhmc123/iot-smart-home",
      figmaLink: "https://www.figma.com/design/BzUFwsBoDTSe6gti7vPIMc/Untitled?timeline=keyframe&node-id=0-1&t=aWbg84r0D2pi8R3i-0",
      apiDocsLink: "https://documenter.getpostman.com/view/iot-apis",
      baocaoLink: "https://example.com/Bao_Cao_Do_An_IoT_LeQuocTuan.pdf"
    }
  };

  const devices = [
    { id: 1, code: "LED_TEMP", name: "Temperature", status: "OFF" },
    { id: 2, code: "LED_LIGHT", name: "Luminosity", status: "ON" },
    { id: 3, code: "LED_HUMIDITY", name: "Humidity", status: "OFF" }
  ];
  const mockSensorBaseTime = Date.now();
  const mockActionBaseTime = Date.now();

  function token() {
    return localStorage.getItem("iot_token") || sessionStorage.getItem("iot_token") || "";
  }

  function requireAuth() {
    if (!token()) {
      window.location.href = "index.html";
      return false;
    }
    return true;
  }

  function bindLogout() {
    const logoutBtn = document.getElementById("logoutBtn");
    if (!logoutBtn) return;
    logoutBtn.addEventListener("click", () => {
      localStorage.removeItem("iot_token");
      sessionStorage.removeItem("iot_token");
      localStorage.removeItem("iot_user");
      window.location.href = "index.html";
    });
  }

  async function apiRequest(path, options = {}) {
    const headers = {
      "Content-Type": "application/json",
      ...options.headers
    };
    const jwt = token();
    if (jwt) headers.Authorization = `Bearer ${jwt}`;

    const response = await fetch(`${config.API_BASE_URL}${path}`, {
      ...options,
      headers
    });

    if (response.status === 401) {
      localStorage.removeItem("iot_token");
      sessionStorage.removeItem("iot_token");
      window.location.href = "index.html";
      throw new Error("Unauthorized");
    }

    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return response.json();
  }

  async function safeApi(path, options, fallbackFactory) {
    try {
      const payload = await apiRequest(path, options);
      return payload.data ?? payload;
    } catch (error) {
      if (!config.USE_MOCK_WHEN_API_FAILS) throw error;
      return typeof fallbackFactory === "function" ? fallbackFactory() : fallbackFactory;
    }
  }

  function formatTime(value) {
    const date = value ? new Date(value) : new Date();
    if (Number.isNaN(date.getTime())) return value;
    return date.toLocaleString("vi-VN", {
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
      day: "2-digit",
      month: "2-digit",
      year: "numeric"
    });
  }

  function statusBadge(status) {
    const label = String(status || "--");
    const normalized = label.toLowerCase() === "fail" ? "failed" : label.toLowerCase();
    return `<span class="badge ${normalized}">${label}</span>`;
  }

  function queryFromForm(form, extras = {}) {
    const params = new URLSearchParams();
    new FormData(form).forEach((value, key) => {
      if (value !== "") params.set(key, value);
    });
    Object.entries(extras).forEach(([key, value]) => params.set(key, value));
    return params;
  }

  function normalizePageData(pageData, fallbackPage = 0, fallbackSize = config.PAGE_SIZE) {
    const content = Array.isArray(pageData?.content)
      ? pageData.content
      : Array.isArray(pageData)
        ? pageData
        : [];
    const page = Number(pageData?.page ?? pageData?.number ?? fallbackPage);
    const size = Number(pageData?.size ?? fallbackSize);
    const totalElements = Number(pageData?.totalElements ?? pageData?.total ?? content.length);
    const totalPages = Number(pageData?.totalPages ?? Math.max(1, Math.ceil(totalElements / Math.max(size, 1))));
    return { ...pageData, content, page, size, totalElements, totalPages };
  }

	  function pageSummary(pageData, singularLabel = "record", pluralLabel = "records") {
	    const data = normalizePageData(pageData);
	    const labelMap = {
	      record: "bản ghi",
	      records: "bản ghi",
	      action: "thao tác",
	      actions: "thao tác"
	    };
	    const label = labelMap[data.totalElements === 1 ? singularLabel : pluralLabel] || pluralLabel;
	    if (!data.totalElements) return `Hiển thị 0 ${label}`;
	    const from = data.page * data.size + 1;
	    const to = Math.min((data.page + 1) * data.size, data.totalElements);
	    return `Hiển thị ${from}-${to} trong ${data.totalElements.toLocaleString("vi-VN")} ${label}`;
	  }

	  function renderPagination(root, pageData, onPage, labels = {}) {
	    const data = normalizePageData(pageData);
	    const page = data.page;
	    const totalPages = data.totalPages;
	    const pageSizeOptions = labels.pageSizeOptions || [5, 10, 20, 50,100];
	    const currentSize = Number(data.size || config.PAGE_SIZE);
	    root.innerHTML = "";
	    const summaryText = pageSummary(data, labels.singular || "record", labels.plural || "records").replace(" trong ", " của ");
	    const summary = document.createElement("span");
	    summary.className = "pagination-summary";
	    summary.textContent = summaryText;

	    const sizeControl = document.createElement("label");
	    sizeControl.className = "pagination-size";
	    const sizePrefix = document.createElement("span");
	    sizePrefix.textContent = "Hiển thị:";
	    const sizeSelect = document.createElement("select");
	    pageSizeOptions.forEach((option) => {
	      const choice = document.createElement("option");
	      choice.value = option;
	      choice.textContent = option;
	      choice.selected = option === currentSize;
	      sizeSelect.appendChild(choice);
	    });
	    if (![...sizeSelect.options].some((option) => Number(option.value) === currentSize)) {
	      const choice = document.createElement("option");
	      choice.value = currentSize;
	      choice.textContent = currentSize;
	      choice.selected = true;
	      sizeSelect.prepend(choice);
	    }
	    const sizeSuffix = document.createElement("span");
	    sizeSuffix.textContent = "bản ghi/trang";
	    sizeSelect.addEventListener("change", () => {
	      config.PAGE_SIZE = Number(sizeSelect.value);
	      onPage(0);
	    });
	    sizeControl.append(sizePrefix, sizeSelect, sizeSuffix);

	    const pageButtons = document.createElement("div");
	    pageButtons.className = "pagination-buttons";
	    root.append(summary, sizeControl, pageButtons);

	    const start = Math.max(0, Math.min(page - 2, totalPages - 5));
	    const end = Math.min(totalPages, start + 5);
	    const buttons = [
	      { label: "<", page: Math.max(0, page - 1), disabled: page === 0 },
	      ...Array.from({ length: end - start }, (_, index) => ({ label: start + index + 1, page: start + index })),
	      { label: ">", page: Math.min(totalPages - 1, page + 1), disabled: page >= totalPages - 1 }
	    ];
	
	    buttons.forEach((item) => {
	      const button = document.createElement("button");
	      button.type = "button";
	      button.textContent = item.label;
	      button.disabled = item.disabled;
	      if (item.page === page && typeof item.label === "number") button.classList.add("active");
	      button.addEventListener("click", () => onPage(item.page));
	      pageButtons.appendChild(button);
	    });
	  }

  function normalizeSensorRecord(item) {
    const sensorType = item.sensorType || item.type || item.sensorName || item.name || item.sensor?.name || "Unknown";
    const timestamp = item.timestamp || item.time || item.createdAt || item.created_at;
    return {
      ...item,
      id: item.id,
      sensorType,
      value: item.value,
      unit: item.unit || (sensorType === "Temperature" ? "°C" : sensorType === "Humidity" ? "%" : sensorType === "Luminosity" ? "Lux" : ""),
      timestamp
    };
  }

  function normalizeActionRecord(item) {
    return {
      ...item,
      id: item.id || item.actionId,
      deviceName: item.deviceName || item.device?.name || item.name || "Unknown",
      action: item.action || item.command || item.requestedAction || "--",
      status: item.status || item.result || item.action || "--",
      timestamp: item.timestamp || item.createdAt || item.created_at || item.time
    };
  }

  function normalizeLatestSensor(data) {
    return {
      temperature: Number(data.temperature ?? data.temp ?? 0),
      humidity: Number(data.humidity ?? 0),
      luminosity: Number(data.luminosity ?? data.light ?? data.lux ?? 0),
      timestamp: data.timestamp || data.time || data.createdAt || new Date().toISOString()
    };
  }

  function mockChartData(limit = 20) {
    const now = Date.now();
    return Array.from({ length: limit }, (_, index) => {
      const t = new Date(now - (limit - index - 1) * 2000);
      return {
        time: t.toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit", second: "2-digit" }),
        temperature: +(29 + Math.sin(index / 2) * 2.8).toFixed(1),
        humidity: +(62 + Math.cos(index / 3) * 8).toFixed(1),
        luminosity: +(410 + Math.sin(index / 2.8) * 95).toFixed(0)
      };
    });
  }

  function mockLatest() {
    const point = mockChartData(1)[0];
    return { ...point, timestamp: new Date().toISOString() };
  }

	  function sensorHistory(page = 0, size = config.PAGE_SIZE, filters = {}) {
	    const types = [
	      ["Temperature", "°C"],
	      ["Humidity", "%"],
	      ["Luminosity", "Lux"]
	    ];
	    const matchesTime = (timestamp, query) => {
	      const normalizedQuery = String(query || "").trim().toLowerCase();
	      if (!normalizedQuery) return true;
	      const raw = String(timestamp || "").toLowerCase();
	      const formatted = formatTime(timestamp).toLowerCase();
	      const vietnameseDate = normalizedQuery.match(/^(\d{4})-(\d{2})-(\d{2})$/)
	        ? normalizedQuery.replace(/^(\d{4})-(\d{2})-(\d{2})$/, "$3/$2/$1")
	        : "";
	      const normalizedSlashDate = normalizedQuery.replaceAll("-", "/");
	      return [raw, formatted].some((value) => (
	        value.includes(normalizedQuery)
	        || value.includes(normalizedSlashDate)
	        || (vietnameseDate && value.includes(vietnameseDate))
	      ));
	    };
	    const allRows = Array.from({ length: 48 }, (_, i) => {
	      const id = i + 1;
	      const [sensorType, unit] = types[id % 3];
	      const value = sensorType === "Temperature" ? 27 + (id % 9) * 0.7 : sensorType === "Humidity" ? 55 + (id % 12) : 360 + (id % 16) * 18;
	      return {
	        id,
	        sensorType,
	        value: +value.toFixed(1),
	        unit,
	        timestamp: formatTime(mockSensorBaseTime - id * 120000)
	      };
		    });
		    const keyword = String(filters.keyword || filters.value || "").trim().toLowerCase();
		    const sensorTypeFilter = String(filters.sensorType || "").trim().toLowerCase();
		    const filtered = allRows.filter((row) => {
		      const typeOk = !sensorTypeFilter || row.sensorType.toLowerCase() === sensorTypeFilter;
		      const valueOk = String(row.value).toLowerCase().includes(keyword);
		      const timeOk = matchesTime(row.timestamp, keyword);
		      const keywordOk = !keyword || valueOk || timeOk;
		      return typeOk && keywordOk;
		    });
    const direction = filters.sortDir === "asc" ? 1 : -1;
    filtered.sort((a, b) => direction * (new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime()));
    const totalElements = filtered.length;
    const content = filtered.slice(page * size, page * size + size);
    return { content, page, size, totalElements, totalPages: Math.ceil(totalElements / size) };
  }

	  function actionHistory(page = 0, size = config.PAGE_SIZE, filters = {}) {
	    const names = ["Temperature", "Luminosity", "Humidity"];
	    const statuses = ["ON", "OFF", "Loading", "Failed"];
	    const matchesTime = (timestamp, query) => {
	      const normalizedQuery = String(query || "").trim().toLowerCase();
	      if (!normalizedQuery) return true;
	      const raw = String(timestamp || "").toLowerCase();
	      const formatted = formatTime(timestamp).toLowerCase();
	      const vietnameseDate = normalizedQuery.match(/^(\d{4})-(\d{2})-(\d{2})$/)
	        ? normalizedQuery.replace(/^(\d{4})-(\d{2})-(\d{2})$/, "$3/$2/$1")
	        : "";
	      const normalizedSlashDate = normalizedQuery.replaceAll("-", "/");
	      return [raw, formatted].some((value) => (
	        value.includes(normalizedQuery)
	        || value.includes(normalizedSlashDate)
	        || (vietnameseDate && value.includes(vietnameseDate))
	      ));
	    };
	    const allRows = Array.from({ length: 32 }, (_, i) => {
	      const id = i + 1;
	      const action = id % 2 === 0 ? "OFF" : "ON";
      return {
        id,
        deviceName: names[id % 3],
        action,
        status: statuses[id % 4],
	        timestamp: formatTime(mockActionBaseTime - id * 180000)
      };
    });
	    const keyword = String(filters.keyword || filters.deviceName || "").trim().toLowerCase();
	    const actionFilter = String(filters.action || "").trim().toLowerCase();
	    const statusFilter = String(filters.status || "").trim().toLowerCase();
	    const timeFilter = String(filters.time || "").trim();
	    const filtered = allRows.filter((row) => {
	      const keywordOk = !keyword || row.deviceName.toLowerCase().includes(keyword);
	      const actionOk = !actionFilter || row.action.toLowerCase() === actionFilter;
	      const statusOk = !statusFilter || row.status.toLowerCase() === statusFilter;
	      const timeOk = matchesTime(row.timestamp, timeFilter);
	      return keywordOk && actionOk && statusOk && timeOk;
	    });
    const direction = filters.sortDir === "asc" ? 1 : -1;
    filtered.sort((a, b) => direction * (new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime()));
    const totalElements = filtered.length;
    const content = filtered.slice(page * size, page * size + size);
    return { content, page, size, totalElements, totalPages: Math.ceil(totalElements / size) };
  }

  window.IOT = {
    config,
    devices,
    mockProfile,
    token,
    requireAuth,
    bindLogout,
    apiRequest,
    safeApi,
    formatTime,
    statusBadge,
    queryFromForm,
    normalizePageData,
    pageSummary,
    renderPagination,
    normalizeSensorRecord,
    normalizeActionRecord,
    normalizeLatestSensor,
    mockChartData,
    mockLatest,
    sensorHistory,
    actionHistory
  };

  document.addEventListener("DOMContentLoaded", () => {
    if (document.body.dataset.page) {
      requireAuth();
      bindLogout();
    }
  });
})();
