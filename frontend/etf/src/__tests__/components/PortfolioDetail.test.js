import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import PortfolioDetail from '../../components/PortfolioDetail';
import { authService } from '../../utils/auth';

jest.mock('../../utils/auth');
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useParams: () => ({ id: '1' }),
  useNavigate: () => jest.fn()
}));

global.fetch = jest.fn();

const PortfolioDetailWithRouter = ({ user }) => (
  <BrowserRouter>
    <PortfolioDetail user={user} />
  </BrowserRouter>
);

describe('PortfolioDetail Component', () => {
  const mockUser = { id: 1, username: 'testuser', role: 'USER' };
  const mockPortfolio = {
    id: 1,
    name: 'Test Portfolio',
    userId: 1,
    isPublic: true
  };
  const mockEtfs = [
    {
      id: 1,
      ticker: 'SPY',
      description: 'SPDR S&P 500 ETF',
      assetClass: 'Equity',
      expenseRatio: 0.0945
    }
  ];

  beforeEach(() => {
    jest.clearAllMocks();
    authService.getAuthHeaders.mockReturnValue({ Authorization: 'Bearer mock-token' });
  });

  it('renders portfolio detail with ETFs', async () => {
    fetch
      .mockResolvedValueOnce({ ok: true, json: async () => mockPortfolio })
      .mockResolvedValueOnce({ ok: true, json: async () => mockEtfs })
      .mockResolvedValueOnce({ ok: true, json: async () => [] });

    render(<PortfolioDetailWithRouter user={mockUser} />);

    await waitFor(() => {
      expect(screen.getByText('Test Portfolio')).toBeInTheDocument();
      expect(screen.getByText('SPY')).toBeInTheDocument();
      expect(screen.getByText('SPDR S&P 500 ETF')).toBeInTheDocument();
    });
  });

  it('shows Add ETF button for portfolio owner', async () => {
    fetch
      .mockResolvedValueOnce({ ok: true, json: async () => mockPortfolio })
      .mockResolvedValueOnce({ ok: true, json: async () => mockEtfs })
      .mockResolvedValueOnce({ ok: true, json: async () => [] });

    render(<PortfolioDetailWithRouter user={mockUser} />);

    await waitFor(() => {
      expect(screen.getByText('Add ETF')).toBeInTheDocument();
    });
  });

  it('shows Remove button for ETFs when user can manage portfolio', async () => {
    fetch
      .mockResolvedValueOnce({ ok: true, json: async () => mockPortfolio })
      .mockResolvedValueOnce({ ok: true, json: async () => mockEtfs })
      .mockResolvedValueOnce({ ok: true, json: async () => [] });

    render(<PortfolioDetailWithRouter user={mockUser} />);

    await waitFor(() => {
      expect(screen.getByText('Remove')).toBeInTheDocument();
    });
  });

  it('opens add ETF modal', async () => {
    const availableEtfs = [
      { id: 2, ticker: 'BND', description: 'Bond ETF', assetClass: 'Bond', expenseRatio: 0.035 }
    ];

    fetch
      .mockResolvedValueOnce({ ok: true, json: async () => mockPortfolio })
      .mockResolvedValueOnce({ ok: true, json: async () => mockEtfs })
      .mockResolvedValueOnce({ ok: true, json: async () => availableEtfs });

    render(<PortfolioDetailWithRouter user={mockUser} />);

    await waitFor(() => {
      fireEvent.click(screen.getByText('Add ETF'));
    });

    expect(screen.getByText('Add ETF to Portfolio')).toBeInTheDocument();
    expect(screen.getByText('BND')).toBeInTheDocument();
  });

  it('handles adding ETF to portfolio', async () => {
    const availableEtfs = [
      { id: 2, ticker: 'BND', description: 'Bond ETF', assetClass: 'Bond', expenseRatio: 0.035 }
    ];

    fetch
      .mockResolvedValueOnce({ ok: true, json: async () => mockPortfolio })
      .mockResolvedValueOnce({ ok: true, json: async () => mockEtfs })
      .mockResolvedValueOnce({ ok: true, json: async () => availableEtfs });

    render(<PortfolioDetailWithRouter user={mockUser} />);

    await waitFor(() => {
      fireEvent.click(screen.getByText('Add ETF'));
    });

    fetch.mockResolvedValueOnce({ ok: true });
    fetch
      .mockResolvedValueOnce({ ok: true, json: async () => mockPortfolio })
      .mockResolvedValueOnce({ ok: true, json: async () => [...mockEtfs, availableEtfs[0]] })
      .mockResolvedValueOnce({ ok: true, json: async () => [] });

    fireEvent.click(screen.getAllByText('Add')[0]);

    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith('/api/portfolios/1/etfs/2', {
        method: 'POST',
        headers: { Authorization: 'Bearer mock-token' }
      });
    });
  });

  it('handles removing ETF from portfolio', async () => {
    fetch
      .mockResolvedValueOnce({ ok: true, json: async () => mockPortfolio })
      .mockResolvedValueOnce({ ok: true, json: async () => mockEtfs })
      .mockResolvedValueOnce({ ok: true, json: async () => [] });

    window.confirm = jest.fn(() => true);

    render(<PortfolioDetailWithRouter user={mockUser} />);

    await waitFor(() => {
      expect(screen.getByText('Remove')).toBeInTheDocument();
    });

    fetch.mockResolvedValueOnce({ ok: true });
    fetch
      .mockResolvedValueOnce({ ok: true, json: async () => mockPortfolio })
      .mockResolvedValueOnce({ ok: true, json: async () => [] })
      .mockResolvedValueOnce({ ok: true, json: async () => [] });

    fireEvent.click(screen.getByText('Remove'));

    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith('/api/portfolios/1/etfs/1', {
        method: 'DELETE',
        headers: { Authorization: 'Bearer mock-token' }
      });
    });
  });
});