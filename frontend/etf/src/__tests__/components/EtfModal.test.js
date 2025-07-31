
import { render, screen, fireEvent } from '@testing-library/react';
import EtfModal from '../../components/EtfModal';

describe('EtfModal Component', () => {
  const mockProps = {
    showModal: true,
    setShowModal: jest.fn(),
    editingEtf: null,
    formData: {
      ticker: '',
      description: '',
      assetClass: '',
      expenseRatio: '',
      isPublic: false
    },
    setFormData: jest.fn(),
    handleSubmit: jest.fn()
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders modal when showModal is true', () => {
    render(<EtfModal {...mockProps} />);
    
    expect(screen.getByText('Add New ETF')).toBeInTheDocument();
    expect(screen.getByLabelText('Ticker')).toBeInTheDocument();
    expect(screen.getByLabelText('Description')).toBeInTheDocument();
    expect(screen.getByLabelText('Asset Class')).toBeInTheDocument();
    expect(screen.getByLabelText('Expense Ratio')).toBeInTheDocument();
    expect(screen.getByLabelText('Make Public')).toBeInTheDocument();
  });

  it('does not render when showModal is false', () => {
    render(<EtfModal {...mockProps} showModal={false} />);
    
    expect(screen.queryByText('Add New ETF')).not.toBeInTheDocument();
  });

  it('shows Edit ETF title when editing', () => {
    const editingEtf = { id: 1, ticker: 'SPY' };
    render(<EtfModal {...mockProps} editingEtf={editingEtf} />);
    
    expect(screen.getByText('Edit ETF')).toBeInTheDocument();
    expect(screen.getByText('Update')).toBeInTheDocument();
  });

  it('handles form input changes', () => {
    render(<EtfModal {...mockProps} />);
    
    fireEvent.change(screen.getByLabelText('Ticker'), {
      target: { value: 'SPY' }
    });

    expect(mockProps.setFormData).toHaveBeenCalledWith({
      ...mockProps.formData,
      ticker: 'SPY'
    });
  });

  it('handles checkbox change', () => {
    render(<EtfModal {...mockProps} />);
    
    fireEvent.click(screen.getByLabelText('Make Public'));

    expect(mockProps.setFormData).toHaveBeenCalledWith({
      ...mockProps.formData,
      isPublic: true
    });
  });

  it('handles form submission', () => {
    render(<EtfModal {...mockProps} />);

    const form = screen.getByLabelText('Ticker').closest('form');
    fireEvent.submit(form);

    expect(mockProps.handleSubmit).toHaveBeenCalled();
  });

  it('handles modal close', () => {
    render(<EtfModal {...mockProps} />);
    
    fireEvent.click(screen.getByText('Cancel'));

    expect(mockProps.setShowModal).toHaveBeenCalledWith(false);
  });
});
