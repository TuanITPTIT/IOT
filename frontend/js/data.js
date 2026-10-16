(function () {
  const form = document.getElementById("dataFilterForm");
  const rows = document.getElementById("dataRows");
  const summary = document.getElementById("resultSummary");
  const pagination = document.getElementById("dataPagination");
  const resetButton = document.getElementById("resetDataBtn");
  let currentPage = 0;
  let sortDir = "desc";
  let cachedPageData = null;

  function currentFilters() {
    const filters = Object.fromEntries(window.IOT.queryFromForm(form, {
      page: currentPage,
      size: window.IOT.config.PAGE_SIZE,
      sortBy: "timestamp",
      sortDir
    }));
    filters.keyword = String(filters.keyword || "").trim();
    return filters;
  }

  function matchesTime(timestamp, query) {
    const normalizedQuery = String(query || "").trim().toLowerCase();
    if (!normalizedQuery) return true;

    const raw = String(timestamp || "").toLowerCase();
    const formatted = window.IOT.formatTime(timestamp).toLowerCase();
    const vietnameseDate = normalizedQuery.match(/^(\d{4})-(\d{2})-(\d{2})$/)
      ? normalizedQuery.replace(/^(\d{4})-(\d{2})-(\d{2})$/, "$3/$2/$1")
      : "";
    const normalizedSlashDate = normalizedQuery.replaceAll("-", "/");

    return [raw, formatted].some((value) => (
      value.includes(normalizedQuery)
      || value.includes(normalizedSlashDate)
      || (vietnameseDate && value.includes(vietnameseDate))
    ));
  }

  function applyClientFilter(content, filters) {
    const keyword = String(filters.keyword || "").trim().toLowerCase();
    const searchBy = String(filters.sensorType || "").trim().toLowerCase();
    if (!keyword) return content;

    return content.filter((item) => {
      const type = normalizedSensorType(item.sensorType);
      const typeText = type.toLowerCase();
      const codeText = sensorCode(type).toLowerCase();
      const valueText = `${item.value}`;
      const timeOk = matchesTime(item.timestamp, keyword);
      const valueOk = valueText.toLowerCase().includes(keyword);

      if (searchBy === "time") return timeOk;
      if (searchBy) return typeText === searchBy && valueOk;

      return typeText.includes(keyword) || codeText.includes(keyword) || valueOk || timeOk;
    });
  }

  function applyClientSort(content) {
    return [...content].sort((a, b) => {
      const timeA = new Date(a.timestamp || 0).getTime();
      const timeB = new Date(b.timestamp || 0).getTime();
      const result = timeA - timeB;
      return sortDir === "asc" ? result : -result;
    });
  }

  function normalizedSensorType(sensorType) {
    const value = String(sensorType || "").trim().toLowerCase();
    const types = {
      temperature: "Temperature",
      temp: "Temperature",
      nd: "Temperature",
      humidity: "Humidity",
      humid: "Humidity",
      da: "Humidity",
      luminosity: "Luminosity",
      light: "Luminosity",
      lux: "Luminosity",
      as: "Luminosity"
    };
    return types[value] || sensorType || "Unknown";
  }

  function sensorCode(sensorType) {
    const codes = {
      Temperature: "ND",
      Humidity: "DA",
      Luminosity: "AS"
    };
    return codes[normalizedSensorType(sensorType)] || "SN-UNKNOWN";
  }

  function buildFilteredPage(pageData) {
    const filters = currentFilters();
    const normalizedPage = window.IOT.normalizePageData(pageData, 0);
    const filteredContent = applyClientSort(applyClientFilter(normalizedPage.content.map(window.IOT.normalizeSensorRecord), filters));

    const size = window.IOT.config.PAGE_SIZE;
    const totalElements = filteredContent.length;
    const totalPages = Math.max(1, Math.ceil(totalElements / size));
    const safePage = Math.min(currentPage, totalPages - 1);
    const start = safePage * size;
    return {
      ...normalizedPage,
      content: filteredContent.slice(start, start + size),
      page: safePage,
      size,
      totalElements,
      totalPages
    };
  }

  function renderTable(pageData) {
    const normalizedPage = buildFilteredPage(pageData);
    rows.innerHTML = "";
    if (!normalizedPage.content.length) {
      rows.innerHTML = `<tr><td class="empty-row" colspan="4">Không tìm thấy bản ghi nào phù hợp</td></tr>`;
    } else {
      normalizedPage.content.forEach((item) => {
        const type = normalizedSensorType(item.sensorType);
        const value = `${item.value} ${item.unit || ""}`.trim();
        rows.insertAdjacentHTML("beforeend", `
          <tr>
            <td>${sensorCode(type)}</td>
            <td>${type}</td>
            <td>${value}</td>
            <td>${window.IOT.timeCopyControl(item.timestamp)}</td>
          </tr>
        `);
      });
    }
    window.IOT.bindCopyTime(rows);
    summary.textContent = `${normalizedPage.totalElements.toLocaleString("vi-VN")} records found`;
    window.IOT.renderPagination(pagination, normalizedPage, (page) => {
      currentPage = page;
      renderTable(cachedPageData);
    }, { singular: "record", plural: "records" });
  }

  async function loadData() {
    if (!cachedPageData) {
      const params = new URLSearchParams({
        page: "0",
        size: "1000",
        sortBy: "timestamp",
        sortDir
      });
      cachedPageData = await window.IOT.safeApi(`/api/sensors/history?${params.toString()}`, {}, () => window.IOT.sensorHistory(0, 1000, { sortDir }));
    }
    renderTable(cachedPageData);
  }

  document.addEventListener("DOMContentLoaded", loadData);
  form.addEventListener("submit", (event) => {
    event.preventDefault();
    currentPage = 0;
    loadData();
  });
  resetButton.addEventListener("click", () => {
    form.reset();
    currentPage = 0;
    sortDir = "desc";
    loadData();
  });
})();
