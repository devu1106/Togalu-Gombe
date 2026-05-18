// GLOBAL APPLICATION STATE
let db, auth;
let unsubscribeList = [];
let activeSection = 'section-dashboard';
let currentEditId = null; // null for add, string for edit
let currentModelType = null; // 'plays', 'scenes', 'puppets', 'artists', 'history'

// DOM ELEMENTS
const setupScreen = document.getElementById('setup-screen');
const loginScreen = document.getElementById('login-screen');
const appShell = document.getElementById('app-shell');
const setupForm = document.getElementById('setup-form');
const loginForm = document.getElementById('login-form');
const toastContainer = document.getElementById('toast-container');
const userDisplayEmail = document.getElementById('user-display-email');
const dbConnectionBadge = document.getElementById('db-connection-badge');

// IN-MEMORY CACHE FOR FOREIGN FIELDS (e.g. Scenes dropdown selection)
let cachePlays = [];

// ==========================================
// 1. INITIALIZATION & FIREBASE SETUP
// ==========================================
document.addEventListener('DOMContentLoaded', () => {
  initFirebase();
  setupNavigation();
  setupGlobalEventListeners();
});

function showToast(message, type = 'success') {
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  let icon = 'fa-circle-check';
  if (type === 'danger') icon = 'fa-circle-exclamation';
  if (type === 'warning') icon = 'fa-triangle-exclamation';
  if (type === 'info') icon = 'fa-circle-info';
  
  toast.innerHTML = `
    <i class="fa-solid ${icon}"></i>
    <span>${message}</span>
  `;
  toastContainer.appendChild(toast);
  
  setTimeout(() => {
    toast.style.animation = 'slideIn 0.35s reverse forwards';
    setTimeout(() => toast.remove(), 350);
  }, 4000);
}

function initFirebase() {
  let savedConfig = localStorage.getItem('firebaseConfig');
  
  if (!savedConfig) {
    // Automatically pre-populate the provided Firebase credentials for plug-and-play ease
    const defaultConfig = {
      apiKey: "AIzaSyBehH8uM42NjhhU3K8LAAK7eKDU5lojCMk",
      authDomain: "togalu-gombe-ai-guide.firebaseapp.com",
      projectId: "togalu-gombe-ai-guide",
      storageBucket: "togalu-gombe-ai-guide.firebasestorage.app",
      messagingSenderId: "711091015135",
      appId: "1:711091015135:android:e8496a1096957ce4ea886a"
    };
    localStorage.setItem('firebaseConfig', JSON.stringify(defaultConfig));
    savedConfig = JSON.stringify(defaultConfig);
  }
  
  try {
    const config = JSON.parse(savedConfig);
    // Initialize Firebase Compat SDK (Bypasses Browser ES Module Blocks!)
    if (!firebase.apps.length) {
      firebase.initializeApp(config);
    }
    
    db = firebase.firestore();
    auth = firebase.auth();
    
    setupScreen.classList.add('hidden');
    
    // Listen for Auth changes
    auth.onAuthStateChanged(user => {
      if (user) {
        // Authenticated! Show app shell
        loginScreen.classList.add('hidden');
        appShell.classList.remove('hidden');
        userDisplayEmail.textContent = user.email;
        dbConnectionBadge.innerHTML = `<i class="fa-solid fa-circle-check"></i> Connected`;
        dbConnectionBadge.classList.remove('disconnected');
        
        showToast(`Welcome back, ${user.email}!`, 'success');
        startRealtimeListeners();
      } else {
        // Not authenticated. Show login screen
        loginScreen.classList.remove('hidden');
        appShell.classList.add('hidden');
        stopRealtimeListeners();
      }
    });
    
  } catch (error) {
    console.error("Firebase init failed:", error);
    showToast("Invalid Firebase configuration saved! resetting configuration.", "danger");
    localStorage.removeItem('firebaseConfig');
    setTimeout(() => window.location.reload(), 1500);
  }
}

// Reconfigure credentials listener
document.getElementById('btn-reset-db').addEventListener('click', () => {
  if (confirm("Are you sure you want to reconfigure the Firebase Database credentials?")) {
    localStorage.removeItem('firebaseConfig');
    window.location.reload();
  }
});

