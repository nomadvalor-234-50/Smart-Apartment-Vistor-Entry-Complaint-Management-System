import type { Metadata } from "next";
import { AdminUsersDashboard } from "@/components/admin-users-dashboard";

export const metadata: Metadata = {
  title: "User Management | SaveCMS",
  description: "Admin view of registered SaveCMS community users.",
};

export default function AdminUsersPage() {
  return <AdminUsersDashboard />;
}
