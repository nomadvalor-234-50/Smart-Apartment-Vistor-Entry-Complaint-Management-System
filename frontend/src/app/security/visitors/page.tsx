import type { Metadata } from "next";
import { VisitorManagement } from "@/components/visitor-management";

export const metadata: Metadata = {
  title: "Visitor Entry & History | SaveCMS",
  description: "Record visitor entry and exit, and review visit history.",
};

export default function VisitorsPage() {
  return <VisitorManagement />;
}