// Setup Form Submission
setupForm.addEventListener('submit', (e) => {
  e.preventDefault();
  const config = {
    apiKey: document.getElementById('setup-apiKey').value.trim(),
    authDomain: document.getElementById('setup-authDomain').value.trim(),
    projectId: document.getElementById('setup-projectId').value.trim(),
    storageBucket: document.getElementById('setup-storageBucket').value.trim(),
    messagingSenderId: document.getElementById('setup-messagingSenderId').value.trim(),
    appId: document.getElementById('setup-appId').value.trim()
  };
  
  localStorage.setItem('firebaseConfig', JSON.stringify(config));
  showToast("Firebase Config saved successfully!", "success");
  setTimeout(() => window.location.reload(), 1000);
});

// Login Form Submission
loginForm.addEventListener('submit', (e) => {
  e.preventDefault();
  const email = document.getElementById('login-email').value.trim();
  const password = document.getElementById('login-password').value.trim();
  
  const submitBtn = document.getElementById('btn-login-submit');
  const btnLabel = submitBtn.querySelector('.btn-label');
  const spinner = submitBtn.querySelector('.spinner');
  
  btnLabel.classList.add('hidden');
  spinner.classList.remove('hidden');
  submitBtn.disabled = true;
  
  auth.signInWithEmailAndPassword(email, password)
    .catch(err => {
      console.error(err);
      showToast(err.message, "danger");
      btnLabel.classList.remove('hidden');
      spinner.classList.add('hidden');
      submitBtn.disabled = false;
    });
});

// Logout action
document.getElementById('btn-logout').addEventListener('click', () => {
  if (confirm("Are you sure you want to sign out?")) {
    auth.signOut().then(() => {
      showToast("Signed out successfully", "info");
    });
  }
});

// ==========================================
// 2. NAVIGATION & RESPONSIVENESS
// ==========================================
function setupNavigation() {
  const sidebar = document.querySelector('.sidebar');
  const toggleBtn = document.getElementById('btn-sidebar-toggle');
  const menuItems = document.querySelectorAll('.menu-item');
  const pageTitle = document.getElementById('current-page-title');
  
  // Mobile sidebar toggle
  toggleBtn.addEventListener('click', () => {
    sidebar.classList.toggle('open');
  });
  
  // Section Navigation switching
  menuItems.forEach(item => {
    item.addEventListener('click', (e) => {
      e.preventDefault();
      
      // Remove active states
      menuItems.forEach(m => m.classList.remove('active'));
      
      // Add active state to selected
      item.classList.add('active');
      
      // Swap content sections
      const targetSectionId = item.getAttribute('data-target');
      document.querySelectorAll('.content-section').forEach(section => {
        section.classList.add('hidden');
      });
      document.getElementById(targetSectionId).classList.remove('hidden');
      
      // Update page title
      const label = item.querySelector('span').textContent;
      pageTitle.textContent = label;
      activeSection = targetSectionId;
      
      // Close mobile sidebar
      sidebar.classList.remove('open');
      
      // Log analytic
      logActivity("View Navigated", `Opened ${label} view.`);
    });
  });
}

// Activity Logging simulation
function logActivity(action, description) {
  const timeline = document.getElementById('dashboard-recent-activity');
  if (!timeline) return;
  
  const now = new Date();
  const timeStr = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  
  const item = document.createElement('div');
  item.className = 'timeline-item';
  item.innerHTML = `
    <div class="timeline-bullet bg-primary-gold"></div>
    <div class="timeline-body">
      <span class="timeline-title">${action}</span>
      <span class="timeline-desc">${description}</span>
      <span class="timeline-time">${timeStr}</span>
    </div>
  `;
  timeline.prepend(item);
  
  // Cap at 6 logs
  while (timeline.children.length > 6) {
    timeline.lastChild.remove();
  }
}

