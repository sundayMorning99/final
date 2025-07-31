import { useState } from 'react';
import { authService } from '../utils/auth';

const API_BASE_URL = 'http://localhost:8080';

const UserProfile = ({ user }) => {
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [passwordData, setPasswordData] = useState({currentPassword: '', newPassword: '', confirmPassword: ''});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handlePasswordChange = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');

    if (passwordData.newPassword !== passwordData.confirmPassword) {
      setError('New passwords do not match');
      setLoading(false);
      return;
    }

    try {
      const response = await fetch(`${API_BASE_URL}/api/auth/change-password`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          ...authService.getAuthHeaders()
        },
        body: JSON.stringify({
          currentPassword: passwordData.currentPassword,
          newPassword: passwordData.newPassword
        })
      })

      if (response.ok) {
        setSuccess('Password changed successfully');
        setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' });
        setShowPasswordModal(false);
      } else {
        const errorText = await response.text();
        setError(errorText || 'Failed to change password');
      }
    } catch (error) {
      setError('Network error occurred');
    } finally {
      setLoading(false);
    }
  }

  const resetPasswordForm = () => {
    setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' });
    setError('');
    setSuccess('');
  };

  return (
    <div>
      <h2>User Profile</h2>
      
      {success && <div className="alert alert-success">{success}</div>}
      
      <div className="card">
        <div className="card-body">
          <h5 className="card-title">Account Information</h5>
          <p><strong>Username:</strong> {user.username}</p>
          <p><strong>Role:</strong> {user.role}</p>
          
          <button className="btn btn-primary" onClick={() => {
              resetPasswordForm()
              setShowPasswordModal(true)
            }}
          >
            Change Password
          </button>
        </div>
      </div>

      {showPasswordModal && (
        <div className="modal show d-block" tabIndex="-1">
          <div className="modal-dialog">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Change Password</h5>
                <button type="button" className="btn-close" onClick={() => setShowPasswordModal(false)}></button>
              </div>
              <form onSubmit={handlePasswordChange}>
                <div className="modal-body">
                  <div className="mb-3">
                    <label htmlFor="currentPassword" className="form-label">Current Password</label>
                    <input id="currentPassword" type="password" className="form-control" value={passwordData.currentPassword} onChange={(e) => setPasswordData({...passwordData, currentPassword: e.target.value})} required/>
                  </div>
                  <div className="mb-3">
                    <label htmlFor="newPassword" className="form-label">New Password</label>
                    <input id="newPassword" type="password" className="form-control" value={passwordData.newPassword} onChange={(e) => setPasswordData({...passwordData, newPassword: e.target.value})} required/>
                  </div>
                  <div className="mb-3">
                    <label htmlFor="confirmPassword" className="form-label">Confirm New Password</label>
                    <input id="confirmPassword" type="password" className="form-control" value={passwordData.confirmPassword} onChange={(e) => setPasswordData({...passwordData, confirmPassword: e.target.value})} required/>
                  </div>
                  {error && <div className="alert alert-danger">{error}</div>}
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-secondary" onClick={() => setShowPasswordModal(false)}>Cancel</button>
                  <button type="submit" className="btn btn-primary" disabled={loading}>{loading ? 'Changing...' : 'Change Password'}</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
      {showPasswordModal && <div className="modal-backdrop show"></div>}
    </div>
  )
}

export default UserProfile;