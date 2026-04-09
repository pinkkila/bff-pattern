import { useAuthContext } from "../hooks/use-auth.ts";
import LogoutButton from "./LogoutButton.tsx";
import LoginButton from "./LoginButton.tsx";

export default function Login() {
  const { username } = useAuthContext();

  return <section>{username ? <LogoutButton /> : <LoginButton />}</section>;
}