// ==========================================
// 3. REAL-TIME SYNCRONIZATION / FIRESTORE
// ==========================================
function startRealtimeListeners() {
  stopRealtimeListeners(); // Safe double-check
  
  const collectionsList = ['plays', 'scenes', 'puppets', 'artists', 'history'];
  
  collectionsList.forEach(col => {
    const unsub = db.collection(col).onSnapshot(snapshot => {
      // 1. Update stats in dashboard
      const statElem = document.getElementById(`stat-${col}`);
      if (statElem) statElem.textContent = snapshot.size;
      
      // Cache plays specifically for drop-down references in scenes
      if (col === 'plays') {
        cachePlays = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
        populateScenesFilterDropdown();
      }
      
      // 2. Render grids
      renderGrid(col, snapshot);
      
    }, err => {
      console.error(`Firestore real-time error on ${col}:`, err);
      showToast(`Database error loading ${col}: ${err.message}`, "danger");
      
      if (err.code === "permission-denied") {
        dbConnectionBadge.innerHTML = `<i class="fa-solid fa-triangle-exclamation"></i> Sync Issue`;
        dbConnectionBadge.className = "db-status disconnected";
      }
    });
    
    unsubscribeList.push(unsub);
  });
}

function stopRealtimeListeners() {
  unsubscribeList.forEach(unsub => unsub());
  unsubscribeList = [];
}

// Populates dropdown in Scenes Filter
function populateScenesFilterDropdown() {
  const filterSelect = document.getElementById('filter-scene-play');
  if (!filterSelect) return;
  
  const currentVal = filterSelect.value;
  filterSelect.innerHTML = `<option value="all">-- All Plays --</option>`;
  
  cachePlays.forEach(play => {
    filterSelect.innerHTML += `<option value="${play.id}">${play.title}</option>`;
  });
  
  filterSelect.value = currentVal;
}

// Handle Scene Play dropdown filter
document.getElementById('filter-scene-play')?.addEventListener('change', (e) => {
  const playId = e.target.value;
  logActivity("Filter Applied", `Filtering scenes by play ID: ${playId}`);
  
  // Reload snapshot visually
  db.collection('scenes').get().then(snapshot => renderGrid('scenes', snapshot, playId));
});

// ==========================================
// 4. RENDERING PANELS AND GRIDS
// ==========================================
function renderGrid(model, snapshot, filterId = 'all') {
  const container = document.getElementById(`grid-${model}`);
  if (!container) return;
  
  if (snapshot.empty) {
    container.innerHTML = `
      <div class="empty-state glass-card">
        <i class="fa-solid fa-box-open"></i>
        <h3>No entries found</h3>
        <p>No ${model} listed in the cloud database. Click "Add New" to get started.</p>
      </div>
    `;
    return;
  }
  
  let html = '';
  
  snapshot.docs.forEach(doc => {
    const data = doc.data();
    const id = doc.id;
    
    // Filter Scenes locally if needed
    if (model === 'scenes' && filterId !== 'all' && data.playId !== filterId) {
      return;
    }
    
    html += getCardHtmlForModel(model, id, data);
  });
  
  if (!html) {
    container.innerHTML = `
      <div class="empty-state glass-card">
        <i class="fa-solid fa-filter"></i>
        <h3>Filtered Empty State</h3>
        <p>No scenes are registered under this specific play yet.</p>
      </div>
    `;
    return;
  }
  
  container.innerHTML = html;
  
  // Bind actions
  container.querySelectorAll('.btn-edit').forEach(btn => {
    btn.addEventListener('click', () => openFormDrawer(model, btn.dataset.id));
  });
  
  container.querySelectorAll('.btn-delete').forEach(btn => {
    btn.addEventListener('click', () => deleteElement(model, btn.dataset.id, btn.dataset.name));
  });
}

