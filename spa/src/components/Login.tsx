import { useAuthContext } from "../hooks/use-auth.ts";
import LogoutButton from "./LogoutButton.tsx";
import LoginButton from "./LoginButton.tsx";

export default function Login() {
  const { user } = useAuthContext();

  return <>{user ? <LogoutButton /> : <LoginButton />}</>;
}
