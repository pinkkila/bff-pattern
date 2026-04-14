// import { useEffect } from "react";
import type { TMessage } from "../lib/types.ts";
import Message from "./Message.tsx";

type MessageListProps = {
  messages: TMessage[];
  fetchMessages: () => void;
}

export default function MessageList({messages, fetchMessages}: MessageListProps) {

  return (
    <>
      {messages.length === 0 && <p>No messages yet.</p>}
      <ul>
        {messages.map((message) => (
          <Message message={message} fetchMessages={fetchMessages} />
        ))}
      </ul>
    </>
  );
}