function getCardHtmlForModel(model, id, data) {
  const fallbackImg = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=600";
  const image = data.imageUrl || fallbackImg;
  
  switch(model) {
    case 'plays':
      return `
        <div class="item-card glass-card">
          <div class="card-media">
            <img src="${image}" alt="${data.title}" loading="lazy">
          </div>
          <div class="card-details">
            <h3>${data.title}</h3>
            <p>${data.description || 'No description provided.'}</p>
            <div class="card-actions">
              <button class="btn btn-primary btn-sm btn-edit" data-id="${id}"><i class="fa-solid fa-pen"></i> Edit</button>
              <button class="btn btn-danger-outline btn-sm btn-delete" data-id="${id}" data-name="${data.title}"><i class="fa-solid fa-trash-can"></i> Delete</button>
            </div>
          </div>
        </div>
      `;
      
    case 'scenes':
      const matchingPlay = cachePlays.find(p => p.id === data.playId);
      const playTitle = matchingPlay ? matchingPlay.title : "Unknown Play";
      return `
        <div class="item-card glass-card">
          <div class="card-media">
            <img src="${image}" alt="${data.title}" loading="lazy">
            <div class="media-overlay">
              <span class="tag tag-gold">Seq: ${data.orderNumber || 0}</span>
            </div>
          </div>
          <div class="card-details">
            <h3>${data.title}</h3>
            <p>${data.description || 'No description provided.'}</p>
            <div class="card-meta-list">
              <div class="card-meta-item"><i class="fa-solid fa-circle-play"></i> <span>Play: ${playTitle}</span></div>
            </div>
            <div class="card-actions">
              <button class="btn btn-primary btn-sm btn-edit" data-id="${id}"><i class="fa-solid fa-pen"></i> Edit</button>
              <button class="btn btn-danger-outline btn-sm btn-delete" data-id="${id}" data-name="${data.title}"><i class="fa-solid fa-trash-can"></i> Delete</button>
            </div>
          </div>
        </div>
      `;
      
    case 'puppets':
      return `
        <div class="item-card glass-card">
          <div class="card-media">
            <img src="${image}" alt="${data.name}" loading="lazy">
          </div>
          <div class="card-details">
            <h3>${data.name}</h3>
            <p>${data.description || 'No description.'}</p>
            <div class="card-meta-list">
              <div class="card-meta-item"><i class="fa-solid fa-wand-magic-sparkles"></i> <span>Powers: ${data.powers || 'None'}</span></div>
              <div class="card-meta-item"><i class="fa-solid fa-scroll"></i> <span>Symbolism: ${data.symbolism || 'None'}</span></div>
            </div>
            <div class="card-actions">
              <button class="btn btn-primary btn-sm btn-edit" data-id="${id}"><i class="fa-solid fa-pen"></i> Edit</button>
              <button class="btn btn-danger-outline btn-sm btn-delete" data-id="${id}" data-name="${data.name}"><i class="fa-solid fa-trash-can"></i> Delete</button>
            </div>
          </div>
        </div>
      `;
      
    case 'artists':
      return `
        <div class="item-card glass-card">
          <div class="card-media">
            <img src="${image}" alt="${data.name}" loading="lazy">
          </div>
          <div class="card-details">
            <h3>${data.name}</h3>
            <p>${data.description || 'No summary bio.'}</p>
            <div class="card-meta-list">
              <div class="card-meta-item"><i class="fa-solid fa-store"></i> <span>Workshop: ${data.workshopDetails || 'Local'}</span></div>
              <div class="card-meta-item"><i class="fa-solid fa-phone"></i> <span>Contact: ${data.phone || 'N/A'}</span></div>
            </div>
            <div class="card-actions">
              <button class="btn btn-primary btn-sm btn-edit" data-id="${id}"><i class="fa-solid fa-pen"></i> Edit</button>
              <button class="btn btn-danger-outline btn-sm btn-delete" data-id="${id}" data-name="${data.name}"><i class="fa-solid fa-trash-can"></i> Delete</button>
            </div>
          </div>
        </div>
      `;
      
    case 'history':
      let formattedDate = 'N/A';
      if (data.createdAt && data.createdAt.seconds) {
        formattedDate = new Date(data.createdAt.seconds * 1000).toLocaleDateString();
      }
      return `
        <div class="item-card glass-card">
          <div class="card-media">
            <img src="${image}" alt="${data.title}" loading="lazy">
          </div>
          <div class="card-details">
            <h3>${data.title}</h3>
            <p>${data.description || 'No story post detail.'}</p>
            <div class="card-meta-list">
              <div class="card-meta-item"><i class="fa-solid fa-calendar"></i> <span>Published: ${formattedDate}</span></div>
            </div>
            <div class="card-actions">
              <button class="btn btn-primary btn-sm btn-edit" data-id="${id}"><i class="fa-solid fa-pen"></i> Edit</button>
              <button class="btn btn-danger-outline btn-sm btn-delete" data-id="${id}" data-name="${data.title}"><i class="fa-solid fa-trash-can"></i> Delete</button>
            </div>
          </div>
        </div>
      `;
      
    default:
      return '';
  }
}

