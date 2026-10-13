(function () {
  const resourceMeta = [
    ["githubLink", "GitHub", "Source code"],
    ["figmaLink", "Figma UI", "Thiết kế giao diện"],
    ["apiDocsLink", "Postman API Docs", "Tài liệu API"],
    ["baocaoLink", "Báo cáo PDF", "Tài liệu đồ án"]
  ];

  function setText(id, value) {
    const element = document.getElementById(id);
    if (!element) return;
    element.textContent = value || "--";
  }

  function renderProfile(profile) {
    setText("fullName", profile.fullName);
    setText("role", profile.role);
    setText("studentId", profile.studentId);
    setText("className", profile.className);
    setText("institution", profile.institution);
    setText("aboutMe", profile.aboutMe);
    setText("phone", profile.contact?.phone);
    setText("email", profile.contact?.email);
    setText("address", profile.contact?.address);

    const resources = document.getElementById("resources");
    resources.innerHTML = "";
    resourceMeta.forEach(([key, title, desc]) => {
      const href = profile.resources?.[key] || "#";
      resources.insertAdjacentHTML("beforeend", `
        <a class="resource-card" href="${href}" target="_blank" rel="noopener noreferrer">
          <strong>${title}</strong>
          <span>${desc}</span>
        </a>
      `);
    });
  }

  document.addEventListener("DOMContentLoaded", async () => {
    const profile = await window.IOT.safeApi("/api/users/profile", {}, window.IOT.mockProfile);
    renderProfile(profile);
  });
})();
