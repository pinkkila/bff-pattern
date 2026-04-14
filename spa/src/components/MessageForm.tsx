import { useState } from "react";
import { createMessage } from "../lib/queries.ts";
import type { TMessageRequest } from "../lib/types.ts";
import * as React from "react";

export default function MessageForm() {
  const [message, setMessage] = useState<TMessageRequest>({ content: "" });

  const handleSubmit = (e: React.SyntheticEvent) => {
    e.preventDefault();
    createMessage(message);
    setMessage({content: ""});
  }

  return (
    <form>
      <input type="text" value={message.content} onChange={(e) => setMessage({content: e.target.value})} />
      <button type="submit" onClick={(e) => handleSubmit(e)}>Send</button>
    </form>
  );
}
