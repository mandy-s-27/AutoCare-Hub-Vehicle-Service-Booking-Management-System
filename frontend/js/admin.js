const user = Auth.requireRole("ADMIN");
if (user) document.getElementById("userChip").textContent = user.name;
document.getElementById("logoutBtn").addEventListener("click", () => Auth.logout());

const globalMsg = document.getElementById("globalMsg");
let centersCache = [];

document.querySelectorAll(".sidebar nav button").forEach(btn => {
  btn.addEventListener("click", () => switchView(btn.dataset.view));
});

function switchView(view) {
  document.querySelectorAll(".sidebar nav button").forEach(b => b.classList.toggle("active", b.dataset.view === view));
  document.querySelectorAll(".view").forEach(v => v.classList.remove("active"));
  document.getElementById("view-" + view).classList.add("active");
  document.getElementById("pageTitle").textContent = document.querySelector(`.sidebar nav button[data-view="${view}"]`).textContent;

  if (view === "overview") loadReports("reportCards");
  if (view === "customers") loadCustomers();
  if (view === "mechanics") loadMechanicsView();
  if (view === "centers") loadCenters();
  if (view === "services") loadServices();
  if (view === "bookings") loadBookings();
  if (view === "reports") loadReports("reportCards2");
}

// ---------- Reports ----------
async function loadReports(targetId) {
  const target = document.getElementById(targetId);
  target.innerHTML = `<div class="card">Loading…</div>`;
  try {
    const r = await apiCall("/admin/reports");
    const entries = [
      ["Total Customers", r.totalCustomers], ["Total Mechanics", r.totalMechanics],
      ["Service Centers", r.totalServiceCenters], ["Total Bookings", r.totalBookings],
      ["Pending", r.pendingBookings], ["Completed", r.completedBookings],
      ["Delivered", r.deliveredBookings], ["Cancelled", r.cancelledBookings]
    ];
    target.innerHTML = entries.map(([label, val]) => `
      <div class="card stat-card"><div class="stat-value">${val}</div><div class="stat-label">${label}</div></div>
    `).join("");
  } catch (err) {
    showMessage(globalMsg, err.message);
  }
}

// ---------- Customers ----------
async function loadCustomers() {
  const body = document.getElementById("customersBody");
  body.innerHTML = `<tr><td colspan="5">Loading…</td></tr>`;
  try {
    const customers = await apiCall("/admin/customers");
    body.innerHTML = customers.length ? customers.map(c => `
      <tr>
        <td>#${c.id}</td><td>${c.name}</td><td>${c.email}</td><td>${c.phone ?? "-"}</td>
        <td><button class="btn btn-danger btn-sm" onclick="deleteCustomer(${c.id})">Delete</button></td>
      </tr>`).join("") : `<tr><td colspan="5" class="empty-state">No customers yet</td></tr>`;
  } catch (err) {
    showMessage(globalMsg, err.message);
  }
}
async function deleteCustomer(id) {
  if (!confirm("Delete this customer?")) return;
  try { await apiCall(`/admin/customer/${id}`, "DELETE"); loadCustomers(); }
  catch (err) { showMessage(globalMsg, err.message); }
}

// ---------- Mechanics ----------
async function loadMechanicsView() {
  await loadCentersDropdown();
  await loadMechanics();
}
async function loadCentersDropdown() {
  try {
    centersCache = await apiCall("/admin/service-centers");
    document.getElementById("mCenter").innerHTML = `<option value="">Unassigned</option>` +
      centersCache.map(c => `<option value="${c.id}">${c.name}</option>`).join("");
  } catch (err) { showMessage(globalMsg, err.message); }
}
async function loadMechanics() {
  const body = document.getElementById("mechanicsBody");
  body.innerHTML = `<tr><td colspan="6">Loading…</td></tr>`;
  try {
    const mechanics = await apiCall("/admin/mechanics");
    body.innerHTML = mechanics.length ? mechanics.map(m => `
      <tr>
        <td>#${m.id}</td><td>${m.name}</td><td>${m.specialization ?? "-"}</td>
        <td>${m.serviceCenter?.name ?? "Unassigned"}</td><td>${m.email}</td>
        <td><button class="btn btn-danger btn-sm" onclick="deleteMechanic(${m.id})">Delete</button></td>
      </tr>`).join("") : `<tr><td colspan="6" class="empty-state">No mechanics yet</td></tr>`;
  } catch (err) { showMessage(globalMsg, err.message); }
}
document.getElementById("mechanicForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const payload = {
    name: document.getElementById("mName").value.trim(),
    specialization: document.getElementById("mSpec").value.trim(),
    phone: document.getElementById("mPhone").value.trim(),
    email: document.getElementById("mEmail").value.trim(),
    password: document.getElementById("mPassword").value,
    serviceCenterId: document.getElementById("mCenter").value ? Number(document.getElementById("mCenter").value) : null
  };
  try {
    await apiCall("/admin/mechanic", "POST", payload);
    e.target.reset();
    loadMechanics();
    showMessage(globalMsg, "Mechanic added.", "success");
  } catch (err) { showMessage(globalMsg, err.message); }
});
async function deleteMechanic(id) {
  if (!confirm("Delete this mechanic?")) return;
  try { await apiCall(`/admin/mechanic/${id}`, "DELETE"); loadMechanics(); }
  catch (err) { showMessage(globalMsg, err.message); }
}

