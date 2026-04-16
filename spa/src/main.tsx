import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import AuthContextProvider from "./contexts/AuthContextProvider.tsx";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";

const queryClient = new QueryClient();
// const queryClient = new QueryClient({
//   defaultOptions: {
//     queries: {
//       retry: (failureCount, error: any) => {
//         if (error?.status === 401) return false;
//         return failureCount < 3;
//       },
//     },
//     mutations: {
//       onError: (error: any) => {
//         if (error?.status === 401) {
//           queryClient.invalidateQueries({ queryKey: ["auth"] });
//         }
//       },
//     },
//   },
// });

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <AuthContextProvider>
        <App />
      </AuthContextProvider>
    </QueryClientProvider>
  </StrictMode>,
)
