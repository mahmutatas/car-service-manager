import { useState } from 'react';
import CarForm from './components/CarForm';
import CarList from './components/CarList';
import type { Car } from './types/car';

function App() {
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const [selectedCar, setSelectedCar] = useState<Car | null>(null);

  function handleCarCreated() {
    setRefreshTrigger((prev) => prev + 1);
  }

  return (
    <div style={{ maxWidth: 900, margin: '0 auto', padding: 24 }}>
      <h1>Car Service Manager</h1>

      <CarForm onCarCreated={handleCarCreated} />

      <hr />

      <CarList
        refreshTrigger={refreshTrigger}
        selectedCarId={selectedCar?.id ?? null}
        onSelectCar={setSelectedCar}
      />

      {selectedCar && (
        <div style={{ marginTop: 24 }}>
          <h3>Services for {selectedCar.licensePlate}</h3>
          <p>(Service list will go here next)</p>
        </div>
      )}
    </div>
  );
}

export default App;