// ==========================================
// 5. DRAWER / FORM LOGIC (ADD & EDIT)
// ==========================================
const drawer = document.getElementById('universal-drawer');
const drawerTitle = document.getElementById('drawer-title');
const fieldsContainer = document.getElementById('dynamic-fields-container');
const universalForm = document.getElementById('universal-form');
const uploadProgressContainer = document.getElementById('upload-progress-container');
const uploadProgressFill = document.getElementById('upload-progress-fill');
const uploadProgressPercent = document.getElementById('upload-progress-percent');
const btnSubmitDrawer = document.getElementById('btn-submit-drawer');
const btnSubmitLabel = document.getElementById('btn-submit-label');

function setupGlobalEventListeners() {
  // Add Modals listeners
  document.getElementById('btn-add-play-modal').addEventListener('click', () => openFormDrawer('plays'));
  document.getElementById('btn-add-scene-modal').addEventListener('click', () => openFormDrawer('scenes'));
  document.getElementById('btn-add-puppet-modal').addEventListener('click', () => openFormDrawer('puppets'));
  document.getElementById('btn-add-artist-modal').addEventListener('click', () => openFormDrawer('artists'));
  document.getElementById('btn-add-history-modal').addEventListener('click', () => openFormDrawer('history'));
  
  // Dashboard action shortcuts
  document.querySelectorAll('.btn-quick-add').forEach(btn => {
    btn.addEventListener('click', () => {
      const action = btn.dataset.action;
      if (action === 'add-play') openFormDrawer('plays');
      if (action === 'add-scene') openFormDrawer('scenes');
      if (action === 'add-puppet') openFormDrawer('puppets');
      if (action === 'add-artist') openFormDrawer('artists');
      if (action === 'add-history') openFormDrawer('history');
    });
  });
  
  // Close buttons
  document.getElementById('btn-close-drawer').addEventListener('click', closeFormDrawer);
  document.getElementById('btn-cancel-drawer').addEventListener('click', closeFormDrawer);
  document.getElementById('drawer-overlay-btn').addEventListener('click', closeFormDrawer);
  
  // Form submission
  universalForm.addEventListener('submit', handleFormSubmit);
}

function openFormDrawer(model, editId = null) {
  currentModelType = model;
  currentEditId = editId;
  
  // Reset Progress Loader
  uploadProgressContainer.classList.add('hidden');
  uploadProgressFill.style.width = '0%';
  uploadProgressPercent.textContent = '0%';
  
  // Swap Submit Buttons Labels
  btnSubmitLabel.textContent = editId ? "Save Changes" : "Create Item";
  btnSubmitDrawer.querySelector('.spinner').classList.add('hidden');
  btnSubmitDrawer.disabled = false;
  
  // Set Title
  const capitalizedModelName = model.charAt(0).toUpperCase() + model.slice(1);
  drawerTitle.textContent = `${editId ? 'Edit' : 'Add New'} ${capitalizedModelName.replace('s', '')}`;
  
  // Render Dynamic Fields Template
  fieldsContainer.innerHTML = getFieldsTemplateForModel(model);
  
  // Fetch existing details if Editing
  if (editId) {
    db.collection(model).doc(editId).get()
      .then(doc => {
        if (doc.exists) {
          prefillFormFields(model, doc.data());
        }
      })
      .catch(err => showToast(err.message, "danger"));
  }
  
  drawer.classList.remove('hidden');
}

function closeFormDrawer() {
  drawer.classList.add('hidden');
  universalForm.reset();
  currentEditId = null;
  currentModelType = null;
}

