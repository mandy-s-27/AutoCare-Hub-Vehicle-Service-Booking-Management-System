
const user = Auth.requireRole("CUSTOMER");

if (user) {
  document.getElementById("userChip").textContent = user.name;
  document.getElementById("pName").textContent = user.name;
  document.getElementById("pEmail").textContent = user.email;
}

document.getElementById("logoutBtn").addEventListener("click", () => Auth.logout());

const TRACK_STEPS = [
  "PENDING", "CONFIRMED", "RECEIVED", "INSPECTION_COMPLETED",
  "REPAIR_STARTED", "WAITING_FOR_SPARE_PARTS", "REPAIR_COMPLETED",
  "READY_FOR_DELIVERY", "DELIVERED"
];

let state = { vehicles: [], centers: [], services: [], bookings: [] };

// ---------- Navigation ----------
document.querySelectorAll(".sidebar nav button").forEach(btn => {
  btn.addEventListener("click", () => switchView(btn.dataset.view));
});

function switchView(view) {
  document.querySelectorAll(".sidebar nav button").forEach(b =>
    b.classList.toggle("active", b.dataset.view === view)
  );

  document.querySelectorAll(".view").forEach(v =>
    v.classList.remove("active")
  );

  document.getElementById("view-" + view).classList.add("active");

  document.getElementById("pageTitle").textContent =
    document.querySelector(`.sidebar nav button[data-view="${view}"]`).textContent;

  if (view === "vehicles") loadVehicles();
  if (view === "book") loadBookFormOptions();
  if (view === "history") loadHistory();
  if (view === "track") loadTrackOptions();
}

const globalMsg = document.getElementById("globalMsg");

// ---------- Overview ----------
async function loadOverview() {
  try {
    const [vehicles, bookings] = await Promise.all([
      apiCall("/customer/vehicles"),
      apiCall("/customer/booking/history?page=0&size=100")
    ]);

    state.vehicles = vehicles;
    state.bookings = bookings.content || [];

    document.getElementById("statVehicles").textContent = vehicles.length;
    document.getElementById("statBookings").textContent = state.bookings.length;

    const active = state.bookings.filter(
      b => !["DELIVERED", "CANCELLED", "COMPLETED"].includes(b.status)
    ).length;

    document.getElementById("statActive").textContent = active;

    const body = document.getElementById("recentBookingsBody");
    const recent = state.bookings.slice(0, 5);

    body.innerHTML = recent.length
      ? recent.map(b => `
          <tr>
            <td>${fmtDate(b.bookingDate)}</td>
            <td>${b.service?.serviceName ?? "-"}</td>
            <td>${b.vehicle?.vehicleNumber ?? "-"}</td>
            <td><span class="${statusPillClass(b.status)}">${statusLabel(b.status)}</span></td>
          </tr>
        `).join("")
      : `<tr><td colspan="4" class="empty-state">No bookings yet</td></tr>`;
  } catch (err) {
    showMessage(globalMsg, err.message);
  }
}

// ---------- Vehicles ----------
async function loadVehicles() {
  const body = document.getElementById("vehiclesBody");
  body.innerHTML = `<tr><td colspan="4">Loading…</td></tr>`;

  try {
    const vehicles = await apiCall("/customer/vehicles");
    state.vehicles = vehicles;

    body.innerHTML = vehicles.length
      ? vehicles.map(v => `
          <tr>
            <td>${v.vehicleNumber}</td>
            <td>${v.model}</td>
            <td>${v.company}</td>
            <td>${v.fuelType ?? "-"}</td>
          </tr>
        `).join("")
      : `<tr><td colspan="4" class="empty-state">No vehicles added yet</td></tr>`;
  } catch (err) {
    showMessage(globalMsg, err.message);
  }
}

document.getElementById("vehicleForm").addEventListener("submit", async (e) => {
  e.preventDefault();

  const payload = {
    vehicleNumber: document.getElementById("vNumber").value.trim(),
    model: document.getElementById("vModel").value.trim(),
    company: document.getElementById("vCompany").value.trim(),
    fuelType: document.getElementById("vFuel").value
  };

  try {
    await apiCall("/customer/vehicle", "POST", payload);
    e.target.reset();
    loadVehicles();
    showMessage(globalMsg, "Vehicle added successfully.", "success");
  } catch (err) {
    showMessage(globalMsg, err.message);
  }
});

