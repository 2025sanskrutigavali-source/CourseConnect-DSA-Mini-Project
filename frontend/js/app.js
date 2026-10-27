/**
 * CourseConnect — Main Frontend Application Controller
 * Handles SPA navigation, role-based view switching, search/filter logic,
 * and live updates across dashboards.
 */

// Application State
const state = {
  currentUser: null,
  selectedLoginRole: 'STUDENT',
  currentTab: 'dashboard',
  courses: [],
  studentEnrollments: [],
  institutionStats: null,
  institutionEnrollments: [],
  institutionStudents: []
};

// Initialize Application on Page Load
document.addEventListener('DOMContentLoaded', async () => {
  setupLoginRoleTabs();
  setupRegistrationForm();
  setupPasswordToggle();
  setupModalBackdrops();

  // Check if active session exists
  const token = API.getToken();
  if (token) {
    try {
      const res = await API.getMe();
      if (res.data && res.data.user) {
        state.currentUser = res.data.user;
        launchPortal(state.currentUser);
        return;
      }
    } catch (e) {
      console.warn('Session verification failed, displaying login screen.');
      API.clearSession();
    }
  }

  // Default: Open on login page
  showScreen('loginScreen');
});

// ==========================================================================
// SCREEN & VIEW ROUTING
// ==========================================================================

function showScreen(screenId) {
  document.getElementById('loginScreen').style.display = 'none';
  document.getElementById('studentPortal').style.display = 'none';
  document.getElementById('institutionPortal').style.display = 'none';

  const target = document.getElementById(screenId);
  if (target) {
    target.style.display = screenId === 'loginScreen' ? 'flex' : 'block';
  }
}

function launchPortal(user) {
  if (user.role === 'STUDENT') {
    showScreen('studentPortal');
    document.getElementById('studentNavUserName').innerText = user.fullName || user.username;
    document.getElementById('studentNavUserId').innerText = `${user.username} • ${user.department || 'Student'}`;
    document.getElementById('studentNavAvatar').innerText = (user.fullName || user.username).charAt(0).toUpperCase();
    switchStudentTab('dashboard');
  } else if (user.role === 'INSTITUTION') {
    showScreen('institutionPortal');
    document.getElementById('instNavUserName').innerText = user.fullName || user.username;
    document.getElementById('instNavUserId').innerText = `${user.department || 'Registrar Office'}`;
    document.getElementById('instNavAvatar').innerText = (user.fullName || user.username).charAt(0).toUpperCase();
    switchInstitutionTab('dashboard');
  }
}

// ==========================================================================
// AUTHENTICATION & LOGIN SCREEN
// ==========================================================================

function setupLoginRoleTabs() {
  const studentBtn = document.getElementById('roleTabStudent');
  const instBtn = document.getElementById('roleTabInst');
  const roleDesc = document.getElementById('roleDescText');
  const userLabel = document.getElementById('loginUserLabel');
  const userInput = document.getElementById('loginUsername');

  studentBtn.addEventListener('click', () => {
    state.selectedLoginRole = 'STUDENT';
    studentBtn.classList.add('active');
    instBtn.classList.remove('active');
    roleDesc.innerText = 'Student Course Enrollment & Schedule Portal';
    userLabel.innerText = 'Student ID / Username';
    userInput.placeholder = 'e.g. student01';
  });

  instBtn.addEventListener('click', () => {
    state.selectedLoginRole = 'INSTITUTION';
    instBtn.classList.add('active');
    studentBtn.classList.remove('active');
    roleDesc.innerText = 'Institutional Registrar & Course Administration';
    userLabel.innerText = 'Administrator Username';
    userInput.placeholder = 'e.g. admin01';
  });
}

function setupRegistrationForm() {
  const roleSelect = document.getElementById('registerRole');
  document.getElementById('showRegistrationBtn').addEventListener('click', () => {
    toggleAuthForms(true);
    hideLoginError();
    hideLoginSuccess();
  });
  document.getElementById('showLoginBtn').addEventListener('click', () => {
    toggleAuthForms(false);
    hideRegistrationMessage();
  });
  roleSelect.addEventListener('change', updateRegistrationLabels);
  updateRegistrationLabels();
}

function toggleAuthForms(showRegistration) {
  document.getElementById('loginForm').style.display = showRegistration ? 'none' : '';
  document.getElementById('registrationForm').style.display = showRegistration ? '' : 'none';
  document.getElementById('showRegistrationBtn').style.display = showRegistration ? 'none' : '';
  document.getElementById('showLoginBtn').style.display = showRegistration ? '' : 'none';
  document.getElementById('demoAccountsBox').style.display = showRegistration ? 'none' : '';
}

function updateRegistrationLabels() {
  const institution = document.getElementById('registerRole').value === 'INSTITUTION';
  document.getElementById('registerNameLabel').innerText = institution ? 'Institution name' : 'Full name';
  document.getElementById('registerEmailLabel').innerText = institution ? 'Official email' : 'Email';
  document.getElementById('registerName').autocomplete = institution ? 'organization' : 'name';
}

