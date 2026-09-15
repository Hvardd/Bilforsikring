import InsurancePurchaseForm from './components/InsurancePurchaseForm'
import './App.css'

function App() {
  return (
    <main className="purchase-page">
      <header className="purchase-page__header">
        <h1>Kjøp Bilforsikring</h1>
        <p>
          Det er fire forskjellige forsikringer å velge mellom.
          Ansvarsforsikring er lovpålagt om kjøretøyet er registrert og skal
          brukes på veien. I tillegg kan du utvide forsikringen avhengig av hvor
          gammel bilen din er og hvordan du bruker den.
        </p>
      </header>
      <InsurancePurchaseForm />
    </main>
  )
}

export default App
