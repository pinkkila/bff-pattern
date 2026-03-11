import "./App.css";
import Footer from "./components/Footer.tsx";

function App() {
  return (
    <>
      <h1>Vite + React</h1>
      <button
        onClick={() =>
          (window.location.href = "http://localhost:8080/oauth2/authorization/bff-client")
        }
      >
        Login
      </button>
      <Footer />
    </>
  );
}

export default App;
