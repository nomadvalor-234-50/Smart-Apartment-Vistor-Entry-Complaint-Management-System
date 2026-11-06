import type { Metadata } from "next";
import { FlatManagement } from "@/components/flat-management";

export const metadata: Metadata = {
  title: "Flats & Residents | SaveCMS",
  description: "Manage registered flats, residents, and flat QR codes.",
};

export default function AdminFlatsPage() {
  return <FlatManagement />;
}
