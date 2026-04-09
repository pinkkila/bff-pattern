// const API_URL = import.meta.env.VITE_API_URL;
const API_URL = "http://localhost:8080";

export async function getUserinfo(): Promise<{ sub: string }> {
  const response = await fetch(`${API_URL}/userinfo`, {
    credentials: "include",
    headers: {
      "Accept": "application/json",
    },
  });
  if (!response.ok) {
    throw new Error(`Not authenticated. ${response.statusText}`);
  }
  return await response.json();
}

