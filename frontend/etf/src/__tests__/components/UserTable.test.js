import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import UserTable from '../../components/UserTable';

describe('UserTable Component', () => {
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

  const mockProps = {
    users: mockUsers,
    searchTerm: '',
    setSearchTerm: jest.fn(),
    onSearchChange: jest.fn(),
    handleEdit: jest.fn(),
    handleDelete: jest.fn(),
    onAddUser: jest.fn(),
    currentUser: { id: 1, role: 'ADMIN' }
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders user table with data', () => {
    render(<UserTable {...mockProps} />);
    
    expect(screen.getByText('User Management')).toBeInTheDocument();
    expect(screen.getByText('admin')).toBeInTheDocument();
    expect(screen.getByText('user1')).toBeInTheDocument();
    expect(screen.getByText('ID')).toBeInTheDocument();
    expect(screen.getByText('Username')).toBeInTheDocument();
    expect(screen.getByText('Role')).toBeInTheDocument();
    expect(screen.getByText('Actions')).toBeInTheDocument();
  });

  it('shows correct role badges', () => {
    render(<UserTable {...mockProps} />);
    
    expect(screen.getByText('ADMIN')).toBeInTheDocument();
    expect(screen.getByText('USER')).toBeInTheDocument();
  });

  it('displays user IDs correctly', () => {
    render(<UserTable {...mockProps} />);
    
    expect(screen.getByText('1')).toBeInTheDocument();
    expect(screen.getByText('2')).toBeInTheDocument();
  });

  it('shows Edit and Delete buttons for each user', () => {
    render(<UserTable {...mockProps} />);
    
    const editButtons = screen.getAllByText('Edit');
    const deleteButtons = screen.getAllByText('Delete');
    
    expect(editButtons).toHaveLength(2);
    expect(deleteButtons).toHaveLength(2);
  });

  it('handles search input change', () => {
    render(<UserTable {...mockProps} />);
    
    const searchInput = screen.getByPlaceholderText('Search users...');
    fireEvent.change(searchInput, { target: { value: 'admin' } });

    expect(mockProps.setSearchTerm).toHaveBeenCalledWith('admin');
  });

  it('calls handleEdit when edit button is clicked', () => {
    render(<UserTable {...mockProps} />);
    
    const editButtons = screen.getAllByText('Edit');
    fireEvent.click(editButtons[0]);
    
    expect(mockProps.handleEdit).toHaveBeenCalledWith(mockUsers[0]);
  });

  it('calls handleDelete when delete button is clicked', () => {
    render(<UserTable {...mockProps} />);
    
    const deleteButtons = screen.getAllByText('Delete');
    fireEvent.click(deleteButtons[0]);
    
    expect(mockProps.handleDelete).toHaveBeenCalledWith(1);
  });

  it('calls onAddUser when Add User button is clicked', () => {
    render(<UserTable {...mockProps} />);
    
    const addButton = screen.getByText('Add User');
    fireEvent.click(addButton);
    
    expect(mockProps.onAddUser).toHaveBeenCalled();
  });

  it('shows empty table when users array is empty', () => {
    render(<UserTable {...mockProps} users={[]} />);
    
    // The table should still render with headers but no body content
    expect(screen.getByText('User Management')).toBeInTheDocument();
    expect(screen.getByText('ID')).toBeInTheDocument();
    expect(screen.getByText('Username')).toBeInTheDocument();
    expect(screen.getByText('Role')).toBeInTheDocument();
    expect(screen.getByText('Actions')).toBeInTheDocument();
    
    // Check that there are no user rows
    expect(screen.queryByText('admin')).not.toBeInTheDocument();
    expect(screen.queryByText('user1')).not.toBeInTheDocument();
  });

  it('displays search term in input field', () => {
    render(<UserTable {...mockProps} searchTerm="test search" />);
    
    const searchInput = screen.getByPlaceholderText('Search users...');
    expect(searchInput.value).toBe('test search');
  });

  it('renders correct table headers', () => {
    render(<UserTable {...mockProps} />);
    
    expect(screen.getByText('ID')).toBeInTheDocument();
    expect(screen.getByText('Username')).toBeInTheDocument();
    expect(screen.getByText('Role')).toBeInTheDocument();
    expect(screen.getByText('Actions')).toBeInTheDocument();
  });

  it('applies correct CSS classes to role badges', () => {
    render(<UserTable {...mockProps} />);
    
    const adminBadge = screen.getByText('ADMIN');
    const userBadge = screen.getByText('USER');
    
    expect(adminBadge).toHaveClass('badge');
    expect(userBadge).toHaveClass('badge');
  });

  it('renders table with responsive wrapper', () => {
    render(<UserTable {...mockProps} />);
    
    const tableWrapper = document.querySelector('.table-responsive');
    expect(tableWrapper).toBeInTheDocument();
  });

  it('handles multiple user interactions', () => {
    render(<UserTable {...mockProps} />);
    
    const searchInput = screen.getByPlaceholderText('Search users...');
    fireEvent.change(searchInput, { target: { value: 'user1' } });
    expect(mockProps.setSearchTerm).toHaveBeenCalledWith('user1');
    
    const editButtons = screen.getAllByText('Edit');
    fireEvent.click(editButtons[1]);
    expect(mockProps.handleEdit).toHaveBeenCalledWith(mockUsers[1]);
    
    const deleteButtons = screen.getAllByText('Delete');
    fireEvent.click(deleteButtons[1]);
    expect(mockProps.handleDelete).toHaveBeenCalledWith(2);
  });
});