// ---------- Service Centers ----------
async function loadCenters() {
  const body = document.getElementById("centersBody");
  body.innerHTML = `<tr><td colspan="5">Loading…</td></tr>`;
  try {
    const centers = await apiCall("/admin/service-centers");
    centersCache = centers;
    body.innerHTML = centers.length ? centers.map(c => `
      <tr>
        <td>#${c.id}</td><td>${c.name}</td><td>${c.address}</td><td>${c.city}</td>
        <td><button class="btn btn-danger btn-sm" onclick="deleteCenter(${c.id})">Delete</button></td>
      </tr>`).join("") : `<tr><td colspan="5" class="empty-state">No service centers yet</td></tr>`;
  } catch (err) { showMessage(globalMsg, err.message); }
}
document.getElementById("centerForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const payload = {
    name: document.getElementById("cName").value.trim(),
    address: document.getElementById("cAddress").value.trim(),
    city: document.getElementById("cCity").value.trim()
  };
  try {
    await apiCall("/admin/service-center", "POST", payload);
    e.target.reset();
    loadCenters();
    showMessage(globalMsg, "Service center added.", "success");
  } catch (err) { showMessage(globalMsg, err.message); }
});
async function deleteCenter(id) {
  if (!confirm("Delete this service center?")) return;
  try { await apiCall(`/admin/service-center/${id}`, "DELETE"); loadCenters(); }
  catch (err) { showMessage(globalMsg, err.message); }
}

// ---------- Services & Prices ----------
async function loadServices() {
  const body = document.getElementById("servicesBody");
  body.innerHTML = `<tr><td colspan="5">Loading…</td></tr>`;
  try {
    const services = await apiCall("/admin/services");
    body.innerHTML = services.length ? services.map(s => `
      <tr>
        <td>#${s.id}</td><td>${s.serviceName}</td><td>${fmtCurrency(s.price)}</td><td>${s.duration ?? "-"} min</td>
        <td><button class="btn btn-danger btn-sm" onclick="deleteService(${s.id})">Delete</button></td>
      </tr>`).join("") : `<tr><td colspan="5" class="empty-state">No services yet</td></tr>`;
  } catch (err) { showMessage(globalMsg, err.message); }
}
document.getElementById("serviceForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const payload = {
    serviceName: document.getElementById("sName").value.trim(),
    price: Number(document.getElementById("sPrice").value),
    duration: document.getElementById("sDuration").value ? Number(document.getElementById("sDuration").value) : null
  };
  try {
    await apiCall("/admin/service", "POST", payload);
    e.target.reset();
    loadServices();
    showMessage(globalMsg, "Service added.", "success");
  } catch (err) { showMessage(globalMsg, err.message); }
});
async function deleteService(id) {
  if (!confirm("Delete this service?")) return;
  try { await apiCall(`/admin/service/${id}`, "DELETE"); loadServices(); }
  catch (err) { showMessage(globalMsg, err.message); }
}

// ---------- All Bookings ----------
async function loadBookings() {
  const body = document.getElementById("bookingsBody");
  body.innerHTML = `<tr><td colspan="8">Loading…</td></tr>`;
  try {
    const [page, mechanics] = await Promise.all([
      apiCall("/admin/all-bookings?page=0&size=100"),
      apiCall("/admin/mechanics")
    ]);
    const bookings = page.content || [];
    body.innerHTML = bookings.length ? bookings.map(b => `
      <tr>
        <td>#${b.id}</td>
        <td>${b.customer?.name ?? "-"}</td>
        <td>${b.vehicle?.vehicleNumber ?? "-"}</td>
        <td>${b.service?.serviceName ?? "-"}</td>
        <td>${b.serviceCenter?.name ?? "-"}</td>
        <td><span class="${statusPillClass(b.status)}">${statusLabel(b.status)}</span></td>
        <td>${b.mechanic?.name ?? "Unassigned"}</td>
        <td>
          ${!b.mechanic ? `<button class="btn btn-primary btn-sm" onclick="openAssign(${b.id}, ${JSON.stringify(mechanics).replace(/"/g, '&quot;')})">Assign</button>` : "-"}
        </td>
      </tr>`).join("") : `<tr><td colspan="8" class="empty-state">No bookings yet</td></tr>`;
  } catch (err) { showMessage(globalMsg, err.message); }
}

function openAssign(bookingId, mechanics) {
  const options = mechanics.map(m => `<option value="${m.id}">${m.name} - ${m.specialization ?? ""}</option>`).join("");
  openModal(`
    <div class="modal-header"><h3>Assign Mechanic - Booking #${bookingId}</h3><button class="close-btn" onclick="closeModal()">✕</button></div>
    <form id="assignForm">
      <label>Mechanic</label>
      <select id="assignMechanic">${options || "<option value=''>No mechanics available</option>"}</select>
      <button type="submit" class="btn btn-primary">Assign</button>
    </form>
  `);
  document.getElementById("assignForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    try {
      await apiCall("/admin/booking/assign-mechanic", "PUT", {
        bookingId, mechanicId: Number(document.getElementById("assignMechanic").value)
      });
      closeModal();
      loadBookings();
      showMessage(globalMsg, "Mechanic assigned.", "success");
    } catch (err) { showMessage(globalMsg, err.message); }
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

loadReports("reportCards");
