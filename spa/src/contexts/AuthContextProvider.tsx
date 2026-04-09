import { useEffect, useState } from "react";
import * as React from "react";
import { AuthContext } from "./AuthContext";
import { getUserinfo, logoutRequest } from "../lib/queries.ts";

export default function AuthContextProvider({children}: {children: React.ReactNode}) {
  const [data, setData] = useState<{ sub: string } | null>(null);

  useEffect(() => {
    const fetchUserInfo = async () => {
      try {
        const userData = await getUserinfo();
        setData(userData);
      } catch (error) {
        console.info("Auth initialization:", error);
        setData(null);
      }
    };

    fetchUserInfo();
  }, [])

  const logout = async () => {
    logoutRequest()
    setData(null)
  }

  return (
    <AuthContext.Provider value={{username: data?.sub ?? null, logout}}>{children}</AuthContext.Provider>
  );
}
