import type { TMessage } from "../lib/types.ts";
import { deleteMessage, updateMessage } from "../lib/queries.ts";
import { useState } from "react";

export default function Message({ message }: { message: TMessage }) {
  const [isEditing, setIsEditing] = useState(false);
  const [content, setContent] = useState(message.content);

  const handleUpdate = async () => {
    try {
      // TMessageRequest expects { content: string }
      await updateMessage(message.id, { content });
      setIsEditing(false);
      // Optional: Refresh the page or trigger a re-fetch in the parent component
      // window.location.reload();
    } catch (error) {
      console.error("Failed to update message:", error);
    }
  };

  if (isEditing) {
    return (
      <li key={message.id}>
        <input value={content} onChange={(e) => setContent(e.target.value)} />
        <button onClick={handleUpdate}>Save</button>
        <button onClick={() => setIsEditing(false)}>Cancel</button>
      </li>
    );
  }

  return (
    <li key={message.id}>
      <p>{message.content}</p>
      <button onClick={() => setIsEditing(true)}>Edit</button>
      <button onClick={() => deleteMessage(message.id)}>Delete</button>
    </li>
  );
}
