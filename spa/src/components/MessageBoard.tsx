import { useQuery } from "@tanstack/react-query";
import { getMessages } from "../lib/queries.ts";
import type { TUser } from "../lib/types.ts";
import MessageList from "./MessageList.tsx";
import MessageForm from "./MessageForm.tsx";

type MessageBoardProps = {
  user: TUser;
};

export default function MessageBoard({ user }: MessageBoardProps) {
  const { data: messagePage, isLoading, isError, } = useQuery({
    queryKey: ["messages"],
    queryFn: getMessages,
    enabled: !!user,
  });

  return (
    <main>
      <p>Welcome, {user.username}!</p>

      {isLoading && <p>Loading messages...</p>}
      {isError && <p>Error loading messages. Please try again later.</p>}

      {messagePage && <MessageList messages={messagePage.content} />}
      <MessageForm />
    </main>
  );
}
