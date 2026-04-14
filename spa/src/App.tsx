import "./App.css";
import Footer from "./components/Footer.tsx";
import Login from "./components/Login.tsx";
import { useAuthContext } from "./hooks/use-auth.ts";
import MessageList from "./components/MessageList.tsx";
import MessageForm from "./components/MessageForm.tsx";
import { getMessages } from "./lib/queries.ts";
import { useEffect, useState } from "react";
import type { TMessage } from "./lib/types.ts";

function App() {
  const { username } = useAuthContext();
  const [messages, setMessages] = useState<TMessage[]>([]);

  const fetchMessages = async () => {
    try {
      const messagesPage = await getMessages();
      setMessages(messagesPage.content);
    } catch (error) {
      console.error("Error fetching messages:", error);
    }
  };

  useEffect(() => {
    if (username) {
      fetchMessages();
    }
  }, [username]);

  return (
    <>
      <h1>BFF Pattern</h1>
      <Login />
      {username && <p>Welcome, {username}!</p>}
      {username && <MessageList messages={messages} fetchMessages={fetchMessages} />}
      {username && <MessageForm fetchMessages={fetchMessages} />}
      <Footer />
    </>
  );
}

export default App;
