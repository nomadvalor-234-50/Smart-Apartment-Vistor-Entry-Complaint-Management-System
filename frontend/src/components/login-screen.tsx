"use client";

import {
  IconArrowRight,
  IconAt,
  IconBellRinging,
  IconClockHour4,
  IconDeviceDesktop,
  IconEye,
  IconEyeOff,
  IconLock,
  IconShieldCheck,
  IconTruckDelivery,
} from "@tabler/icons-react";
import axios from "axios";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { type FormEvent, useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { UserRole } from "@/lib/api/generated/model/userRole";
import { useLoginUser } from "@/lib/api/generated/user-controller/user-controller";

const quickProfiles = [
  { label: "Society Admin", email: "admin@greenwood.in" },
  { label: "New Guard (Pending)", email: "guard@greenwood.in" },
  { label: "Resident (Demo)", email: "resident@greenwood.in" },
];

const terminalStats = [
  {
    icon: IconBellRinging,
    label: "GUARD SYNC",
    title: "Instant Push",
    detail: "Sub-second relays",
  },
  {
    icon: IconShieldCheck,
    label: "APPROVALS",
    title: "1-Tap Verified",
    detail: "App & SMS gatepass",
  },
  {
    icon: IconClockHour4,
    label: "SLA CLOCK",
    title: "Ticketing",
    detail: "98.4% timely closes",
  },
];

function getLoginErrorMessage(error: unknown) {
  if (!axios.isAxiosError(error)) {
    return "Unable to sign in. Please try again.";
  }

  const responseData: unknown = error.response?.data;
  if (
    responseData &&
    typeof responseData === "object" &&
    "message" in responseData &&
    typeof responseData.message === "string"
  ) {
    return responseData.message;
  }

  if (error.response?.status === 401) {
    return "Invalid email or password.";
  }

  if (error.response?.status === 403) {
    return "Your account is pending administrator approval.";
  }

  if (!error.response) {
    return "Could not reach the server. Check your connection and try again.";
  }

  return "The sign-in request failed. Please try again.";
}

function BrandMark() {
  return (
    <div className="flex items-center gap-2.5">
      <div className="grid size-10 place-items-center rounded-xl bg-primary text-primary-foreground shadow-sm">
        <IconShieldCheck aria-hidden="true" size={22} stroke={1.8} />
      </div>
      <div>
        <p className="text-sm font-bold tracking-tight text-foreground">
          Save<span className="text-primary">CMS</span>
        </p>
        <p className="text-[9px] font-semibold tracking-[0.14em] text-muted-foreground">
          SMART VISITOR & ACCESS SYSTEM
        </p>
      </div>
    </div>
  );
}

function SecurityPanel() {
  return (
    <aside className="relative flex min-h-[440px] flex-col overflow-hidden bg-[#121b2d] px-7 py-8 text-white sm:px-10 sm:py-10 lg:min-h-0 lg:w-[41%] lg:px-11">
      <div className="pointer-events-none absolute -right-32 bottom-[-8rem] size-96 rounded-full bg-primary/20 blur-3xl" />
      <div className="relative">
        <div className="inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/[0.07] px-3 py-1.5 text-[10px] font-semibold tracking-wide text-slate-100">
          <span className="size-1.5 rounded-full bg-cyan-300" />
          GREENWOOD HEIGHTS SOCIETY PORTAL V2.4
        </div>
        <p className="mt-8 text-[11px] font-semibold tracking-[0.15em] text-slate-400">
          AUTOMATED SECURITY PERIMETER
        </p>
        <h1 className="mt-2 max-w-sm text-3xl font-semibold leading-tight tracking-tight sm:text-[2.1rem]">
          Secure Gateways,
          <br />
          Effortless Living
        </h1>
        <p className="mt-3 max-w-sm text-sm leading-relaxed text-slate-400">
          Integrated real-time visitor authorization, ANPR vehicle tracking, and
          rapid resident complaint resolution.
        </p>
      </div>

      <div className="relative mt-10 rounded-xl border border-white/[0.06] bg-[#0b1220] p-4 shadow-xl sm:mt-12 lg:mt-auto lg:mb-8">
        <div className="flex items-center justify-between gap-3">
          <div className="flex items-center gap-2 text-xs font-medium">
            <IconShieldCheck
              className="text-cyan-300"
              size={16}
              aria-hidden="true"
            />
            Gate 01 · Main Entry Terminal
          </div>
          <span className="rounded-full bg-emerald-400/10 px-2.5 py-1 text-[9px] font-bold tracking-wider text-emerald-300">
            ACTIVE PASS
          </span>
        </div>
        <div className="mt-4 flex items-center gap-3 rounded-lg bg-white/[0.05] p-3">
          <span className="grid size-10 shrink-0 place-items-center rounded-lg bg-cyan-400/10 text-cyan-300">
            <IconTruckDelivery size={20} aria-hidden="true" />
          </span>
          <div className="min-w-0 flex-1">
            <p className="truncate text-xs font-semibold">
              BlueDart Express (#BD-982)
            </p>
            <p className="mt-0.5 truncate text-[11px] text-slate-400">
              Resident: Tower B, Flat 402
            </p>
          </div>
          <span className="text-[10px] text-slate-400">11:42 AM</span>
        </div>
      </div>

      <div className="relative grid grid-cols-3 gap-3 sm:gap-5">
        {terminalStats.map(({ icon: Icon, label, title, detail }) => (
          <div key={label} className="min-w-0">
            <div className="flex items-center gap-1.5 text-[9px] font-bold tracking-wide text-cyan-300 sm:text-[10px]">
              <Icon size={13} aria-hidden="true" />
              {label}
            </div>
            <p className="mt-2 text-[11px] font-semibold text-white">{title}</p>
            <p className="mt-1 text-[10px] leading-snug text-slate-400">
              {detail}
            </p>
          </div>
        ))}
      </div>

      <div className="relative mt-10 flex flex-wrap items-center justify-between gap-2 border-t border-white/10 pt-5 text-[10px] text-slate-400 lg:mt-auto lg:border-0 lg:pt-0">
        <span className="inline-flex items-center gap-1.5">
          <IconLock size={13} className="text-cyan-300" aria-hidden="true" />
          256-bit Encrypted Gate Vault
        </span>
        <span>ISO 27001 Certified</span>
      </div>
    </aside>
  );
}

function LoginForm() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [feedback, setFeedback] = useState<{
    kind: "error" | "info" | "success";
    message: string;
  } | null>(null);

  useEffect(() => {
    const notice = window.sessionStorage.getItem("savecms.loginNotice");
    if (notice) {
      window.sessionStorage.removeItem("savecms.loginNotice");
      setFeedback({ kind: "error", message: notice });
    }
  }, []);

  const loginMutation = useLoginUser({
    mutation: {
      onSuccess: (response) => {
        const { message, role, token } = response.data;
        if (typeof token !== "string" || token.length === 0) {
          setFeedback({
            kind: "info",
            message: "The server response did not include a session token.",
          });
          return;
        }

        window.sessionStorage.setItem("savecms.sessionToken", token);
        if (role === UserRole.ADMIN) {
          router.replace("/admin/users");
          return;
        }

        setFeedback({
          kind: "success",
          message:
            typeof message === "string" ? message : "Signed in successfully.",
        });
      },
      onError: (error) => {
        setFeedback({ kind: "error", message: getLoginErrorMessage(error) });
      },
    },
  });

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setFeedback(null);
    loginMutation.mutate({ data: { email, password } });
  }

  return (
    <form className="w-full max-w-[410px]" onSubmit={handleSubmit}>
      <div className="mb-7">
        <h2 className="text-2xl font-semibold tracking-tight text-foreground">
          Welcome Back
        </h2>
        <p className="mt-1 text-sm leading-relaxed text-muted-foreground">
          Sign in to manage society access, active visitors, and resident
          operations.
        </p>
      </div>

      <div className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="email" className="text-xs font-semibold">
            Work Email or Terminal ID
          </Label>
          <div className="relative">
            <IconAt
              className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground"
              size={17}
              aria-hidden="true"
            />
            <Input
              autoComplete="username"
              className="h-11 rounded-lg border-border bg-background pl-10 text-sm"
              id="email"
              name="email"
              onChange={(event) => setEmail(event.target.value)}
              disabled={loginMutation.isPending}
              placeholder="name@society.in"
              required
              type="email"
              value={email}
            />
          </div>
        </div>

        <div className="space-y-2">
          <div className="flex items-center justify-between gap-3">
            <Label htmlFor="password" className="text-xs font-semibold">
              Password
            </Label>
            <button
              className="text-xs font-semibold text-primary transition-colors hover:text-primary/80"
              onClick={() =>
                setFeedback({
                  kind: "error",
                  message: "Password recovery is not connected yet.",
                })
              }
              type="button"
            >
              Forgot password?
            </button>
          </div>
          <div className="relative">
            <IconLock
              className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground"
              size={17}
              aria-hidden="true"
            />
            <Input
              autoComplete="current-password"
              className="h-11 rounded-lg border-border bg-background px-10 text-sm"
              id="password"
              name="password"
              onChange={(event) => setPassword(event.target.value)}
              disabled={loginMutation.isPending}
              required
              type={showPassword ? "text" : "password"}
              value={password}
            />
            <button
              aria-label={showPassword ? "Hide password" : "Show password"}
              className="absolute right-2 top-1/2 grid size-8 -translate-y-1/2 place-items-center rounded-md text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
              onClick={() => setShowPassword((visible) => !visible)}
              type="button"
            >
              {showPassword ? (
                <IconEyeOff size={17} aria-hidden="true" />
              ) : (
                <IconEye size={17} aria-hidden="true" />
              )}
            </button>
          </div>
        </div>
      </div>

      <div className="mt-4 flex flex-wrap items-center justify-between gap-3">
        <Label className="flex cursor-pointer items-center gap-2 text-xs font-normal text-muted-foreground">
          <Checkbox
            defaultChecked
            disabled={loginMutation.isPending}
            id="remember"
            name="remember"
          />
          Remember me on this gate terminal
        </Label>
        <span className="inline-flex items-center gap-1.5 text-[10px] text-muted-foreground">
          <IconDeviceDesktop size={13} aria-hidden="true" />
          Term #04
        </span>
      </div>

      <Button
        className="mt-5 h-11 w-full rounded-lg text-sm font-semibold shadow-sm"
        disabled={loginMutation.isPending}
        type="submit"
      >
        {loginMutation.isPending ? (
          "Signing in..."
        ) : (
          <>
            Login to Dashboard
            <IconArrowRight size={17} aria-hidden="true" />
          </>
        )}
      </Button>

      {feedback && (
        <p
          aria-live="polite"
          className={`mt-3 rounded-lg border px-3 py-2 text-xs ${
            feedback.kind === "error"
              ? "border-destructive/30 bg-destructive/5 text-destructive"
              : feedback.kind === "success"
                ? "border-primary/20 bg-primary/5 text-foreground"
                : "border-border bg-muted/50 text-muted-foreground"
          }`}
          role={feedback.kind === "error" ? "alert" : "status"}
        >
          {feedback.message}
        </p>
      )}

      <div className="mt-6 rounded-xl bg-muted/70 p-3">
        <p className="text-[10px] font-semibold tracking-wide text-muted-foreground">
          QUICK PROFILE AUTOFILL
        </p>
        <div className="mt-2 flex flex-wrap gap-1.5">
          {quickProfiles.map((profile) => (
            <Button
              className="h-7 rounded-md bg-background px-2 text-[10px] font-medium"
              disabled={loginMutation.isPending}
              key={profile.label}
              onClick={() => {
                setEmail(profile.email);
                setPassword("");
                setFeedback(null);
              }}
              type="button"
              variant="outline"
            >
              {profile.label}
            </Button>
          ))}
        </div>
      </div>
    </form>
  );
}

