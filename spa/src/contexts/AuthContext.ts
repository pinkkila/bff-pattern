import { createContext } from "react";

export type TAuthContext = {
  username: string | null;
};

export const AuthContext = createContext<TAuthContext | null>(null);
