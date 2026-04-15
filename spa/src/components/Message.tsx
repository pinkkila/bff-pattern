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

  return (
    <li className="message">
      {isEditing ? (
        <MessageEdit
          content={content}
          setContent={setContent}
          onSave={() => update(content)}
          onCancel={() => {
            setIsEditing(false);
            setContent(message.content);
          }}
          isPending={isPending}
          updateIsPending={updateIsPending}
        />
      ) : (
        <MessageView
          content={message.content}
          onEdit={() => setIsEditing(true)}
          onDelete={() => remove()}
          isPending={isPending}
          deleteIsPending={deleteIsPending}
        />
      )}
    </li>
  );
}

type MessageViewProps = {
  content: string;
  onEdit: () => void;
  onDelete: () => void;
  isPending: boolean;
  deleteIsPending: boolean;
};

function MessageView({ content, onEdit, onDelete, isPending, deleteIsPending }: MessageViewProps) {
  return (
    <>
      <p>{content}</p>
      <div className="actions">
        <button onClick={onEdit} disabled={isPending}>
          Edit
        </button>
        <button onClick={onDelete} disabled={isPending}>
          {deleteIsPending ? "Deleting..." : "Delete"}
        </button>
      </div>
    </>
  );
}

type MessageEditProps = {
  content: string;
  setContent: (value: string) => void;
  onSave: () => void;
  onCancel: () => void;
  isPending: boolean;
  updateIsPending: boolean;
};

function MessageEdit({ content, setContent, onSave, onCancel, isPending, updateIsPending }: MessageEditProps) {
  return (
    <>
      <input
        value={content}
        onChange={(e) => setContent(e.target.value)}
        disabled={isPending}
        autoFocus
      />
      <div className="actions">
        <button onClick={onSave} disabled={isPending || !content.trim()}>
          {updateIsPending ? "Saving..." : "Save"}
        </button>
        <button onClick={onCancel} disabled={isPending}>
          Cancel
        </button>
      </div>
    </>
  );
}
