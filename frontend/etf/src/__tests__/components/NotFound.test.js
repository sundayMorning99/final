import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import { BrowserRouter } from 'react-router-dom';
import NotFound from '../../components/NotFound';

const renderWithRouter = (component) => {
  return render(
    <BrowserRouter>
      {component}
    </BrowserRouter>
  );
};

describe('NotFound Component', () => {
  test('renders 404 error page', () => {
    renderWithRouter(<NotFound />);
    
    expect(screen.getByText('404')).toBeInTheDocument();
    expect(screen.getByText('Page Not Found')).toBeInTheDocument();
    expect(screen.getByText("Sorry, the page you are looking for doesn't exist.")).toBeInTheDocument();
  });

  test('renders go home link', () => {
    renderWithRouter(<NotFound />);
    
    const homeLink = screen.getByRole('link', { name: 'Go Home' });
    expect(homeLink).toBeInTheDocument();
    expect(homeLink).toHaveAttribute('href', '/');
  });

  test('has correct CSS classes', () => {
    renderWithRouter(<NotFound />);
    
    const container = document.querySelector('.container');
    expect(container).toHaveClass('text-center', 'mt-5');
    
    const heading = screen.getByText('404');
    expect(heading).toHaveClass('display-1');
    
    const description = screen.getByText("Sorry, the page you are looking for doesn't exist.");
    expect(description).toHaveClass('lead');
    
    const homeLink = screen.getByRole('link', { name: 'Go Home' });
    expect(homeLink).toHaveClass('btn', 'btn-primary');
  });
});