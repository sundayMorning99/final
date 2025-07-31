import { useState } from 'react';
import { authService } from '../utils/auth';

const Login = ({ onLogin }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [isRegistering, setIsRegistering] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      let userData;
      if (isRegistering) {
        userData = await authService.register(username, password);
      } else {
        userData = await authService.login(username, password);
      }
      onLogin(userData);
    } catch (error) {
      setError(isRegistering ? 'Registration failed' : 'Invalid username or password');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="container">
      <div className="row justify-content-center">
        <div className="col-md-6">
          <div className="card mt-5">
            <div className="card-body">
              <h2 className="card-title text-center">
                {isRegistering ? 'Register' : 'Login'} - ETF Portfolio Manager
              </h2>
              <form onSubmit={handleSubmit}>
                <div className="mb-3">
                  <label htmlFor="username" className="form-label">Username</label>
                  <input type="text" className="form-control" id="username" value={username} onChange={(e) => setUsername(e.target.value)} required/>
                </div>
                <div className="mb-3">
                  <label htmlFor="password" className="form-label">Password</label>
                  <input type="password" className="form-control" id="password" value={password} onChange={(e) => setPassword(e.target.value)} required/>
                </div>
                {error && <div className="alert alert-danger">{error}</div>}
                <button type="submit" className="btn btn-primary w-100" disabled={loading}>
                  {loading ? (isRegistering ? 'Registering...' : 'Logging in...') : (isRegistering ? 'Register' : 'Login')}
                </button>
              </form>
              <div className="mt-3 text-center">
                <button type="button" className="btn btn-link" onClick={() => setIsRegistering(!isRegistering)}>
                  {isRegistering ? 'Already have an account? Login' : 'Need an account? Register'}
                </button>
              </div>
              {!isRegistering && (
                <div className="mt-3 text-center">
                  <small className="text-muted">
                    Demo users: admin, john_doe, jane_smith, bob_wilson, alice_johnson<br/>
                    Password: password123
                  </small>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Login;