// ---------- Book Service ----------
async function loadBookFormOptions() {
  try {
    const [vehicles, centers, services] = await Promise.all([
      apiCall("/customer/vehicles"),
      apiCall("/public/service-centers", "GET", null, false),
      apiCall("/public/services", "GET", null, false)
    ]);

    state.vehicles = vehicles;
    state.centers = centers;
    state.services = services;

    document.getElementById("bVehicle").innerHTML = vehicles.length
      ? vehicles.map(v =>
          `<option value="${v.id}">${v.vehicleNumber} - ${v.model}</option>`
        ).join("")
      : `<option value="">No vehicles - add one first</option>`;

    document.getElementById("bCenter").innerHTML =
      centers.map(c =>
        `<option value="${c.id}">${c.name} (${c.city})</option>`
      ).join("");

    document.getElementById("bService").innerHTML =
      services.map(s =>
        `<option value="${s.id}">${s.serviceName} - ${fmtCurrency(s.price)}</option>`
      ).join("");
  } catch (err) {
    showMessage(globalMsg, err.message);
  }
}

document.getElementById("bookForm").addEventListener("submit", async (e) => {
  e.preventDefault();

  const vehicleId = document.getElementById("bVehicle").value;

  if (!vehicleId) {
    showMessage(globalMsg, "Please add a vehicle before booking a service.");
    return;
  }

  // Booking details, including the customer's complaint.
  const payload = {
    vehicleId: Number(vehicleId),
    serviceCenterId: Number(document.getElementById("bCenter").value),
    serviceId: Number(document.getElementById("bService").value),
    bookingDate: document.getElementById("bDate").value,
    bookingTime: document.getElementById("bTime").value,
    complaintDescription: document.getElementById("bComplaint").value.trim()
  };

  try {
    await apiCall("/customer/booking", "POST", payload);

    e.target.reset();
    showMessage(globalMsg, "Service booked successfully!", "success");
    switchView("history");
  } catch (err) {
    showMessage(globalMsg, err.message);
  }
});

// ---------- History ----------
async function loadHistory() {
  const body = document.getElementById("historyBody");
  body.innerHTML = `<tr><td colspan="6">Loading…</td></tr>`;

  try {
    const page = await apiCall("/customer/booking/history?page=0&size=100");
    const bookings = page.content || [];
    state.bookings = bookings;

    body.innerHTML = bookings.length
      ? bookings.map(b => `
          <tr>
            <td>#${b.id}</td>
            <td>${fmtDate(b.bookingDate)} ${b.bookingTime ?? ""}</td>
            <td>${b.service?.serviceName ?? "-"}</td>
            <td>${b.serviceCenter?.name ?? "-"}</td>
            <td><span class="${statusPillClass(b.status)}">${statusLabel(b.status)}</span></td>
            <td>
              <button class="btn btn-outline btn-sm" onclick="viewInvoice(${b.id})">Invoice</button>
              ${b.status === "DELIVERED"
                ? `<button class="btn btn-secondary btn-sm" onclick="openReview(${b.id})">Review</button>`
                : ""}
              ${!["DELIVERED", "CANCELLED", "COMPLETED"].includes(b.status)
                ? `<button class="btn btn-danger btn-sm" onclick="cancelBooking(${b.id})">Cancel</button>`
                : ""}
            </td>
          </tr>
        `).join("")
      : `<tr><td colspan="6" class="empty-state">No bookings yet</td></tr>`;
  } catch (err) {
    showMessage(globalMsg, err.message);
  }
}

async function cancelBooking(id) {
  if (!confirm("Cancel this booking?")) return;

  try {
    await apiCall(`/customer/booking/${id}`, "DELETE");
    loadHistory();
  } catch (err) {
    showMessage(globalMsg, err.message);
  }
}

