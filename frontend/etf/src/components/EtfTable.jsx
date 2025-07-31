const EtfTable = ({ etfs, searchTerm, setSearchTerm, sortBy, setSortBy, handleEdit, handleDelete, canEditEtf, onAddEtf 
}) => {
  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>ETFs</h2>
        <button className="btn btn-primary" onClick={onAddEtf}>Add ETF</button>
      </div>

      <div className="row mb-3">
        <div className="col-md-6">
          <input type="text" className="form-control" placeholder="Search ETFs..." value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)}/>
        </div>
        <div className="col-md-3">
          <select className="form-select" value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
            <option value="ticker">Sort by Ticker</option>
            <option value="assetClass">Sort by Asset Class</option>
          </select>
        </div>
      </div>

      <div className="table-responsive">
        <table className="table table-striped">
          <thead>
            <tr>
              <th>Ticker</th>
              <th>Description</th>
              <th>Asset Class</th>
              <th>Expense Ratio</th>
              <th>Visibility</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {etfs.map(etf => (
              <tr key={etf.id}>
                <td><strong>{etf.ticker}</strong></td>
                <td>{etf.description}</td>
                <td>{etf.assetClass}</td>
                <td>{(etf.expenseRatio * 100).toFixed(2)}%</td>
                <td>
                  <span className={`badge ${etf.isPublic ? 'bg-success' : 'bg-secondary'}`}>
                    {etf.isPublic ? 'Public' : 'Private'}
                  </span>
                </td>
                <td>
                  {canEditEtf(etf) && (
                    <>
                      <button className="btn btn-sm btn-outline-primary me-2" onClick={() => handleEdit(etf)}>Edit</button>
                      <button className="btn btn-sm btn-outline-danger" onClick={() => handleDelete(etf.id)}>Delete</button>
                    </>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

export default EtfTable;