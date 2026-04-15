import "./App.css";
import Footer from "./components/Footer.tsx";
import Login from "./components/Login.tsx";
import { useAuthContext } from "./hooks/use-auth.ts";
import MessageList from "./components/MessageList.tsx";
import MessageForm from "./components/MessageForm.tsx";
import { getMessages } from "./lib/queries.ts";
import { useQuery } from "@tanstack/react-query";

function App() {
  const { username, isPending: usernameIsPending } = useAuthContext();

  const { data: messagePage, isLoading, isError } = useQuery({
    queryKey: ["messages"],
    queryFn: getMessages,
    enabled: !!username && !usernameIsPending,
  });

  return (
    <>
      <h1>BFF Pattern</h1>
      <Login />
      {username && <p>Welcome, {username}!</p>}

      {username && isLoading && <p>Loading...</p>}
      {username && isError && <p>Error loading messages</p>}

      {username && messagePage && (
        <MessageList messages={messagePage.content}
        />
      )}
      {username && <MessageForm />}
      <Footer />
    </>
  );
}

export default App;