function showRegistrationMessage(message, success = false) {
  const messageBox = document.getElementById('registrationMessage');
  messageBox.className = `auth-message ${success ? 'badge-success' : 'badge-danger'}`;
  messageBox.innerText = message;
  messageBox.style.display = 'block';
}

function hideRegistrationMessage() {
  document.getElementById('registrationMessage').style.display = 'none';
}

function hideLoginSuccess() {
  document.getElementById('loginSuccessAlert').style.display = 'none';
}

function setupPasswordToggle() {
  const toggleBtn = document.getElementById('passwordToggleBtn');
  const pwdInput = document.getElementById('loginPassword');
  const icon = toggleBtn.querySelector('.material-symbols-outlined');

  toggleBtn.addEventListener('click', () => {
    if (pwdInput.type === 'password') {
      pwdInput.type = 'text';
      icon.innerText = 'visibility_off';
    } else {
      pwdInput.type = 'password';
      icon.innerText = 'visibility';
    }
  });
}

function quickFill(username, password, role) {
  document.getElementById('loginUsername').value = username;
  document.getElementById('loginPassword').value = password;
  if (role === 'STUDENT') {
    document.getElementById('roleTabStudent').click();
  } else {
    document.getElementById('roleTabInst').click();
  }
  hideLoginError();
}

function showLoginError(msg) {
  const errBox = document.getElementById('loginErrorAlert');
  const errMsg = document.getElementById('loginErrorMessage');
  errMsg.innerText = msg;
  errBox.style.display = 'flex';
}

function hideLoginError() {
  document.getElementById('loginErrorAlert').style.display = 'none';
}

async function handleRegistrationSubmit(event) {
  event.preventDefault();
  hideRegistrationMessage();

  const fullName = document.getElementById('registerName').value.trim();
  const email = document.getElementById('registerEmail').value.trim();
  const password = document.getElementById('registerPassword').value;
  const confirmPassword = document.getElementById('confirmPassword').value;
  const role = document.getElementById('registerRole').value;

  if (!fullName || !email || !password.trim() || !confirmPassword.trim()) {
    showRegistrationMessage('Please complete all required fields.');
    return;
  }
  if (!document.getElementById('registerEmail').checkValidity()) {
    showRegistrationMessage('Please enter a valid email address.');
    return;
  }
  if (password !== confirmPassword) {
    showRegistrationMessage('Passwords do not match.');
    return;
  }

  const submitBtn = document.getElementById('registerSubmitBtn');
  submitBtn.disabled = true;
  submitBtn.innerText = 'Creating account...';
  try {
    await API.register(fullName, email, password, confirmPassword, role);
    document.getElementById('loginUsername').value = email;
    document.getElementById('loginPassword').value = '';
    document.getElementById(role === 'STUDENT' ? 'roleTabStudent' : 'roleTabInst').click();
    toggleAuthForms(false);
    hideLoginError();
    const successAlert = document.getElementById('loginSuccessAlert');
    successAlert.innerText = 'Account created successfully. Sign in with your new credentials.';
    successAlert.style.display = 'block';
    document.getElementById('registrationForm').reset();
    updateRegistrationLabels();
  } catch (error) {
    showRegistrationMessage(error.message || 'Account registration failed. Please try again.');
  } finally {
    submitBtn.disabled = false;
    submitBtn.innerText = 'Create Account';
  }
}

async function handleLoginSubmit(event) {
  event.preventDefault();
  hideLoginError();
  hideLoginSuccess();

  const username = document.getElementById('loginUsername').value.trim();
  const password = document.getElementById('loginPassword').value;
  const role = state.selectedLoginRole;

  if (!username) {
    showLoginError('Please enter your Student ID or username.');
    return;
  }
  if (!password.trim()) {
    showLoginError('Please enter your password.');
    return;
  }

  const submitBtn = document.getElementById('loginSubmitBtn');
  submitBtn.disabled = true;
  submitBtn.innerText = 'Signing in...';

  try {
    const res = await API.login(username, password, role);
    state.currentUser = res.data.user;
    UI.showToast(`Welcome back, ${res.data.user.fullName}!`, 'success');
    launchPortal(state.currentUser);
  } catch (error) {
    showLoginError(error.message || 'Authentication failed. Please verify credentials.');
  } finally {
    submitBtn.disabled = false;
    submitBtn.innerText = 'Sign In to Portal';
  }
}

async function handleLogout() {
  UI.confirmDialog({
    title: 'Sign Out Confirmation',
    message: 'Are you sure you want to end your current session and sign out?',
    confirmText: 'Sign Out',
    confirmClass: 'btn-danger',
    onConfirm: async () => {
      try {
        await API.logout();
      } catch (ignored) {}
      state.currentUser = null;
      document.getElementById('loginPassword').value = '';
      showScreen('loginScreen');
      UI.showToast('You have been safely signed out.', 'success');
    }
  });
}

// ==========================================================================
// STUDENT PORTAL LOGIC
// ==========================================================================

