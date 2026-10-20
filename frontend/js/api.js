/* ============================================================
   Core API client
   Backend runs at http://localhost:8080 (Spring Boot + PostgreSQL).
   Frontend is served separately (e.g. VS Code Live Server on
   http://localhost:5500) - both are "localhost", different ports.
   ============================================================ */
const API_BASE = "http://localhost:8080/api";

const Auth = {
  getToken() { return localStorage.getItem("vs_token"); },
  getUser() {
    const raw = localStorage.getItem("vs_user");
    return raw ? JSON.parse(raw) : null;
  },
  setSession(authResponse) {
    localStorage.setItem("vs_token", authResponse.token);
    localStorage.setItem("vs_user", JSON.stringify({
      id: authResponse.id,
      name: authResponse.name,
      email: authResponse.email,
      role: authResponse.role
    }));
  },
  clear() {
    localStorage.removeItem("vs_token");
    localStorage.removeItem("vs_user");
  },
  requireRole(role) {
    const user = this.getUser();
    if (!user || !this.getToken()) {
      window.location.href = "index.html";
      return null;
    }
    if (user.role !== role) {
      window.location.href = "index.html";
      return null;
    }
    return user;
  },
  logout() {
    this.clear();
    window.location.href = "../index.html";
  }
};

/**
 * Generic API call helper.
 * @param {string} path - e.g. "/customer/vehicles"
 * @param {string} method
 * @param {object|null} body
 * @param {boolean} auth - attach bearer token
 */
async function apiCall(path, method = "GET", body = null, auth = true) {
  const headers = { "Content-Type": "application/json" };
  if (auth) {
    const token = Auth.getToken();
    if (token) headers["Authorization"] = "Bearer " + token;
  }

  const res = await fetch(API_BASE + path, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined
  });

  let data = null;
  const text = await res.text();
  try { data = text ? JSON.parse(text) : null; } catch (e) { data = text; }

  if (!res.ok) {
    if (res.status === 401 || res.status === 403) {
      // token invalid/expired or wrong role - send back to login
      if (path !== "/auth/login") {
        Auth.clear();
      }
    }
    const message = (data && data.message) ? data.message : `Request failed (${res.status})`;
    throw new Error(message);
  }
  return data;
}

function fmtDate(d) {
  if (!d) return "-";
  return new Date(d).toLocaleDateString(undefined, { year: "numeric", month: "short", day: "numeric" });
}

function fmtCurrency(n) {
  if (n === null || n === undefined) return "-";
  return "₹" + Number(n).toFixed(2);
}

function statusLabel(status) {
  return status.replace(/_/g, " ").toLowerCase().replace(/\b\w/g, c => c.toUpperCase());
}

function statusPillClass(status) {
  return "pill pill-" + status.toLowerCase();
}

function showMessage(containerEl, text, type = "error") {
  containerEl.innerHTML = `<div class="msg msg-${type}">${text}</div>`;
}
