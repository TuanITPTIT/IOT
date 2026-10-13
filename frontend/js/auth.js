(function () {
  const form = document.getElementById("loginForm");
  const message = document.getElementById("loginMessage");

  if (localStorage.getItem("iot_token") || sessionStorage.getItem("iot_token")) {
    window.location.href = "dashboard.html";
  }

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    message.textContent = "";
    const submitBtn = form.querySelector("button");
    submitBtn.disabled = true;
    submitBtn.textContent = "Đang đăng nhập...";

    const username = form.username.value.trim();
    const password = form.password.value;

    try {
      const response = await fetch(`${window.APP_CONFIG.API_BASE_URL}/api/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password })
      });
      if (!response.ok) throw new Error("Tên đăng nhập hoặc mật khẩu không chính xác");
      const payload = await response.json();
      const data = payload.data || payload;
      localStorage.setItem("iot_token", data.token);
      localStorage.setItem("iot_user", JSON.stringify(data.user || { username }));
      window.location.href = "dashboard.html";
    } catch (error) {
      if (window.APP_CONFIG.USE_MOCK_WHEN_API_FAILS && username === "admin" && password === "password123") {
        localStorage.setItem("iot_token", "mock-jwt-token");
        localStorage.setItem("iot_user", JSON.stringify({ id: 1, username: "admin", fullName: "Lê Quốc Tuấn", role: "ROLE_ADMIN" }));
        window.location.href = "dashboard.html";
        return;
      }
      message.textContent = error.message || "Tên đăng nhập hoặc mật khẩu không chính xác";
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = "Đăng nhập";
    }
  });
})();
