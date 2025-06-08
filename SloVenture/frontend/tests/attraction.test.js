import React, { act } from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import Attractions from '../src/components/Attractions';
import axios from 'axios';
import { MemoryRouter } from 'react-router-dom';

jest.mock('axios');

test('prikaže ime in opis znamenitosti', async () => {
  axios.get.mockImplementation((url) => {
    if (url.includes('regions'))
        return Promise.resolve({ data: [] });
    
    return Promise.resolve({
      data: [{
        attraction: { _id: '1', name: 'Grad', description: 'Opis gradu', locationType: 'Zgodovinska' },
        images: [{ url: '/images/grad.jpg' }]
      }]
    });
  });

  render(
    <MemoryRouter>
      <Attractions />
    </MemoryRouter>
  );

  expect(await screen.findByText('Grad')).toBeInTheDocument();
  expect(screen.getByText('Opis gradu')).toBeInTheDocument();
});