function getFieldsTemplateForModel(model) {
  // All schemas contain two options for image loading: standard text box OR file upload via ImgBB
  const mediaSectionHtml = `
    <div class="form-group">
      <label>Media Option A: External Web URL</label>
      <input type="text" id="field-imageUrl" placeholder="https://images.unsplash.com/photo...">
    </div>
    <div class="form-group">
      <label>Media Option B: Upload Local File (ImgBB API)</label>
      <input type="file" id="field-imageFile" accept="image/*">
      <small style="color: #8b92a5; display: block; margin-top: 5px;">Image will be uploaded securely to ImgBB.</small>
    </div>
  `;
  
  switch(model) {
    case 'plays':
      return `
        <div class="form-group">
          <label for="field-title">Play Title <span class="required">*</span></label>
          <input type="text" id="field-title" required placeholder="e.g. Ramayana Shadow Play">
        </div>
        <div class="form-group">
          <label for="field-description">Full Story/Description <span class="required">*</span></label>
          <textarea id="field-description" required placeholder="Describe the play storyline..."></textarea>
        </div>
        ${mediaSectionHtml}
      `;
      
    case 'scenes':
      // Populate select plays
      let playsOptions = cachePlays.map(p => `<option value="${p.id}">${p.title}</option>`).join('');
      return `
        <div class="form-group">
          <label for="field-playId">Select Play <span class="required">*</span></label>
          <select id="field-playId" required>
            <option value="">-- Choose parent play --</option>
            ${playsOptions}
          </select>
        </div>
        <div class="form-group">
          <label for="field-title">Scene Title <span class="required">*</span></label>
          <input type="text" id="field-title" required placeholder="e.g. Seetha Swayamvara">
        </div>
        <div class="form-group">
          <label for="field-orderNumber">Scene Order Number <span class="required">*</span></label>
          <input type="number" id="field-orderNumber" required min="1" placeholder="e.g. 1" value="1">
        </div>
        <div class="form-group">
          <label for="field-description">Scene Explanation/Summary <span class="required">*</span></label>
          <textarea id="field-description" required placeholder="Describe what happens in this scene..."></textarea>
        </div>
        ${mediaSectionHtml}
      `;
      
    case 'puppets':
      return `
        <div class="form-group">
          <label for="field-name">Puppet Name <span class="required">*</span></label>
          <input type="text" id="field-name" required placeholder="e.g. Ravana Tenth Head">
        </div>
        <div class="form-group">
          <label for="field-powers">Mythological Powers <span class="required">*</span></label>
          <input type="text" id="field-powers" required placeholder="e.g. Shape-shifting, fire arrows">
        </div>
        <div class="form-group">
          <label for="field-symbolism">Symbolic meaning / Background <span class="required">*</span></label>
          <input type="text" id="field-symbolism" required placeholder="e.g. Hubris, intense devotion">
        </div>
        <div class="form-group">
          <label for="field-description">Detailed Description <span class="required">*</span></label>
          <textarea id="field-description" required placeholder="Physical attributes and craft details..."></textarea>
        </div>
        ${mediaSectionHtml}
      `;
      
    case 'artists':
      return `
        <div class="form-group">
          <label for="field-name">Master Artisan Name <span class="required">*</span></label>
          <input type="text" id="field-name" required placeholder="e.g. Gundappa Master Rao">
        </div>
        <div class="form-group">
          <label for="field-phone">Contact Phone Number <span class="required">*</span></label>
          <input type="text" id="field-phone" required placeholder="e.g. +91 98450 12345">
        </div>
        <div class="form-group">
          <label for="field-workshopDetails">Workshop Location & Details <span class="required">*</span></label>
          <input type="text" id="field-workshopDetails" required placeholder="e.g. Ramanagara Toy & Leather Hub">
        </div>
        <div class="form-group">
          <label for="field-description">Bio / Creative Journey <span class="required">*</span></label>
          <textarea id="field-description" required placeholder="Artisan experience details..."></textarea>
        </div>
        ${mediaSectionHtml}
      `;
      
    case 'history':
      return `
        <div class="form-group">
          <label for="field-title">Post Title <span class="required">*</span></label>
          <input type="text" id="field-title" required placeholder="e.g. Origin of Shadow Puppets in Kolar">
        </div>
        <div class="form-group">
          <label for="field-description">Historical Narrative / Content <span class="required">*</span></label>
          <textarea id="field-description" required placeholder="Write the history snippet or post content..."></textarea>
        </div>
        ${mediaSectionHtml}
      `;
      
    default:
      return '';
  }
}

function prefillFormFields(model, data) {
  // Prefill standard inputs
  const textInputs = ['title', 'name', 'powers', 'symbolism', 'description', 'phone', 'workshopDetails', 'orderNumber', 'playId', 'imageUrl'];
  
  textInputs.forEach(fieldName => {
    const input = document.getElementById(`field-${fieldName}`);
    if (input && data[fieldName] !== undefined) {
      input.value = data[fieldName];
    }
  });
}


