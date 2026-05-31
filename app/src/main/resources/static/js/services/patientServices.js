import { API_BASE_URL } from "../config/config.js";

const PATIENT_API = API_BASE_URL + '/patient';

/**
 * Handle patient signup by sending patient details to the backend.
 * @param {Object} data - Patient signup details.
 * @returns {Promise<Object>} Object containing success status and message.
 */
export async function patientSignup(data) {
  try {
    const response = await fetch(`${PATIENT_API}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(data)
    });

    const result = await response.json();
    if (!response.ok) {
      throw new Error(result.message || "Failed to register patient");
    }
    return { success: response.ok, message: result.message };
  } catch (error) {
    console.error("Error :: patientSignup :: ", error);
    return { success: false, message: error.message };
  }
}

/**
 * Handle patient login by verifying credentials.
 * @param {Object} data - Object containing email and password.
 * @returns {Promise<Response>} The raw Fetch Response object.
 */
export async function patientLogin(data) {
  console.log("patientLogin :: ", data);
  return await fetch(`${PATIENT_API}/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(data)
  });
}

/**
 * Fetch patient information using their authentication token.
 * @param {string} token - Patient session token.
 * @returns {Promise<Object|null>} Patient details or null on error.
 */
export async function getPatientData(token) {
  try {
    const response = await fetch(`${PATIENT_API}/${token}`);
    const data = await response.json();
    if (response.ok) return data.patient;
    return null;
  } catch (error) {
    console.error("Error fetching patient details:", error);
    return null;
  }
}

/**
 * Fetch all appointments for a patient. Shared endpoint for doctor/patient context.
 * @param {number|string} id - Patient ID.
 * @param {string} token - Session authentication token.
 * @param {string} user - Role context ("patient" or "doctor").
 * @returns {Promise<Array|null>} List of appointments or null on error.
 */
export async function getPatientAppointments(id, token, user) {
  try {
    const response = await fetch(`${PATIENT_API}/${id}/${user}/${token}`);
    const data = await response.json();
    if (response.ok) {
      return data.appointments;
    }
    return null;
  } catch (error) {
    console.error("Error fetching patient appointments:", error);
    return null;
  }
}

/**
 * Filter patient appointments based on status/condition and name.
 * @param {string} condition - Appointment status/condition.
 * @param {string} name - Doctor or patient name query.
 * @param {string} token - Session authentication token.
 * @returns {Promise<Object>} Object containing the list of filtered appointments.
 */
export async function filterAppointments(condition, name, token) {
  try {
    const condParam = (condition !== null && condition !== undefined && condition !== "") ? condition : "null";
    const nameParam = (name !== null && name !== undefined && name !== "") ? name : "null";

    const response = await fetch(`${PATIENT_API}/filter/${condParam}/${nameParam}/${token}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    });

    if (response.ok) {
      return await response.json();
    } else {
      console.error("Failed to filter appointments:", response.statusText);
      return { appointments: [] };
    }
  } catch (error) {
    console.error("Error filtering appointments:", error);
    alert("Something went wrong!");
    return { appointments: [] };
  }
}
