import { useState } from 'react';
import CarForm from './components/CarForm';
import CarList from './components/CarList';
import ServiceForm from './components/ServiceForm';
import ServiceList from './components/ServiceList';
import type { Car } from './types/car';

function App() {
  const [carRefreshTrigger, setCarRefreshTrigger] = useState(0);
  const [serviceRefreshTrigger, setServiceRefreshTrigger] = useState(0);
  const [selectedCar, setSelectedCar] = useState<Car | null>(null);

  function handleCarCreated() {
    setCarRefreshTrigger((prev) => prev + 1);
  }

  function handleServiceCreated() {
    setServiceRefreshTrigger((prev) => prev + 1);
  }

  return (
    <div style={{ maxWidth: 900, margin: '0 auto', padding: 24 }}>
      <h1>Car Service Manager</h1>

      <CarForm onCarCreated={handleCarCreated} />

      <hr />

      <CarList
        refreshTrigger={carRefreshTrigger}
        selectedCarId={selectedCar?.id ?? null}
        onSelectCar={setSelectedCar}
      />

      <hr />

      <ServiceForm onServiceCreated={handleServiceCreated} defaultCarId={selectedCar?.id ?? null} />

      <hr />

      <ServiceList selectedCarId={selectedCar?.id ?? null} refreshTrigger={serviceRefreshTrigger} />
    </div>
  );
}

export default App;