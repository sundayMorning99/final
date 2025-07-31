const UserModal = ({ showModal, setShowModal,   editingUser, formData, setFormData, handleSubmit, error 
}) => {
  if (!showModal) return null;

  return (
    <>
      <div className="modal show d-block" tabIndex="-1">
        <div className="modal-dialog">
          <div className="modal-content">
            <div className="modal-header">
              <h5 className="modal-title">
                {editingUser ? 'Edit User' : 'Add New User'}
              </h5>
              <button type="button" className="btn-close" onClick={() => setShowModal(false)}></button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="modal-body">
                <div className="mb-3">
                  <label htmlFor="username" className="form-label">Username</label>
                  <input id="username" type="text" className="form-control" value={formData.username} onChange={(e) => setFormData({...formData, username: e.target.value})} required />
                </div>
                <div className="mb-3">
                  <label className="form-label">Role</label>
                  <select className="form-select" value={formData.role} onChange={(e) => setFormData({...formData, role: e.target.value})}>
                    <option value="USER">User</option>
                    <option value="ADMIN">Admin</option>
                  </select>
                </div>
                <div className="mb-3">
                  <label htmlFor="password" className="form-label">{editingUser ? 'New Password (leave blank to keep current)' : 'Password'}</label>
                  <input id="password" type="password" className="form-control" value={formData.password} onChange={(e) => setFormData({...formData, password: e.target.value})} required={!editingUser}/>
                </div>
                <div className="mb-3">
                  <label htmlFor="confirmPassword" className="form-label">Confirm Password</label>
                  <input id="confirmPassword" type="password" className="form-control" value={formData.confirmPassword} onChange={(e) => setFormData({...formData, confirmPassword: e.target.value})} required={!editingUser || formData.password !== ''} />
                </div>
                {error && <div className="alert alert-danger">{error}</div>}
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">{editingUser ? 'Update' : 'Create'}</button>
              </div>
            </form>
          </div>
        </div>
      </div>
      <div className="modal-backdrop show"></div>
    </>
  )
}

export default UserModal;