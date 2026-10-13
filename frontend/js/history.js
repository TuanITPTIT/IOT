(function () {
  const form = document.getElementById("historyFilterForm");
  const rows = document.getElementById("historyRows");
  const summary = document.getElementById("historySummary");
  const pagination = document.getElementById("historyPagination");
  const timeFilter = document.getElementById("timeFilter");
  const resetButton = document.getElementById("resetHistoryBtn");
  let currentPage = 0;
  let sortDir = "desc";

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

  function deviceCode(deviceName) {
    const codes = {
      Temperature: "LEDD",
      Luminosity: "LEDV",
      Humidity: "LEDX"
    };
    return codes[deviceName] || "LED-UNKNOWN";
  }

  function buildFilteredPage(pageData) {
    const normalizedPage = window.IOT.normalizePageData(pageData, currentPage);
    return {
      ...normalizedPage,
      content: normalizedPage.content.map(window.IOT.normalizeActionRecord)
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
            <td>${window.IOT.formatTime(item.timestamp)}</td>
          </tr>
        `);
      });
    }
    summary.textContent = window.IOT.pageSummary(normalizedPage, "action", "actions");
    window.IOT.renderPagination(pagination, normalizedPage, (page) => {
      currentPage = page;
      loadHistory();
    }, { singular: "action", plural: "actions" });
  }

  async function loadHistory() {
    try {
      const filters = currentFilters();
      const params = new URLSearchParams(filters);
      const keyword = params.get("keyword");
      if (keyword) {
        params.delete("keyword");
        if (!params.get("deviceName")) {
          params.set("deviceName", keyword.trim());
        }
      }
      const pageData = await window.IOT.safeApi(`/api/actions/history?${params.toString()}`, {}, () => window.IOT.actionHistory(currentPage, window.IOT.config.PAGE_SIZE, filters));
      renderTable(pageData);
    } catch (error) {
      rows.innerHTML = `<tr><td class="empty-row" colspan="5">Không tải được lịch sử thao tác. Kiểm tra backend/API rồi tải lại trang.</td></tr>`;
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
    loadHistory();
  });
})();
