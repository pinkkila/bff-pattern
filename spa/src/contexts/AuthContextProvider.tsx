import { useEffect, useState } from "react";
import * as React from "react";
import { AuthContext } from "./AuthContext";
import { getUserinfo } from "../lib/queries.ts";

export default function AuthContextProvider({children}: {children: React.ReactNode}) {
  const [data, setData] = useState<{ sub: string } | null>(null);

  useEffect(() => {
    const fetchUserInfo = async () => {
      try {
        const userData = await getUserinfo();
        setData(userData);
      } catch (error) {
        // console.error("Auth initialization error:", error);
        setData(null);
      }
    };

    fetchUserInfo();
  }, [])


  return (
    <AuthContext.Provider value={{username: data?.sub ?? null}}>{children}</AuthContext.Provider>
  );
}
