// patientDashboard.js
import { getDoctors, filterDoctors } from './services/doctorServices.js';
import { openModal } from './components/modals.js';
import { createDoctorCard } from './components/doctorCard.js';
import { patientSignup, patientLogin } from './services/patientServices.js';

// Consolidated page initialization
document.addEventListener("DOMContentLoaded", () => {
  // Load initial doctor cards
  loadDoctorCards();

  // Bind login and signup triggers
  const signupBtn = document.getElementById("patientSignup");
  if (signupBtn) {
    signupBtn.addEventListener("click", () => openModal("patientSignup"));
  }

  const loginBtn = document.getElementById("patientLogin");
  if (loginBtn) {
    loginBtn.addEventListener("click", () => openModal("patientLogin"));
  }

  // Bind filter/search controls
  const searchBar = document.getElementById("searchBar");
  const filterTime = document.getElementById("filterTime");
  const filterSpecialty = document.getElementById("filterSpecialty");

  if (searchBar) searchBar.addEventListener("input", filterDoctorsOnChange);
  if (filterTime) filterTime.addEventListener("change", filterDoctorsOnChange);
  if (filterSpecialty) filterSpecialty.addEventListener("change", filterDoctorsOnChange);
});

/**
 * Render a list of doctor cards into the content container.
 * @param {Array} doctors - List of doctors to display.
 */
export function renderDoctorCards(doctors) {
  const contentDiv = document.getElementById("content");
  if (!contentDiv) return;
  contentDiv.innerHTML = "";
  doctors.forEach(doctor => {
    const card = createDoctorCard(doctor);
    contentDiv.appendChild(card);
  });
}

/**
 * Load all doctors on page load and display them.
 */
async function loadDoctorCards() {
  try {
    const doctors = await getDoctors();
    renderDoctorCards(doctors);
  } catch (error) {
    console.error("Failed to load doctors:", error);
  }
}

/**
 * Filter doctors list on input/selection change.
 */
async function filterDoctorsOnChange() {
  const searchInput = document.getElementById("searchBar");
  const timeSelect = document.getElementById("filterTime");
  const specialtySelect = document.getElementById("filterSpecialty");

  const searchVal = searchInput ? searchInput.value.trim() : "";
  const timeVal = timeSelect ? timeSelect.value : "";
  const specialtyVal = specialtySelect ? specialtySelect.value : "";

  const name = searchVal.length > 0 ? searchVal : null;
  const time = timeVal.length > 0 ? timeVal : null;
  const specialty = specialtyVal.length > 0 ? specialtyVal : null;

  try {
    const response = await filterDoctors(name, time, specialty);
    const doctors = response.doctors || [];
    const contentDiv = document.getElementById("content");
    if (!contentDiv) return;
    contentDiv.innerHTML = "";

    if (doctors.length > 0) {
      renderDoctorCards(doctors);
    } else {
      contentDiv.innerHTML = "<p>No doctors found with the given filters.</p>";
    }
  } catch (error) {
    console.error("Failed to filter doctors:", error);
    alert("❌ An error occurred while filtering doctors.");
  }
}

/**
 * Submit patient signup form. Exposed globally on window.
 */
window.signupPatient = async function () {
  try {
    const name = document.getElementById("name").value;
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;
    const phone = document.getElementById("phone").value;
    const address = document.getElementById("address").value;

    const data = { name, email, password, phone, address };
    const { success, message } = await patientSignup(data);
    if (success) {
      alert(message);
      document.getElementById("modal").style.display = "none";
      window.location.reload();
    } else {
      alert(message);
    }
  } catch (error) {
    console.error("Signup failed:", error);
    alert("❌ An error occurred while signing up.");
  }
};

/**
 * Submit patient login form. Exposed globally on window.
 */
window.loginPatient = async function () {
  try {
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    const data = { email, password };
    const response = await patientLogin(data);
    
    if (response.ok) {
      const result = await response.json();
      localStorage.setItem('token', result.token);
      if (typeof window.selectRole === "function") {
        window.selectRole('loggedPatient');
      } else if (typeof selectRole === "function") {
        selectRole('loggedPatient');
      } else {
        window.location.href = '/pages/loggedPatientDashboard.html';
      }
    } else {
      alert('❌ Invalid credentials!');
    }
  } catch (error) {
    console.error("Error during patient login:", error);
    alert("❌ Failed to Login. Please try again later.");
  }
};
