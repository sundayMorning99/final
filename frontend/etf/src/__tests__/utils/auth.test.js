import { authService } from '../../utils/auth';

const mockLocalStorage = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
};
Object.defineProperty(window, 'localStorage', { value: mockLocalStorage });

global.fetch = jest.fn();

describe('authService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    fetch.mockClear();
  });

  describe('login', () => {
    it('should login successfully and store token', async () => {
      const mockResponse = {
        accessToken: { token: 'mock-jwt-token' }
      };
      
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockResponse
      });

      const result = await authService.login('testuser', 'password');

      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: 'testuser', password: 'password' })
      });
      
      expect(mockLocalStorage.setItem).toHaveBeenCalledWith('jwt_token', 'mock-jwt-token');
      expect(result).toEqual({ username: 'testuser', token: 'mock-jwt-token' });
    });

    it('should handle login failure', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        status: 401,
        text: async () => 'Invalid credentials'
      });

      await expect(authService.login('wronguser', 'wrongpass'))
        .rejects.toThrow('Invalid username or password');
    });

    it('should handle network errors', async () => {
      fetch.mockRejectedValueOnce(new Error('Network error'));

      await expect(authService.login('testuser', 'password'))
        .rejects.toThrow('Login failed - network error');
    });
  });

  describe('isAuthenticated', () => {
    it('should return true for valid token', () => {
      const futureTime = Math.floor(Date.now() / 1000) + 3600;
      const mockToken = 'header.' + btoa(JSON.stringify({ exp: futureTime })) + '.signature';
      mockLocalStorage.getItem.mockReturnValue(mockToken);

      expect(authService.isAuthenticated()).toBe(true);
    });

    it('should return false for expired token', () => {
      const pastTime = Math.floor(Date.now() / 1000) - 3600; 
      const mockToken = 'header.' + btoa(JSON.stringify({ exp: pastTime })) + '.signature';
      mockLocalStorage.getItem.mockReturnValue(mockToken);

      expect(authService.isAuthenticated()).toBe(false);
      expect(mockLocalStorage.removeItem).toHaveBeenCalledWith('jwt_token');
    });

    it('should return false for no token', () => {
      mockLocalStorage.getItem.mockReturnValue(null);
      expect(authService.isAuthenticated()).toBe(false);
    });
  });

  describe('getAuthHeaders', () => {
    it('should return authorization header with token', () => {
      mockLocalStorage.getItem.mockReturnValue('mock-token');
      const headers = authService.getAuthHeaders();
      expect(headers).toEqual({ Authorization: 'Bearer mock-token' });
    });

    it('should return empty object when no token', () => {
      mockLocalStorage.getItem.mockReturnValue(null);
      const headers = authService.getAuthHeaders();
      expect(headers).toEqual({});
    });
  });
});