function switchStudentTab(tabId) {
  state.currentTab = tabId;

  // Update active sidebar link
  document.querySelectorAll('#studentSidebar .nav-link').forEach(link => {
    if (link.dataset.tab === tabId) {
      link.classList.add('active');
    } else {
      link.classList.remove('active');
    }
  });

  // Hide all views
  document.getElementById('studentDashboardView').style.display = 'none';
  document.getElementById('studentCatalogueView').style.display = 'none';
  document.getElementById('studentEnrollmentsView').style.display = 'none';
  document.getElementById('studentProfileView').style.display = 'none';

  if (tabId === 'dashboard') {
    document.getElementById('studentDashboardView').style.display = 'block';
    loadStudentDashboard();
  } else if (tabId === 'catalogue') {
    document.getElementById('studentCatalogueView').style.display = 'block';
    loadStudentCatalogue();
  } else if (tabId === 'enrollments') {
    document.getElementById('studentEnrollmentsView').style.display = 'block';
    loadStudentEnrollments();
  } else if (tabId === 'profile') {
    document.getElementById('studentProfileView').style.display = 'block';
    loadStudentProfile();
  }
}

async function loadStudentDashboard() {
  try {
    // 1. Fetch courses and enrollments in parallel
    const [coursesRes, enrollRes] = await Promise.all([
      API.getCourses(),
      API.getStudentEnrollments()
    ]);

    const courses = coursesRes.data.courses || [];
    const enrollments = enrollRes.data.enrollments || [];
    const totalCredits = enrollRes.data.totalCredits || 0;
    const maxCredits = enrollRes.data.maxCredits || 18;

    state.courses = courses;
    state.studentEnrollments = enrollments;

    // Update sidebar enrollments badge
    document.getElementById('studentSidebarEnrollBadge').innerText = enrollments.length;

    // Personal welcome banner
    document.getElementById('studentWelcomeName').innerText = state.currentUser.fullName || state.currentUser.username;

    // Metric KPI 1: Available Courses
    const availableSections = courses.filter(c => c.availableSeats > 0).length;
    document.getElementById('kpiAvailableCourses').innerText = availableSections;

    // Metric KPI 2: Enrolled Courses
    document.getElementById('kpiEnrolledCourses').innerText = enrollments.length;

    // Metric KPI 3: Credit Load
    document.getElementById('kpiCreditLoad').innerText = `${totalCredits} / ${maxCredits}`;
    const percentCap = Math.min(100, Math.round((totalCredits / maxCredits) * 100));
    document.getElementById('kpiCreditPercent').innerText = `${percentCap}% cap`;
    document.getElementById('kpiCreditBar').style.width = `${percentCap}%`;
    const remainingCredits = Math.max(0, maxCredits - totalCredits);
    document.getElementById('kpiCreditRemainingText').innerText = `${remainingCredits} credits permitted for registration`;

    // Render Recommended Courses (show first 3 courses the student is not yet enrolled in)
    const recommended = courses.filter(c => !c.isEnrolled).slice(0, 3);
    renderRecommendedCards(recommended);

    // Render Enrolled Schedule Quick-Glance Table
    renderStudentScheduleTable(enrollments);
  } catch (err) {
    UI.showToast(err.message, 'error');
  }
}

function renderRecommendedCards(courses) {
  const container = document.getElementById('recommendedCoursesGrid');
  if (!container) return;

  if (courses.length === 0) {
    container.innerHTML = '<p class="text-muted" style="padding: 1rem;">No recommended courses available right now.</p>';
    return;
  }

  container.innerHTML = courses.map(c => `
    <div class="course-card">
      <div class="course-card-header">
        <div>
          <span class="course-code">${escapeHtml(c.courseId)}</span>
          <h3 class="course-title">${escapeHtml(c.courseName)}</h3>
        </div>
        <span class="badge badge-neutral">${c.credits} Credits</span>
      </div>
      <div class="course-card-body">
        <div class="course-meta-row">
          <span class="material-symbols-outlined">person</span>
          <span>${escapeHtml(c.instructor)}</span>
        </div>
        <div class="course-meta-row">
          <span class="material-symbols-outlined">schedule</span>
          <span>${escapeHtml(c.schedule || 'TBA')}</span>
        </div>
        <div class="course-meta-row">
          <span class="material-symbols-outlined">room</span>
          <span>${escapeHtml(c.location || 'Campus Hall')}</span>
        </div>
        <div class="seat-status-meter">
          <span style="font-size: 0.8125rem; color: var(--text-muted); font-weight: 500;">
            ${c.availableSeats} / ${c.capacity} seats open
          </span>
          ${c.availableSeats <= 0
            ? '<span class="badge badge-danger">Full</span>'
            : '<span class="badge badge-success">Available</span>'}
        </div>
      </div>
      <div class="course-card-footer">
        <button class="btn btn-secondary btn-sm" style="flex: 1;" onclick="openCourseDetails('${c.courseId}')">
          View Details
        </button>
        ${c.isEnrolled
          ? '<button class="btn btn-secondary btn-sm" disabled>Enrolled</button>'
          : (c.availableSeats <= 0
            ? '<button class="btn btn-secondary btn-sm" disabled>Course Full</button>'
            : `<button class="btn btn-primary btn-sm" onclick="quickEnrollCourse('${c.courseId}')">Enroll</button>`)}
      </div>
    </div>
  `).join('');
}

