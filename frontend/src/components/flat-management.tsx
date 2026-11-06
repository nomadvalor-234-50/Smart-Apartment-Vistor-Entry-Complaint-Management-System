"use client";

import {
  IconBuildingCommunity,
  IconHome,
  IconQrcode,
  IconUsers,
} from "@tabler/icons-react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import axios from "axios";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { QRCodeSVG } from "qrcode.react";
import { type FormEvent, useEffect, useMemo, useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { apiClient } from "@/lib/api/api-client";

type FlatRecord = {
  flatId: number;
  flatNumber: string;
  blockName: string;
  floorNumber: number;
  numberOfRooms: number;
  qrCode?: string | null;
};

type ResidentRecord = {
  residentId: number;
  fullName: string;
  email: string;
  phone: string;
  emergencyContact: string;
  numberOfOccupants: number;
  flatId: number;
  flatNumber: string;
};

type FlatForm = {
  flatNumber: string;
  blockName: string;
  floorNumber: string;
  numberOfRooms: string;
};

type ResidentForm = {
  fullName: string;
  email: string;
  phone: string;
  emergencyContact: string;
  numberOfOccupants: string;
  flatId: string;
};

const emptyFlat: FlatForm = {
  flatNumber: "",
  blockName: "",
  floorNumber: "0",
  numberOfRooms: "1",
};

const emptyResident: ResidentForm = {
  fullName: "",
  email: "",
  phone: "",
  emergencyContact: "",
  numberOfOccupants: "1",
  flatId: "",
};

function errorMessage(error: unknown, fallback: string) {
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
    if (!error.response) return "Could not reach the server. Try again.";
  }
  return fallback;
}

function TextField({
  label,
  name,
  value,
  onChange,
  type = "text",
  min,
  required = true,
}: {
  label: string;
  name: string;
  value: string;
  onChange: (value: string) => void;
  type?: string;
  min?: number;
  required?: boolean;
}) {
  return (
    <div className="space-y-1.5">
      <Label htmlFor={name} className="text-xs font-medium text-slate-700">
        {label}
      </Label>
      <Input
        className="h-10 rounded-lg border-slate-200 bg-white text-sm"
        id={name}
        min={min}
        name={name}
        onChange={(event) => onChange(event.target.value)}
        required={required}
        type={type}
        value={value}
      />
    </div>
  );
}

