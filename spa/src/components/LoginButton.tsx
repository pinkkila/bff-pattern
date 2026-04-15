const LOGIN_URL = "http://localhost:8080/oauth2/authorization/bff-client";

export default function LoginButton() {
  const handleLogin = () => {
    window.location.href = LOGIN_URL;
  };

  return (
    <button onClick={handleLogin} className="login-btn">
      Login
    </button>
  );
}
