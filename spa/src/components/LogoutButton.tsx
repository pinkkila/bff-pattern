import { useAuthContext } from "../hooks/use-auth.ts";

export default function LogoutButton() {
  const { logout } = useAuthContext();

  return <button onClick={logout} className="login-btn">Logout</button>;
}
