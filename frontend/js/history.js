(function () {
  const form = document.getElementById("historyFilterForm");
  const rows = document.getElementById("historyRows");
  const summary = document.getElementById("historySummary");
  const pagination = document.getElementById("historyPagination");
  const timeFilter = document.getElementById("timeFilter");
  const resetButton = document.getElementById("resetHistoryBtn");
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
    filters.time = timeFilter.value.trim();
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

  function deviceCode(deviceName) {
    const codes = {
      Temperature: "LEDD",
      Luminosity: "LEDV",
      Humidity: "LEDX"
    };
    return codes[deviceName] || "LED-UNKNOWN";
  }

  function applyClientFilter(content, filters) {
    const deviceName = String(filters.deviceName || "").trim().toLowerCase();
    const action = String(filters.action || "").trim().toLowerCase();
    const status = String(filters.status || "").trim().toLowerCase();
    const time = String(filters.time || "").trim();

    return content.filter((item) => {
      const rowDeviceName = String(item.deviceName || "").trim().toLowerCase();
      const rowAction = String(item.action || "").trim().toLowerCase();
      const rowStatus = String(item.status || "").trim().toLowerCase();

      const deviceOk = !deviceName || rowDeviceName.includes(deviceName);
      const actionOk = !action || rowAction === action;
      const statusOk = !status || rowStatus === status;
      const timeOk = matchesTime(item.timestamp, time);

      return deviceOk && actionOk && statusOk && timeOk;
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

  function buildFilteredPage(pageData) {
    const filters = currentFilters();
    const normalizedPage = window.IOT.normalizePageData(pageData, 0);
    const filteredContent = applyClientSort(applyClientFilter(normalizedPage.content.map(window.IOT.normalizeActionRecord), filters));

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
      rows.innerHTML = `<tr><td class="empty-row" colspan="5">Không tìm thấy lịch sử thao tác phù hợp</td></tr>`;
    } else {
      normalizedPage.content.forEach((item) => {
        const actionClass = item.action === "ON" ? "action-on" : "action-off";
        rows.insertAdjacentHTML("beforeend", `
          <tr>
            <td>${deviceCode(item.deviceName)}</td>
            <td>${item.deviceName}</td>
            <td class="${actionClass}">${item.action}</td>
            <td>${window.IOT.statusBadge(item.status)}</td>
            <td>${window.IOT.timeCopyControl(item.timestamp)}</td>
          </tr>
        `);
      });
    }
    window.IOT.bindCopyTime(rows);
    summary.textContent = window.IOT.pageSummary(normalizedPage, "action", "actions");
    window.IOT.renderPagination(pagination, normalizedPage, (page) => {
      currentPage = page;
      renderTable(cachedPageData);
    }, { singular: "action", plural: "actions" });
  }

  async function loadHistory() {
    try {
      if (!cachedPageData) {
        const params = new URLSearchParams({
          page: "0",
          size: "1000",
          sortBy: "timestamp",
          sortDir
        });
        cachedPageData = await window.IOT.safeApi(`/api/actions/history?${params.toString()}`, {}, () => window.IOT.actionHistory(0, 1000, { sortDir }));
      }
      renderTable(cachedPageData);
    } catch (error) {
      console.error("Cannot load action history", error);
      rows.innerHTML = `<tr><td class="empty-row" colspan="5">Không tải được lịch sử thao tác: ${error.message || "Lỗi không xác định"}</td></tr>`;
      pagination.innerHTML = "";
      summary.textContent = "Không tải được dữ liệu";
    }
  }

  document.addEventListener("DOMContentLoaded", loadHistory);
  form.addEventListener("submit", (event) => {
    event.preventDefault();
    currentPage = 0;
    loadHistory();
  });
  form.addEventListener("change", () => {
    currentPage = 0;
    loadHistory();
  });
  timeFilter.addEventListener("change", () => {
    currentPage = 0;
    loadHistory();
  });
  timeFilter.addEventListener("keydown", (event) => {
    if (event.key !== "Enter") return;
    event.preventDefault();
    currentPage = 0;
    loadHistory();
  });
  resetButton.addEventListener("click", () => {
    form.reset();
    timeFilter.value = "";
    currentPage = 0;
    sortDir = "desc";
    loadHistory();
  });
})();
