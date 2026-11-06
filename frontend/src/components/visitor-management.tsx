"use client";

import {
  IconBuildingCommunity,
  IconCamera,
  IconCheck,
  IconClock,
  IconLogout,
  IconShield,
} from "@tabler/icons-react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { BrowserQRCodeReader, type IScannerControls } from "@zxing/browser";
import axios from "axios";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { type FormEvent, useEffect, useRef, useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { apiClient } from "@/lib/api/api-client";

type Visit = {
  visitorId: number;
  visitorName: string;
  phone: string;
  purpose: string;
  flatId: number;
  flatNumber: string;
  entryTime: string;
  exitTime: string | null;
  status: "IN" | "OUT";
};

type FlatQrValidation = {
  flatId: number;
  flatNumber: string;
  blockName: string;
};

type VisitorForm = {
  visitorName: string;
  phone: string;
  purpose: string;
};

const initialForm: VisitorForm = {
  visitorName: "",
  phone: "",
  purpose: "",
};

function responseMessage(error: unknown, fallback: string) {
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
      return "Your account does not have permission for this action.";
    }
    if (error.response?.status === 401) {
      return "Your session is invalid or expired. Sign in again.";
    }
    if (!error.response) return "Could not reach the server. Try again.";
  }
  return fallback;
}

function formatTime(value: string | null) {
  if (!value) return "—";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
}

