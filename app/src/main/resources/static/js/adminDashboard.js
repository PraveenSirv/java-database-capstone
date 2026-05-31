import { getDoctors, saveDoctor, filterDoctors } from "./services/doctorServices.js";
import { createDoctorCard } from "./components/doctorCard.js";
import { openModal } from "./components/modals.js";

document.addEventListener("DOMContentLoaded", () => {
  const addDocBtn = document.getElementById("addDocBtn");
  if (addDocBtn) {
    addDocBtn.addEventListener("click", () => openModal("addDoctor"));
  }

  loadDoctorCards();

  const searchBar = document.getElementById("searchBar");
  const filterTime = document.getElementById("filterTime");
  const filterSpecialty = document.getElementById("filterSpecialty");

  if (searchBar) {
    searchBar.addEventListener("input", filterDoctorsOnChange);
  }
  if (filterTime) {
    filterTime.addEventListener("change", filterDoctorsOnChange);
  }
  if (filterSpecialty) {
    filterSpecialty.addEventListener("change", filterDoctorsOnChange);
  }
});

export async function loadDoctorCards() {
  try {
    const doctors = await getDoctors();
    renderDoctorCards(doctors);
  } catch (error) {
    console.error("Failed to load doctors:", error);
  }
}

export function renderDoctorCards(doctors) {
  const contentDiv = document.getElementById("content");
  if (!contentDiv) return;
  contentDiv.innerHTML = "";
  doctors.forEach((doctor) => {
    const card = createDoctorCard(doctor);
    contentDiv.appendChild(card);
  });
}

export async function filterDoctorsOnChange() {
  const searchBar = document.getElementById("searchBar");
  const filterTime = document.getElementById("filterTime");
  const filterSpecialty = document.getElementById("filterSpecialty");

  const nameVal = searchBar ? searchBar.value.trim() : "";
  const timeVal = filterTime ? filterTime.value : "";
  const specialtyVal = filterSpecialty ? filterSpecialty.value : "";

  const name = nameVal.length > 0 ? nameVal : "null";
  const time = timeVal.length > 0 ? timeVal : "null";
  const specialty = specialtyVal.length > 0 ? specialtyVal : "null";

  try {
    const response = await filterDoctors(name, time, specialty);
    const doctors = response.doctors || [];
    const contentDiv = document.getElementById("content");
    if (!contentDiv) return;
    contentDiv.innerHTML = "";

    if (doctors.length > 0) {
      doctors.forEach((doctor) => {
        const card = createDoctorCard(doctor);
        contentDiv.appendChild(card);
      });
    } else {
      contentDiv.innerHTML = "<p>No doctors found with the given filters.</p>";
    }
  } catch (error) {
    console.error("Error filtering doctors:", error);
    alert("❌ An error occurred while filtering doctors.");
  }
}

export async function adminAddDoctor(e) {
  if (e) e.preventDefault();

  const nameInput = document.getElementById("doctorName");
  const specialtySelect = document.getElementById("specialization");
  const emailInput = document.getElementById("doctorEmail");
  const passwordInput = document.getElementById("doctorPassword");
  const phoneInput = document.getElementById("doctorPhone");
  const checkboxes = document.querySelectorAll('input[name="availability"]:checked');

  if (!nameInput || !specialtySelect || !emailInput || !passwordInput || !phoneInput) return;

  const name = nameInput.value.trim();
  const specialty = specialtySelect.value;
  const email = emailInput.value.trim();
  const password = passwordInput.value;
  const phone = phoneInput.value.trim();

  const availableTimes = Array.from(checkboxes).map((cb) => cb.value);

  const token = localStorage.getItem("token");
  if (!token) {
    alert("❌ Session expired or invalid. Please log in again.");
    window.location.href = "/";
    return;
  }

  const doctor = {
    name,
    specialty,
    email,
    password,
    phone,
    availableTimes,
  };

  const res = await saveDoctor(doctor, token);
  if (res.success) {
    alert("✅ Doctor added successfully.");
    document.getElementById("modal").style.display = "none";
    loadDoctorCards();
  } else {
    alert("❌ Failed to add doctor: " + res.message);
  }
}

window.adminAddDoctor = adminAddDoctor;
window.filterDoctorsOnChange = filterDoctorsOnChange;
