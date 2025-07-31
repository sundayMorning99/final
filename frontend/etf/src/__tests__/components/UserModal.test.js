import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import UserModal from '../../components/UserModal';

const mockFormData = {
  username: 'testuser',
  role: 'USER',
  password: 'password123',
  confirmPassword: 'password123'
};

const mockProps = {
  showModal: true,
  setShowModal: jest.fn(),
  editingUser: null,
  formData: mockFormData,
  setFormData: jest.fn(),
  handleSubmit: jest.fn(),
  error: ''
};

describe('UserModal Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders modal when showModal is true', () => {
    render(<UserModal {...mockProps} />);
    
    expect(screen.getByText('Add New User')).toBeInTheDocument();
    expect(screen.getByText('Username')).toBeInTheDocument();
    expect(screen.getByText('Role')).toBeInTheDocument();
    expect(screen.getByText('Password')).toBeInTheDocument();
    expect(screen.getByText('Confirm Password')).toBeInTheDocument();
  });

  test('does not render when showModal is false', () => {
    const props = { ...mockProps, showModal: false };
    render(<UserModal {...props} />);
    
    expect(screen.queryByText('Add New User')).not.toBeInTheDocument();
  });

  test('shows edit title when editing user', () => {
    const props = { ...mockProps, editingUser: { id: 1, username: 'testuser' } };
    render(<UserModal {...props} />);
    
    expect(screen.getByText('Edit User')).toBeInTheDocument();
    expect(screen.getByText('Update')).toBeInTheDocument();
  });

  test('shows different password label when editing', () => {
    const props = { ...mockProps, editingUser: { id: 1, username: 'testuser' } };
    render(<UserModal {...props} />);
    
    expect(screen.getByText('New Password (leave blank to keep current)')).toBeInTheDocument();
  });

  test('password not required when editing user', () => {
    const props = { ...mockProps, editingUser: { id: 1, username: 'testuser' } };
    render(<UserModal {...props} />);
    
    const passwordInputs = screen.getAllByDisplayValue('password123');
    expect(passwordInputs[0]).not.toHaveAttribute('required');
  });

  test('password required when creating user', () => {
    render(<UserModal {...mockProps} />);
    
    const passwordInputs = screen.getAllByDisplayValue('password123');
    expect(passwordInputs[0]).toHaveAttribute('required');
  });

  test('displays form data in inputs', () => {
    render(<UserModal {...mockProps} />);
    
    expect(screen.getByDisplayValue('testuser')).toBeInTheDocument();
    const roleSelect = screen.getByRole('combobox');
    expect(roleSelect.value).toBe('USER');
    expect(screen.getAllByDisplayValue('password123')).toHaveLength(2);
  });

  test('calls setFormData when inputs change', () => {
    render(<UserModal {...mockProps} />);
    
    const usernameInput = screen.getByDisplayValue('testuser');
    fireEvent.change(usernameInput, { target: { value: 'newuser' } });
    
    expect(mockProps.setFormData).toHaveBeenCalledWith({
      ...mockFormData,
      username: 'newuser'
    });
  });

  test('calls setFormData when role changes', () => {
    render(<UserModal {...mockProps} />);
    
    const roleSelect = screen.getByRole('combobox');
    fireEvent.change(roleSelect, { target: { value: 'ADMIN' } });
    
    expect(mockProps.setFormData).toHaveBeenCalledWith({
      ...mockFormData,
      role: 'ADMIN'
    });
  });

  test('displays error message when error prop is provided', () => {
    const props = { ...mockProps, error: 'Username already exists' };
    render(<UserModal {...props} />);
    
    expect(screen.getByText('Username already exists')).toBeInTheDocument();
  });

  test('calls handleSubmit when form is submitted', () => {
    render(<UserModal {...mockProps} />);
    
    const form = document.querySelector('form');
    fireEvent.submit(form);
    
    expect(mockProps.handleSubmit).toHaveBeenCalled();
  });

  test('calls setShowModal when close button is clicked', () => {
    render(<UserModal {...mockProps} />);
    
    const closeButton = screen.getByRole('button', { name: '' }); 
    fireEvent.click(closeButton);
    
    expect(mockProps.setShowModal).toHaveBeenCalledWith(false);
  });

  test('calls setShowModal when cancel button is clicked', () => {
    render(<UserModal {...mockProps} />);
    
    const cancelButton = screen.getByText('Cancel');
    fireEvent.click(cancelButton);
    
    expect(mockProps.setShowModal).toHaveBeenCalledWith(false);
  });

  test('confirm password required when password is provided during edit', () => {
    const props = { 
      ...mockProps, 
      editingUser: { id: 1, username: 'testuser' },
      formData: { ...mockFormData, password: 'newpass' }
    };
    render(<UserModal {...props} />);
    
    const passwordInputs = screen.getAllByDisplayValue('password123');
    if (passwordInputs[1]) {
      expect(passwordInputs[1]).toHaveAttribute('required');
    }
  });

  test('renders modal backdrop', () => {
    render(<UserModal {...mockProps} />);
    
    const modal = document.querySelector('.modal.show.d-block');
    expect(modal).toBeInTheDocument();
    
    const backdrop = document.querySelector('.modal-backdrop.show');
    expect(backdrop).toBeInTheDocument();
  });

  test('handles password input changes', () => {
    render(<UserModal {...mockProps} />);
    
    const passwordInputs = screen.getAllByDisplayValue('password123');
    const passwordInput = passwordInputs[0];
    
    fireEvent.change(passwordInput, { target: { value: 'newpassword' } });
    
    expect(mockProps.setFormData).toHaveBeenCalledWith({
      ...mockFormData,
      password: 'newpassword'
    });
  });

  test('handles confirm password input changes', () => {
    render(<UserModal {...mockProps} />);
    
    const passwordInputs = screen.getAllByDisplayValue('password123');
    const confirmPasswordInput = passwordInputs[1];
    
    fireEvent.change(confirmPasswordInput, { target: { value: 'newconfirm' } });
    
    expect(mockProps.setFormData).toHaveBeenCalledWith({
      ...mockFormData,
      confirmPassword: 'newconfirm'
    });
  });

  test('renders correct form elements for new user', () => {
    render(<UserModal {...mockProps} />);
    
    expect(screen.getByDisplayValue('testuser')).toBeInTheDocument();
    expect(screen.getByRole('combobox')).toBeInTheDocument();
    expect(screen.getAllByDisplayValue('password123')).toHaveLength(2);
    expect(screen.getByText('Create')).toBeInTheDocument();
  });

  test('renders correct form elements for editing user', () => {
    const props = { ...mockProps, editingUser: { id: 1, username: 'testuser' } };
    render(<UserModal {...props} />);
    
    expect(screen.getByDisplayValue('testuser')).toBeInTheDocument();
    expect(screen.getByRole('combobox')).toBeInTheDocument();
    expect(screen.getByText('Update')).toBeInTheDocument();
    expect(screen.getByText('New Password (leave blank to keep current)')).toBeInTheDocument();
  });
});