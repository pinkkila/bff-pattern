import "./App.css";
import Footer from "./components/Footer.tsx";
import Login from "./components/Login.tsx";
import { useAuthContext } from "./hooks/use-auth.ts";
import MessageList from "./components/MessageList.tsx";
import MessageForm from "./components/MessageForm.tsx";
import { getMessages } from "./lib/queries.ts";
import { useQuery } from "@tanstack/react-query";

function App() {
  const { user, isPending: userIsPending } = useAuthContext();

  const { data: messagePage, isLoading, isError } = useQuery({
    queryKey: ["messages"],
    queryFn: getMessages,
    enabled: !!user && !userIsPending,
  });

  return (
    <>
      <h1>BFF Pattern</h1>
      <Login />
      {user && <p>Welcome, {user.username}!</p>}

      {user && isLoading && <p>Loading...</p>}
      {user && isError && <p>Error loading messages</p>}

      {user && messagePage && (
        <MessageList messages={messagePage.content}
        />
      )}
      {user && <MessageForm />}
      <Footer />
    </>
  );
}

export default App;
