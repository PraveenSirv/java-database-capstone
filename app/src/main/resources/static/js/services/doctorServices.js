import { API_BASE_URL } from "../config/config.js";

const DOCTOR_API = API_BASE_URL + "/doctor";

/**
 * Fetch all available doctors from the backend database.
 * @returns {Promise<Array>} A promise that resolves to an array of doctor objects.
 */
export async function getDoctors() {
  try {
    const response = await fetch(DOCTOR_API);
    const result = await response.json();
    return result.doctors || [];
  } catch (error) {
    console.error("Error fetching doctors:", error);
    return [];
  }
}

/**
 * Delete a doctor from the backend system. Requires Admin token.
 * @param {string|number} id - Doctor ID.
 * @param {string} token - Admin authentication token.
 * @returns {Promise<Object>} An object indicating success status and message.
 */
export async function deleteDoctor(id, token) {
  try {
    const response = await fetch(`${DOCTOR_API}/delete/${id}/${token}`, {
      method: "DELETE",
    });
    const result = await response.json();
    return {
      success: response.ok,
      message: result.message,
    };
  } catch (error) {
    console.error("Error deleting doctor:", error);
    return {
      success: false,
      message: "Network error occurred.",
    };
  }
}

/**
 * Save (create or add) a new doctor in the backend. Requires Admin token.
 * @param {Object} doctor - Doctor details object.
 * @param {string} token - Admin authentication token.
 * @returns {Promise<Object>} An object indicating success status and message.
 */
export async function saveDoctor(doctor, token) {
  try {
    const response = await fetch(`${DOCTOR_API}/${token}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(doctor),
    });
    const result = await response.json();
    return {
      success: response.ok,
      message: result.message,
    };
  } catch (error) {
    console.error("Error saving doctor:", error);
    return {
      success: false,
      message: "Network error occurred.",
    };
  }
}

/**
 * Retrieve filtered list of doctors matching name, time, and specialty.
 * @param {string} name - Name filter.
 * @param {string} time - Time filter.
 * @param {string} specialty - Specialty filter.
 * @returns {Promise<Object>} Object containing the list of filtered doctors.
 */
export async function filterDoctors(name, time, specialty) {
  try {
    const nameParam = (name !== null && name !== undefined && name !== "") ? name : "null";
    const timeParam = (time !== null && time !== undefined && time !== "") ? time : "null";
    const specialtyParam = (specialty !== null && specialty !== undefined && specialty !== "") ? specialty : "null";

    const url = `${DOCTOR_API}/filter/${nameParam}/${timeParam}/${specialtyParam}`;
    const response = await fetch(url);
    if (response.ok) {
      return await response.json();
    } else {
      console.error("Failed to filter doctors:", response.statusText);
      return { doctors: [] };
    }
  } catch (error) {
    console.error("Error filtering doctors:", error);
    alert("Something went wrong while filtering doctors!");
    return { doctors: [] };
  }
}
