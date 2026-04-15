import { useState } from "react";
import { createMessage } from "../lib/queries.ts";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import type { TMessageRequest } from "../lib/types.ts";

export default function MessageForm() {
  const [message, setMessage] = useState("");
  const queryClient = useQueryClient();

  const {
    mutate,
    // isPending: createMessageIsPending,
    // isError: createMessageIsError,
  } = useMutation({
    mutationFn: (messageRequest: TMessageRequest) =>
      createMessage(messageRequest),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["messages"] });
    },
  });

  const handleSubmit = () => {
    mutate({ content: message });
  };

  return (
    <form>
      <input
        type="text"
        value={message}
        onChange={(e) => setMessage(e.target.value)}
      />
      <button type="submit" onClick={handleSubmit}>
        Send
      </button>
    </form>
  );
}
