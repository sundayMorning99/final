import { useState, useEffect } from 'react'
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { authService } from './utils/auth'
import Navbar from './components/Navbar'
import Login from './components/Login'
import EtfList from './components/EtfList'
import PortfolioList from './components/PortfolioList'
import PortfolioDetail from './components/PortfolioDetail'
import UserProfile from './components/UserProfile'
import UserManagement from './components/UserManagement'
import NotFound from './components/NotFound'

const API_BASE_URL = 'http://localhost:8080';

function App() {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  //Because [] is empty, useEffect will load 'echeckAuth()' once when the application starts.
  useEffect(() => {
    checkAuth()
  }, [])

  const checkAuth = async () => {
    if (authService.isAuthenticated()) {
      try {
        const response = await fetch(`${API_BASE_URL}/api/auth/user`, {
          headers: {
            ...authService.getAuthHeaders()
          }
        });
        
        if (response.ok) {
          const userData = await response.json();
          setUser(userData);
        } else if (response.status === 401) {
          // Try to refresh token 
          const newToken = await authService.refreshToken();
          if (newToken) {
            // Retry getting user info
            const retryResponse = await fetch(`${API_BASE_URL}/api/auth/user`, {
              headers: {
                ...authService.getAuthHeaders()
              }
            });
            if (retryResponse.ok) {
              const userData = await retryResponse.json();
              setUser(userData);
            } else {
              authService.logout();
            }
          } else {
            authService.logout();
          }
        }
      } catch (error) {
        console.error('Failed to get user info:', error);
        authService.logout();
      }
    }
    setLoading(false);
  }

  const handleLogin = async (userData) => {
    console.log('Login userData:', userData);

    try {
      const response = await fetch(`${API_BASE_URL}/api/auth/user`, {
        headers: {
          ...authService.getAuthHeaders()
        }
      });
      
      if (response.ok) {
        const completeUserData = await response.json();
        console.log('Complete user data after login:', completeUserData);
        setUser(completeUserData);
      } else {
        setUser(userData); 
      }
    } catch (error) {
      console.error('Failed to get complete user info after login:', error);
      setUser(userData); 
    }
  }

  const handleLogout = async () => {
    await authService.logout();
    setUser(null);
  }

  if (loading) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ height: '100vh' }}>
        <div className="spinner-border" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    )
  }

  if (!user) {
    return <Login onLogin={handleLogin} />
  }

  return (
    <Router>
      <div className="App">
        <Navbar user={user} onLogout={handleLogout} />
        <div className="container mt-4">
          <Routes>
            <Route path="/" element={<Navigate to="/etfs" replace />} />
            <Route path="/etfs" element={<EtfList user={user} />} />
            <Route path="/portfolios" element={<PortfolioList user={user} />} />
            <Route path="/portfolios/:id" element={<PortfolioDetail user={user} />} />
            <Route path="/profile" element={<UserProfile user={user} />} />
            {user.role === 'ADMIN' && (
              <Route path="/users" element={<UserManagement />} />
            )}
            <Route path="*" element={<NotFound />} />
          </Routes>
        </div>
      </div>
    </Router>
  )
}

export default App