const EtfModal = ({ showModal, setShowModal, editingEtf, formData, setFormData, handleSubmit 
}) => {
  if (!showModal) return null;

  return (
    <>
      <div className="modal show d-block" tabIndex="-1">
        <div className="modal-dialog">
          <div className="modal-content">
            <div className="modal-header">
              <h5 className="modal-title">
                {editingEtf ? 'Edit ETF' : 'Add New ETF'}
              </h5>
              <button type="button" className="btn-close" onClick={() => setShowModal(false)}></button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="modal-body">
                <div className="mb-3">
                  <label htmlFor="ticker" className="form-label">Ticker</label>
                  <input id="ticker" type="text" className="form-control" value={formData.ticker} onChange={(e) => setFormData({...formData, ticker: e.target.value})} required />
                </div>
                <div className="mb-3">
                  <label htmlFor="description" className="form-label">Description</label>
                  <input id="description" type="text" className="form-control" value={formData.description} onChange={(e) => setFormData({...formData, description: e.target.value})} required />
                </div>
                <div className="mb-3">
                  <label htmlFor="assetClass" className="form-label">Asset Class</label>
                  <input id="assetClass" type="text" className="form-control" value={formData.assetClass} onChange={(e) => setFormData({...formData, assetClass: e.target.value})} required />
                </div>
                <div className="mb-3">
                  <label htmlFor="expenseRatio" className="form-label">Expense Ratio</label>
                  <input id="expenseRatio" type="number" step="0.0001" min="0" className="form-control" value={formData.expenseRatio} onChange={(e) => setFormData({...formData, expenseRatio: e.target.value})} required />
                </div>
                <div className="mb-3 form-check">
                  <input type="checkbox" className="form-check-input" id="isPublic" checked={formData.isPublic} onChange={(e) => setFormData({...formData, isPublic: e.target.checked})}/>
                  <label className="form-check-label" htmlFor="isPublic">Make Public</label>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">{editingEtf ? 'Update' : 'Create'}</button>
              </div>
            </form>
          </div>
        </div>
      </div>
      <div className="modal-backdrop show"></div>
    </>
  )
}

export default EtfModal;