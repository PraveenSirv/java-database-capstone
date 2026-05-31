import { openModal } from "../components/modals.js";
import { API_BASE_URL } from "../config/config.js";

const ADMIN_API = API_BASE_URL + '/admin';
const DOCTOR_API = API_BASE_URL + '/doctor/login';

window.onload = function () {
  const adminBtn = document.getElementById('adminLogin');
  const doctorBtn = document.getElementById('doctorLogin');

  if (adminBtn) {
    adminBtn.addEventListener('click', () => {
      openModal('adminLogin');
    });
  }

  if (doctorBtn) {
    doctorBtn.addEventListener('click', () => {
      openModal('doctorLogin');
    });
  }
};

window.selectRole = function (role) {
  const token = localStorage.getItem("token");
  localStorage.setItem("userRole", role);
  if (role === "admin") {
    window.location.href = `/adminDashboard/${token}`;
  } else if (role === "doctor") {
    window.location.href = `/doctorDashboard/${token}`;
  } else if (role === "loggedPatient") {
    window.location.href = `/pages/loggedPatientDashboard.html`;
  } else if (role === "patient") {
    window.location.href = `/pages/patientDashboard.html`;
  }
};

window.adminLoginHandler = async function () {
  const usernameInput = document.getElementById("username");
  const passwordInput = document.getElementById("password");

  if (!usernameInput || !passwordInput) return;

  const username = usernameInput.value.trim();
  const password = passwordInput.value;

  const admin = { username, password };

  try {
    const response = await fetch(ADMIN_API, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(admin)
    });

    if (response.ok) {
      const result = await response.json();
      localStorage.setItem("token", result.token);
      window.selectRole("admin");
    } else {
      alert("Invalid credentials!");
    }
  } catch (error) {
    console.error("Error during admin login:", error);
    alert("Invalid credentials!");
  }
};

window.doctorLoginHandler = async function () {
  const emailInput = document.getElementById("email");
  const passwordInput = document.getElementById("password");

  if (!emailInput || !passwordInput) return;

  const email = emailInput.value.trim();
  const password = passwordInput.value;

  const doctor = { email, password };

  try {
    const response = await fetch(DOCTOR_API, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(doctor)
    });

    if (response.ok) {
      const result = await response.json();
      localStorage.setItem("token", result.token);
      window.selectRole("doctor");
    } else {
      alert("Invalid credentials!");
    }
  } catch (error) {
    console.error("Error during doctor login:", error);
    alert("Invalid credentials!");
  }
};
