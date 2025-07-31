import { render } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import App from '../App';
import { authService } from '../utils/auth';

jest.mock('../utils/auth', () => ({
  authService: {
    isAuthenticated: jest.fn(),
    getCurrentUser: jest.fn(),
    logout: jest.fn()
  }
}));

beforeEach(() => {
  jest.clearAllMocks();
});

describe('App Component', () => {
  test('renders without crashing', () => {
    authService.isAuthenticated.mockReturnValue(false);
    authService.getCurrentUser.mockResolvedValue(null);
    
    expect(() => render(
      <MemoryRouter>
        <App />
      </MemoryRouter>
    )).not.toThrow();
  });

  test('calls isAuthenticated on mount', () => {
    authService.isAuthenticated.mockReturnValue(false);
    authService.getCurrentUser.mockResolvedValue(null);
    
    render(
      <MemoryRouter>
        <App />
      </MemoryRouter>
    );
    
    expect(authService.isAuthenticated).toHaveBeenCalled();
  });
});