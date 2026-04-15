import { type SyntheticEvent, useState } from "react";
import { createMessage } from "../lib/queries.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";

export default function MessageForm() {
  const [message, setMessage] = useState("");
  const queryClient = useQueryClient();

  const { mutate, isPending: createMessageIsPending } = useMutation({
    mutationFn: createMessage,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["messages"] });
      setMessage("");
    },
    onError: (error) => {
      console.error("Failed to create message:", error);
    },
  });

  const handleSubmit = (e: SyntheticEvent) => {
    e.preventDefault();
    mutate({ content: message });
  };

  return (
    <form onSubmit={handleSubmit} className="message-form">
      <input
        type="text"
        value={message}
        onChange={(e) => setMessage(e.target.value)}
        disabled={createMessageIsPending}
      />
      <button
        type="submit"
        disabled={createMessageIsPending || !message.trim()}
      >
        {createMessageIsPending ? "Sending..." : "Send"}
      </button>
    </form>
  );
}
