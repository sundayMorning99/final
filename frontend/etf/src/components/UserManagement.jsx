import { useState, useEffect } from 'react';
import { authService } from '../utils/auth';
import UserTable from './UserTable';
import UserModal from './UserModal';

const API_BASE_URL = 'http://localhost:8080';

const UserManagement = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [formData, setFormData] = useState({username: '', role: 'USER', password: '', confirmPassword: ''});
  const [error, setError] = useState('');

  useEffect(() => {
    fetchUsers();
  }, [searchTerm]);

  const fetchUsers = async () => {
    try {
      const params = new URLSearchParams();
      if (searchTerm) params.append('search', searchTerm);
      
      const response = await fetch(`${API_BASE_URL}/api/auth/users?${params}`, {
        headers: {
          ...authService.getAuthHeaders()
        }
      })
      
      if (response.ok) {
        const data = await response.json();
        setUsers(data);
      }
    } catch (error) {
      console.error('Failed to fetch users:', error);
    } finally {
      setLoading(false);
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!editingUser && formData.password !== formData.confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    //Again, to combine update and create methods together, we check editingUser. 
    //If we're editing a user, we are updating and use 'PUT'.
    try {
      const url = editingUser ? 
        `${API_BASE_URL}/api/auth/users/${editingUser.id}` : 
        `${API_BASE_URL}/api/auth/users`
      const method = editingUser ? 'PUT' : 'POST';
      
      const requestBody = editingUser ? {
        username: formData.username,
        role: formData.role,
        newPassword: formData.password || null
      } : {
        username: formData.username,
        role: formData.role,
        password: formData.password
      }
      
      const response = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json',
          ...authService.getAuthHeaders()
        },
        body: JSON.stringify(requestBody)
      })
      
      if (response.ok) {
        setShowModal(false);
        setEditingUser(null);
        resetForm();
        fetchUsers();
      } else {
        const errorText = await response.text();
        setError(errorText || 'Failed to save user');
      }
    } catch (error) {
      setError('Network error occurred');
    }
  }

  const handleEdit = (user) => {
    setEditingUser(user);
    setFormData({
      username: user.username,
      role: user.role,
      password: '',
      confirmPassword: ''
    });
    setError('');
    setShowModal(true);
  }

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this user?')) {
      try {
        const response = await fetch(`${API_BASE_URL}/api/auth/users/${id}`, {
          method: 'DELETE',
          headers: {
            ...authService.getAuthHeaders()
          }
        })
        
        if (response.ok) {
          fetchUsers();
        } else {
          const errorText = await response.text();
          alert(errorText || 'Failed to delete user');
        }
      } catch (error) {
        alert('Network error occurred');
      }
    }
  }

  const resetForm = () => {
    setFormData({
      username: '',
      role: 'USER',
      password: '',
      confirmPassword: ''
    });
    setError('');
  }

  const handleAddUser = () => {
    setEditingUser(null);
    resetForm();
    setShowModal(true);
  }

  if (loading) {
    return <div className="text-center">Loading...</div>;
  }

  return (
    <div>
      <UserTable
        users={users}
        searchTerm={searchTerm}
        setSearchTerm={setSearchTerm}
        handleEdit={handleEdit}
        handleDelete={handleDelete}
        onAddUser={handleAddUser}
      />
      
      <UserModal
        showModal={showModal}
        setShowModal={setShowModal}
        editingUser={editingUser}
        formData={formData}
        setFormData={setFormData}
        handleSubmit={handleSubmit}
        error={error}
      />
    </div>
  )
}

export default UserManagement;