"use client";

import {
  IconBuildingCommunity,
  IconCheck,
  IconChevronDown,
  IconClipboardList,
  IconHome,
  IconLogout,
  IconShield,
  IconTrash,
  IconUserShield,
  IconUsers,
} from "@tabler/icons-react";
import { useQueryClient } from "@tanstack/react-query";
import axios from "axios";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useMemo, useState } from "react";
import { Button } from "@/components/ui/button";
import { UserRole } from "@/lib/api/generated/model/userRole";
import {
  getGetAllUsersQueryKey,
  useApproveUser,
  useChangeUserRole,
  useDeleteUser,
  useGetAllUsers,
  useLogoutUser,
} from "@/lib/api/generated/user-controller/user-controller";

type UserRecord = {
  userId?: number;
  fullName?: string;
  email?: string;
  phone?: string;
  role?: string;
  address?: string;
  accountStatus?: string;
};

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}

function getUserRecords(responseData: unknown): UserRecord[] | null {
  if (!isRecord(responseData) || !Array.isArray(responseData.users)) {
    return null;
  }

  return responseData.users.filter(isRecord).map((user) => ({
    userId: typeof user.userId === "number" ? user.userId : undefined,
    fullName: typeof user.fullName === "string" ? user.fullName : undefined,
    email: typeof user.email === "string" ? user.email : undefined,
    phone: typeof user.phone === "string" ? user.phone : undefined,
    role: typeof user.role === "string" ? user.role : undefined,
    address: typeof user.address === "string" ? user.address : undefined,
    accountStatus:
      typeof user.accountStatus === "string" ? user.accountStatus : undefined,
  }));
}

function getErrorMessage(error: unknown) {
  if (!axios.isAxiosError(error)) {
    return "Could not load users. Please try again.";
  }

  if (error.response?.status === 401) {
    return "Your session is invalid or expired. Sign in again to continue.";
  }

  if (error.response?.status === 403) {
    return "Administrator access is required to view users.";
  }

  const responseData: unknown = error.response?.data;
  if (
    isRecord(responseData) &&
    typeof responseData.message === "string" &&
    responseData.message.length > 0
  ) {
    return responseData.message;
  }

  if (!error.response) {
    return "Could not reach the server. Check your connection and try again.";
  }

  return "Could not load users. Please try again.";
}

function getActionErrorMessage(error: unknown, fallback: string) {
  if (!axios.isAxiosError(error)) {
    return fallback;
  }

  if (error.response?.status === 401) {
    return "Your session is invalid or expired. Sign in again to continue.";
  }

  if (error.response?.status === 403) {
    return "Administrator access is required to manage users.";
  }

  const responseData: unknown = error.response?.data;
  if (
    isRecord(responseData) &&
    typeof responseData.message === "string" &&
    responseData.message.length > 0
  ) {
    return responseData.message;
  }

  if (!error.response) {
    return "Could not reach the server. Check your connection and try again.";
  }

  return fallback;
}

function displayValue(value: string | undefined) {
  return value?.trim() || "—";
}

function roleLabel(role: string | undefined) {
  if (!role) return "Unknown role";
  return role
    .toLowerCase()
    .split("_")
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");
}

