import type { TMessage } from "../lib/types.ts";
import { deleteMessage, updateMessage } from "../lib/queries.ts";
import { useState } from "react";

type MessageProps = {
  message: TMessage;
  fetchMessages: () => void;
}

export default function Message({ message, fetchMessages }: MessageProps) {
  const [isEditing, setIsEditing] = useState(false);
  const [content, setContent] = useState(message.content);

  const handleUpdate = async () => {
    try {
      await updateMessage(message.id, { content });
      setIsEditing(false);
      fetchMessages();
    } catch (error) {
      console.error("Failed to update message:", error);
    }
  };

  const handleDelete = async () => {
    await deleteMessage(message.id);
    fetchMessages(); // Refresh the list
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
      <button onClick={handleDelete}>Delete</button>
    </li>
  );
}