export function VisitorManagement() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const videoRef = useRef<HTMLVideoElement>(null);
  const scannerRef = useRef<IScannerControls | null>(null);
  const [authorized, setAuthorized] = useState(false);
  const [isGuard, setIsGuard] = useState(false);
  const [isAdmin, setIsAdmin] = useState(false);
  const [scanning, setScanning] = useState(false);
  const [qrCode, setQrCode] = useState("");
  const [validatedFlat, setValidatedFlat] = useState<FlatQrValidation | null>(
    null,
  );
  const [form, setForm] = useState(initialForm);
  const [feedback, setFeedback] = useState<{
    kind: "error" | "success";
    message: string;
  } | null>(null);

  useEffect(() => {
    if (!window.sessionStorage.getItem("savecms.sessionToken")) {
      router.replace("/");
      return;
    }
    const role = window.sessionStorage.getItem("savecms.sessionRole");
    setIsGuard(role === "SECURITY_GUARD");
    setIsAdmin(role === "ADMIN");
    setAuthorized(true);
    return () => scannerRef.current?.stop();
  }, [router]);

  const historyQuery = useQuery({
    queryKey: ["visitor-history"],
    enabled: authorized,
    retry: false,
    queryFn: async () => {
      const response = await apiClient<{ visitors: Visit[] }>({
        url: "/api/visitors",
        method: "GET",
      });
      return response.data.visitors;
    },
  });

  const qrMutation = useMutation({
    mutationFn: async (identifier: string) => {
      const response = await apiClient<FlatQrValidation>({
        url: "/api/flats/qr-code/validate",
        method: "POST",
        data: { qrCode: identifier },
      });
      return { flat: response.data, identifier };
    },
    onSuccess: ({ flat, identifier }) => {
      setValidatedFlat(flat);
      setQrCode(identifier);
      setFeedback({
        kind: "success",
        message: `Verified ${flat.blockName} · ${flat.flatNumber}. Enter visitor details to record entry.`,
      });
    },
    onError: (error) => {
      setValidatedFlat(null);
      setFeedback({
        kind: "error",
        message: responseMessage(error, "Could not validate this flat QR."),
      });
    },
  });

  const entryMutation = useMutation({
    mutationFn: async () => {
      if (!validatedFlat) {
        throw new Error(
          "Scan and validate a flat QR code before recording entry.",
        );
      }
      return apiClient({
        url: "/api/visitors/entry",
        method: "POST",
        data: {
          ...form,
          qrCode,
        },
      });
    },
    onSuccess: async () => {
      setForm(initialForm);
      setQrCode("");
      setValidatedFlat(null);
      setFeedback({ kind: "success", message: "Visitor entry recorded." });
      await queryClient.invalidateQueries({ queryKey: ["visitor-history"] });
    },
    onError: (error) =>
      setFeedback({
        kind: "error",
        message: responseMessage(error, "Could not record visitor entry."),
      }),
  });

  const exitMutation = useMutation({
    mutationFn: async (visitorId: number) =>
      apiClient({
        url: `/api/visitors/${visitorId}/exit`,
        method: "PUT",
      }),
    onSuccess: async () => {
      setFeedback({ kind: "success", message: "Visitor exit recorded." });
      await queryClient.invalidateQueries({ queryKey: ["visitor-history"] });
    },
    onError: (error) =>
      setFeedback({
        kind: "error",
        message: responseMessage(error, "Could not record visitor exit."),
      }),
  });

  const authError =
    historyQuery.error ??
    qrMutation.error ??
    entryMutation.error ??
    exitMutation.error;

  useEffect(() => {
    if (axios.isAxiosError(authError) && authError.response?.status === 401) {
      window.sessionStorage.removeItem("savecms.sessionToken");
      window.sessionStorage.removeItem("savecms.sessionRole");
      window.sessionStorage.setItem(
        "savecms.loginNotice",
        "Your session expired. Sign in again to continue.",
      );
      router.replace("/");
    }
  }, [authError, router]);

  async function startScanner() {
    if (!videoRef.current) return;
    setFeedback(null);
    try {
      const reader = new BrowserQRCodeReader();
      scannerRef.current = await reader.decodeFromVideoDevice(
        undefined,
        videoRef.current,
        (result, _error, controls) => {
          scannerRef.current = controls;
          if (!result) return;
          const identifier = result.getText();
          controls.stop();
          scannerRef.current = null;
          setScanning(false);
          setQrCode(identifier);
          qrMutation.mutate(identifier);
        },
      );
      setScanning(true);
    } catch (error) {
      setFeedback({
        kind: "error",
        message:
          error instanceof Error
            ? `Could not start camera scan: ${error.message}`
            : "Could not start camera scan. Check browser camera permissions.",
      });
    }
  }

  function stopScanner() {
    scannerRef.current?.stop();
    scannerRef.current = null;
    setScanning(false);
  }

  function validateIdentifier(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const identifier = qrCode.trim();
    if (!identifier) return;
    setValidatedFlat(null);
    setFeedback(null);
    qrMutation.mutate(identifier);
  }

  function recordEntry(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!validatedFlat) {
      setFeedback({
        kind: "error",
        message: "Validate a registered flat QR code before recording entry.",
      });
      return;
    }
    entryMutation.mutate();
  }

  if (!authorized) {
    return (
      <main className="grid min-h-screen place-items-center bg-slate-50">
        <p className="text-sm text-slate-500">Checking your session…</p>
      </main>
    );
  }

  const visits = historyQuery.data ?? [];
  const activeVisits = visits.filter((visit) => visit.status === "IN");

  return (
    <main className="min-h-screen bg-[#f5f7ff] text-slate-900">
      <header className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-200 bg-white px-5 py-4 sm:px-8">
        <Link className="flex items-center gap-2 font-semibold" href="/">
          <IconBuildingCommunity className="text-teal-700" size={20} />
          SaveCMS Security
        </Link>
        <nav
          className="flex items-center gap-2 text-sm"
          aria-label="Security navigation"
        >
          <Link
            className="rounded-lg bg-teal-50 px-3 py-2 font-medium text-teal-800"
            href="/security/visitors"
          >
            Visitors
          </Link>
          <Link
            className="rounded-lg px-3 py-2 text-slate-600 hover:bg-slate-100"
            href="/security/flat-qr"
          >
            Flat QR
          </Link>
          {isAdmin && (
            <Link
              className="rounded-lg px-3 py-2 text-slate-600 hover:bg-slate-100"
              href="/admin/flats"
            >
              Flats
            </Link>
          )}
        </nav>
      </header>

      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-8">
        <div className="flex flex-wrap items-end justify-between gap-4">
          <div>
            <p className="text-[10px] font-bold tracking-[0.16em] text-teal-800">
              EP-03 · VISITOR ENTRY AND EXIT
            </p>
            <h1 className="mt-1 text-3xl font-semibold tracking-tight">
              Visitor log
            </h1>
            <p className="mt-2 text-sm text-slate-500">
              Verify a flat QR before entry, record exits, and review
              chronological visit history.
            </p>
          </div>
          <div className="flex gap-3 text-xs">
            <span className="rounded-lg bg-white px-3 py-2 shadow-sm">
              {activeVisits.length} active
            </span>
            <span className="rounded-lg bg-white px-3 py-2 shadow-sm">
              {visits.length} visits
            </span>
          </div>
        </div>

        {feedback && (
          <p
            aria-live="polite"
            className={`mt-5 rounded-lg border px-4 py-3 text-sm ${
              feedback.kind === "error"
                ? "border-red-200 bg-red-50 text-red-700"
                : "border-emerald-200 bg-emerald-50 text-emerald-800"
            }`}
          >
            {feedback.message}
          </p>
        )}

        {isGuard && (
          <section className="mt-5 grid gap-5 lg:grid-cols-[minmax(0,1.1fr)_minmax(320px,0.9fr)]">
            <div className="space-y-4 rounded-xl border border-slate-100 bg-white p-5 shadow-sm">
              <div>
                <h2 className="font-semibold">1. Validate destination flat</h2>
                <p className="mt-1 text-xs text-slate-500">
                  Scan the registered flat QR or enter its identifier.
                </p>
              </div>
              <div className="flex flex-wrap gap-2">
                <Button
                  disabled={scanning || qrMutation.isPending}
                  onClick={startScanner}
                  type="button"
                >
                  <IconCamera size={16} /> Scan flat QR
                </Button>
                {scanning && (
                  <Button onClick={stopScanner} type="button" variant="outline">
                    Stop camera
                  </Button>
                )}
              </div>
              <video
                aria-label="Camera preview for visitor destination QR scanning"
                className={`max-h-[300px] w-full rounded-xl bg-black object-contain ${
                  scanning ? "" : "hidden"
                }`}
                muted
                playsInline
                ref={videoRef}
              />
              <form className="space-y-2" onSubmit={validateIdentifier}>
                <Label className="text-xs font-medium" htmlFor="visitorFlatQr">
                  Flat QR identifier
                </Label>
                <div className="flex flex-col gap-2 sm:flex-row">
                  <Input
                    autoComplete="off"
                    className="h-10 rounded-lg border-slate-200 bg-white font-mono text-sm"
                    id="visitorFlatQr"
                    onChange={(event) => {
                      setQrCode(event.target.value);
                      setValidatedFlat(null);
                    }}
                    placeholder="Scan or enter QR identifier"
                    required
                    value={qrCode}
                  />
                  <Button disabled={qrMutation.isPending} type="submit">
                    {qrMutation.isPending ? "Checking…" : "Validate"}
                  </Button>
                </div>
              </form>
              {validatedFlat && (
                <output
                  aria-live="polite"
                  className="block rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-900"
                >
                  <span className="inline-flex items-center gap-2 font-semibold">
                    <IconCheck size={16} /> {validatedFlat.blockName} ·{" "}
                    {validatedFlat.flatNumber} verified
                  </span>
                </output>
              )}
            </div>

            <form
              className="space-y-4 rounded-xl border border-slate-100 bg-white p-5 shadow-sm"
              onSubmit={recordEntry}
            >
              <div>
                <h2 className="font-semibold">2. Record visitor entry</h2>
                <p className="mt-1 text-xs text-slate-500">
                  An entry timestamp is recorded by the server.
                </p>
              </div>
              <div className="space-y-1.5">
                <Label className="text-xs font-medium" htmlFor="visitorName">
                  Visitor name
                </Label>
                <Input
                  className="h-10 rounded-lg border-slate-200 bg-white text-sm"
                  id="visitorName"
                  onChange={(event) =>
                    setForm({ ...form, visitorName: event.target.value })
                  }
                  required
                  value={form.visitorName}
                />
              </div>
              <div className="space-y-1.5">
                <Label className="text-xs font-medium" htmlFor="visitorPhone">
                  Phone
                </Label>
                <Input
                  className="h-10 rounded-lg border-slate-200 bg-white text-sm"
                  id="visitorPhone"
                  onChange={(event) =>
                    setForm({ ...form, phone: event.target.value })
                  }
                  required
                  type="tel"
                  value={form.phone}
                />
              </div>
              <div className="space-y-1.5">
                <Label className="text-xs font-medium" htmlFor="visitorPurpose">
                  Purpose of visit
                </Label>
                <Input
                  className="h-10 rounded-lg border-slate-200 bg-white text-sm"
                  id="visitorPurpose"
                  onChange={(event) =>
                    setForm({ ...form, purpose: event.target.value })
                  }
                  required
                  value={form.purpose}
                />
              </div>
              <Button
                className="w-full"
                disabled={!validatedFlat || entryMutation.isPending}
                type="submit"
              >
                {entryMutation.isPending
                  ? "Recording…"
                  : "Record visitor entry"}
              </Button>
            </form>
          </section>
        )}

        <section className="mt-6 overflow-hidden rounded-xl border border-slate-100 bg-white shadow-sm">
          <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-100 px-5 py-4">
            <div>
              <h2 className="font-semibold">Visit history</h2>
              <p className="mt-1 text-xs text-slate-500">
                Newest entries first; exit time and status are kept on each
                visit.
              </p>
            </div>
            <span className="inline-flex items-center gap-1.5 rounded-full bg-slate-100 px-3 py-1.5 text-xs text-slate-600">
              <IconClock size={14} /> Chronological
            </span>
          </div>
          {historyQuery.isLoading ? (
            <p className="p-5 text-sm text-slate-500">Loading visit history…</p>
          ) : historyQuery.isError ? (
            <p className="p-5 text-sm text-red-700">
              {responseMessage(
                historyQuery.error,
                "Could not load visitor history.",
              )}
            </p>
          ) : visits.length === 0 ? (
            <p className="p-5 text-sm text-slate-500">
              No visitor records yet.
            </p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full min-w-[760px] text-left text-sm">
                <thead className="bg-slate-50 text-[10px] uppercase tracking-wide text-slate-500">
                  <tr>
                    <th className="px-5 py-3 font-semibold">Visitor</th>
                    <th className="px-5 py-3 font-semibold">Flat / purpose</th>
                    <th className="px-5 py-3 font-semibold">Entry</th>
                    <th className="px-5 py-3 font-semibold">Exit</th>
                    <th className="px-5 py-3 font-semibold">Status</th>
                    {isGuard && (
                      <th className="px-5 py-3 font-semibold">Action</th>
                    )}
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {visits.map((visit) => (
                    <tr key={visit.visitorId}>
                      <td className="px-5 py-4">
                        <p className="font-medium">{visit.visitorName}</p>
                        <p className="mt-1 text-xs text-slate-500">
                          {visit.phone}
                        </p>
                      </td>
                      <td className="px-5 py-4">
                        <p className="font-medium">{visit.flatNumber}</p>
                        <p className="mt-1 text-xs text-slate-500">
                          {visit.purpose}
                        </p>
                      </td>
                      <td className="px-5 py-4 text-xs text-slate-600">
                        {formatTime(visit.entryTime)}
                      </td>
                      <td className="px-5 py-4 text-xs text-slate-600">
                        {formatTime(visit.exitTime)}
                      </td>
                      <td className="px-5 py-4">
                        <span
                          className={`rounded-full px-2.5 py-1 text-[10px] font-bold ${visit.status === "IN" ? "bg-emerald-100 text-emerald-800" : "bg-slate-100 text-slate-600"}`}
                        >
                          {visit.status === "IN" ? "ACTIVE" : "EXITED"}
                        </span>
                      </td>
                      {isGuard && (
                        <td className="px-5 py-4">
                          {visit.status === "IN" ? (
                            <Button
                              disabled={exitMutation.isPending}
                              onClick={() =>
                                exitMutation.mutate(visit.visitorId)
                              }
                              size="sm"
                              variant="outline"
                            >
                              <IconLogout size={14} /> Record exit
                            </Button>
                          ) : (
                            <span className="text-xs text-slate-400">
                              Complete
                            </span>
                          )}
                        </td>
                      )}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
        <footer className="mt-5 flex items-center gap-2 text-xs text-slate-500">
          <IconShield size={14} /> Entry and exit actions are restricted to
          authenticated security guards.
        </footer>
      </div>
    </main>
  );
}