function StatusBadge({ status }: { status: string | undefined }) {
  const isPending = status?.toUpperCase() === "PENDING";

  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-[10px] font-bold tracking-wide ${
        isPending
          ? "bg-amber-100 text-amber-800"
          : status?.toUpperCase() === "ACTIVE"
            ? "bg-teal-100 text-teal-800"
            : "bg-slate-100 text-slate-600"
      }`}
    >
      <span
        className={`size-1.5 rounded-full ${
          isPending
            ? "bg-amber-500"
            : status?.toUpperCase() === "ACTIVE"
              ? "bg-teal-600"
              : "bg-slate-400"
        }`}
      />
      {displayValue(status).toUpperCase()}
    </span>
  );
}

function UserTable({
  users,
  onApprove,
  onRoleChange,
  onDelete,
  actionsDisabled,
}: {
  users: UserRecord[];
  onApprove: (user: UserRecord) => void;
  onRoleChange: (userId: number, role: string) => void;
  onDelete: (user: UserRecord) => void;
  actionsDisabled: boolean;
}) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full min-w-[1060px] border-collapse text-left">
        <thead>
          <tr className="bg-[#f0f4ff] text-[10px] font-bold uppercase tracking-[0.08em] text-slate-600">
            <th className="px-5 py-3.5">User identity</th>
            <th className="px-4 py-3.5">Contact email</th>
            <th className="px-4 py-3.5">Phone number</th>
            <th className="px-4 py-3.5">Account role</th>
            <th className="px-4 py-3.5">Account status</th>
            <th className="px-4 py-3.5">Actions</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {users.map((user, index) => (
            <tr
              className={index % 2 === 1 ? "bg-[#fffdfa]" : "bg-white"}
              key={user.userId ?? `${user.email ?? "user"}-${index}`}
            >
              <td className="px-5 py-4">
                <div className="flex items-center gap-3">
                  <span className="grid size-9 shrink-0 place-items-center rounded-full bg-blue-100 text-[11px] font-bold text-blue-900">
                    {displayValue(user.fullName)
                      .split(/\s+/)
                      .filter(Boolean)
                      .slice(0, 2)
                      .map((part) => part[0]?.toUpperCase())
                      .join("") || "?"}
                  </span>
                  <div className="min-w-0">
                    <p className="truncate text-sm font-semibold text-slate-900">
                      {displayValue(user.fullName)}
                    </p>
                    <p className="mt-0.5 truncate text-[10px] text-slate-500">
                      {user.address
                        ? displayValue(user.address)
                        : "Community member"}
                    </p>
                  </div>
                </div>
              </td>
              <td className="px-4 py-4 text-xs text-slate-600">
                {displayValue(user.email)}
              </td>
              <td className="px-4 py-4 text-xs text-slate-600">
                {displayValue(user.phone)}
              </td>
              <td className="px-4 py-4">
                <span className="inline-flex rounded-md bg-blue-50 px-2.5 py-1.5 text-[10px] font-semibold text-blue-900">
                  {roleLabel(user.role)}
                </span>
              </td>
              <td className="px-4 py-4">
                <StatusBadge status={user.accountStatus} />
              </td>
              <td className="px-4 py-3">
                <div className="flex items-center gap-2">
                  {user.accountStatus?.toUpperCase() === "PENDING" && (
                    <Button
                      className="h-8 gap-1 px-2.5 text-[10px]"
                      disabled={user.userId === undefined || actionsDisabled}
                      onClick={() => onApprove(user)}
                      type="button"
                      variant="outline"
                    >
                      <IconCheck size={14} aria-hidden="true" />
                      Approve
                    </Button>
                  )}
                  <label
                    className="sr-only"
                    htmlFor={`user-role-${user.userId}`}
                  >
                    Change role for {displayValue(user.fullName)}
                  </label>
                  <select
                    className="h-8 min-w-32 rounded-md border border-slate-200 bg-white px-2 text-[10px] text-slate-800 outline-none focus-visible:border-blue-500 focus-visible:ring-2 focus-visible:ring-blue-500/30"
                    disabled={user.userId === undefined || actionsDisabled}
                    id={`user-role-${user.userId}`}
                    onChange={(event) => {
                      if (user.userId !== undefined) {
                        onRoleChange(user.userId, event.target.value);
                      }
                    }}
                    value={user.role ?? ""}
                  >
                    <option disabled value="">
                      Unknown role
                    </option>
                    {Object.values(UserRole).map((role) => (
                      <option key={role} value={role}>
                        {roleLabel(role)}
                      </option>
                    ))}
                  </select>
                  <Button
                    aria-label={`Delete ${displayValue(user.fullName)}`}
                    className="size-8 p-0 text-red-700 hover:bg-red-50 hover:text-red-800"
                    disabled={user.userId === undefined || actionsDisabled}
                    onClick={() => onDelete(user)}
                    type="button"
                    variant="ghost"
                  >
                    <IconTrash size={15} aria-hidden="true" />
                  </Button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function UserTableSkeleton() {
  const skeletonRows = ["one", "two", "three", "four", "five"];
  const skeletonCells = ["identity", "email", "phone", "role", "status"];

  return (
    <div className="space-y-px" aria-busy="true">
      {skeletonRows.map((row) => (
        <div
          className="grid min-w-[780px] grid-cols-[1.4fr_1.2fr_0.8fr_1fr_0.8fr] gap-4 px-5 py-5"
          key={`user-skeleton-${row}`}
        >
          {skeletonCells.map((cell) => (
            <span
              className="h-4 animate-pulse rounded bg-slate-100"
              key={cell}
            />
          ))}
        </div>
      ))}
      <span className="sr-only">Loading user list…</span>
    </div>
  );
}

function SummaryCard({
  icon: Icon,
  label,
  value,
  tone,
}: {
  icon: typeof IconUsers;
  label: string;
  value: number;
  tone: "blue" | "teal" | "amber" | "navy";
}) {
  const tones = {
    blue: "bg-blue-100 text-blue-800",
    teal: "bg-teal-100 text-teal-800",
    amber: "bg-amber-100 text-amber-800",
    navy: "bg-slate-900 text-white",
  };

  return (
    <div className="rounded-xl border border-slate-100 bg-white p-4 shadow-sm sm:p-5">
      <div className="flex items-start justify-between gap-2">
        <p className="text-[10px] font-bold uppercase tracking-wide text-slate-600">
          {label}
        </p>
        <span
          className={`grid size-8 shrink-0 place-items-center rounded-lg ${tones[tone]}`}
        >
          <Icon size={16} aria-hidden="true" />
        </span>
      </div>
      <p className="mt-2 text-3xl font-semibold tracking-tight text-slate-950">
        {value}
      </p>
    </div>
  );
}

export function AdminUsersDashboard() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const [authChecked, setAuthChecked] = useState(false);
  const [hasSession, setHasSession] = useState(false);
  const [actionFeedback, setActionFeedback] = useState<{
    kind: "error" | "success";
    message: string;
  } | null>(null);

  function handleActionError(error: unknown, fallback: string) {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      window.sessionStorage.removeItem("savecms.sessionToken");
      window.sessionStorage.removeItem("savecms.sessionRole");
      window.sessionStorage.setItem(
        "savecms.loginNotice",
        "Your session expired. Sign in again to continue.",
      );
      router.replace("/");
      return;
    }

    setActionFeedback({
      kind: "error",
      message: getActionErrorMessage(error, fallback),
    });
  }

  async function refreshUsers() {
    await queryClient.invalidateQueries({
      queryKey: getGetAllUsersQueryKey(),
    });
  }

  const approveMutation = useApproveUser({
    mutation: {
      onSuccess: async () => {
        setActionFeedback({
          kind: "success",
          message: "User approved successfully.",
        });
        await refreshUsers();
      },
      onError: (error) =>
        handleActionError(error, "Could not approve this user."),
    },
  });
  const changeRoleMutation = useChangeUserRole({
    mutation: {
      onSuccess: async () => {
        setActionFeedback({
          kind: "success",
          message: "User role updated successfully.",
        });
        await refreshUsers();
      },
      onError: (error) =>
        handleActionError(error, "Could not update this user's role."),
    },
  });
  const deleteMutation = useDeleteUser({
    mutation: {
      onSuccess: async () => {
        setActionFeedback({
          kind: "success",
          message: "User deleted successfully.",
        });
        await refreshUsers();
      },
      onError: (error) =>
        handleActionError(error, "Could not delete this user."),
    },
  });
  const logoutMutation = useLogoutUser({
    mutation: {
      onSuccess: () => finishSignOut(),
      onError: (error) => {
        const notice =
          axios.isAxiosError(error) && error.response?.status === 401
            ? "Your session expired. You have been signed out."
            : "You were signed out on this device, but the server could not confirm logout.";
        finishSignOut(notice);
      },
    },
  });

  const usersQuery = useGetAllUsers({
    query: {
      enabled: authChecked && hasSession,
      retry: false,
    },
  });

  useEffect(() => {
    const token = window.sessionStorage.getItem("savecms.sessionToken");
    if (!token) {
      router.replace("/");
      return;
    }

    setHasSession(true);
    setAuthChecked(true);
  }, [router]);

  const users = useMemo(
    () => getUserRecords(usersQuery.data?.data),
    [usersQuery.data],
  );

  const summary = useMemo(() => {
    const list = users ?? [];
    return {
      total: list.length,
      active: list.filter(
        (user) => user.accountStatus?.toUpperCase() === "ACTIVE",
      ).length,
      pending: list.filter(
        (user) => user.accountStatus?.toUpperCase() === "PENDING",
      ).length,
      guards: list.filter((user) => user.role === "SECURITY_GUARD").length,
    };
  }, [users]);

  const errorMessage = usersQuery.isError
    ? getErrorMessage(usersQuery.error)
    : null;

  useEffect(() => {
    if (usersQuery.isError && axios.isAxiosError(usersQuery.error)) {
      if (usersQuery.error.response?.status === 401) {
        window.sessionStorage.removeItem("savecms.sessionToken");
        window.sessionStorage.removeItem("savecms.sessionRole");
        window.sessionStorage.setItem(
          "savecms.loginNotice",
          "Your session expired. Sign in again to continue.",
        );
        router.replace("/");
      }
    }
  }, [router, usersQuery.error, usersQuery.isError]);

  const actionIsPending =
    approveMutation.isPending ||
    changeRoleMutation.isPending ||
    deleteMutation.isPending;

  function finishSignOut(notice?: string) {
    window.sessionStorage.removeItem("savecms.sessionToken");
    window.sessionStorage.removeItem("savecms.sessionRole");
    if (notice) {
      window.sessionStorage.setItem("savecms.loginNotice", notice);
    }
    router.replace("/");
  }

  function approveUser(user: UserRecord) {
    if (user.userId === undefined) return;
    setActionFeedback(null);
    approveMutation.mutate({ id: user.userId });
  }

  function changeUserRole(userId: number, role: string) {
    if (
      !Object.values(UserRole).some((supportedRole) => supportedRole === role)
    ) {
      return;
    }
    setActionFeedback(null);
    changeRoleMutation.mutate({ id: userId, data: { role } });
  }

  function deleteUser(user: UserRecord) {
    if (user.userId === undefined) return;
    const identity = user.fullName || user.email || `user ${user.userId}`;
    if (!window.confirm(`Delete ${identity}? This action cannot be undone.`)) {
      return;
    }

    setActionFeedback(null);
    deleteMutation.mutate({ userId: user.userId });
  }

  if (!authChecked) {
    return (
      <main className="grid min-h-screen place-items-center bg-[#f5f7ff]">
        <p aria-live="polite" className="text-sm text-slate-500">
          Checking your session…
        </p>
      </main>
    );
  }

  return (
    <div className="min-h-screen bg-[#f5f7ff] text-slate-900">
      <aside className="fixed inset-y-0 left-0 z-10 hidden w-[220px] flex-col bg-[#121b2d] text-white lg:flex">
        <Link
          className="flex h-[74px] items-center gap-3 border-b border-white/10 px-5"
          href="/"
        >
          <span className="grid size-9 place-items-center rounded-lg bg-teal-600">
            <IconShield size={20} aria-hidden="true" />
          </span>
          <span>
            <span className="block text-sm font-bold">SaveCMS</span>
            <span className="block text-[9px] tracking-wide text-slate-400">
              SMART COMMUNITY
            </span>
          </span>
        </Link>
        <div className="px-4 pt-7 text-[9px] font-bold tracking-[0.16em] text-slate-500">
          MAIN NAVIGATION
        </div>
        <nav className="mt-3 space-y-1 px-2" aria-label="Main navigation">
          <button
            aria-label="Dashboard, coming in a later epic"
            className="flex w-full cursor-not-allowed items-center gap-3 rounded-lg px-3 py-2.5 text-left text-xs text-slate-400"
            disabled
            type="button"
          >
            <IconHome size={16} aria-hidden="true" />
            <span className="flex-1">Dashboard</span>
            <span className="text-[8px]">Coming in a later epic</span>
          </button>
          <Link
            aria-current="page"
            className="flex items-center gap-3 rounded-lg bg-blue-100 px-3 py-2.5 text-xs font-semibold text-slate-900"
            href="/admin/users"
          >
            <IconUsers size={16} aria-hidden="true" />
            Users
          </Link>
          <Link
            className="flex items-center gap-3 rounded-lg px-3 py-2.5 text-xs text-slate-300 transition-colors hover:bg-white/10 hover:text-white"
            href="/admin/flats"
          >
            <IconBuildingCommunity size={16} aria-hidden="true" />
            <span>Flats & Residents</span>
          </Link>
          <Link
            className="flex items-center gap-3 rounded-lg px-3 py-2.5 text-xs text-slate-300 transition-colors hover:bg-white/10 hover:text-white"
            href="/security/flat-qr"
          >
            <IconShield size={16} aria-hidden="true" />
            <span>Flat QR validation</span>
          </Link>
          <Link
            className="flex items-center gap-3 rounded-lg px-3 py-2.5 text-xs text-slate-300 transition-colors hover:bg-white/10 hover:text-white"
            href="/security/visitors"
          >
            <IconUsers size={16} aria-hidden="true" />
            <span>Visitor history</span>
          </Link>
          <button
            aria-label="Reports, coming in a later epic"
            className="flex w-full cursor-not-allowed items-center gap-3 rounded-lg px-3 py-2.5 text-left text-xs text-slate-400"
            disabled
            type="button"
          >
            <IconClipboardList size={16} aria-hidden="true" />
            <span className="flex-1">Reports</span>
            <span className="text-[8px]">Coming in a later epic</span>
          </button>
        </nav>
        <button
          className="mt-auto flex items-center gap-3 border-t border-white/10 px-5 py-4 text-xs text-slate-400 transition-colors hover:text-white"
          disabled={logoutMutation.isPending}
          onClick={() => logoutMutation.mutate()}
          type="button"
        >
          <IconLogout size={16} aria-hidden="true" />
          {logoutMutation.isPending ? "Signing out…" : "Sign out"}
        </button>
      </aside>

      <main className="min-h-screen lg:ml-[220px]">
        <header className="flex min-h-[74px] flex-wrap items-center justify-between gap-3 border-b border-slate-200 bg-white px-5 py-3 sm:px-8">
          <div className="flex items-center gap-2 text-xs font-medium text-slate-600">
            <IconBuildingCommunity
              size={16}
              className="text-teal-700"
              aria-hidden="true"
            />
            Greenwood Heights
            <IconChevronDown size={14} aria-hidden="true" />
          </div>
          <div className="flex items-center gap-2 rounded-full bg-teal-50 px-3 py-1.5 text-[10px] font-semibold text-teal-800">
            <IconUserShield size={14} aria-hidden="true" />
            Admin workspace
          </div>
        </header>

        <div className="mx-auto max-w-[1500px] px-4 py-6 sm:px-7 sm:py-8">
          <div className="flex flex-wrap items-end justify-between gap-4">
            <div>
              <p className="text-[10px] font-bold tracking-[0.16em] text-teal-800">
                COMMUNITY GOVERNANCE
              </p>
              <h1 className="mt-1 text-2xl font-semibold tracking-tight text-slate-950 sm:text-3xl">
                User Management
              </h1>
              <p className="mt-1 max-w-2xl text-sm text-slate-500">
                Review registered apartment residents and community staff.
              </p>
            </div>
          </div>

          <section
            aria-label="User summary"
            className="mt-6 grid grid-cols-2 gap-3 xl:grid-cols-4"
          >
            <SummaryCard
              icon={IconUsers}
              label="Total users"
              tone="blue"
              value={summary.total}
            />
            <SummaryCard
              icon={IconUserShield}
              label="Active users"
              tone="teal"
              value={summary.active}
            />
            <SummaryCard
              icon={IconClipboardList}
              label="Pending approvals"
              tone="amber"
              value={summary.pending}
            />
            <SummaryCard
              icon={IconShield}
              label="Security guards"
              tone="navy"
              value={summary.guards}
            />
          </section>

          <section className="mt-6 overflow-hidden rounded-xl border border-slate-100 bg-white shadow-sm">
            <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-100 px-4 py-4 sm:px-5">
              <div>
                <h2 className="text-sm font-semibold text-slate-900">
                  Registered community users
                </h2>
                <p className="mt-1 text-xs text-slate-500">
                  {users ? `${users.length} accounts` : "User directory"}
                </p>
              </div>
              {usersQuery.isSuccess && users && (
                <span className="rounded-full bg-teal-50 px-2.5 py-1 text-[10px] font-bold text-teal-800">
                  LIVE DIRECTORY
                </span>
              )}
            </div>

            {actionFeedback && (
              <div
                aria-live="polite"
                className={`mx-4 mt-4 rounded-lg border px-3 py-2.5 text-xs sm:mx-5 ${
                  actionFeedback.kind === "error"
                    ? "border-red-200 bg-red-50 text-red-800"
                    : "border-teal-200 bg-teal-50 text-teal-800"
                }`}
                role={actionFeedback.kind === "error" ? "alert" : "status"}
              >
                {actionFeedback.message}
              </div>
            )}

            {(actionIsPending ||
              (usersQuery.isFetching && usersQuery.isSuccess)) && (
              <p
                aria-live="polite"
                className="px-4 pt-3 text-xs text-slate-500 sm:px-5"
              >
                {usersQuery.isFetching && usersQuery.isSuccess
                  ? "Refreshing user list…"
                  : "Saving user changes…"}
              </p>
            )}

            {usersQuery.isLoading || !authChecked ? (
              <UserTableSkeleton />
            ) : errorMessage ? (
              <div className="flex flex-col items-center px-5 py-12 text-center">
                <p
                  className="max-w-lg text-sm font-medium text-red-700"
                  role="alert"
                >
                  {errorMessage}
                </p>
                <Button
                  className="mt-4"
                  onClick={() => usersQuery.refetch()}
                  type="button"
                  variant="outline"
                >
                  Try again
                </Button>
              </div>
            ) : users === null ? (
              <div
                className="px-5 py-12 text-center text-sm text-amber-800"
                role="alert"
              >
                The server response did not contain a valid users list.
              </div>
            ) : users.length === 0 ? (
              <div className="px-5 py-12 text-center">
                <IconUsers
                  className="mx-auto text-slate-300"
                  size={30}
                  aria-hidden="true"
                />
                <p className="mt-3 text-sm font-semibold text-slate-700">
                  No users found
                </p>
                <p className="mt-1 text-xs text-slate-500">
                  Registered accounts will appear here.
                </p>
              </div>
            ) : (
              <UserTable
                actionsDisabled={actionIsPending}
                onApprove={approveUser}
                onDelete={deleteUser}
                onRoleChange={changeUserRole}
                users={users}
              />
            )}
          </section>
        </div>
      </main>
    </div>
  );
}
