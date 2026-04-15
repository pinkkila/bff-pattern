import { createContext } from "react";

export type TAuthContext = {
  username: string | null;
  isPending: boolean;
  logout: () => void;
  logoutIsPending: boolean;
};

export const AuthContext = createContext<TAuthContext | null>(null);
