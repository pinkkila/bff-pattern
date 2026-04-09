import { AuthContext } from "../contexts/AuthContext.ts";
import { useContext } from "react";

export function useAuthContext() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error(
      "useAuthContext must be used within an AuthContextProvider",
    );
  }
  return context;
}
