import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import Login from '../../components/Login';
import { authService } from '../../utils/auth';

jest.mock('../../utils/auth');

describe('Login Component', () => {
  const mockOnLogin = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders login form by default', () => {
    render(<Login onLogin={mockOnLogin} />);
    
    expect(screen.getByText('Login - ETF Portfolio Manager')).toBeInTheDocument();
    expect(screen.getByLabelText('Username')).toBeInTheDocument();
    expect(screen.getByLabelText('Password')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Login' })).toBeInTheDocument();
  });

  it('switches to registration form', () => {
    render(<Login onLogin={mockOnLogin} />);
    
    fireEvent.click(screen.getByText('Need an account? Register'));
    
    expect(screen.getByText('Register - ETF Portfolio Manager')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Register' })).toBeInTheDocument();
  });

  it('handles successful login', async () => {
    const mockUserData = { username: 'testuser', token: 'mock-token' };
    authService.login.mockResolvedValue(mockUserData);

    render(<Login onLogin={mockOnLogin} />);
    
    fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'testuser' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'password' } });
    fireEvent.click(screen.getByRole('button', { name: 'Login' }));

    await waitFor(() => {
      expect(authService.login).toHaveBeenCalledWith('testuser', 'password');
      expect(mockOnLogin).toHaveBeenCalledWith(mockUserData);
    });
  });

  it('handles login failure', async () => {
    authService.login.mockRejectedValue(new Error('Invalid credentials'));

    render(<Login onLogin={mockOnLogin} />);
    
    fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'wronguser' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'wrongpass' } });
    fireEvent.click(screen.getByRole('button', { name: 'Login' }));

    await waitFor(() => {
      expect(screen.getByText('Invalid username or password')).toBeInTheDocument();
    });
  });

  it('handles successful registration', async () => {
    const mockUserData = { username: 'newuser', token: 'mock-token' };
    authService.register.mockResolvedValue(mockUserData);

    render(<Login onLogin={mockOnLogin} />);
    
    fireEvent.click(screen.getByText('Need an account? Register'));
    fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'newuser' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'password' } });
    fireEvent.click(screen.getByRole('button', { name: 'Register' }));

    await waitFor(() => {
      expect(authService.register).toHaveBeenCalledWith('newuser', 'password');
      expect(mockOnLogin).toHaveBeenCalledWith(mockUserData);
    });
  });

  it('shows loading state during submission', async () => {
    authService.login.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)));

    render(<Login onLogin={mockOnLogin} />);
    
    fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'testuser' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'password' } });
    fireEvent.click(screen.getByRole('button', { name: 'Login' }));

    expect(screen.getByText('Logging in...')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Logging in...' })).toBeDisabled();
  });
});