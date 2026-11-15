import axios from 'axios';

const API_BASE_URL = 'http://localhost:8085';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 35000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Response interceptor for consistent error handling
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    let message = 'An unexpected error occurred.';

    const data = error.response?.data;

    if (typeof data === 'string') {
      message = data;
    } else if (data?.detail) {
      message = data.detail;
    } else if (data?.message) {
      message = data.message;
    } else if (data?.title) {
      message = data.title;
    } else if (error.message) {
      message = error.message;
    }

    return Promise.reject(new Error(message));
  }
);

export const api = {
  // 1. Natural Language Query
  submitQuery: async ({ question, conversationId, previousQuestion, previousSql }) => {
    const response = await apiClient.post('/api/query', {
      question,
      conversationId,
      previousQuestion,
      previousSql,
    });
    return response.data;
  },

  // 2. Dashboard Metrics
  getDashboardStats: async () => {
    const response = await apiClient.get('/api/dashboard');
    return response.data;
  },

  // 3. Analytics
  getDepartmentAnalytics: async () => {
    const response = await apiClient.get('/api/analytics/department');
    return response.data;
  },

  getAttendanceAnalytics: async () => {
    const response = await apiClient.get('/api/analytics/attendance');
    return response.data;
  },

  getPlacementAnalytics: async () => {
    const response = await apiClient.get('/api/analytics/placements');
    return response.data;
  },

  // 4. Schema Explorer
  getSchema: async () => {
    const response = await apiClient.get('/api/schema');
    return response.data;
  },

  // 5. Query History
  getHistory: async (limit = 50) => {
    const response = await apiClient.get(`/api/history?limit=${limit}`);
    return response.data;
  },

  getHistoryById: async (id) => {
    const response = await apiClient.get(`/api/history/${id}`);
    return response.data;
  },

  // 6. Health
  getHealth: async () => {
    const response = await apiClient.get('/api/health');
    return response.data;
  },
};

export default api;
