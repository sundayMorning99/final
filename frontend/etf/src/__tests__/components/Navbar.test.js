import { render, screen, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Navbar from '../../components/Navbar';

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useLocation: () => ({ pathname: '/etfs' })
}));

const NavbarWithRouter = ({ user, onLogout }) => (
  <BrowserRouter>
    <Navbar user={user} onLogout={onLogout} />
  </BrowserRouter>
);

describe('Navbar Component', () => {
  const mockUser = { username: 'testuser', role: 'USER' };
  const mockAdminUser = { username: 'admin', role: 'ADMIN' };
  const mockOnLogout = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders navbar with user information', () => {
    render(<NavbarWithRouter user={mockUser} onLogout={mockOnLogout} />);
    
    expect(screen.getByText('ETF Portfolio')).toBeInTheDocument();
    expect(screen.getByText('ETFs')).toBeInTheDocument();
    expect(screen.getByText('Portfolios')).toBeInTheDocument();
    expect(screen.getByText('testuser')).toBeInTheDocument();
  });

  it('shows Users link for admin users', () => {
    render(<NavbarWithRouter user={mockAdminUser} onLogout={mockOnLogout} />);
    
    expect(screen.getByText('Users')).toBeInTheDocument();
    expect(screen.getByText('admin (Admin)')).toBeInTheDocument();
  });

  it('does not show Users link for regular users', () => {
    render(<NavbarWithRouter user={mockUser} onLogout={mockOnLogout} />);
    
    expect(screen.queryByText('Users')).not.toBeInTheDocument();
  });

  it('handles logout when logout button is clicked', () => {
    render(<NavbarWithRouter user={mockUser} onLogout={mockOnLogout} />);
    
    // Note: This test assumes the dropdown functionality works
    // In a real test, you might need to handle Bootstrap dropdown behavior
    const logoutButton = screen.getByText('Logout');
    fireEvent.click(logoutButton);
    
    expect(mockOnLogout).toHaveBeenCalled();
  });
});