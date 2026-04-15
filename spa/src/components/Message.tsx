import type { TMessage } from "../lib/types.ts";
import { deleteMessage, updateMessage } from "../lib/queries.ts";
import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";

type MessageProps = {
  message: TMessage;
};

export default function Message({ message }: MessageProps) {
  const [isEditing, setIsEditing] = useState(false);
  const [content, setContent] = useState(message.content);

  const queryClient = useQueryClient();

  const { mutate: update, isPending: updateIsPending } = useMutation({
    mutationFn: (newContent: string) =>
      updateMessage(message.id, { content: newContent }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["messages"] });
      setIsEditing(false);
    },
  });

  const { mutate: remove, isPending: deleteIsPending } = useMutation({
    mutationFn: () => deleteMessage(message.id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["messages"] });
    },
  });

  const isPending = updateIsPending || deleteIsPending;

  if (isEditing) {
    return (
      <li>
        <input
          value={content}
          onChange={(e) => setContent(e.target.value)}
          disabled={isPending}
        />
        <button onClick={() => update(content)} disabled={isPending}>
          {updateIsPending ? "Saving..." : "Save"}
        </button>
        <button onClick={() => setIsEditing(false)} disabled={isPending}>
          Cancel
        </button>
      </li>
    );
  }

  return (
    <li>
      <p>{message.content}</p>
      <button onClick={() => setIsEditing(true)} disabled={isPending}>
        Edit
      </button>
      <button onClick={() => remove()} disabled={isPending}>
        {deleteIsPending ? "Deleting..." : "Delete"}
      </button>
    </li>
  );
}
