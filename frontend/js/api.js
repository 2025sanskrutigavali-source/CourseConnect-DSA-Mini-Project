/**
 * CourseConnect API Client
 * Configured for CourseConnect on port 8082.
 * Wraps backend REST endpoints with session authentication and uniform error handling.
 */
const API = {
  TOKEN_KEY: 'courseconnect_session_token',
  USER_KEY: 'courseconnect_user_data',
  DEFAULT_PORT: 8082,

  // Resolves the API base URL (defaults to current origin e.g. http://localhost:8082)
  getBaseUrl() {
    if (typeof window !== 'undefined' && window.location && window.location.origin && window.location.origin !== 'null') {
      return window.location.origin;
    }
    return `http://localhost:${this.DEFAULT_PORT}`;
  },

  getToken() {
    return localStorage.getItem(this.TOKEN_KEY);
  },

  setSession(token, user) {
    if (token) localStorage.setItem(this.TOKEN_KEY, token);
    if (user) localStorage.setItem(this.USER_KEY, JSON.stringify(user));
  },

  clearSession() {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
  },

  getCurrentUser() {
    const data = localStorage.getItem(this.USER_KEY);
    try {
      return data ? JSON.parse(data) : null;
    } catch (e) {
      return null;
    }
  },

  async request(endpoint, options = {}) {
    const path = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;
    const url = `${this.getBaseUrl()}${path}`;
    const headers = {
      'Content-Type': 'application/json',
      ...(options.headers || {})
    };

    const token = this.getToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
      ...options,
      headers
    };

    try {
      const response = await fetch(url, config);
      const data = await response.json();

      if (!response.ok) {
        const errorMsg = data && data.message ? data.message : `HTTP error ${response.status}`;
        const err = new Error(errorMsg);
        err.status = response.status;
        err.data = data;
        throw err;
      }

      return data;
    } catch (error) {
      if (error.status === 401) {
        // Invalidate stale local session
        this.clearSession();
      }
      throw error;
    }
  },

  // Auth Endpoints
  async login(username, password, role) {
    const res = await this.request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password, role })
    });
    if (res.data && res.data.token) {
      this.setSession(res.data.token, res.data.user);
    }
    return res;
  },

  async register(fullName, email, password, confirmPassword, role) {
    return await this.request('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify({ fullName, email, password, confirmPassword, role })
    });
  },

  async logout() {
    try {
      await this.request('/api/auth/logout', { method: 'POST' });
    } finally {
      this.clearSession();
    }
  },

  async getMe() {
    return await this.request('/api/auth/me', { method: 'GET' });
  },

  // Course Catalogue Endpoints
  async getCourses(params = {}) {
    const q = new URLSearchParams();
    if (params.search) q.append('search', params.search);
    if (params.department && params.department !== 'ALL') q.append('department', params.department);
    if (params.credits) q.append('credits', params.credits);
    if (params.sort) q.append('sort', params.sort);

    const queryStr = q.toString() ? `?${q.toString()}` : '';
    return await this.request(`/api/courses${queryStr}`, { method: 'GET' });
  },

  async getCourseDetails(courseId) {
    return await this.request(`/api/courses/${encodeURIComponent(courseId)}`, { method: 'GET' });
  },

  async addCourse(courseData) {
    return await this.request('/api/courses', {
      method: 'POST',
      body: JSON.stringify(courseData)
    });
  },

  async updateCourse(courseId, courseData) {
    return await this.request(`/api/courses/${encodeURIComponent(courseId)}`, {
      method: 'PUT',
      body: JSON.stringify(courseData)
    });
  },

  async deleteCourse(courseId) {
    return await this.request(`/api/courses/${encodeURIComponent(courseId)}`, {
      method: 'DELETE'
    });
  },

  // Student Endpoints
  async getStudentEnrollments() {
    return await this.request('/api/student/enrollments', { method: 'GET' });
  },

  async enrollCourse(courseId) {
    return await this.request('/api/student/enroll', {
      method: 'POST',
      body: JSON.stringify({ courseId })
    });
  },

  async dropCourse(courseId) {
    return await this.request('/api/student/drop', {
      method: 'POST',
      body: JSON.stringify({ courseId })
    });
  },

  async getStudentProfile() {
    return await this.request('/api/student/profile', { method: 'GET' });
  },

  // Institution Endpoints
  async getAdminStats() {
    return await this.request('/api/admin/stats', { method: 'GET' });
  },

  async getAdminEnrollments(params = {}) {
    const q = new URLSearchParams();
    if (params.search) q.append('search', params.search);
    if (params.courseId && params.courseId !== 'ALL') q.append('courseId', params.courseId);

    const queryStr = q.toString() ? `?${q.toString()}` : '';
    return await this.request(`/api/admin/enrollments${queryStr}`, { method: 'GET' });
  },

  async getAdminStudents() {
    return await this.request('/api/admin/students', { method: 'GET' });
  }
};