export function LoginScreen() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-[#f4f6fb] p-3 sm:p-5 lg:p-6">
      <div className="flex min-h-[min(760px,calc(100vh-2rem))] w-full max-w-[1240px] flex-col overflow-hidden rounded-2xl border border-border/70 bg-background shadow-[0_24px_70px_-35px_rgba(15,23,42,0.35)] lg:min-h-[760px] lg:flex-row">
        <SecurityPanel />
        <section className="flex flex-1 flex-col bg-background px-6 py-7 sm:px-10 sm:py-9 lg:px-11 lg:py-10">
          <header className="flex flex-wrap items-center justify-between gap-4">
            <div className="flex items-center gap-5">
              <BrandMark />
              <div className="hidden border-l border-border pl-5 sm:block">
                <p className="text-sm font-semibold text-foreground">SaveCMS</p>
                <p className="text-[10px] text-muted-foreground">
                  Smart Visitor & Complaint Suite
                </p>
              </div>
            </div>
            <div className="flex gap-1 rounded-lg bg-muted/70 p-1 text-[10px] font-medium text-muted-foreground">
              <span className="rounded-md bg-background px-2.5 py-1.5 text-foreground shadow-sm">
                Standard
              </span>
              <span className="px-2.5 py-1.5">Error State</span>
              <span className="px-2.5 py-1.5">Pending Approval</span>
            </div>
          </header>

          <div className="flex flex-1 items-center justify-center py-10 lg:py-8">
            <LoginForm />
          </div>

          <footer className="flex flex-col items-start justify-between gap-3 border-t border-border/70 pt-4 text-[10px] text-muted-foreground sm:flex-row sm:items-center lg:border-0 lg:pt-0">
            <p>
              New resident or security personnel?{" "}
              <Link
                className="font-semibold text-primary hover:underline"
                href="/register"
              >
                Register here
              </Link>
            </p>
            <p className="flex flex-wrap items-center gap-2">
              <span>Gate Manual</span>
              <span aria-hidden="true">•</span>
              <span>Security Dispatch: Ext 901</span>
            </p>
          </footer>
        </section>
      </div>
    </main>
  );
}
