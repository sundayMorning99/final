import { render, screen, fireEvent } from '@testing-library/react';
import EtfTable from '../../components/EtfTable';

describe('EtfTable Component', () => {
  const mockEtfs = [
    {
      id: 1,
      ticker: 'SPY',
      description: 'SPDR S&P 500 ETF',
      assetClass: 'Equity',
      expenseRatio: 0.0945,
      userId: 1,
      isPublic: true
    },
    {
      id: 2,
      ticker: 'BND',
      description: 'Vanguard Total Bond Market ETF',
      assetClass: 'Bond',
      expenseRatio: 0.035,
      userId: 2,
      isPublic: false
    }
  ];

  const mockProps = {
    etfs: mockEtfs,
    searchTerm: '',
    setSearchTerm: jest.fn(),
    sortBy: 'ticker',
    setSortBy: jest.fn(),
    handleEdit: jest.fn(),
    handleDelete: jest.fn(),
    canEditEtf: jest.fn((etf) => etf.userId === 1),
    onAddEtf: jest.fn()
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders ETF table with data', () => {
    render(<EtfTable {...mockProps} />);
    
    expect(screen.getByText('ETFs')).toBeInTheDocument();
    expect(screen.getByText('SPY')).toBeInTheDocument();
    expect(screen.getByText('SPDR S&P 500 ETF')).toBeInTheDocument();
    expect(screen.getByText('BND')).toBeInTheDocument();
    expect(screen.getByText('9.45%')).toBeInTheDocument(); // 0.0945 * 100 - converting to %
    expect(screen.getByText('3.50%')).toBeInTheDocument(); // 0.035 * 100
  });

  it('shows public/private badges correctly', () => {
    render(<EtfTable {...mockProps} />);
    
    expect(screen.getByText('Public')).toBeInTheDocument();
    expect(screen.getByText('Private')).toBeInTheDocument();
  });

  it('handles search input change', () => {
    render(<EtfTable {...mockProps} />);
    
    fireEvent.change(screen.getByPlaceholderText('Search ETFs...'), {
      target: { value: 'SPY' }
    });

    expect(mockProps.setSearchTerm).toHaveBeenCalledWith('SPY');
  });

  it('handles sort selection change', () => {
    render(<EtfTable {...mockProps} />);
    
    fireEvent.change(screen.getByDisplayValue('Sort by Ticker'), {
      target: { value: 'assetClass' }
    });

    expect(mockProps.setSortBy).toHaveBeenCalledWith('assetClass');
  });

  it('shows edit/delete buttons only for editable ETFs', () => {
    render(<EtfTable {...mockProps} />);
    
    const editButtons = screen.getAllByText('Edit');
    const deleteButtons = screen.getAllByText('Delete');
    
    expect(editButtons).toHaveLength(1); // Only for ETF with userId 1
    expect(deleteButtons).toHaveLength(1);
  });

  it('calls handleEdit when edit button is clicked', () => {
    render(<EtfTable {...mockProps} />);
    
    fireEvent.click(screen.getByText('Edit'));
    
    expect(mockProps.handleEdit).toHaveBeenCalledWith(mockEtfs[0]);
  });

  it('calls handleDelete when delete button is clicked', () => {
    render(<EtfTable {...mockProps} />);
    
    fireEvent.click(screen.getByText('Delete'));
    
    expect(mockProps.handleDelete).toHaveBeenCalledWith(1);
  });

  it('calls onAddEtf when Add ETF button is clicked', () => {
    render(<EtfTable {...mockProps} />);
    
    fireEvent.click(screen.getByText('Add ETF'));
    
    expect(mockProps.onAddEtf).toHaveBeenCalled();
  });
});
