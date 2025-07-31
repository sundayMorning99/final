import { Link, useLocation } from 'react-router-dom';

const Navbar = ({ user, onLogout }) => {
  const location = useLocation();

  const handleLogout = async () => {
    await onLogout();
  }

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
      <div className="container">
        <Link className="navbar-brand" to="/">ETF Portfolio</Link>
        
        <div className="navbar-nav me-auto">
          <Link className={`nav-link ${location.pathname === '/etfs' ? 'active' : ''}`} to="/etfs">ETFs</Link>
          <Link className={`nav-link ${location.pathname === '/portfolios' ? 'active' : ''}`} to="/portfolios">Portfolios</Link>
          {user.role === 'ADMIN' && (
            <Link className={`nav-link ${location.pathname === '/users' ? 'active' : ''}`} to="/users">Users</Link>
          )}
        </div>
        
        <div className="navbar-nav">
          <div className="nav-item dropdown">
            <a className="nav-link dropdown-toggle" href="#" id="navbarDropdown" role="button" data-bs-toggle="dropdown">
              {user.username} {user.role === 'ADMIN' && '(Admin)'}
            </a>
            <ul className="dropdown-menu">
              <li><Link className="dropdown-item" to="/profile">Profile</Link></li>
              <li><hr className="dropdown-divider" /></li>
              <li><button className="dropdown-item" onClick={handleLogout}>Logout</button></li>
            </ul>
          </div>
        </div>
      </div>
    </nav>
  )
}

export default Navbar;