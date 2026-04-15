import "./App.css";
import Footer from "./components/Footer.tsx";
import Login from "./components/Login.tsx";
import { useAuthContext } from "./hooks/use-auth.ts";
import MessageBoard from "./components/MessageBoard.tsx";

export default function App() {
  const { user, isPending: userIsPending } = useAuthContext();

  if (userIsPending) {
    return <p>Loading application...</p>;
  }

  return (
    <>
      <div className="header">
        <h1>BFF Pattern</h1>
        <Login />
      </div>
      {user ? <MessageBoard user={user} /> : <p className="please-login">Please login.</p>}
      <Footer />
    </>
  );
}
