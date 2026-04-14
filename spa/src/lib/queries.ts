import { getCsrfToken } from "./utils.ts";
import type { TMessageRequest, TMessagesPage } from "./types.ts";

const BFF_URL = "http://localhost:8080";

export async function getUserinfo(): Promise<{ sub: string }> {
  const response = await fetch(`${BFF_URL}/userinfo`, {
    credentials: "include",
    headers: {
      Accept: "application/json",
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

export async function getMessages(): Promise<TMessagesPage> {
  const response = await fetch(`${BFF_URL}/api/messages`, {
    credentials: "include",
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch messages: ${response.statusText}`);
  }

  return await response.json();
}

export async function createMessage(messageRequest: TMessageRequest) {
  const csrfToken = getCsrfToken();

  const response = await fetch(`${BFF_URL}/api/messages`, {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      ...(csrfToken ? { "X-XSRF-TOKEN": csrfToken } : {}),
    },
    body: JSON.stringify(messageRequest),
  });

  if (!response.ok) {
    throw new Error("Something went wrong, when creating new message. " + response.statusText);
  }
}

export async function updateMessage(id: number, messageRequest: TMessageRequest) {
  const csrfToken = getCsrfToken();

  const response = await fetch(`${BFF_URL}/api/messages/${id}`, {
    method: "PUT",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      ...(csrfToken ? { "X-XSRF-TOKEN": csrfToken } : {}),
    },
    body: JSON.stringify(messageRequest),
  })

  if (!response.ok) {
    throw new Error("Something went wrong, when updating message. " + response.statusText);
  }
}

export async function deleteMessage(id: number) {
  const csrfToken = getCsrfToken();

  const response = await fetch(`${BFF_URL}/api/messages/${id}`, {
    method: "DELETE",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      ...(csrfToken ? { "X-XSRF-TOKEN": csrfToken } : {}),
    },
  });

  if (!response.ok) {
    throw new Error("Something went wrong, when deleting message. " + response.statusText);
  }
}