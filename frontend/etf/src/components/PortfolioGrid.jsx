import { Link } from 'react-router-dom';

const PortfolioGrid = ({ portfolios, searchTerm, setSearchTerm, sortBy, setSortBy, handleEdit, handleDelete, canEditPortfolio, onAddPortfolio 
}) => {
  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>Portfolios</h2>
        <button className="btn btn-primary" onClick={onAddPortfolio}>Add Portfolio</button>
      </div>

      <div className="row mb-3">
        <div className="col-md-6">
          <input type="text" className="form-control" placeholder="Search portfolios..." value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)}/>
        </div>
        <div className="col-md-3">
          <select className="form-select" value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
            <option value="name">Sort by Name</option>
            <option value="userId">Sort by User ID</option>
          </select>
        </div>
      </div>

      <div className="row">
        {portfolios.map(portfolio => (
          <div key={portfolio.id} className="col-md-4 mb-3">
            <div className="card">
              <div className="card-body">
                <h5 className="card-title">{portfolio.name}</h5>
                <p className="card-text">
                  <span className={`badge ${portfolio.isPublic ? 'bg-success' : 'bg-secondary'} me-2`}>
                    {portfolio.isPublic ? 'Public' : 'Private'}
                  </span>
                  User ID: {portfolio.userId}
                </p>
                <div className="d-flex gap-2">
                  <Link to={`/portfolios/${portfolio.id}`} className="btn btn-outline-primary btn-sm">View</Link>
                  {canEditPortfolio(portfolio) && (
                    <>
                      <button className="btn btn-outline-secondary btn-sm" onClick={() => handleEdit(portfolio)}>Edit</button>
                      <button className="btn btn-outline-danger btn-sm" onClick={() => handleDelete(portfolio.id)}>Delete</button>
                    </>
                  )}
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}

export default PortfolioGrid;