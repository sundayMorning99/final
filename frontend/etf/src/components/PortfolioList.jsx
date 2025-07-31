import { useState, useEffect } from 'react';
import { authService } from '../utils/auth';
import PortfolioGrid from './PortfolioGrid';
import PortfolioModal from './PortfolioModal';

const PortfolioList = ({ user }) => {
  const [portfolios, setPortfolios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [sortBy, setSortBy] = useState('name');
  const [showModal, setShowModal] = useState(false);
  const [editingPortfolio, setEditingPortfolio] = useState(null);
  const [formData, setFormData] = useState({name: '', isPublic: false});

  useEffect(() => {
    fetchPortfolios();
  }, [searchTerm, sortBy]);

  const fetchPortfolios = async () => {
    try {
      const params = new URLSearchParams();
      if (searchTerm) params.append('search', searchTerm);
      if (sortBy) params.append('sortBy', sortBy);
      
      const response = await fetch(`/api/portfolios?${params}`, {
        headers: {
          ...authService.getAuthHeaders()
        }
      })
      
      if (response.ok) {
        const data = await response.json();
        setPortfolios(data);
      } else if (response.status === 401) {
        authService.logout();
        window.location.reload();
      }
    } catch (error) {
      console.error('Failed to fetch portfolios:', error);
    } finally {
      setLoading(false);
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      const url = editingPortfolio ? `/api/portfolios/${editingPortfolio.id}` : '/api/portfolios';
      const method = editingPortfolio ? 'PUT' : 'POST';
      
      const response = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json',
          ...authService.getAuthHeaders()
        },
        body: JSON.stringify(formData)
      })
      
      if (response.ok) {
        setShowModal(false);
        setEditingPortfolio(null);
        resetForm();
        fetchPortfolios();
      } else if (response.status === 401) {
        authService.logout();
        window.location.reload();
      }
    } catch (error) {
      console.error('Failed to save portfolio:', error);
    }
  }

  const handleEdit = (portfolio) => {
    setEditingPortfolio(portfolio);
    setFormData({
      name: portfolio.name,
      isPublic: portfolio.isPublic
    });
    setShowModal(true);
  }

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this portfolio?')) {
      try {
        const response = await fetch(`/api/portfolios/${id}`, {
          method: 'DELETE',
          headers: {
            ...authService.getAuthHeaders()
          }
        })
        
        if (response.ok) {
          fetchPortfolios();
        } else if (response.status === 401) {
          authService.logout();
          window.location.reload();
        }
      } catch (error) {
        console.error('Failed to delete portfolio:', error);
      }
    }
  }

  const resetForm = () => {
    setFormData({
      name: '',
      isPublic: false
    });
  }

  const canEditPortfolio = (portfolio) => {
    return user.role === 'ADMIN' || portfolio.userId === user.id;
  }

  const handleAddPortfolio = () => {
    setEditingPortfolio(null);
    resetForm();
    setShowModal(true);
  }

  if (loading) {
    return <div className="text-center">Loading...</div>;
  }

  return (
    <div>
      <PortfolioGrid
        portfolios={portfolios}
        searchTerm={searchTerm}
        setSearchTerm={setSearchTerm}
        sortBy={sortBy}
        setSortBy={setSortBy}
        handleEdit={handleEdit}
        handleDelete={handleDelete}
        canEditPortfolio={canEditPortfolio}
        onAddPortfolio={handleAddPortfolio}
      />
      
      <PortfolioModal
        showModal={showModal}
        setShowModal={setShowModal}
        editingPortfolio={editingPortfolio}
        formData={formData}
        setFormData={setFormData}
        handleSubmit={handleSubmit}
      />
    </div>
  )
}

export default PortfolioList;