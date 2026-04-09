import { createContext } from "react";

export type TAuthContext = {
  username: string | null;
  logout: () => void;
};

export const AuthContext = createContext<TAuthContext | null>(null);
