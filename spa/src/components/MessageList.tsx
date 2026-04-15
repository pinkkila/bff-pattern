import type { TMessage } from "../lib/types.ts";
import Message from "./Message.tsx";

type MessageListProps = {
  messages: TMessage[];
};

export default function MessageList({ messages }: MessageListProps) {
  return (
    <>
      {messages.length === 0 && <p>No messages yet.</p>}
      <ul>
        {messages.map((message) => (
          <Message key={message.id} message={message} />
        ))}
      </ul>
    </>
  );
}
