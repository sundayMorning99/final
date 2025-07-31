import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { authService } from '../utils/auth';

const PortfolioDetail = ({ user }) => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [portfolio, setPortfolio] = useState(null);
  const [portfolioEtfs, setPortfolioEtfs] = useState([]);
  const [availableEtfs, setAvailableEtfs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showAddModal, setShowAddModal] = useState(false);

  useEffect(() => {
    fetchPortfolioData();
  }, [id]);

  const fetchPortfolioData = async () => {
    try {
      const headers = { ...authService.getAuthHeaders() }
      
      // This is JavaScript's destructuring syntax to fetch multiple resources in parallel.
      // It allows us to make multiple API calls and wait for all of them to complete.
      // We use Promise.all to wait for all fetch calls to complete. This is one of Flex-Path Quiz questions. ^__^
      const [portfolioResponse, etfsResponse, availableResponse] = await Promise.all([
        fetch(`/api/portfolios/${id}`, { headers }),
        fetch(`/api/portfolios/${id}/etfs`, { headers }),
        fetch('/api/etfs', { headers })
      ])

      if (portfolioResponse.ok) {
        const portfolioData = await portfolioResponse.json();
        setPortfolio(portfolioData);
      } else if (portfolioResponse.status === 404) {
        navigate('/portfolios')
        return
      } else if (portfolioResponse.status === 401) {
        authService.logout();
        window.location.reload();
        return
      }

      if (etfsResponse.ok) {
        const etfsData = await etfsResponse.json();
        setPortfolioEtfs(etfsData);
      }

      if (availableResponse.ok) {
        const availableData = await availableResponse.json();
        setAvailableEtfs(availableData);
      }
    } catch (error) {
      console.error('Failed to fetch portfolio data:', error)
    } finally {
      setLoading(false);
    }
  }

  const handleAddEtf = async (etfId) => {
    try {
      const response = await fetch(`/api/portfolios/${id}/etfs/${etfId}`, {
        method: 'POST',
        headers: {
          ...authService.getAuthHeaders()
        }
      })

      if (response.ok) {
        fetchPortfolioData(); // Refresh portfolio data to include the newly added ETF
        setShowAddModal(false); // Close the modal after adding
      } else if (response.status === 409) {
        alert('ETF is already in this portfolio');
      } else if (response.status === 401) {
        authService.logout();
        window.location.reload();
      }
    } catch (error) {
      console.error('Failed to add ETF:', error);
    }
  }

  const handleRemoveEtf = async (etfId) => {
    if (window.confirm('Are you sure you want to remove this ETF from the portfolio?')) {
      try {
        const response = await fetch(`/api/portfolios/${id}/etfs/${etfId}`, {
          method: 'DELETE',
          headers: {
            ...authService.getAuthHeaders()
          }
        })

        if (response.ok) {
          fetchPortfolioData();
        } else if (response.status === 401) {
          authService.logout();
          window.location.reload();
        }
      } catch (error) {
        console.error('Failed to remove ETF:', error);
      }
    }
  }

  // Only admins and the users who created the portfolio can manage it
  const canManagePortfolio = () => {
    return portfolio && (user.role === 'ADMIN' || portfolio.userId === user.id);
  }

  const getAvailableEtfsToAdd = () => {
    const portfolioEtfIds = portfolioEtfs.map(etf => etf.id);
    return availableEtfs.filter(etf => !portfolioEtfIds.includes(etf.id));
  }

  if (loading) {
    return <div className="text-center">Loading...</div>;
  }

  if (!portfolio) {
    return <div className="text-center">Portfolio not found</div>;
  }

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2>{portfolio.name}</h2>
          <span className={`badge ${portfolio.isPublic ? 'bg-success' : 'bg-secondary'} me-2`}>
            {portfolio.isPublic ? 'Public' : 'Private'}
          </span>
          <span className="text-muted">User ID: {portfolio.userId}</span>
        </div>
        <div>
          {canManagePortfolio() && (
            <button className="btn btn-primary me-2" onClick={() => setShowAddModal(true)}>Add ETF</button>
          )}
          <Link to="/portfolios" className="btn btn-outline-secondary">Back to Portfolios</Link>
        </div>
      </div>

      <h4>ETFs in Portfolio ({portfolioEtfs.length})</h4>
      {portfolioEtfs.length === 0 ? (
        <div className="alert alert-info">
          No ETFs in this portfolio yet.
        </div>
      ) : (
        <div className="table-responsive">
          <table className="table table-striped">
            <thead>
              <tr>
                <th>Ticker</th>
                <th>Description</th>
                <th>Asset Class</th>
                <th>Expense Ratio</th>
                {canManagePortfolio() && <th>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {portfolioEtfs.map(etf => (
                <tr key={etf.id}>
                  <td><strong>{etf.ticker}</strong></td>
                  <td>{etf.description}</td>
                  <td>{etf.assetClass}</td>
                  <td>{(etf.expenseRatio * 100).toFixed(2)}%</td>
                  {canManagePortfolio() && (
                    <td>
                      <button className="btn btn-sm btn-outline-danger" onClick={() => handleRemoveEtf(etf.id)}>Remove</button>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Again, these elements will display on showAddModal is true */}
      {showAddModal && (
        <div className="modal show d-block" tabIndex="-1">
          <div className="modal-dialog modal-lg">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Add ETF to Portfolio</h5>
                <button type="button" className="btn-close" onClick={() => setShowAddModal(false)}></button>
              </div>
              <div className="modal-body">
                <div className="table-responsive">
                  <table className="table table-sm">
                    <thead>
                      <tr>
                        <th>Ticker</th>
                        <th>Description</th>
                        <th>Asset Class</th>
                        <th>Expense Ratio</th>
                        <th>Action</th>
                      </tr>
                    </thead>
                    <tbody>
                      {getAvailableEtfsToAdd().map(etf => (
                        <tr key={etf.id}>
                          <td><strong>{etf.ticker}</strong></td>
                          <td>{etf.description}</td>
                          <td>{etf.assetClass}</td>
                          <td>{(etf.expenseRatio * 100).toFixed(2)}%</td>
                          <td>
                            <button className="btn btn-sm btn-primary" onClick={() => handleAddEtf(etf.id)}>Add</button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
                {getAvailableEtfsToAdd().length === 0 && (
                  <div className="text-center text-muted">No more ETFs available to add.</div>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
      {showAddModal && <div className="modal-backdrop show"></div>}
    </div>
  )
}

export default PortfolioDetail;