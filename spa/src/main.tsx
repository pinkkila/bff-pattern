import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import AuthContextProvider from "./contexts/AuthContextProvider.tsx";
import { MutationCache, QueryCache, QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { UnauthorizedError } from "./lib/queries.ts";

// When the BFF session has ended, fetching userinfo again shows the logged-out view
function handleError(error: Error) {
  if (error instanceof UnauthorizedError) {
    void queryClient.invalidateQueries({ queryKey: ["auth"] });
  }
}

const queryClient = new QueryClient({
  queryCache: new QueryCache({ onError: handleError }),
  mutationCache: new MutationCache({ onError: handleError }),
  defaultOptions: {
    queries: {
      retry: (failureCount, error) =>
        !(error instanceof UnauthorizedError) && failureCount < 3,
    },
  },
});

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <AuthContextProvider>
        <App />
      </AuthContextProvider>
    </QueryClientProvider>
  </StrictMode>,
)
