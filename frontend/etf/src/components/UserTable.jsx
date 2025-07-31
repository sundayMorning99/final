const UserTable = ({ users, searchTerm, setSearchTerm, handleEdit, handleDelete, onAddUser 
}) => {
  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>User Management</h2>
        <button className="btn btn-primary" onClick={onAddUser}>Add User</button>
      </div>

      <div className="row mb-3">
        <div className="col-md-6">
          <input type="text" className="form-control" placeholder="Search users..." value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)}/>
        </div>
      </div>

      <div className="table-responsive">
        <table className="table table-striped">
          <thead>
            <tr>
              <th>ID</th>
              <th>Username</th>
              <th>Role</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.map(user => (
              <tr key={user.id}>
                <td>{user.id}</td>
                <td><strong>{user.username}</strong></td>
                <td>
                  <span className={`badge ${user.role === 'ADMIN' ? 'bg-danger' : 'bg-primary'}`}>
                    {user.role}
                  </span>
                </td>
                <td>
                  <button className="btn btn-sm btn-outline-primary me-2" onClick={() => handleEdit(user)}>Edit</button>
                  <button className="btn btn-sm btn-outline-danger" onClick={() => handleDelete(user.id)}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

export default UserTable;