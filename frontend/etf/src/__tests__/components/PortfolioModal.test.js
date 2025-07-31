import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import PortfolioModal from '../../components/PortfolioModal';

const mockFormData = {
  name: 'Test Portfolio',
  isPublic: true
};

const mockProps = {
  showModal: true,
  setShowModal: jest.fn(),
  editingPortfolio: null,
  formData: mockFormData,
  setFormData: jest.fn(),
  handleSubmit: jest.fn()
};

describe('PortfolioModal Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders modal when showModal is true', () => {
    render(<PortfolioModal {...mockProps} />);
    
    expect(screen.getByText('Add New Portfolio')).toBeInTheDocument();
    expect(screen.getByText('Name')).toBeInTheDocument();
    expect(screen.getByText('Make Public')).toBeInTheDocument();
  });

  test('does not render when showModal is false', () => {
    const props = { ...mockProps, showModal: false };
    render(<PortfolioModal {...props} />);
    
    expect(screen.queryByText('Add New Portfolio')).not.toBeInTheDocument();
  });

  test('shows edit title when editing portfolio', () => {
    const props = { ...mockProps, editingPortfolio: { id: 1, name: 'Test' } };
    render(<PortfolioModal {...props} />);
    
    expect(screen.getByText('Edit Portfolio')).toBeInTheDocument();
    expect(screen.getByText('Update')).toBeInTheDocument();
  });

  test('shows create title when adding new portfolio', () => {
    render(<PortfolioModal {...mockProps} />);
    
    expect(screen.getByText('Add New Portfolio')).toBeInTheDocument();
    expect(screen.getByText('Create')).toBeInTheDocument();
  });

  test('displays form data in inputs', () => {
    render(<PortfolioModal {...mockProps} />);
    
    expect(screen.getByDisplayValue('Test Portfolio')).toBeInTheDocument();
    expect(screen.getByRole('checkbox', { name: 'Make Public' })).toBeChecked();
  });

  test('calls setFormData when name input changes', () => {
    render(<PortfolioModal {...mockProps} />);
    
    const nameInput = screen.getByDisplayValue('Test Portfolio');
    fireEvent.change(nameInput, { target: { value: 'New Portfolio' } });
    
    expect(mockProps.setFormData).toHaveBeenCalledWith({
      ...mockFormData,
      name: 'New Portfolio'
    });
  });

  test('calls setFormData when checkbox changes', () => {
    render(<PortfolioModal {...mockProps} />);
    
    const checkbox = screen.getByRole('checkbox', { name: 'Make Public' });
    fireEvent.click(checkbox);
    
    expect(mockProps.setFormData).toHaveBeenCalledWith({
      ...mockFormData,
      isPublic: false
    });
  });

  test('calls setShowModal when close button is clicked', () => {
    render(<PortfolioModal {...mockProps} />);
    
    const closeButton = screen.getByRole('button', { name: '' }); // btn-close
    fireEvent.click(closeButton);
    
    expect(mockProps.setShowModal).toHaveBeenCalledWith(false);
  });

  test('calls setShowModal when cancel button is clicked', () => {
    render(<PortfolioModal {...mockProps} />);
    
    const cancelButton = screen.getByText('Cancel');
    fireEvent.click(cancelButton);
    
    expect(mockProps.setShowModal).toHaveBeenCalledWith(false);
  });

  test('name input is required', () => {
    render(<PortfolioModal {...mockProps} />);
    
    const nameInput = screen.getByDisplayValue('Test Portfolio');
    expect(nameInput).toHaveAttribute('required');
  });

  test('displays unchecked checkbox when isPublic is false', () => {
    const props = { ...mockProps, formData: { ...mockFormData, isPublic: false } };
    render(<PortfolioModal {...props} />);
    
    const checkbox = screen.getByRole('checkbox', { name: 'Make Public' });
    expect(checkbox).not.toBeChecked();
  });

  test('renders modal backdrop', () => {
    render(<PortfolioModal {...mockProps} />);
    
    const modal = document.querySelector('.modal.show.d-block');
    expect(modal).toBeInTheDocument();
    
    const backdrop = document.querySelector('.modal-backdrop.show');
    expect(backdrop).toBeInTheDocument();
  });
});
