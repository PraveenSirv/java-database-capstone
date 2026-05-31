// header.js

import { openModal } from "./modals.js";

export function renderHeader() {
  const headerDiv = document.getElementById("header");
  if (!headerDiv) return;

  const path = window.location.pathname;
  if (path === "/" || path.endsWith("/") || path.endsWith("/index.html")) {
    localStorage.removeItem("userRole");
    localStorage.removeItem("token");
    headerDiv.innerHTML = `
      <header class="header">
        <div class="logo-section">
          <img src="../assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
          <span class="logo-title">Hospital CMS</span>
        </div>
      </header>`;
    return;
  }

  const role = localStorage.getItem("userRole");
  const token = localStorage.getItem("token");

  if ((role === "loggedPatient" || role === "admin" || role === "doctor") && !token) {
    localStorage.removeItem("userRole");
    alert("Session expired or invalid login. Please log in again.");
    window.location.href = "/";
    return;
  }

  let headerContent = `
    <header class="header">
      <div class="logo-section" style="cursor:pointer;" onclick="window.location.href='/'">
        <img src="../assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
        <span class="logo-title">Hospital CMS</span>
      </div>
      <nav>`;

  if (role === "admin") {
    headerContent += `
      <button id="addDocBtn" class="adminBtn" onclick="openModal('addDoctor')">Add Doctor</button>
      <a href="#" onclick="logout()">Logout</a>`;
  } else if (role === "doctor") {
    headerContent += `
      <button class="adminBtn" onclick="selectRole('doctor')">Home</button>
      <a href="#" onclick="logout()">Logout</a>`;
  } else if (role === "patient") {
    headerContent += `
      <button id="patientLoginBtn" class="adminBtn">Login</button>
      <button id="patientSignupBtn" class="adminBtn">Sign Up</button>`;
  } else if (role === "loggedPatient") {
    headerContent += `
      <button id="home" class="adminBtn" onclick="window.location.href='/pages/loggedPatientDashboard.html'">Home</button>
      <button id="patientAppointments" class="adminBtn" onclick="window.location.href='/pages/patientAppointments.html'">Appointments</button>
      <a href="#" onclick="logoutPatient()">Logout</a>`;
  }

  headerContent += `</nav></header>`;
  headerDiv.innerHTML = headerContent;

  attachHeaderButtonListeners();
}

function attachHeaderButtonListeners() {
  const loginBtn = document.getElementById("patientLoginBtn");
  const signupBtn = document.getElementById("patientSignupBtn");
  if (loginBtn) {
    loginBtn.addEventListener("click", () => {
      const modal = document.getElementById("patientLoginModal");
      if (modal) modal.style.display = "block";
    });
  }
  if (signupBtn) {
    signupBtn.addEventListener("click", () => {
      const modal = document.getElementById("patientSignupModal");
      if (modal) modal.style.display = "block";
    });
  }
}

export function logout() {
  localStorage.removeItem("userRole");
  localStorage.removeItem("token");
  window.location.href = "/";
}

export function logoutPatient() {
  localStorage.removeItem("userRole");
  localStorage.removeItem("token");
  window.location.href = "/pages/patientDashboard.html";
}

export function selectRole(role) {
  if (role === "doctor") {
    const token = localStorage.getItem("token");
    window.location.href = `/doctorDashboard/${token}`;
  }
}

// Bind to window for HTML onclick attributes
window.logout = logout;
window.logoutPatient = logoutPatient;
window.selectRole = selectRole;
window.openModal = openModal;

document.addEventListener("DOMContentLoaded", renderHeader);
