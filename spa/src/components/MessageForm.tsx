import { useState } from "react";
import { createMessage } from "../lib/queries.ts";

export default function MessageForm() {
  const [message, setMessage] = useState("");

  const handleSubmit = () => {
    createMessage(message);
  }

  return (
    <form>
      <input type="text" value={message} onChange={(e) => setMessage(e.target.value)} />
      <button type="submit" onClick={handleSubmit}>Send</button>
    </form>
  );
}