async function viewInvoice(bookingId) {
  try {
    const invoice = await apiCall(`/customer/booking/${bookingId}/invoice`);

    openModal(`
      <div class="modal-header">
        <h3>Invoice #${invoice.id}</h3>
        <button class="close-btn" onclick="closeModal()">✕</button>
      </div>
      <p><strong>Booking:</strong> #${bookingId}</p>
      <p><strong>GST:</strong> ${fmtCurrency(invoice.gst)}</p>
      <p><strong>Total Amount:</strong> ${fmtCurrency(invoice.totalAmount)}</p>
      <p><strong>Payment Status:</strong> ${invoice.paymentStatus}</p>
      <button class="btn btn-primary" onclick="window.print()">Download / Print</button>
    `);
  } catch (err) {
    showMessage(globalMsg, err.message);
  }
}

function openReview(bookingId) {
  openModal(`
    <div class="modal-header">
      <h3>Rate Your Service</h3>
      <button class="close-btn" onclick="closeModal()">✕</button>
    </div>
    <form id="reviewForm">
      <label>Rating (1-5)</label>
      <select id="rRating">
        <option>5</option>
        <option>4</option>
        <option>3</option>
        <option>2</option>
        <option>1</option>
      </select>
      <label>Comment</label>
      <textarea id="rComment" rows="3" placeholder="Tell us about your experience..."></textarea>
      <button type="submit" class="btn btn-primary">Submit Review</button>
    </form>
  `);

  document.getElementById("reviewForm").addEventListener("submit", async (e) => {
    e.preventDefault();

    try {
      await apiCall("/customer/review", "POST", {
        bookingId,
        rating: Number(document.getElementById("rRating").value),
        comment: document.getElementById("rComment").value.trim()
      });

      closeModal();
      showMessage(globalMsg, "Thanks for your feedback!", "success");
    } catch (err) {
      showMessage(globalMsg, err.message);
    }
  });
}

function openModal(html) {
  document.getElementById("modalBody").innerHTML = html;
  document.getElementById("modalBackdrop").classList.remove("hidden");
}

function closeModal() {
  document.getElementById("modalBackdrop").classList.add("hidden");
}

document.getElementById("modalBackdrop").addEventListener("click", (e) => {
  if (e.target.id === "modalBackdrop") closeModal();
});

// ---------- Track Service ----------
async function loadTrackOptions() {
  try {
    const page = await apiCall("/customer/booking/history?page=0&size=100");
    const bookings = (page.content || []).filter(b => b.status !== "CANCELLED");
    const select = document.getElementById("trackSelect");

    select.innerHTML = bookings.length
      ? bookings.map(b =>
          `<option value="${b.id}">#${b.id} - ${b.service?.serviceName ?? ""} (${fmtDate(b.bookingDate)})</option>`
        ).join("")
      : `<option value="">No active bookings</option>`;

    select.onchange = () =>
      renderTrack(bookings.find(b => b.id === Number(select.value)));

    if (bookings.length) {
      renderTrack(bookings[0]);
    } else {
      document.getElementById("trackResult").innerHTML =
        `<p class="empty-state">No bookings to track yet.</p>`;
    }
  } catch (err) {
    showMessage(globalMsg, err.message);
  }
}

function renderTrack(booking) {
  const result = document.getElementById("trackResult");

  if (!booking) {
    result.innerHTML = "";
    return;
  }

  if (booking.status === "COMPLETED") {
    result.innerHTML =
      `<p>This service is <strong>completed</strong>. Awaiting delivery update.</p>`;
    return;
  }

  const currentIndex = TRACK_STEPS.indexOf(booking.status);

  const stepsHtml = TRACK_STEPS.map((s, i) => {
    let cls = "step";
    if (i < currentIndex) cls += " done";
    else if (i === currentIndex) cls += " current";

    return `<div class="${cls}">
      <div class="line"></div>
      <div class="dot"></div>
      <span>${statusLabel(s)}</span>
    </div>`;
  }).join("");

  result.innerHTML = `
    <div class="stepper">${stepsHtml}</div>
    <div class="card" style="margin-top:var(--space-lg);">
      <p><strong>Vehicle:</strong> ${booking.vehicle?.vehicleNumber ?? "-"}</p>
      <p><strong>Service Center:</strong> ${booking.serviceCenter?.name ?? "-"}</p>
      <p><strong>Mechanic:</strong> ${booking.mechanic?.name ?? "Not yet assigned"}</p>
    </div>
  `;
}

loadOverview();
