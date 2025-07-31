const TOKEN_KEY = 'jwt_token';
const API_BASE_URL = 'http://localhost:8080';

// Fraho has built-in endpoints: /auth/login, /auth/logout, and /auth/refresh.
// Fraho-generated token has accessToken and refreshToken keys. 
// If we send key-value data, Fraho returns a token.
export const authService = {
  login: async (username, password) => {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username, password }),
      });

      if (response.ok) {
        const data = await response.json();
        
        // Fraho returns token in accessToken.token structure - This is something we need to memorize
        const token = data.accessToken ? data.accessToken.token : data.token;
        
        if (!token) {
          console.error('No token found in response:', data);
          throw new Error('No token received from server');
        }
        
        // saving the token as key-value pair.
        localStorage.setItem(TOKEN_KEY, token);
        console.log('Token stored successfully');
        return { username, token };
      } else {
        const errorText = await response.text();
        console.error('Login failed with status:', response.status, 'Error:', errorText);
        throw new Error('Invalid username or password');
      }
    } catch (error) {
      console.error('Login error:', error);
      if (error.message === 'Invalid username or password') {
        throw error;
      }
      throw new Error('Login failed - network error');
    }
  },

  register: async (username, password) => {
    try {
      const requestBody = JSON.stringify({ username, password });
      
      const response = await fetch(`${API_BASE_URL}/api/register`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: requestBody,
      });

      if (response.ok) {
        const responseText = await response.text();
        return await authService.login(username, password);
      } else {
        const errorText = await response.text();
        console.error('Registration failed with status:', response.status, 'Error:', errorText);
        
        let detailedError;
        try {
          const errorJson = JSON.parse(errorText);
          console.error('Parsed error details:', errorJson);
          detailedError = errorJson.message || errorJson.error || 'Registration failed';
        } catch (parseError) {
          console.error('Could not parse error as JSON:', parseError);
          detailedError = errorText || 'Registration failed';
        }
        
        throw new Error(detailedError);
      }
    } catch (error) {
      console.error('Registration error:', error);
      if (error.message.includes('already taken') || error.message.includes('required')) {
        throw error;
      }
      throw new Error('Registration failed - network error');
    }
  },

  logout: async () => {
    console.log('Logout attempt');
    
    try {
      await fetch(`${API_BASE_URL}/auth/logout`, {
        method: 'POST',
        headers: {
          ...this.getAuthHeaders()
        }
      });
    } catch (error) {
      console.log('Logout request failed, but clearing local storage anyway:', error);
    }
    
    // You can just use key to remove the token from the localStroage.
    localStorage.removeItem(TOKEN_KEY);
  },

  refreshToken: async () => {
    console.log('Token refresh attempt');
    
    try {
      const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
        method: 'GET',
        headers: {
          ...this.getAuthHeaders()
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        localStorage.setItem(TOKEN_KEY, data.token);
        return data.token;
      } else {
        console.log('Token refresh failed:', response.status);
      }
    } catch (error) {
      console.error('Token refresh error:', error);
    }
    return null;
  },

  getToken: () => {
    return localStorage.getItem(TOKEN_KEY);
  },

  isAuthenticated: () => {
    const token = localStorage.getItem(TOKEN_KEY);
    if (!token) return false;
    
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const isValid = payload.exp > Date.now() / 1000;
      
      if (!isValid) {
        localStorage.removeItem(TOKEN_KEY);
      }
      
      return isValid;
    } catch (error) {
      console.error('Token validation error:', error);
      localStorage.removeItem(TOKEN_KEY);
      return false;
    }
  },

  getAuthHeaders: () => {
    const token = localStorage.getItem(TOKEN_KEY);
    return token ? { Authorization: `Bearer ${token}` } : {};
  },

  // Helper method to get current user info
  getCurrentUser: async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/auth/user`, {
        headers: {
          ...this.getAuthHeaders()
        }
      });

      if (response.ok) {
        return await response.json();
      } else if (response.status === 401) {
        const newToken = await this.refreshToken();
        if (newToken) {
          // Retry with new token
          const retryResponse = await fetch(`${API_BASE_URL}/api/auth/user`, {
            headers: {
              ...this.getAuthHeaders()
            }
          });
          if (retryResponse.ok) {
            return await retryResponse.json();
          }
        }
        this.logout();
      }
    } catch (error) {
      console.error('Get current user error:', error);
    }
    return null;
  }
};