import { getAllAppointments } from "./services/appointmentRecordService.js";
import { createPatientRow } from "./components/patientRows.js";

// Global variables to store dashboard state
let patientTableBody = null;
let selectedDate = null;
let token = null;
let patientName = "null";

// Utility function to get today's date in YYYY-MM-DD format
const getTodayDateStr = () => {
  const today = new Date();
  const yyyy = today.getFullYear();
  const mm = String(today.getMonth() + 1).padStart(2, "0");
  const dd = String(today.getDate()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd}`;
};

/**
 * Load appointments from the service and render them.
 */
async function loadAppointments() {
  if (!patientTableBody) return;
  try {
    const appointments = await getAllAppointments(selectedDate, patientName, token);
    patientTableBody.innerHTML = "";

    if (!appointments || appointments.length === 0) {
      patientTableBody.innerHTML = `<tr><td colspan="5" style="text-align:center;">No Appointments found for today</td></tr>`;
      return;
    }

    appointments.forEach((app) => {
      const patient = {
        id: app.patientId,
        name: app.patientName,
        phone: app.patientPhone,
        email: app.patientEmail,
      };
      const row = createPatientRow(patient, app.id, app.doctorId);
      patientTableBody.appendChild(row);
    });
  } catch (error) {
    console.error("Error loading appointments:", error);
    patientTableBody.innerHTML = `<tr><td colspan="5" style="text-align:center; color:red;">Error loading appointments. Try again later.</td></tr>`;
  }
}

// Attach page initialization event listener
document.addEventListener("DOMContentLoaded", () => {
  // Call renderContent helper function if it is defined globally
  if (typeof window.renderContent === "function") {
    window.renderContent();
  } else if (typeof renderContent === "function") {
    renderContent();
  }

  // Initialize variables referencing DOM elements and status
  patientTableBody = document.getElementById("patientTableBody");
  const searchBar = document.getElementById("searchBar");
  const todayButton = document.getElementById("todayButton");
  const datePicker = document.getElementById("datePicker");

  selectedDate = getTodayDateStr();
  token = localStorage.getItem("token");
  patientName = "null";

  if (datePicker) {
    datePicker.value = selectedDate;
  }

  // Setup search bar listener
  if (searchBar) {
    searchBar.addEventListener("input", () => {
      const val = searchBar.value.trim();
      patientName = val.length > 0 ? val : "null";
      loadAppointments();
    });
  }

  // Setup today button click listener
  if (todayButton) {
    todayButton.addEventListener("click", () => {
      selectedDate = getTodayDateStr();
      if (datePicker) {
        datePicker.value = selectedDate;
      }
      loadAppointments();
    });
  }

  // Setup date picker change listener
  if (datePicker) {
    datePicker.addEventListener("change", () => {
      selectedDate = datePicker.value;
      loadAppointments();
    });
  }

  // Perform initial fetch
  loadAppointments();
});
