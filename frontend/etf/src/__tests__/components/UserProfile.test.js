import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import UserProfile from '../../components/UserProfile';

global.fetch = jest.fn();

describe('UserProfile Component', () => {
  const mockUser = {
    username: 'testuser',
    role: 'USER'
  };

  beforeEach(() => {
    fetch.mockClear();
    Object.defineProperty(window, 'localStorage', {
      value: {
        getItem: jest.fn(() => 'mock-token'),
        setItem: jest.fn(),
        removeItem: jest.fn(),
        clear: jest.fn(),
      },
      writable: true,
    });
  });

  test('renders user profile information', () => {
    render(<UserProfile user={mockUser} />);
    
    expect(screen.getByText('User Profile')).toBeInTheDocument();
    expect(screen.getByText('Account Information')).toBeInTheDocument();
    expect(screen.getByText('testuser')).toBeInTheDocument();
    expect(screen.getByText('USER')).toBeInTheDocument();
  });

  test('opens change password modal', () => {
    render(<UserProfile user={mockUser} />);
    
    const cardButton = document.querySelector('.card-body button');
    fireEvent.click(cardButton);
    
    expect(screen.getByLabelText('Current Password')).toBeInTheDocument();
    expect(screen.getByLabelText('New Password')).toBeInTheDocument();
    expect(screen.getByLabelText('Confirm New Password')).toBeInTheDocument();
    expect(document.querySelector('.modal.show')).toBeInTheDocument();
  });

  test('handles successful password change', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ message: 'Password changed successfully' })
    });

    render(<UserProfile user={mockUser} />);
    
    const cardButton = document.querySelector('.card-body button');
    fireEvent.click(cardButton);

    fireEvent.change(screen.getByLabelText('Current Password'), {
      target: { value: 'oldpass' }
    });
    fireEvent.change(screen.getByLabelText('New Password'), {
      target: { value: 'newpass' }
    });
    fireEvent.change(screen.getByLabelText('Confirm New Password'), {
      target: { value: 'newpass' }
    });
    
    const modal = document.querySelector('.modal.show');
    const submitButton = modal.querySelector('button[type="submit"]');
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/auth/change-password', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer mock-token'
        },
        body: JSON.stringify({
          currentPassword: 'oldpass',
          newPassword: 'newpass'
        })
      });
    });
  });

  test('shows error for password mismatch', async () => {
    render(<UserProfile user={mockUser} />);
    
    const cardButton = document.querySelector('.card-body button');
    fireEvent.click(cardButton);

    fireEvent.change(screen.getByLabelText('Current Password'), {
      target: { value: 'oldpass' }
    });
    fireEvent.change(screen.getByLabelText('New Password'), {
      target: { value: 'newpass' }
    });
    fireEvent.change(screen.getByLabelText('Confirm New Password'), {
      target: { value: 'different' }
    });
    
    const modal = document.querySelector('.modal.show');
    const submitButton = modal.querySelector('button[type="submit"]');
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('New passwords do not match')).toBeInTheDocument();
    });
  });

  test('shows success message after password change', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ message: 'Password changed successfully' })
    });

    render(<UserProfile user={mockUser} />);
    
    const cardButton = document.querySelector('.card-body button');
    fireEvent.click(cardButton);

    fireEvent.change(screen.getByLabelText('Current Password'), {
      target: { value: 'oldpass' }
    });
    fireEvent.change(screen.getByLabelText('New Password'), {
      target: { value: 'newpass' }
    });
    fireEvent.change(screen.getByLabelText('Confirm New Password'), {
      target: { value: 'newpass' }
    });
    
    const modal = document.querySelector('.modal.show');
    const submitButton = modal.querySelector('button[type="submit"]');
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Password changed successfully')).toBeInTheDocument();
    });
  });
});