import * as React from "react";
import { AuthContext } from "./AuthContext";
import { getUserinfo, logoutRequest } from "../lib/queries.ts";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

export default function AuthContextProvider({
  children,
}: {
  children: React.ReactNode;
}) {
  const queryClient = useQueryClient();

  const { data, isPending } = useQuery({
    queryKey: ["auth", "userinfo"],
    queryFn: getUserinfo,
    retry: false,
    staleTime: Infinity,
  });

  const { mutate: logout, isPending: logoutIsPending } = useMutation({
    mutationFn: logoutRequest,
    onMutate: async () => {
      await queryClient.cancelQueries({ queryKey: ["auth", "userinfo"] });
      queryClient.setQueryData(["auth", "userinfo"], null);
    },
    onSuccess: () => {
      queryClient.removeQueries({ queryKey: ["auth"] });
      queryClient.removeQueries({ queryKey: ["messages"] });

      // TODO: Add toaster?
    },
    onError: (error) => {
      console.error("Logout failed:", error);
      // TODO: Add toaster?
    },
  });

  return (
    <AuthContext.Provider
      value={{
        username: data?.sub ?? null,
        isPending,
        logout,
        logoutIsPending,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}
