import type { TMessage } from "../lib/types.ts";

export default function Message({ message }: { message: TMessage }) {
  return <div>
    <p>{message.content}</p>
    <button>Edit</button>
    <button>Delete</button>
  </div>;
}