function renderStudentScheduleTable(enrollments) {
  const tbody = document.getElementById('studentScheduleTbody');
  if (!tbody) return;

  if (enrollments.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="5" style="text-align: center; padding: 2rem; color: var(--text-muted);">
          You have not enrolled in any courses yet.
          <br><button class="btn btn-outline-primary btn-sm" style="margin-top: 0.75rem;" onclick="switchStudentTab('catalogue')">Browse Course Catalogue</button>
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = enrollments.map(e => `
    <tr>
      <td><strong class="course-code">${escapeHtml(e.courseId)}</strong></td>
      <td><strong>${escapeHtml(e.courseName)}</strong></td>
      <td>${escapeHtml(e.schedule || 'Regular Term')}</td>
      <td>${escapeHtml(e.location || 'Lecture Hall')}</td>
      <td><span class="badge badge-neutral">${e.credits} Cr</span></td>
    </tr>
  `).join('');
}

// ==========================================================================
// COURSE CATALOGUE LOGIC
// ==========================================================================

async function loadStudentCatalogue() {
  const search = document.getElementById('courseSearchInput').value.trim();
  const department = document.getElementById('courseDeptFilter').value;
  const credits = document.getElementById('courseCreditsFilter').value;
  const sort = document.getElementById('courseSortSelect').value;

  try {
    const res = await API.getCourses({ search, department, credits, sort });
    state.courses = res.data.courses || [];
    renderCourseCatalogueGrid(state.courses);
  } catch (err) {
    UI.showToast(err.message, 'error');
  }
}

function renderCourseCatalogueGrid(courses) {
  const container = document.getElementById('catalogueGrid');
  const countEl = document.getElementById('catalogueCount');
  countEl.innerText = `${courses.length} courses found`;

  if (courses.length === 0) {
    container.innerHTML = `
      <div class="empty-state" style="grid-column: 1 / -1;">
        <div class="empty-state-icon">
          <span class="material-symbols-outlined" style="font-size: 28px;">search_off</span>
        </div>
        <h3>No courses match your filter criteria</h3>
        <p class="text-muted" style="font-size: 0.875rem;">Try adjusting your search terms or clearing department filters.</p>
        <button class="btn btn-secondary btn-sm" onclick="resetCatalogueFilters()">Reset Filters</button>
      </div>
    `;
    return;
  }

  container.innerHTML = courses.map(c => `
    <div class="course-card">
      <div class="course-card-header">
        <div>
          <span class="course-code">${escapeHtml(c.courseId)}</span>
          <h3 class="course-title">${escapeHtml(c.courseName)}</h3>
        </div>
        <span class="badge badge-neutral">${c.credits} Credits</span>
      </div>
      <div class="course-card-body">
        <div class="course-meta-row">
          <span class="material-symbols-outlined">domain</span>
          <span>${escapeHtml(c.department)}</span>
        </div>
        <div class="course-meta-row">
          <span class="material-symbols-outlined">person</span>
          <span>${escapeHtml(c.instructor)}</span>
        </div>
        <div class="course-meta-row">
          <span class="material-symbols-outlined">schedule</span>
          <span>${escapeHtml(c.schedule || 'Mon & Wed 10:00 - 11:30 AM')}</span>
        </div>
        <div class="seat-status-meter">
          <span style="font-size: 0.8125rem; color: var(--text-muted); font-weight: 500;">
            ${c.availableSeats} / ${c.capacity} seats open
          </span>
          ${c.isEnrolled
            ? '<span class="badge badge-success"><span class="material-symbols-outlined" style="font-size: 14px;">check</span> Enrolled</span>'
            : (c.availableSeats <= 0
              ? '<span class="badge badge-danger">Course Full</span>'
              : '<span class="badge badge-success">Available</span>')}
        </div>
      </div>
      <div class="course-card-footer">
        <button class="btn btn-secondary btn-sm" style="flex: 1;" onclick="openCourseDetails('${c.courseId}')">
          View Details
        </button>
        ${c.isEnrolled
          ? '<button class="btn btn-secondary btn-sm" disabled style="background:#ecfdf5; color:#065f46; border-color:#a7f3d0;">Enrolled</button>'
          : (c.availableSeats <= 0
            ? '<button class="btn btn-secondary btn-sm" disabled>Full</button>'
            : `<button class="btn btn-primary btn-sm" onclick="quickEnrollCourse('${c.courseId}')">Enroll</button>`)}
      </div>
    </div>
  `).join('');
}

function resetCatalogueFilters() {
  document.getElementById('courseSearchInput').value = '';
  document.getElementById('courseDeptFilter').value = 'ALL';
  document.getElementById('courseCreditsFilter').value = '';
  document.getElementById('courseSortSelect').value = 'id_asc';
  loadStudentCatalogue();
}

async function quickEnrollCourse(courseId) {
  try {
    const res = await API.enrollCourse(courseId);
    UI.showToast(res.message || `Successfully enrolled in ${courseId}!`, 'success');
    
    // Refresh catalogue & dashboard
    await loadStudentCatalogue();
    
    // Update badge in sidebar
    const enrollRes = await API.getStudentEnrollments();
    state.studentEnrollments = enrollRes.data.enrollments || [];
    document.getElementById('studentSidebarEnrollBadge').innerText = state.studentEnrollments.length;
  } catch (err) {
    UI.showToast(err.message, 'error');
  }
}

async function openCourseDetails(courseId) {
  try {
    const res = await API.getCourseDetails(courseId);
    const c = res.data;

    document.getElementById('detailCourseId').innerText = c.courseId;
    document.getElementById('detailCourseName').innerText = c.courseName;
    document.getElementById('detailDept').innerText = c.department;
    document.getElementById('detailInstructor').innerText = c.instructor;
    document.getElementById('detailCredits').innerText = `${c.credits} Credits`;
    document.getElementById('detailCapacity').innerText = `${c.enrolledCount} / ${c.capacity} enrolled (${c.availableSeats} available)`;
    document.getElementById('detailSchedule').innerText = c.schedule || 'Scheduled according to academic timetable';
    document.getElementById('detailLocation').innerText = c.location || 'Main University Campus';
    document.getElementById('detailDescription').innerText = c.description || 'No detailed syllabus available.';

    const enrollBtn = document.getElementById('detailModalEnrollBtn');
    if (state.currentUser && state.currentUser.role === 'STUDENT') {
      enrollBtn.style.display = 'inline-flex';
      if (c.isEnrolled) {
        enrollBtn.disabled = true;
        enrollBtn.innerText = 'Already Enrolled';
        enrollBtn.className = 'btn btn-secondary';
      } else if (c.availableSeats <= 0) {
        enrollBtn.disabled = true;
        enrollBtn.innerText = 'Course Full';
        enrollBtn.className = 'btn btn-secondary';
      } else {
        enrollBtn.disabled = false;
        enrollBtn.innerText = 'Enroll in Course';
        enrollBtn.className = 'btn btn-primary';
        enrollBtn.onclick = async () => {
          UI.closeModal('courseDetailsModal');
          await quickEnrollCourse(c.courseId);
        };
      }
    } else {
      enrollBtn.style.display = 'none';
    }

    UI.openModal('courseDetailsModal');
  } catch (err) {
    UI.showToast(err.message, 'error');
  }
}

// ==========================================================================
// MY ENROLLMENTS LOGIC
// ==========================================================================

async function loadStudentEnrollments() {
  try {
    const res = await API.getStudentEnrollments();
    state.studentEnrollments = res.data.enrollments || [];
    const totalCredits = res.data.totalCredits || 0;
    const maxCredits = res.data.maxCredits || 18;

    document.getElementById('studentSidebarEnrollBadge').innerText = state.studentEnrollments.length;
    document.getElementById('enrollmentsTotalCourses').innerText = state.studentEnrollments.length;
    document.getElementById('enrollmentsTotalCredits').innerText = totalCredits;
    document.getElementById('enrollmentsRemainingCredits').innerText = Math.max(0, maxCredits - totalCredits);

    renderEnrollmentsTable(state.studentEnrollments);
  } catch (err) {
    UI.showToast(err.message, 'error');
  }
}

function renderEnrollmentsTable(enrollments) {
  const tbody = document.getElementById('myEnrollmentsTbody');
  const emptyState = document.getElementById('enrollmentsEmptyState');
  const tableCard = document.getElementById('enrollmentsTableCard');

  if (enrollments.length === 0) {
    tableCard.style.display = 'none';
    emptyState.style.display = 'flex';
    return;
  }

  tableCard.style.display = 'block';
  emptyState.style.display = 'none';

  tbody.innerHTML = enrollments.map(e => `
    <tr>
      <td><strong class="course-code">${escapeHtml(e.courseId)}</strong></td>
      <td><strong>${escapeHtml(e.courseName)}</strong></td>
      <td>${escapeHtml(e.department || 'Computer Science')}</td>
      <td><span class="badge badge-neutral">${e.credits} Credits</span></td>
      <td>${escapeHtml(e.instructor || 'Faculty Staff')}</td>
      <td>${escapeHtml(e.enrolledAt || 'Current Term')}</td>
      <td><span class="badge badge-success">Enrolled & Confirmed</span></td>
      <td style="text-align: right;">
        <button class="btn btn-outline-danger btn-sm" onclick="promptDropCourse('${e.courseId}', '${escapeHtml(e.courseName)}')">
          Drop Course
        </button>
      </td>
    </tr>
  `).join('');
}

function promptDropCourse(courseId, courseName) {
  UI.confirmDialog({
    title: 'Drop Course Confirmation',
    message: `Are you sure you want to drop ${courseId} (${courseName})? Your seat will be released and your semester credit tally updated.`,
    confirmText: 'Drop Course',
    confirmClass: 'btn-danger',
    onConfirm: async () => {
      try {
        const res = await API.dropCourse(courseId);
        UI.showToast(res.message || `Course ${courseId} dropped successfully.`, 'success');
        await loadStudentEnrollments();
      } catch (err) {
        UI.showToast(err.message, 'error');
      }
    }
  });
}

// ==========================================================================
// STUDENT PROFILE LOGIC
// ==========================================================================

async function loadStudentProfile() {
  try {
    const res = await API.getStudentProfile();
    const data = res.data;
    const user = data.user;

    document.getElementById('profileFullName').innerText = user.fullName || 'Alex Mercer';
    document.getElementById('profileStudentId').innerText = user.username;
    document.getElementById('profileEmail').innerText = user.email || `${user.username}@university.edu`;
    document.getElementById('profileDept').innerText = user.department || 'Computer Science & Eng.';
    document.getElementById('profileYear').innerText = user.academicYear || '3rd Year';
    
    document.getElementById('profileEnrolledCount').innerText = data.enrolledCoursesCount;
    document.getElementById('profileTotalCredits').innerText = `${data.totalCredits} / ${data.maxCredits} credits`;
    document.getElementById('profileRemainingCredits').innerText = `${data.creditsRemaining} credits available`;

    const percent = Math.min(100, Math.round((data.totalCredits / data.maxCredits) * 100));
    document.getElementById('profileProgressBar').style.width = `${percent}%`;
  } catch (err) {
    UI.showToast(err.message, 'error');
  }
}

// ==========================================================================
// INSTITUTION PORTAL LOGIC
// ==========================================================================

function switchInstitutionTab(tabId) {
  state.currentTab = tabId;

  // Update active sidebar link
  document.querySelectorAll('#institutionSidebar .nav-link').forEach(link => {
    if (link.dataset.tab === tabId) {
      link.classList.add('active');
    } else {
      link.classList.remove('active');
    }
  });

  // Hide all institution views
  document.getElementById('instDashboardView').style.display = 'none';
  document.getElementById('instCoursesView').style.display = 'none';
  document.getElementById('instEnrollmentsView').style.display = 'none';
  document.getElementById('instStudentsView').style.display = 'none';

  if (tabId === 'dashboard') {
    document.getElementById('instDashboardView').style.display = 'block';
    loadInstitutionDashboard();
  } else if (tabId === 'courses') {
    document.getElementById('instCoursesView').style.display = 'block';
    loadInstitutionCourses();
  } else if (tabId === 'enrollments') {
    document.getElementById('instEnrollmentsView').style.display = 'block';
    loadInstitutionEnrollments();
  } else if (tabId === 'students') {
    document.getElementById('instStudentsView').style.display = 'block';
    loadInstitutionStudents();
  }
}

async function loadInstitutionDashboard() {
  try {
    const [statsRes, enrollRes] = await Promise.all([
      API.getAdminStats(),
      API.getAdminEnrollments()
    ]);

    const stats = statsRes.data;
    state.institutionStats = stats;
    state.institutionEnrollments = enrollRes.data.enrollments || [];

    document.getElementById('instKpiTotalCourses').innerText = stats.totalCourses;
    document.getElementById('instKpiTotalStudents').innerText = stats.totalStudents;
    document.getElementById('instKpiTotalEnrollments').innerText = stats.totalEnrollments;
    document.getElementById('instKpiCapacityRate').innerText = `${stats.utilizationRate}%`;
    document.getElementById('instKpiAvailableSeats').innerText = `${stats.totalAvailableSeats} open seats across campus`;

    // Render Department Breakdown Table
    renderDeptDistributionTable(stats.departments || []);

    // Render Recent Enrollments Audit Stream
    renderRecentAuditTable(state.institutionEnrollments.slice(0, 5));
  } catch (err) {
    UI.showToast(err.message, 'error');
  }
}

function renderDeptDistributionTable(depts) {
  const tbody = document.getElementById('instDeptDistributionTbody');
  if (!tbody) return;

  if (depts.length === 0) {
    tbody.innerHTML = '<tr><td colspan="4" style="text-align: center; color: var(--text-muted);">No departmental data available.</td></tr>';
    return;
  }

  tbody.innerHTML = depts.map(d => {
    const rate = d.totalCapacity > 0 ? Math.round((d.enrolledCount / d.totalCapacity) * 100) : 0;
    return `
      <tr>
        <td><strong>${escapeHtml(d.department)}</strong></td>
        <td>${d.courseCount} Courses</td>
        <td>${d.enrolledCount} / ${d.totalCapacity} Seats</td>
        <td>
          <div style="display: flex; align-items: center; gap: 0.5rem;">
            <div class="progress-track" style="flex: 1; height: 6px;">
              <div class="progress-fill" style="width: ${rate}%;"></div>
            </div>
            <span style="font-size: 0.75rem; font-weight: 600;">${rate}%</span>
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

function renderRecentAuditTable(records) {
  const tbody = document.getElementById('instRecentAuditTbody');
  if (!tbody) return;

  if (records.length === 0) {
    tbody.innerHTML = '<tr><td colspan="4" style="text-align: center; color: var(--text-muted); padding: 1.5rem;">No recent enrollment records.</td></tr>';
    return;
  }

  tbody.innerHTML = records.map(r => `
    <tr>
      <td><strong>${escapeHtml(r.studentName || r.studentId)}</strong></td>
      <td><span class="course-code">${escapeHtml(r.courseId)}</span> ${escapeHtml(r.courseName)}</td>
      <td><span class="badge badge-neutral">${r.credits} Credits</span></td>
      <td>${escapeHtml(r.enrolledAt)}</td>
    </tr>
  `).join('');
}

// ==========================================================================
// INSTITUTION COURSE MANAGEMENT
// ==========================================================================

async function loadInstitutionCourses() {
  const search = document.getElementById('instCourseSearchInput').value.trim();
  const department = document.getElementById('instCourseDeptFilter').value;

  try {
    const res = await API.getCourses({ search, department });
    state.courses = res.data.courses || [];
    renderInstitutionCoursesTable(state.courses);
  } catch (err) {
    UI.showToast(err.message, 'error');
  }
}

function renderInstitutionCoursesTable(courses) {
  const tbody = document.getElementById('instCoursesTbody');
  if (!tbody) return;

  if (courses.length === 0) {
    tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; padding: 2rem; color: var(--text-muted);">No courses found.</td></tr>';
    return;
  }

  tbody.innerHTML = courses.map(c => `
    <tr>
      <td><strong class="course-code">${escapeHtml(c.courseId)}</strong></td>
      <td><strong>${escapeHtml(c.courseName)}</strong></td>
      <td>${escapeHtml(c.department)}</td>
      <td>${escapeHtml(c.instructor)}</td>
      <td><span class="badge badge-neutral">${c.credits} Cr</span></td>
      <td>
        <strong>${c.enrolledCount}</strong> / ${c.capacity}
        <span style="font-size: 0.75rem; color: var(--text-muted); display: block;">(${c.availableSeats} available)</span>
      </td>
      <td>
        ${c.enrolledCount >= c.capacity
          ? '<span class="badge badge-danger">Capacity Full</span>'
          : '<span class="badge badge-success">Open</span>'}
      </td>
      <td style="text-align: right; white-space: nowrap;">
        <button class="btn btn-secondary btn-sm" onclick="openEditCourseModal('${c.courseId}')">
          Edit
        </button>
        <button class="btn btn-outline-danger btn-sm" onclick="handleDeleteCourse('${c.courseId}', ${c.enrolledCount})">
          Delete
        </button>
      </td>
    </tr>
  `).join('');
}

function openAddCourseModal() {
  document.getElementById('courseModalTitle').innerText = 'Add New University Course';
  document.getElementById('editCourseOriginalId').value = '';
  document.getElementById('formCourseId').value = '';
  document.getElementById('formCourseId').disabled = false;
  document.getElementById('formCourseName').value = '';
  document.getElementById('formDepartment').value = 'Computer Science';
  document.getElementById('formInstructor').value = '';
  document.getElementById('formCredits').value = '4';
  document.getElementById('formCapacity').value = '30';
  document.getElementById('formSchedule').value = 'Mon & Wed • 10:00 AM - 11:30 AM';
  document.getElementById('formLocation').value = 'Turing Hall 101';
  document.getElementById('formDescription').value = '';

  UI.openModal('courseEditModal');
}

async function openEditCourseModal(courseId) {
  try {
    const res = await API.getCourseDetails(courseId);
    const c = res.data;

    document.getElementById('courseModalTitle').innerText = `Edit Course: ${c.courseId}`;
    document.getElementById('editCourseOriginalId').value = c.courseId;
    document.getElementById('formCourseId').value = c.courseId;
    document.getElementById('formCourseId').disabled = true; // Cannot alter ID during edit
    document.getElementById('formCourseName').value = c.courseName;
    document.getElementById('formDepartment').value = c.department;
    document.getElementById('formInstructor').value = c.instructor;
    document.getElementById('formCredits').value = c.credits;
    document.getElementById('formCapacity').value = c.capacity;
    document.getElementById('formSchedule').value = c.schedule || '';
    document.getElementById('formLocation').value = c.location || '';
    document.getElementById('formDescription').value = c.description || '';

    UI.openModal('courseEditModal');
  } catch (err) {
    UI.showToast(err.message, 'error');
  }
}

async function handleSaveCourseForm(event) {
  event.preventDefault();

  const originalId = document.getElementById('editCourseOriginalId').value.trim();
  const courseId = document.getElementById('formCourseId').value.trim();
  const courseName = document.getElementById('formCourseName').value.trim();
  const department = document.getElementById('formDepartment').value.trim();
  const instructor = document.getElementById('formInstructor').value.trim();
  const credits = parseInt(document.getElementById('formCredits').value, 10);
  const capacity = parseInt(document.getElementById('formCapacity').value, 10);
  const schedule = document.getElementById('formSchedule').value.trim();
  const location = document.getElementById('formLocation').value.trim();
  const description = document.getElementById('formDescription').value.trim();

  if (!courseId || !courseName || !department || !instructor) {
    UI.showToast('Please fill in all required course fields.', 'error');
    return;
  }

  const payload = {
    courseId,
    courseName,
    department,
    instructor,
    credits,
    capacity,
    schedule,
    location,
    description
  };

  try {
    if (originalId) {
      // Update
      await API.updateCourse(originalId, payload);
      UI.showToast(`Course ${originalId} updated successfully.`, 'success');
    } else {
      // Create
      await API.addCourse(payload);
      UI.showToast(`Course ${courseId} added to catalogue.`, 'success');
    }

    UI.closeModal('courseEditModal');
    loadInstitutionCourses();
  } catch (err) {
    UI.showToast(err.message, 'error');
  }
}

function handleDeleteCourse(courseId, enrolledCount) {
  if (enrolledCount > 0) {
    UI.confirmDialog({
      title: 'Action Prevented: Active Enrollments',
      message: `Course ${courseId} cannot be deleted because it currently has ${enrolledCount} enrolled student(s). All students must be dropped before deletion.`,
      confirmText: 'Understood',
      confirmClass: 'btn-secondary',
      onConfirm: () => {}
    });
    return;
  }

  UI.confirmDialog({
    title: 'Delete Course Offering',
    message: `Are you sure you want to permanently delete course ${courseId}? This cannot be undone.`,
    confirmText: 'Delete Course',
    confirmClass: 'btn-danger',
    onConfirm: async () => {
      try {
        const res = await API.deleteCourse(courseId);
        UI.showToast(res.message || `Course ${courseId} deleted.`, 'success');
        loadInstitutionCourses();
      } catch (err) {
        UI.showToast(err.message, 'error');
      }
    }
  });
}

// ==========================================================================
// INSTITUTION ENROLLMENTS & STUDENTS
// ==========================================================================

async function loadInstitutionEnrollments() {
  const search = document.getElementById('instEnrollmentSearch').value.trim();
  const courseId = document.getElementById('instEnrollmentCourseFilter').value;

  try {
    const res = await API.getAdminEnrollments({ search, courseId });
    state.institutionEnrollments = res.data.enrollments || [];
    renderInstitutionEnrollmentsTable(state.institutionEnrollments);
  } catch (err) {
    UI.showToast(err.message, 'error');
  }
}

function renderInstitutionEnrollmentsTable(records) {
  const tbody = document.getElementById('instEnrollmentsTbody');
  const countEl = document.getElementById('instEnrollmentsCount');
  countEl.innerText = `${records.length} records`;

  if (records.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 2rem; color: var(--text-muted);">No enrollment records found.</td></tr>';
    return;
  }

  tbody.innerHTML = records.map(r => `
    <tr>
      <td><strong>${escapeHtml(r.studentId)}</strong></td>
      <td>${escapeHtml(r.studentName)}</td>
      <td><strong class="course-code">${escapeHtml(r.courseId)}</strong></td>
      <td>${escapeHtml(r.courseName)}</td>
      <td><span class="badge badge-neutral">${r.credits} Credits</span></td>
      <td>${escapeHtml(r.enrolledAt)}</td>
    </tr>
  `).join('');
}

async function loadInstitutionStudents() {
  try {
    const res = await API.getAdminStudents();
    state.institutionStudents = res.data.students || [];
    renderInstitutionStudentsTable(state.institutionStudents);
  } catch (err) {
    UI.showToast(err.message, 'error');
  }
}

function renderInstitutionStudentsTable(students) {
  const tbody = document.getElementById('instStudentsTbody');
  if (!tbody) return;

  if (students.length === 0) {
    tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; padding: 2rem; color: var(--text-muted);">No students registered.</td></tr>';
    return;
  }

  tbody.innerHTML = students.map(s => `
    <tr>
      <td><strong>${escapeHtml(s.username)}</strong></td>
      <td><strong>${escapeHtml(s.fullName)}</strong></td>
      <td>${escapeHtml(s.department || 'N/A')}</td>
      <td><span class="badge badge-neutral">${escapeHtml(s.academicYear || 'Year 1')}</span></td>
      <td>${escapeHtml(s.email)}</td>
    </tr>
  `).join('');
}

// ==========================================================================
// MODAL CLOSE HELPERS
// ==========================================================================

function setupModalBackdrops() {
  document.querySelectorAll('.modal-backdrop').forEach(backdrop => {
    backdrop.addEventListener('click', (e) => {
      if (e.target === backdrop) {
        backdrop.classList.remove('open');
        document.body.style.overflow = '';
      }
    });
  });
}
