import { showBookingOverlay } from '../loggedPatient.js';
import { deleteDoctor } from '../services/doctorServices.js';
import { getPatientData } from '../services/patientServices.js';

export function createDoctorCard(doctor) {
  const card = document.createElement("div");
  card.classList.add("doctor-card");

  const role = localStorage.getItem("userRole");
  const token = localStorage.getItem("token");

  const infoDiv = document.createElement("div");
  infoDiv.classList.add("doctor-info");

  const nameH3 = document.createElement("h3");
  nameH3.innerHTML = `Dr. <span>${doctor.name}</span>`;

  const specialtyP = document.createElement("p");
  specialtyP.innerHTML = `Specialty: <strong>${doctor.specialty}</strong>`;

  const emailP = document.createElement("p");
  emailP.innerHTML = `Email: <strong>${doctor.email}</strong>`;

  const timesP = document.createElement("p");
  timesP.innerHTML = `Available: <strong>${doctor.availableTimes.join(', ')}</strong>`;

  infoDiv.appendChild(nameH3);
  infoDiv.appendChild(specialtyP);
  infoDiv.appendChild(emailP);
  infoDiv.appendChild(timesP);

  const actionsDiv = document.createElement("div");
  actionsDiv.classList.add("card-actions");

  if (role === "admin") {
    const deleteBtn = document.createElement("button");
    deleteBtn.textContent = "Delete";
    deleteBtn.addEventListener("click", async () => {
      if (confirm(`Are you sure you want to delete Dr. ${doctor.name}?`)) {
        const res = await deleteDoctor(doctor.id, token);
        if (res.success) {
          alert(res.message || "Doctor deleted successfully.");
          card.remove();
        } else {
          alert("❌ Failed to delete doctor: " + res.message);
        }
      }
    });
    actionsDiv.appendChild(deleteBtn);
  } else if (role === "loggedPatient") {
    const bookBtn = document.createElement("button");
    bookBtn.textContent = "Book Now";
    bookBtn.addEventListener("click", async (e) => {
      if (!token) {
        alert("Session expired. Please log in again.");
        window.location.href = "/";
        return;
      }
      const patient = await getPatientData(token);
      if (patient) {
        showBookingOverlay(e, doctor, patient);
      } else {
        alert("Failed to fetch patient details.");
      }
    });
    actionsDiv.appendChild(bookBtn);
  } else {
    // Guest/not logged in patient
    const bookBtn = document.createElement("button");
    bookBtn.textContent = "Book Now";
    bookBtn.addEventListener("click", () => {
      alert("Please log in first to book an appointment.");
      const loginModal = document.getElementById("patientLoginModal");
      if (loginModal) {
        loginModal.style.display = "block";
      } else {
        window.location.href = "/";
      }
    });
    actionsDiv.appendChild(bookBtn);
  }

  card.appendChild(infoDiv);
  card.appendChild(actionsDiv);

  return card;
}
