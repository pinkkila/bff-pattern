import { createContext } from "react";
import type { TUser } from "../lib/types.ts";

export type TAuthContext = {
  user: TUser | null;
  isPending: boolean;
  logout: () => void;
  logoutIsPending: boolean;
};

export const AuthContext = createContext<TAuthContext | null>(null);
