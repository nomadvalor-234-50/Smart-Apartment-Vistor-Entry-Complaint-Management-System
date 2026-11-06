import type { Metadata } from "next";
import { FlatQrScanner } from "@/components/flat-qr-scanner";

export const metadata: Metadata = {
  title: "Validate Flat QR | SaveCMS",
  description: "Scan and validate a registered flat QR code.",
};

export default function FlatQrPage() {
  return <FlatQrScanner />;
}
