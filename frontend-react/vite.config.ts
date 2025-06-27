import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

// This is the Vite configuration file. It tells Vite how to build and serve your frontend.
export default defineConfig({
  // Integrate the React plugin for Vite, which provides React-specific optimizations and HMR.
  plugins: [react()],

  // Define the root directory for your project.
  // Vite will look for index.html and other public assets relative to this path.
  // __dirname refers to the directory where vite.config.ts itself is located (frontend-react).
  root: path.resolve(__dirname, './'),

  // Specify the directory where static assets (like index.html) are located.
  // This tells Vite that `public` is the public-facing directory within the `root`.
  publicDir: 'public',

  // Configure the build process.
  build: {
    // The directory where the compiled and optimized frontend assets will be output.
    // This 'build' directory will then be copied into the Ktor backend's resources.
    outDir: 'build',
    // Ensure the output directory is cleaned before each build.
    emptyOutDir: true,
  },

  // Configure the development server.
  server: {
    // Set up a proxy to forward API requests from the frontend to the Ktor backend.
    // When the frontend makes a request to /api, it will be redirected to the Ktor server.
    proxy: {
      // Proxy requests starting with '/api'
      '/api': {
        // The target URL of your Ktor backend.
        // During development, this will typically be localhost:8080.
        target: 'http://localhost:8080',
        // Change the origin of the host header to the target URL.
        // This is often necessary for compatibility with backend CORS policies.
        changeOrigin: true,
        // Rewrite the URL path: remove the '/api' prefix before forwarding to the backend.
        // So, a request to /api/assets becomes /assets on the Ktor server.
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
});