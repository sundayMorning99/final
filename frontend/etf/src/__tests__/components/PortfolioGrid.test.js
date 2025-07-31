import { render, screen, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import PortfolioGrid from '../../components/PortfolioGrid';

const PortfolioGridWithRouter = (props) => (
  <BrowserRouter>
    <PortfolioGrid {...props} />
  </BrowserRouter>
);

describe('PortfolioGrid Component', () => {
  const mockPortfolios = [
    {
      id: 1,
      name: 'Growth Portfolio',
      userId: 1,
      isPublic: true
    },
    {
      id: 2,
      name: 'Conservative Portfolio',
      userId: 2,
      isPublic: false
    }
  ];

  const mockProps = {
    portfolios: mockPortfolios,
    searchTerm: '',
    setSearchTerm: jest.fn(),
    sortBy: 'name',
    setSortBy: jest.fn(),
    handleEdit: jest.fn(),
    handleDelete: jest.fn(),
    canEditPortfolio: jest.fn((portfolio) => portfolio.userId === 1),
    onAddPortfolio: jest.fn()
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders portfolio grid with data', () => {
    render(<PortfolioGridWithRouter {...mockProps} />);
    
    expect(screen.getByText('Portfolios')).toBeInTheDocument();
    expect(screen.getByText('Growth Portfolio')).toBeInTheDocument();
    expect(screen.getByText('Conservative Portfolio')).toBeInTheDocument();
  });

  it('shows public/private badges correctly', () => {
    render(<PortfolioGridWithRouter {...mockProps} />);
    
    expect(screen.getByText('Public')).toBeInTheDocument();
    expect(screen.getByText('Private')).toBeInTheDocument();
  });

  it('shows View buttons for all portfolios', () => {
    render(<PortfolioGridWithRouter {...mockProps} />);
    
    const viewButtons = screen.getAllByText('View');
    expect(viewButtons).toHaveLength(2);
  });

  it('shows edit/delete buttons only for editable portfolios', () => {
    render(<PortfolioGridWithRouter {...mockProps} />);
    
    const editButtons = screen.getAllByText('Edit');
    const deleteButtons = screen.getAllByText('Delete');
    
    expect(editButtons).toHaveLength(1); // Only for portfolio with userId 1
    expect(deleteButtons).toHaveLength(1);
  });

  it('handles search input change', () => {
    render(<PortfolioGridWithRouter {...mockProps} />);
    
    fireEvent.change(screen.getByPlaceholderText('Search portfolios...'), {
      target: { value: 'Growth' }
    });

    expect(mockProps.setSearchTerm).toHaveBeenCalledWith('Growth');
  });

  it('handles sort selection change', () => {
    render(<PortfolioGridWithRouter {...mockProps} />);
    
    fireEvent.change(screen.getByDisplayValue('Sort by Name'), {
      target: { value: 'userId' }
    });

    expect(mockProps.setSortBy).toHaveBeenCalledWith('userId');
  });

  it('calls onAddPortfolio when Add Portfolio button is clicked', () => {
    render(<PortfolioGridWithRouter {...mockProps} />);
    
    fireEvent.click(screen.getByText('Add Portfolio'));
    
    expect(mockProps.onAddPortfolio).toHaveBeenCalled();
  });
});