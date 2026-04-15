import type { TMessage } from "../lib/types.ts";
import { deleteMessage, updateMessage } from "../lib/queries.ts";
import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";

type MessageProps = {
  message: TMessage;
}

export default function Message({ message }: MessageProps) {
  const [isEditing, setIsEditing] = useState(false);
  const [content, setContent] = useState(message.content);

  const queryClient = useQueryClient();

  const updateMutation = useMutation({
    mutationFn: (newContent: string) =>
      updateMessage(message.id, { content: newContent }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["messages"] });
      setIsEditing(false);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: () => deleteMessage(message.id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["messages"] });
    },
  });

  if (isEditing) {
    return (
      <li key={message.id}>
        <input value={content} onChange={(e) => setContent(e.target.value)} />
        <button onClick={() => updateMutation.mutate(content)}>Save</button>
        <button onClick={() => setIsEditing(false)}>Cancel</button>
      </li>
    );
  }

  return (
    <li key={message.id}>
      <p>{message.content}</p>
      <button onClick={() => setIsEditing(true)}>Edit</button>
      <button onClick={() => deleteMutation.mutate()}>Delete</button>
    </li>
  );
}
