const PortfolioModal = ({ showModal, setShowModal, editingPortfolio, formData, setFormData, handleSubmit 
}) => {
  if (!showModal) return null;

  return (
    <>
      <div className="modal show d-block" tabIndex="-1">
        <div className="modal-dialog">
          <div className="modal-content">
            <div className="modal-header">
              <h5 className="modal-title">
                {editingPortfolio ? 'Edit Portfolio' : 'Add New Portfolio'}
              </h5>
              <button type="button" className="btn-close" onClick={() => setShowModal(false)}></button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="modal-body">
                <div className="mb-3">
                  <label htmlFor="portfolioName" className="form-label">Name</label>
                  <input id="portfolioName" type="text" className="form-control" value={formData.name} onChange={(e) => setFormData({...formData, name: e.target.value})} required />
                </div>
                <div className="mb-3 form-check">
                  <input type="checkbox" className="form-check-input" id="isPublic" checked={formData.isPublic} onChange={(e) => setFormData({...formData, isPublic: e.target.checked})}/>
                  <label className="form-check-label" htmlFor="isPublic">Make Public</label>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">{editingPortfolio ? 'Update' : 'Create'}</button>
              </div>
            </form>
          </div>
        </div>
      </div>
      <div className="modal-backdrop show"></div>
    </>
  )
}

export default PortfolioModal;