export function FlatManagement() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const [authorized, setAuthorized] = useState(false);
  const [tab, setTab] = useState<"flats" | "residents">("flats");
  const [flatForm, setFlatForm] = useState(emptyFlat);
  const [editingFlatId, setEditingFlatId] = useState<number | null>(null);
  const [residentForm, setResidentForm] = useState(emptyResident);
  const [editingResidentId, setEditingResidentId] = useState<number | null>(
    null,
  );
  const [feedback, setFeedback] = useState<string | null>(null);
  const [qrFlat, setQrFlat] = useState<FlatRecord | null>(null);

  useEffect(() => {
    if (!window.sessionStorage.getItem("savecms.sessionToken")) {
      router.replace("/");
      return;
    }
    setAuthorized(true);
  }, [router]);

  const flatsQuery = useQuery({
    queryKey: ["ep02-flats"],
    enabled: authorized,
    retry: false,
    queryFn: async () => {
      const response = await apiClient<{ flats: FlatRecord[] }>({
        url: "/api/flats",
        method: "GET",
      });
      return response.data.flats;
    },
  });

  const residentsQuery = useQuery({
    queryKey: ["ep02-residents"],
    enabled: authorized,
    retry: false,
    queryFn: async () => {
      const response = await apiClient<{ residents: ResidentRecord[] }>({
        url: "/api/residents",
        method: "GET",
      });
      return response.data.residents;
    },
  });

  useEffect(() => {
    const error = flatsQuery.error ?? residentsQuery.error;
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      window.sessionStorage.removeItem("savecms.sessionToken");
      window.sessionStorage.setItem(
        "savecms.loginNotice",
        "Your session expired. Sign in again to continue.",
      );
      router.replace("/");
    }
  }, [flatsQuery.error, residentsQuery.error, router]);

  const flats = flatsQuery.data ?? [];
  const residents = residentsQuery.data ?? [];

  const flatMutation = useMutation({
    mutationFn: async () => {
      const data = {
        flatNumber: flatForm.flatNumber.trim(),
        blockName: flatForm.blockName.trim(),
        floorNumber: Number(flatForm.floorNumber),
        numberOfRooms: Number(flatForm.numberOfRooms),
      };
      return apiClient({
        url: editingFlatId ? `/api/flats/${editingFlatId}` : "/api/flats",
        method: editingFlatId ? "PUT" : "POST",
        data,
      });
    },
    onSuccess: async () => {
      setFeedback(editingFlatId ? "Flat updated." : "Flat created.");
      setFlatForm(emptyFlat);
      setEditingFlatId(null);
      await queryClient.invalidateQueries({ queryKey: ["ep02-flats"] });
    },
    onError: (error) =>
      setFeedback(errorMessage(error, "Could not save flat.")),
  });

  const residentMutation = useMutation({
    mutationFn: async () => {
      const data = {
        fullName: residentForm.fullName.trim(),
        email: residentForm.email.trim(),
        phone: residentForm.phone.trim(),
        emergencyContact: residentForm.emergencyContact.trim(),
        numberOfOccupants: Number(residentForm.numberOfOccupants),
        flatId: Number(residentForm.flatId),
      };
      return apiClient({
        url: editingResidentId
          ? `/api/residents/${editingResidentId}`
          : "/api/residents",
        method: editingResidentId ? "PUT" : "POST",
        data,
      });
    },
    onSuccess: async () => {
      setFeedback(
        editingResidentId ? "Resident updated." : "Resident created.",
      );
      setResidentForm(emptyResident);
      setEditingResidentId(null);
      await queryClient.invalidateQueries({ queryKey: ["ep02-residents"] });
    },
    onError: (error) =>
      setFeedback(errorMessage(error, "Could not save resident.")),
  });

  const qrMutation = useMutation({
    mutationFn: async (flatId: number) => {
      const response = await apiClient<FlatRecord & { qrCode: string }>({
        url: `/api/flats/${flatId}/qr-code`,
        method: "POST",
      });
      return response.data;
    },
    onSuccess: async (flat) => {
      setQrFlat({
        flatId: flat.flatId,
        flatNumber: flat.flatNumber,
        blockName: flat.blockName,
        floorNumber: flat.floorNumber,
        numberOfRooms: flat.numberOfRooms,
        qrCode: flat.qrCode,
      });
      setFeedback("Flat QR generated.");
      await queryClient.invalidateQueries({ queryKey: ["ep02-flats"] });
    },
    onError: (error) =>
      setFeedback(errorMessage(error, "Could not generate flat QR.")),
  });

  const residentCounts = useMemo(() => {
    const counts = new Map<number, number>();
    for (const resident of residents) {
      counts.set(resident.flatId, (counts.get(resident.flatId) ?? 0) + 1);
    }
    return counts;
  }, [residents]);

  function submitFlat(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setFeedback(null);
    flatMutation.mutate();
  }

  function submitResident(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setFeedback(null);
    residentMutation.mutate();
  }

  function editFlat(flat: FlatRecord) {
    setTab("flats");
    setEditingFlatId(flat.flatId);
    setFlatForm({
      flatNumber: flat.flatNumber,
      blockName: flat.blockName,
      floorNumber: String(flat.floorNumber),
      numberOfRooms: String(flat.numberOfRooms),
    });
    setFeedback(null);
  }

  function editResident(resident: ResidentRecord) {
    setTab("residents");
    setEditingResidentId(resident.residentId);
    setResidentForm({
      fullName: resident.fullName,
      email: resident.email,
      phone: resident.phone,
      emergencyContact: resident.emergencyContact,
      numberOfOccupants: String(resident.numberOfOccupants),
      flatId: String(resident.flatId),
    });
    setFeedback(null);
  }

  if (!authorized) {
    return (
      <main className="grid min-h-screen place-items-center bg-slate-50">
        <p className="text-sm text-slate-500">Checking your session…</p>
      </main>
    );
  }

  const listError = flatsQuery.isError
    ? errorMessage(flatsQuery.error, "Could not load flats.")
    : residentsQuery.isError
      ? errorMessage(residentsQuery.error, "Could not load residents.")
      : null;

  return (
    <main className="min-h-screen bg-[#f5f7ff] text-slate-900">
      <header className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-200 bg-white px-5 py-4 sm:px-8">
        <Link
          className="flex items-center gap-2 font-semibold"
          href="/admin/users"
        >
          <IconBuildingCommunity className="text-teal-700" size={20} />
          SaveCMS Admin
        </Link>
        <nav
          className="flex items-center gap-2 text-sm"
          aria-label="Admin navigation"
        >
          <Link
            className="rounded-lg px-3 py-2 text-slate-600 hover:bg-slate-100"
            href="/admin/users"
          >
            Users
          </Link>
          <Link
            className="rounded-lg bg-teal-50 px-3 py-2 font-medium text-teal-800"
            href="/admin/flats"
          >
            Flats & Residents
          </Link>
          <Link
            className="rounded-lg px-3 py-2 text-slate-600 hover:bg-slate-100"
            href="/security/flat-qr"
          >
            QR validation
          </Link>
        </nav>
      </header>

      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-8">
        <div className="flex flex-wrap items-end justify-between gap-4">
          <div>
            <p className="text-[10px] font-bold tracking-[0.16em] text-teal-800">
              EP-02 · FLAT AND RESIDENT MANAGEMENT
            </p>
            <h1 className="mt-1 text-3xl font-semibold tracking-tight">
              Flats & Residents
            </h1>
            <p className="mt-2 text-sm text-slate-500">
              Maintain flat records, assign residents, and issue flat QR
              identifiers.
            </p>
          </div>
          <div className="flex gap-3 text-xs">
            <span className="rounded-lg bg-white px-3 py-2 shadow-sm">
              {flats.length} flats
            </span>
            <span className="rounded-lg bg-white px-3 py-2 shadow-sm">
              {residents.length} residents
            </span>
          </div>
        </div>

        <div className="mt-6 flex gap-2 border-b border-slate-200">
          <button
            aria-pressed={tab === "flats"}
            className={`inline-flex items-center gap-2 border-b-2 px-4 py-3 text-sm font-medium ${
              tab === "flats"
                ? "border-teal-700 text-teal-800"
                : "border-transparent text-slate-500"
            }`}
            onClick={() => setTab("flats")}
            type="button"
          >
            <IconHome size={16} /> Flats
          </button>
          <button
            aria-pressed={tab === "residents"}
            className={`inline-flex items-center gap-2 border-b-2 px-4 py-3 text-sm font-medium ${
              tab === "residents"
                ? "border-teal-700 text-teal-800"
                : "border-transparent text-slate-500"
            }`}
            onClick={() => setTab("residents")}
            type="button"
          >
            <IconUsers size={16} /> Residents
          </button>
        </div>

        {listError && (
          <p
            className="mt-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700"
            role="alert"
          >
            {listError}
          </p>
        )}
        {feedback && (
          <output
            className="mt-4 rounded-lg border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700"
            aria-live="polite"
          >
            {feedback}
          </output>
        )}

        {tab === "flats" ? (
          <div className="mt-5 grid gap-5 lg:grid-cols-[minmax(0,1.4fr)_minmax(300px,0.8fr)]">
            <section className="overflow-hidden rounded-xl border border-slate-100 bg-white shadow-sm">
              <div className="border-b border-slate-100 px-5 py-4">
                <h2 className="font-semibold">Registered flats</h2>
                <p className="mt-1 text-xs text-slate-500">
                  Create, edit, or generate a flat QR code.
                </p>
              </div>
              {flatsQuery.isLoading ? (
                <p className="p-5 text-sm text-slate-500">Loading flats…</p>
              ) : flats.length === 0 ? (
                <p className="p-5 text-sm text-slate-500">
                  No flats registered yet.
                </p>
              ) : (
                <ul className="divide-y divide-slate-100">
                  {flats.map((flat) => (
                    <li
                      className="flex flex-wrap items-center justify-between gap-3 px-5 py-4"
                      key={flat.flatId}
                    >
                      <div>
                        <p className="text-sm font-semibold">
                          {flat.blockName} · {flat.flatNumber}
                        </p>
                        <p className="mt-1 text-xs text-slate-500">
                          Floor {flat.floorNumber} · {flat.numberOfRooms} rooms
                          · {residentCounts.get(flat.flatId) ?? 0} residents
                        </p>
                        {flat.qrCode && (
                          <p className="mt-1 max-w-xs truncate font-mono text-[10px] text-slate-400">
                            {flat.qrCode}
                          </p>
                        )}
                      </div>
                      <div className="flex flex-wrap gap-2">
                        <Button
                          onClick={() => editFlat(flat)}
                          size="sm"
                          variant="outline"
                        >
                          Edit
                        </Button>
                        <Button
                          disabled={qrMutation.isPending}
                          onClick={() => qrMutation.mutate(flat.flatId)}
                          size="sm"
                          variant="secondary"
                        >
                          <IconQrcode size={15} />{" "}
                          {flat.qrCode ? "View QR" : "Generate QR"}
                        </Button>
                        {flat.qrCode && (
                          <Button
                            onClick={() => setQrFlat(flat)}
                            size="sm"
                            variant="outline"
                          >
                            Show
                          </Button>
                        )}
                      </div>
                    </li>
                  ))}
                </ul>
              )}
              {qrFlat?.qrCode && (
                <div className="border-t border-slate-100 bg-slate-50 p-5">
                  <div className="flex flex-wrap items-center gap-5">
                    <div className="rounded-lg bg-white p-3">
                      <QRCodeSVG value={qrFlat.qrCode} size={160} level="M" />
                    </div>
                    <div className="min-w-0">
                      <h3 className="font-semibold">
                        QR · {qrFlat.blockName} {qrFlat.flatNumber}
                      </h3>
                      <p className="mt-2 break-all font-mono text-xs text-slate-600">
                        {qrFlat.qrCode}
                      </p>
                      <p className="mt-2 text-xs text-slate-500">
                        Security staff can scan this code from the QR validation
                        page.
                      </p>
                    </div>
                    <Button
                      className="ml-auto"
                      onClick={() => setQrFlat(null)}
                      size="sm"
                      variant="ghost"
                    >
                      Close
                    </Button>
                  </div>
                </div>
              )}
            </section>

            <form
              className="h-fit space-y-4 rounded-xl border border-slate-100 bg-white p-5 shadow-sm"
              onSubmit={submitFlat}
            >
              <div>
                <h2 className="font-semibold">
                  {editingFlatId ? "Update flat" : "Add a flat"}
                </h2>
                <p className="mt-1 text-xs text-slate-500">
                  Flat numbers must be unique.
                </p>
              </div>
              <TextField
                label="Flat number"
                name="flatNumber"
                value={flatForm.flatNumber}
                onChange={(value) =>
                  setFlatForm({ ...flatForm, flatNumber: value })
                }
              />
              <TextField
                label="Block"
                name="blockName"
                value={flatForm.blockName}
                onChange={(value) =>
                  setFlatForm({ ...flatForm, blockName: value })
                }
              />
              <div className="grid grid-cols-2 gap-3">
                <TextField
                  label="Floor"
                  name="floorNumber"
                  type="number"
                  min={0}
                  value={flatForm.floorNumber}
                  onChange={(value) =>
                    setFlatForm({ ...flatForm, floorNumber: value })
                  }
                />
                <TextField
                  label="Rooms"
                  name="numberOfRooms"
                  type="number"
                  min={1}
                  value={flatForm.numberOfRooms}
                  onChange={(value) =>
                    setFlatForm({ ...flatForm, numberOfRooms: value })
                  }
                />
              </div>
              <div className="flex gap-2">
                <Button
                  className="flex-1"
                  disabled={flatMutation.isPending}
                  type="submit"
                >
                  {flatMutation.isPending
                    ? "Saving…"
                    : editingFlatId
                      ? "Save changes"
                      : "Create flat"}
                </Button>
                {editingFlatId && (
                  <Button
                    onClick={() => {
                      setEditingFlatId(null);
                      setFlatForm(emptyFlat);
                    }}
                    type="button"
                    variant="outline"
                  >
                    Cancel
                  </Button>
                )}
              </div>
            </form>
          </div>
        ) : (
          <div className="mt-5 grid gap-5 lg:grid-cols-[minmax(0,1.4fr)_minmax(300px,0.8fr)]">
            <section className="overflow-hidden rounded-xl border border-slate-100 bg-white shadow-sm">
              <div className="border-b border-slate-100 px-5 py-4">
                <h2 className="font-semibold">Registered residents</h2>
                <p className="mt-1 text-xs text-slate-500">
                  Each resident is assigned to a registered flat.
                </p>
              </div>
              {residentsQuery.isLoading ? (
                <p className="p-5 text-sm text-slate-500">Loading residents…</p>
              ) : residents.length === 0 ? (
                <p className="p-5 text-sm text-slate-500">
                  No residents registered yet.
                </p>
              ) : (
                <ul className="divide-y divide-slate-100">
                  {residents.map((resident) => (
                    <li
                      className="flex flex-wrap items-center justify-between gap-3 px-5 py-4"
                      key={resident.residentId}
                    >
                      <div>
                        <p className="text-sm font-semibold">
                          {resident.fullName}
                        </p>
                        <p className="mt-1 text-xs text-slate-500">
                          {resident.email} · {resident.phone}
                        </p>
                        <p className="mt-1 text-xs text-slate-500">
                          {resident.flatNumber} · {resident.numberOfOccupants}{" "}
                          occupants
                        </p>
                      </div>
                      <Button
                        onClick={() => editResident(resident)}
                        size="sm"
                        variant="outline"
                      >
                        Edit
                      </Button>
                    </li>
                  ))}
                </ul>
              )}
            </section>
            <form
              className="h-fit space-y-4 rounded-xl border border-slate-100 bg-white p-5 shadow-sm"
              onSubmit={submitResident}
            >
              <div>
                <h2 className="font-semibold">
                  {editingResidentId ? "Update resident" : "Add a resident"}
                </h2>
                <p className="mt-1 text-xs text-slate-500">
                  Email and phone must be unique.
                </p>
              </div>
              <TextField
                label="Full name"
                name="residentName"
                value={residentForm.fullName}
                onChange={(value) =>
                  setResidentForm({ ...residentForm, fullName: value })
                }
              />
              <TextField
                label="Email"
                name="residentEmail"
                type="email"
                value={residentForm.email}
                onChange={(value) =>
                  setResidentForm({ ...residentForm, email: value })
                }
              />
              <TextField
                label="Phone"
                name="residentPhone"
                type="tel"
                value={residentForm.phone}
                onChange={(value) =>
                  setResidentForm({ ...residentForm, phone: value })
                }
              />
              <TextField
                label="Emergency contact"
                name="emergencyContact"
                type="tel"
                value={residentForm.emergencyContact}
                onChange={(value) =>
                  setResidentForm({ ...residentForm, emergencyContact: value })
                }
              />
              <div className="grid grid-cols-2 gap-3">
                <TextField
                  label="Occupants"
                  name="occupants"
                  type="number"
                  min={1}
                  value={residentForm.numberOfOccupants}
                  onChange={(value) =>
                    setResidentForm({
                      ...residentForm,
                      numberOfOccupants: value,
                    })
                  }
                />
                <div className="space-y-1.5">
                  <Label
                    className="text-xs font-medium text-slate-700"
                    htmlFor="residentFlat"
                  >
                    Flat
                  </Label>
                  <select
                    className="h-10 w-full rounded-lg border border-slate-200 bg-white px-3 text-sm"
                    id="residentFlat"
                    onChange={(event) =>
                      setResidentForm({
                        ...residentForm,
                        flatId: event.target.value,
                      })
                    }
                    required
                    value={residentForm.flatId}
                  >
                    <option value="">Select flat</option>
                    {flats.map((flat) => (
                      <option key={flat.flatId} value={flat.flatId}>
                        {flat.blockName} · {flat.flatNumber}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
              {flats.length === 0 && (
                <p className="text-xs text-amber-700">
                  Create a flat before adding residents.
                </p>
              )}
              <div className="flex gap-2">
                <Button
                  className="flex-1"
                  disabled={residentMutation.isPending || flats.length === 0}
                  type="submit"
                >
                  {residentMutation.isPending
                    ? "Saving…"
                    : editingResidentId
                      ? "Save changes"
                      : "Create resident"}
                </Button>
                {editingResidentId && (
                  <Button
                    onClick={() => {
                      setEditingResidentId(null);
                      setResidentForm(emptyResident);
                    }}
                    type="button"
                    variant="outline"
                  >
                    Cancel
                  </Button>
                )}
              </div>
            </form>
          </div>
        )}
      </div>
    </main>
  );
}
