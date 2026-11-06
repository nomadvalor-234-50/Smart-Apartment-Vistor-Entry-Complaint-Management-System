"use client";

import {
  IconBuildingCommunity,
  IconCamera,
  IconCheck,
  IconShield,
} from "@tabler/icons-react";
import { useMutation } from "@tanstack/react-query";
import { BrowserQRCodeReader, type IScannerControls } from "@zxing/browser";
import axios from "axios";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { type FormEvent, useEffect, useRef, useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { apiClient } from "@/lib/api/api-client";

type ValidatedFlat = {
  message: string;
  flatId: number;
  flatNumber: string;
  blockName: string;
  floorNumber: number;
  qrCode: string;
};

function getErrorMessage(error: unknown) {
  if (axios.isAxiosError(error)) {
    const data: unknown = error.response?.data;
    if (
      typeof data === "object" &&
      data !== null &&
      "message" in data &&
      typeof data.message === "string"
    ) {
      return data.message;
    }
    if (error.response?.status === 403) {
      return "QR validation is available to security guards and administrators.";
    }
    if (error.response?.status === 401) {
      return "Your session is invalid or expired. Sign in again.";
    }
    if (!error.response) return "Could not reach the server. Try again.";
  }
  return "Could not validate this QR code.";
}

export function FlatQrScanner() {
  const router = useRouter();
  const videoRef = useRef<HTMLVideoElement>(null);
  const scannerRef = useRef<IScannerControls | null>(null);
  const [authorized, setAuthorized] = useState(false);
  const [scanning, setScanning] = useState(false);
  const [qrCode, setQrCode] = useState("");
  const [scanError, setScanError] = useState<string | null>(null);

  useEffect(() => {
    if (!window.sessionStorage.getItem("savecms.sessionToken")) {
      router.replace("/");
      return;
    }
    setAuthorized(true);
    return () => scannerRef.current?.stop();
  }, [router]);

  const validation = useMutation({
    mutationFn: async (identifier: string) => {
      const response = await apiClient<ValidatedFlat>({
        url: "/api/flats/qr-code/validate",
        method: "POST",
        data: { qrCode: identifier },
      });
      return response.data;
    },
    onSuccess: (flat) => {
      setQrCode(flat.qrCode);
      setScanError(null);
    },
    onError: (error) => {
      setScanError(getErrorMessage(error));
    },
  });

  useEffect(() => {
    if (
      axios.isAxiosError(validation.error) &&
      validation.error.response?.status === 401
    ) {
      window.sessionStorage.removeItem("savecms.sessionToken");
      window.sessionStorage.setItem(
        "savecms.loginNotice",
        "Your session expired. Sign in again to continue.",
      );
      router.replace("/");
    }
  }, [router, validation.error]);

  async function startScanner() {
    if (!videoRef.current) return;
    setScanError(null);
    try {
      const reader = new BrowserQRCodeReader();
      scannerRef.current = await reader.decodeFromVideoDevice(
        undefined,
        videoRef.current,
        (result, _error, controls) => {
          scannerRef.current = controls;
          if (!result) return;
          const text = result.getText();
          setQrCode(text);
          controls.stop();
          setScanning(false);
          validation.mutate(text);
        },
      );
      setScanning(true);
    } catch (error) {
      setScanError(
        error instanceof Error
          ? `Could not start the camera: ${error.message}`
          : "Could not start the camera. Check browser camera permissions.",
      );
    }
  }

  function stopScanner() {
    scannerRef.current?.stop();
    scannerRef.current = null;
    setScanning(false);
  }

  function submitCode(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const identifier = qrCode.trim();
    if (!identifier) return;
    setScanError(null);
    validation.mutate(identifier);
  }

  if (!authorized) {
    return (
      <main className="grid min-h-screen place-items-center bg-slate-50">
        <p className="text-sm text-slate-500">Checking your session…</p>
      </main>
    );
  }

  const flat = validation.data;

  return (
    <main className="min-h-screen bg-[#f5f7ff] text-slate-900">
      <header className="flex items-center justify-between border-b border-slate-200 bg-white px-5 py-4 sm:px-8">
        <Link className="flex items-center gap-2 font-semibold" href="/">
          <IconBuildingCommunity className="text-teal-700" size={20} />
          SaveCMS Security
        </Link>
        <Link
          className="text-sm text-slate-600 hover:text-slate-900"
          href="/admin/flats"
        >
          Flat management
        </Link>
        <Link
          className="text-sm text-slate-600 hover:text-slate-900"
          href="/security/visitors"
        >
          Visitor log
        </Link>
      </header>

      <div className="mx-auto max-w-3xl px-4 py-10 sm:px-8">
        <div className="text-center">
          <span className="mx-auto grid size-12 place-items-center rounded-xl bg-teal-100 text-teal-800">
            <IconShield size={24} aria-hidden="true" />
          </span>
          <p className="mt-5 text-[10px] font-bold tracking-[0.16em] text-teal-800">
            EP-02 · SECURITY VALIDATION
          </p>
          <h1 className="mt-1 text-3xl font-semibold tracking-tight">
            Validate flat QR
          </h1>
          <p className="mx-auto mt-2 max-w-lg text-sm text-slate-500">
            Scan a flat QR code with your camera, or enter its identifier to
            validate it against registered flats.
          </p>
        </div>

        <section className="mt-7 space-y-5 rounded-2xl border border-slate-100 bg-white p-5 shadow-sm sm:p-7">
          <div className="flex flex-wrap gap-2">
            <Button disabled={scanning} onClick={startScanner} type="button">
              <IconCamera size={16} /> Start camera scan
            </Button>
            {scanning && (
              <Button onClick={stopScanner} type="button" variant="outline">
                Stop camera
              </Button>
            )}
          </div>
          <video
            aria-label="Camera preview for scanning flat QR codes"
            className={`max-h-[360px] w-full rounded-xl bg-black object-contain ${
              scanning ? "" : "hidden"
            }`}
            muted
            playsInline
            ref={videoRef}
          />

          <form className="space-y-2" onSubmit={submitCode}>
            <Label
              className="text-xs font-medium text-slate-700"
              htmlFor="qrCode"
            >
              QR identifier
            </Label>
            <div className="flex flex-col gap-2 sm:flex-row">
              <Input
                autoComplete="off"
                className="h-10 rounded-lg border-slate-200 bg-white font-mono text-sm"
                id="qrCode"
                onChange={(event) => {
                  setQrCode(event.target.value);
                  setScanError(null);
                }}
                placeholder="Paste or scan the flat QR identifier"
                required
                value={qrCode}
              />
              <Button disabled={validation.isPending} type="submit">
                {validation.isPending ? "Checking…" : "Validate"}
              </Button>
            </div>
          </form>

          {scanError && (
            <p
              className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700"
              role="alert"
            >
              {scanError}
            </p>
          )}
          {flat && (
            <output
              aria-live="polite"
              className="rounded-xl border border-emerald-200 bg-emerald-50 p-5"
            >
              <div className="flex items-center gap-2 font-semibold text-emerald-900">
                <IconCheck size={18} /> Registered flat verified
              </div>
              <dl className="mt-4 grid gap-3 text-sm sm:grid-cols-2">
                <div>
                  <dt className="text-xs text-emerald-800/70">Flat</dt>
                  <dd className="mt-1 font-medium">
                    {flat.blockName} · {flat.flatNumber}
                  </dd>
                </div>
                <div>
                  <dt className="text-xs text-emerald-800/70">Floor</dt>
                  <dd className="mt-1 font-medium">{flat.floorNumber}</dd>
                </div>
                <div className="sm:col-span-2">
                  <dt className="text-xs text-emerald-800/70">QR identifier</dt>
                  <dd className="mt-1 break-all font-mono text-xs">
                    {flat.qrCode}
                  </dd>
                </div>
              </dl>
            </output>
          )}
        </section>
      </div>
    </main>
  );
}
