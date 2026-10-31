import { defineConfig } from "orval";

export default defineConfig({
  savecms: {
    input: {
      target: "http://localhost:8080/v3/api-docs",
    },
    output: {
      mode: "tags-split",
      target: "./src/lib/api/generated/endpoints.ts",
      schemas: "./src/lib/api/generated/model",
      client: "react-query",
      httpClient: "axios",
      override: {
        mutator: {
          path: "./src/lib/api/api-client.ts",
          name: "apiClient",
        },
      },
      clean: true,
    },
  },
});
