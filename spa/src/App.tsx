import "./App.css";
import Footer from "./components/Footer.tsx";
import Login from "./components/Login.tsx";
import { useAuthContext } from "./hooks/use-auth.ts";
import MessageList from "./components/MessageList.tsx";
import MessageForm from "./components/MessageForm.tsx";

function App() {
  const { username } = useAuthContext();

  return (
    <>
      <h1>BFF Pattern</h1>
      <Login />
      {username && <p>Welcome, {username}!</p>}
      {username && <MessageList />}
      {username && <MessageForm />}
      <Footer />
    </>
  );
}

export default App;