// ==========================================
// 6. FORM TRANSACTION SUBMISSION & UPLOADS
// ==========================================
async function handleFormSubmit(e) {
  e.preventDefault();
  
  // Set UI state
  btnSubmitLabel.classList.add('hidden');
  btnSubmitDrawer.querySelector('.spinner').classList.remove('hidden');
  btnSubmitDrawer.disabled = true;
  
  try {
    let payload = {};
    
    // Extract base fields
    const fields = ['title', 'name', 'powers', 'symbolism', 'description', 'phone', 'workshopDetails', 'orderNumber', 'playId', 'imageUrl'];
    fields.forEach(f => {
      const input = document.getElementById(`field-${f}`);
      if (input) {
        let value = input.value.trim();
        // Cast orderNumber specifically to int
        if (f === 'orderNumber') value = parseInt(value) || 1;
        payload[f] = value;
      }
    });
    
    // Handle image file upload via ImgBB API
    const fileInput = document.getElementById('field-imageFile');
    if (fileInput && fileInput.files.length > 0) {
      uploadProgressContainer.classList.remove('hidden');
      uploadProgressPercent.textContent = `Uploading to ImgBB...`;
      uploadProgressFill.style.width = `30%`;
      
      const file = fileInput.files[0];
      
      try {
        console.log(`[UPLOAD] Starting ImgBB upload for file: ${file.name}`);
        
        const formData = new FormData();
        formData.append("image", file);
        
        const IMGBB_API_KEY = "a45f3c8915813a4b33fb4b3f697a425d";
        
        uploadProgressFill.style.width = `60%`;
        const response = await fetch(`https://api.imgbb.com/1/upload?key=${IMGBB_API_KEY}`, {
          method: 'POST',
          body: formData
        });
        
        if (!response.ok) {
           throw new Error(`ImgBB API responded with status ${response.status}`);
        }
        
        const data = await response.json();
        console.log(`[UPLOAD] ImgBB Response:`, data);
        
        if (data && data.data && data.data.url) {
           payload.imageUrl = data.data.url;
           uploadProgressFill.style.width = `100%`;
           uploadProgressPercent.textContent = `100%`;
           console.log(`[UPLOAD] Complete! File available at ${payload.imageUrl}`);
        } else {
           throw new Error("Invalid response format from ImgBB");
        }
        
      } catch (err) {
        console.error(`[UPLOAD] ImgBB upload failed:`, err);
        throw new Error(`Upload Failed: ${err.message}`); // Stop form submission if upload fails
      }
    }
    
    // Validate if any image source is supplied
    if (!payload.imageUrl) {
      // Set default image as buffer if they left URL blank
      payload.imageUrl = "";
    }
    
    // Add operational metadata
    if (currentModelType === 'history') {
      payload.createdAt = firebase.firestore.FieldValue.serverTimestamp();
    }
    
    // Firestore Transaction
    if (currentEditId) {
      // UPDATE
      await db.collection(currentModelType).doc(currentEditId).update(payload);
      showToast("Updated item in cloud database successfully!", "success");
      logActivity("Element Updated", `Updated ${payload.title || payload.name || currentEditId} inside ${currentModelType}.`);
    } else {
      // CREATE
      await db.collection(currentModelType).add(payload);
      showToast("Registered new item successfully!", "success");
      logActivity("Element Added", `Created new ${payload.title || payload.name} in ${currentModelType}.`);
    }
    
    closeFormDrawer();
    
  } catch (error) {
    console.error("Transact error:", error);
    showToast(`Error: ${error.message}`, "danger");
  } finally {
    btnSubmitLabel.classList.remove('hidden');
    btnSubmitDrawer.querySelector('.spinner').classList.add('hidden');
    btnSubmitDrawer.disabled = false;
  }
}

// ==========================================
// 7. DELETE OPERATIONS
// ==========================================
async function deleteElement(model, id, name) {
  if (confirm(`⚠️ WARNING: Are you sure you want to permanently delete "${name || 'this item'}" from the database? This action is irreversible!`)) {
    try {
      await db.collection(model).doc(id).delete();
      showToast("Successfully deleted from Cloud Firestore", "warning");
      logActivity("Element Deleted", `Deleted "${name || id}" from ${model}.`);
    } catch (error) {
      console.error("Delete error:", error);
      showToast(`Delete failed: ${error.message}`, "danger");
    }
  }
}
