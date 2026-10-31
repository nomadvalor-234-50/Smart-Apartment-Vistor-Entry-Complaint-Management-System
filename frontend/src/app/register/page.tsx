import type { Metadata } from "next";
import { RegisterScreen } from "@/components/register-screen";

export const metadata: Metadata = {
  title: "Create Account | SaveCMS",
  description: "Create a SaveCMS account for your apartment community.",
};

export default function RegisterPage() {
  return <RegisterScreen />;
}
