import { useEffect, useState } from "react";
import type { TMessage } from "../lib/types.ts";
import { getMessages } from "../lib/queries.ts";
import Message from "./Message.tsx";

export default function MessageList() {
  const [messages, setMessages] = useState<TMessage[]>([]);

  useEffect(() => {
    const fetchMessages = async () => {
      try {
        const messagesPage = await getMessages();
        setMessages(messagesPage.content);
      } catch (error) {
        console.error("Error fetching messages:", error);
      }
    };
    fetchMessages();
  }, []);

  return (
    <>
      {messages.length === 0 && <p>No messages yet.</p>}
      <ul>
        {messages.map((message) => (
          <Message message={message} />
        ))}
      </ul>
    </>
  );
}
