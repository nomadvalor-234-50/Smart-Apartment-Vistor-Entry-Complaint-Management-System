import axios, { type AxiosRequestConfig, type AxiosResponse } from "axios";

export const apiClient = <T>(
  config: AxiosRequestConfig,
): Promise<AxiosResponse<T>> => {
  const baseURL = process.env.NEXT_PUBLIC_API_BASE_URL;

  if (!baseURL) {
    throw new Error(
      "NEXT_PUBLIC_API_BASE_URL must be set before making API requests.",
    );
  }

  const token =
    typeof window === "undefined"
      ? null
      : window.sessionStorage.getItem("savecms.sessionToken");

  return axios.request<T>({
    ...config,
    baseURL,
    headers: {
      ...config.headers,
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  });
};
