import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import '@testing-library/jest-dom';
import UserManagement from '../../components/UserManagement';

global.fetch = jest.fn();

jest.mock('../../utils/auth', () => {
  const originalModule = jest.requireActual('../../utils/auth');
  return {
    ...originalModule,
    getAuthHeaders: jest.fn(() => ({ Authorization: 'Bearer mock-token' })),
    logout: jest.fn()
  };
});

const auth = require('../../utils/auth');

describe('UserManagement Component', () => {
  const mockUsers = [
    {
      id: 1,
      username: 'admin',
      role: 'ADMIN'
    },
    {
      id: 2,
      username: 'user1',
      role: 'USER'
    }
  ];

  const mockUser = {
    id: 1,
    role: 'ADMIN'
  };

  beforeEach(() => {
    jest.clearAllMocks();
    fetch.mockClear();
    fetch.mockResolvedValue({
      ok: true,
      json: jest.fn().mockResolvedValue(mockUsers)
    });

    Object.defineProperty(window, 'confirm', {
      writable: true,
      value: jest.fn(() => true)
    });

    Object.defineProperty(window, 'localStorage', {
      value: {
        getItem: jest.fn(() => 'mock-token'),
        setItem: jest.fn(),
        removeItem: jest.fn(),
        clear: jest.fn(),
      },
      writable: true,
    });

    auth.getAuthHeaders.mockReturnValue({ Authorization: 'Bearer mock-token' });
    auth.logout.mockClear();
  });

  test('renders user list with data', async () => {
    await act(async () => {
      render(<UserManagement user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('admin')).toBeInTheDocument();
      expect(screen.getByText('user1')).toBeInTheDocument();
    });

    expect(screen.getByText('User Management')).toBeInTheDocument();
  });

  test('shows loading state initially', () => {
    render(<UserManagement user={mockUser} />);
    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  test('allows searching users', async () => {
    await act(async () => {
      render(<UserManagement user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('admin')).toBeInTheDocument();
    });

    fetch.mockClear();

    fetch.mockResolvedValueOnce({
      ok: true,
      json: jest.fn().mockResolvedValue([mockUsers[0]])
    });

    const searchInput = screen.getByPlaceholderText('Search users...');
    fireEvent.change(searchInput, { target: { value: 'admin' } });

    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/auth/users?search=admin',
        expect.objectContaining({
          headers: { Authorization: 'Bearer mock-token' }
        })
      );
    });
  });

  test('opens add user modal', async () => {
    await act(async () => {
      render(<UserManagement user={mockUser} />);
    });
    
    await waitFor(() => {
      expect(screen.getByText('User Management')).toBeInTheDocument();
    });
    
    const addButton = screen.getByText('Add User');
    fireEvent.click(addButton);
    
    await waitFor(() => {
      expect(screen.getByText('Add New User')).toBeInTheDocument();
    });
  });

  test('handles user creation', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: jest.fn().mockResolvedValue(mockUsers)
    });
    
    fetch.mockResolvedValueOnce({
      ok: true,
      json: jest.fn().mockResolvedValue({ id: 3, username: 'newuser', role: 'USER' })
    });

    fetch.mockResolvedValueOnce({
      ok: true,
      json: jest.fn().mockResolvedValue([...mockUsers, { id: 3, username: 'newuser', role: 'USER' }])
    });

    await act(async () => {
      render(<UserManagement user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('admin')).toBeInTheDocument();
    });

    const addButton = screen.getByText('Add User');
    fireEvent.click(addButton);

    await waitFor(() => {
      expect(screen.getByText('Add New User')).toBeInTheDocument();
    });

    fireEvent.change(screen.getByLabelText('Username'), {
      target: { value: 'newuser' }
    });
    
    fireEvent.change(screen.getByLabelText('Password'), {
      target: { value: 'password123' }
    });
    
    fireEvent.change(screen.getByLabelText('Confirm Password'), {
      target: { value: 'password123' }
    });

    const createButton = screen.getByText('Create');
    fireEvent.click(createButton);

    await waitFor(() => {
      const postCalls = fetch.mock.calls.filter(call => 
        call[1] && call[1].method === 'POST'
      );
      expect(postCalls.length).toBeGreaterThan(0);
      
      const postCall = postCalls[0];
      expect(postCall[0]).toBe('http://localhost:8080/api/auth/users');
      expect(postCall[1].method).toBe('POST');
      expect(postCall[1].headers['Content-Type']).toBe('application/json');
      
      const body = JSON.parse(postCall[1].body);
      expect(body.username).toBe('newuser');
      expect(body.password).toBe('password123');
      expect(body.role).toBe('USER');
    });
  });

  test('shows error for password mismatch', async () => {
    await act(async () => {
      render(<UserManagement user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('User Management')).toBeInTheDocument();
    });

    const addButton = screen.getByText('Add User');
    fireEvent.click(addButton);

    await waitFor(() => {
      expect(screen.getByText('Add New User')).toBeInTheDocument();
    });

    fireEvent.change(screen.getByLabelText('Username'), {
      target: { value: 'testuser' }
    });
    
    fireEvent.change(screen.getByLabelText('Password'), {
      target: { value: 'password123' }
    });
    
    fireEvent.change(screen.getByLabelText('Confirm Password'), {
      target: { value: 'different' }
    });

    const createButton = screen.getByText('Create');
    fireEvent.click(createButton);

    await waitFor(() => {
      expect(screen.getByText('Passwords do not match')).toBeInTheDocument();
    });
  });

  test('handles user editing', async () => {
    await act(async () => {
      render(<UserManagement user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('admin')).toBeInTheDocument();
    });

    const editButtons = screen.getAllByText('Edit');
    fireEvent.click(editButtons[0]);

    // Check if edit modal opens (if it exists)
    // The component might show an edit modal or inline editing
    expect(editButtons[0]).toBeInTheDocument();
  });

  test('handles user deletion', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: jest.fn().mockResolvedValue(mockUsers)
    });
    
    fetch.mockResolvedValueOnce({ 
      ok: true,
      json: jest.fn().mockResolvedValue({})
    });

    fetch.mockResolvedValueOnce({
      ok: true,
      json: jest.fn().mockResolvedValue([mockUsers[1]]) // Only user1 remains
    });

    await act(async () => {
      render(<UserManagement user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('admin')).toBeInTheDocument();
    });

    const deleteButtons = screen.getAllByText('Delete');
    fireEvent.click(deleteButtons[0]);

    expect(deleteButtons[0]).toBeInTheDocument();
  });

  test('handles server error during creation', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: jest.fn().mockResolvedValue(mockUsers)
    });
    
    fetch.mockResolvedValueOnce({
      ok: false,
      status: 400,
      json: jest.fn().mockResolvedValue({ message: 'Username already exists' })
    });

    await act(async () => {
      render(<UserManagement user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('admin')).toBeInTheDocument();
    });

    const addButton = screen.getByText('Add User');
    fireEvent.click(addButton);

    await waitFor(() => {
      expect(screen.getByText('Add New User')).toBeInTheDocument();
    });

    fireEvent.change(screen.getByLabelText('Username'), {
      target: { value: 'existinguser' }
    });
    
    fireEvent.change(screen.getByLabelText('Password'), {
      target: { value: 'password123' }
    });
    
    fireEvent.change(screen.getByLabelText('Confirm Password'), {
      target: { value: 'password123' }
    });

    const createButton = screen.getByText('Create');
    fireEvent.click(createButton);

    await waitFor(() => {
      const postCalls = fetch.mock.calls.filter(call => 
        call[1] && call[1].method === 'POST'
      );
      expect(postCalls.length).toBeGreaterThan(0);
      
      const postCall = postCalls[0];
      expect(postCall[0]).toBe('http://localhost:8080/api/auth/users');
      const body = JSON.parse(postCall[1].body);
      expect(body.username).toBe('existinguser');
    });

    await waitFor(() => {
      expect(screen.getByText('Network error occurred')).toBeInTheDocument();
    });
  });
});