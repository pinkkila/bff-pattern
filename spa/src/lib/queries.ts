import { getCsrfToken } from "./utils.ts";

const BFF_URL = "http://localhost:8080";

export async function getUserinfo(): Promise<{ sub: string }> {
  const response = await fetch(`${BFF_URL}/userinfo`, {
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

export async function logoutRequest() {
  const csrfToken = getCsrfToken();

  const response = await fetch(`${BFF_URL}/logout`, {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      ...(csrfToken ? { "X-XSRF-TOKEN": csrfToken } : {}),
    },
  });

  if (!response.ok) {
    throw new Error(`Logout failed: ${response.statusText}`);
  }
}

