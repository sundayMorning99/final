import { useState, useEffect } from 'react';
import { authService } from '../utils/auth';
import EtfTable from './EtfTable';
import EtfModal from './EtfModal';

const EtfList = ({ user }) => {
  const [etfs, setEtfs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [sortBy, setSortBy] = useState('ticker');
  const [showModal, setShowModal] = useState(false);
  const [editingEtf, setEditingEtf] = useState(null);
  const [formData, setFormData] = useState({ticker: '', description: '', assetClass: '', expenseRatio: '',isPublic: false});

  useEffect(() => {
    fetchEtfs();
  }, [searchTerm, sortBy]);

  // When it comes to API, there are two important symbols: ? and &.
  // Base address comes before the ? symbol, and the parameters come after it.
  // If there are multiple parameters, they are separated by the & symbol.
  // (base_address)?param1=value1&param2=value2
  // This is how we can pass multiple parameters to the API.
  // URLSearchParams is a built-in JavaScript object that makes it easy to work with query strings.
  const fetchEtfs = async () => {
    try {
      const params = new URLSearchParams();
      // If the searchTerm is not empty, we add it to the params
      // If the sortBy is not empty, we add it to the params
      if (searchTerm) params.append('search', searchTerm);
      if (sortBy) params.append('sortBy', sortBy);
      
      const response = await fetch(`/api/etfs?${params}`, {
        headers: {
          ...authService.getAuthHeaders()
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        setEtfs(data);
      } else if (response.status === 401) {
        authService.logout();
        window.location.reload();
      }
    } catch (error) {
      console.error('Failed to fetch ETFs:', error);
    } finally {
      setLoading(false);
    }
  }

  // Just like I said in Spring Boot, Creating and Updating are similar
  // So we can use the same handleSubmit function for both creating and updating ETFs
  // The only difference is the URL and method used in the fetch request
  // If editingEtf is null, we create a new ETF, otherwise we update the existing one
  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const url = editingEtf ? `/api/etfs/${editingEtf.id}` : '/api/etfs';
      const method = editingEtf ? 'PUT' : 'POST';
      
      const response = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json',
          ...authService.getAuthHeaders()
        },
        body: JSON.stringify({
          ...formData,
          expenseRatio: parseFloat(formData.expenseRatio)
        })
      });
      
      if (response.ok) {
        setShowModal(false);
        setEditingEtf(null);
        resetForm();
        fetchEtfs();
      } else if (response.status === 401) {
        authService.logout();
        window.location.reload();
      }
    } catch (error) {
      console.error('Failed to save ETF:', error);
    }
  }

  // As we have learned in JavaScript, if the key already exists in the object, it will be updated, otherwise it will be added.
  const handleEdit = (etf) => {
    setEditingEtf(etf);
    setFormData({
      ticker: etf.ticker,
      description: etf.description,
      assetClass: etf.assetClass,
      expenseRatio: etf.expenseRatio.toString(),
      isPublic: etf.isPublic
    });
    setShowModal(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this ETF?')) {
      try {
        const response = await fetch(`/api/etfs/${id}`, {
          method: 'DELETE',
          headers: {
            ...authService.getAuthHeaders()
          }
        });
        
        if (response.ok) {
          fetchEtfs();
        } else if (response.status === 401) {
          authService.logout();
          window.location.reload();
        }
      } catch (error) {
        console.error('Failed to delete ETF:', error);
      }
    }
  }

  // As I mentioned in the handleEdit function, we can reset the form by overwriting the formData state with the initial values.
  const resetForm = () => {
    setFormData({
      ticker: '', 
      description: '', 
      assetClass: '', 
      expenseRatio: '', 
      isPublic: false
    });
  };

  const canEditEtf = (etf) => {
    return user.role === 'ADMIN' || etf.userId === user.id
  };

  const handleAddEtf = () => {
    setEditingEtf(null);
    resetForm();
    setShowModal(true);
  };

  if (loading) {
    return <div className="text-center">Loading...</div>;
  }

  return (
    <div>
      <EtfTable
        etfs={etfs}
        searchTerm={searchTerm}
        setSearchTerm={setSearchTerm}
        sortBy={sortBy}
        setSortBy={setSortBy}
        handleEdit={handleEdit}
        handleDelete={handleDelete}
        canEditEtf={canEditEtf}
        onAddEtf={handleAddEtf}
      />
      
      <EtfModal
        showModal={showModal}
        setShowModal={setShowModal}
        editingEtf={editingEtf}
        formData={formData}
        setFormData={setFormData}
        handleSubmit={handleSubmit}
      />
    </div>
  )
}

export default EtfList;