const user = Auth.requireRole("MECHANIC");
if (user) document.getElementById("userChip").textContent = user.name;
document.getElementById("logoutBtn").addEventListener("click", () => Auth.logout());

const STATUS_FLOW = [
  "RECEIVED", "INSPECTION_COMPLETED", "REPAIR_STARTED",
  "WAITING_FOR_SPARE_PARTS", "REPAIR_COMPLETED", "READY_FOR_DELIVERY", "DELIVERED"
];

const globalMsg = document.getElementById("globalMsg");

document.querySelectorAll(".sidebar nav button").forEach(btn => {
  btn.addEventListener("click", () => switchView(btn.dataset.view));
});

function switchView(view) {
  document.querySelectorAll(".sidebar nav button").forEach(b => b.classList.toggle("active", b.dataset.view === view));
  document.querySelectorAll(".view").forEach(v => v.classList.remove("active"));
  document.getElementById("view-" + view).classList.add("active");
  document.getElementById("pageTitle").textContent = document.querySelector(`.sidebar nav button[data-view="${view}"]`).textContent;
  if (view === "jobs") loadJobs();
  if (view === "completed") loadCompleted();
}

async function loadJobs() {
  const body = document.getElementById("jobsBody");
  body.innerHTML = `<tr><td colspan="6">Loading…</td></tr>`;
  try {
    const jobs = await apiCall("/mechanic/assigned-bookings");
    const active = jobs.filter(j => j.status !== "DELIVERED" && j.status !== "CANCELLED");
    body.innerHTML = active.length ? active.map(b => `
      <tr>
        <td>#${b.id}</td>
        <td>${b.vehicle?.vehicleNumber ?? "-"} (${b.vehicle?.model ?? ""})</td>
        <td>${b.service?.serviceName ?? "-"}</td>
        <td>${fmtDate(b.bookingDate)}</td>
        <td><span class="${statusPillClass(b.status)}">${statusLabel(b.status)}</span></td>
        <td>
          <button class="btn btn-primary btn-sm" onclick="openUpdate(${b.id}, '${b.status}')">Update Status</button>
          <button class="btn btn-outline btn-sm" onclick="openNote(${b.id})">Add Note</button>
        </td>
      </tr>`).join("") : `<tr><td colspan="6" class="empty-state">No active jobs assigned right now</td></tr>`;
  } catch (err) {
    showMessage(globalMsg, err.message);
  }
}

async function loadCompleted() {
  const body = document.getElementById("completedBody");
  body.innerHTML = `<tr><td colspan="5">Loading…</td></tr>`;
  try {
    const jobs = await apiCall("/mechanic/completed-services");
    body.innerHTML = jobs.length ? jobs.map(b => `
      <tr>
        <td>#${b.id}</td>
        <td>${b.vehicle?.vehicleNumber ?? "-"}</td>
        <td>${b.service?.serviceName ?? "-"}</td>
        <td>${fmtDate(b.bookingDate)}</td>
        <td><span class="${statusPillClass(b.status)}">${statusLabel(b.status)}</span></td>
      </tr>`).join("") : `<tr><td colspan="5" class="empty-state">No completed jobs yet</td></tr>`;
  } catch (err) {
    showMessage(globalMsg, err.message);
  }
}

function openUpdate(bookingId, currentStatus) {
  const options = STATUS_FLOW.map(s => `<option value="${s}" ${s === currentStatus ? "selected" : ""}>${statusLabel(s)}</option>`).join("");
  openModal(`
    <div class="modal-header"><h3>Update Status - #${bookingId}</h3><button class="close-btn" onclick="closeModal()">✕</button></div>
    <form id="statusForm">
      <label>New Status</label>
      <select id="newStatus">${options}</select>
      <button type="submit" class="btn btn-primary">Save</button>
    </form>
  `);
  document.getElementById("statusForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    try {
      await apiCall("/mechanic/booking/status", "PUT", {
        bookingId, status: document.getElementById("newStatus").value
      });
      closeModal();
      loadJobs();
      showMessage(globalMsg, "Status updated.", "success");
    } catch (err) {
      showMessage(globalMsg, err.message);
    }
  });
}

function openNote(bookingId) {
  openModal(`
    <div class="modal-header"><h3>Add Repair Note - #${bookingId}</h3><button class="close-btn" onclick="closeModal()">✕</button></div>
    <form id="noteForm">
      <label>Note</label>
      <textarea id="noteText" rows="4" placeholder="Describe the repair work / findings..."></textarea>
      <label>Image URL (optional)</label>
      <input id="noteImage" placeholder="https://...">
      <button type="submit" class="btn btn-primary">Save Note</button>
    </form>
  `);
  document.getElementById("noteForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    try {
      await apiCall("/mechanic/repair-note", "POST", {
        bookingId,
        note: document.getElementById("noteText").value.trim(),
        imageUrl: document.getElementById("noteImage").value.trim() || null
      });
      closeModal();
      showMessage(globalMsg, "Repair note added.", "success");
    } catch (err) {
      showMessage(globalMsg, err.message);
    }
  });
}

function openModal(html) {
  document.getElementById("modalBody").innerHTML = html;
  document.getElementById("modalBackdrop").classList.remove("hidden");
}
function closeModal() { document.getElementById("modalBackdrop").classList.add("hidden"); }
document.getElementById("modalBackdrop").addEventListener("click", (e) => {
  if (e.target.id === "modalBackdrop") closeModal();
});

loadJobs();
