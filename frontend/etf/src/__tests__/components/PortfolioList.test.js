import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import PortfolioList from '../../components/PortfolioList';
import { authService } from '../../utils/auth';

jest.mock('../../utils/auth');

global.fetch = jest.fn();

const PortfolioListWithRouter = ({ user }) => (
  <BrowserRouter>
    <PortfolioList user={user} />
  </BrowserRouter>
);

describe('PortfolioList Component', () => {
  const mockUser = { id: 1, username: 'testuser', role: 'USER' };
  const mockPortfolios = [
    {
      id: 1,
      name: 'My Portfolio',
      userId: 1,
      isPublic: true
    },
    {
      id: 2,
      name: 'Private Portfolio',
      userId: 2,
      isPublic: false
    }
  ];

  beforeEach(() => {
    jest.clearAllMocks();
    authService.getAuthHeaders.mockReturnValue({ Authorization: 'Bearer mock-token' });
  });

  it('renders portfolio list with data', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockPortfolios
    });

    render(<PortfolioListWithRouter user={mockUser} />);

    await waitFor(() => {
      expect(screen.getByText('My Portfolio')).toBeInTheDocument();
      expect(screen.getByText('Private Portfolio')).toBeInTheDocument();
    });
  });

  it('shows loading state initially', () => {
    fetch.mockImplementation(() => new Promise(() => {})); 

    render(<PortfolioListWithRouter user={mockUser} />);
    
    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('allows user to search portfolios', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockPortfolios
    });

    render(<PortfolioListWithRouter user={mockUser} />);

    await waitFor(() => {
      expect(screen.getByPlaceholderText('Search portfolios...')).toBeInTheDocument();
    });

    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => [mockPortfolios[0]]
    });

    fireEvent.change(screen.getByPlaceholderText('Search portfolios...'), {
      target: { value: 'My' }
    });

    await waitFor(() => {
      expect(fetch).toHaveBeenLastCalledWith(
        '/api/portfolios?search=My&sortBy=name',
        expect.objectContaining({
          headers: { Authorization: 'Bearer mock-token' }
        })
      );
    });
  });

  it('shows Add Portfolio button and opens modal', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockPortfolios
    });

    render(<PortfolioListWithRouter user={mockUser} />);

    await waitFor(() => {
      expect(screen.getByText('Add Portfolio')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Add Portfolio'));

    expect(screen.getByText('Add New Portfolio')).toBeInTheDocument();
  });

  it('handles portfolio creation', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockPortfolios
    });

    render(<PortfolioListWithRouter user={mockUser} />);

    await waitFor(() => {
      fireEvent.click(screen.getByText('Add Portfolio'));
    });

    fireEvent.change(screen.getByLabelText('Name'), {
      target: { value: 'New Portfolio' }
    });

    fetch.mockResolvedValueOnce({ ok: true });
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => [...mockPortfolios, { id: 3, name: 'New Portfolio', userId: 1, isPublic: false }]
    });

    const form = screen.getByLabelText('Name').closest('form');
    fireEvent.submit(form);

    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith('/api/portfolios', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: 'Bearer mock-token'
        },
        body: JSON.stringify({
          name: 'New Portfolio',
          isPublic: false
        })
      });
    });
  });
});