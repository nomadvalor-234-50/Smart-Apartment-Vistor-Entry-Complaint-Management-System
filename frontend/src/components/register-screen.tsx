"use client";

import {
  IconArrowLeft,
  IconArrowRight,
  IconBuildingCommunity,
  IconCheck,
  IconEye,
  IconEyeOff,
  IconHome,
  IconLock,
  IconMail,
  IconMapPin,
  IconPhone,
  IconShieldCheck,
  IconUser,
} from "@tabler/icons-react";
import axios from "axios";
import Link from "next/link";
import { type FormEvent, useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import type { User } from "@/lib/api/generated/model/user";
import { useRegisterUser } from "@/lib/api/generated/user-controller/user-controller";

const accountRoles = [
  { value: "RESIDENT", label: "Resident" },
  { value: "SECURITY_GUARD", label: "Security guard" },
  { value: "MAINTENANCE_STAFF", label: "Maintenance staff" },
  { value: "APARTMENT_MANAGER", label: "Apartment manager" },
  { value: "VISITOR", label: "Visitor" },
] as const;

type RegistrationValues = {
  fullName: string;
  email: string;
  phone: string;
  password: string;
  role: (typeof accountRoles)[number]["value"];
  address: string;
};

type RegistrationField = keyof RegistrationValues;

type Feedback = {
  kind: "error" | "success";
  message: string;
};

const initialValues: RegistrationValues = {
  fullName: "",
  email: "",
  phone: "",
  password: "",
  role: "RESIDENT",
  address: "",
};

function Brand() {
  return (
    <Link
      aria-label="SaveCMS home"
      className="inline-flex items-center gap-3"
      href="/"
    >
      <span className="grid size-10 place-items-center rounded-xl bg-primary text-primary-foreground">
        <IconShieldCheck aria-hidden="true" size={23} />
      </span>
      <span>
        <span className="block text-sm font-bold tracking-tight text-foreground">
          Save<span className="text-primary">CMS</span>
        </span>
        <span className="block text-[9px] font-semibold tracking-[0.14em] text-muted-foreground">
          SMART VISITOR & ACCESS SYSTEM
        </span>
      </span>
    </Link>
  );
}

function FieldError({ children }: { children: string }) {
  return <p className="text-xs text-destructive">{children}</p>;
}

function getApiErrorMessage(error: unknown) {
  if (!axios.isAxiosError(error)) {
    return "Unable to create your account. Please try again.";
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

  if (typeof responseData === "string" && responseData.trim()) {
    return responseData;
  }

  if (!error.response) {
    return "Could not reach the server. Check your connection and try again.";
  }

  return "The registration request failed. Please try again.";
}

export function RegisterScreen() {
  const [values, setValues] = useState<RegistrationValues>(initialValues);
  const [errors, setErrors] = useState<
    Partial<Record<RegistrationField, string>>
  >({});
  const [showPassword, setShowPassword] = useState(false);
  const [feedback, setFeedback] = useState<Feedback | null>(null);

  const registrationMutation = useRegisterUser({
    mutation: {
      onSuccess: (response) => {
        const accountStatus = response.data.accountStatus;
        const role =
          typeof response.data.role === "string"
            ? response.data.role
            : values.role;
        const message =
          typeof response.data.message === "string"
            ? response.data.message
            : "Your account was created successfully.";

        setFeedback({
          kind: "success",
          message:
            typeof accountStatus === "string" &&
            accountStatus.toUpperCase() === "PENDING" &&
            role === "SECURITY_GUARD"
              ? "Security guard registration submitted. This account is pending administrator approval."
              : typeof accountStatus === "string" &&
                  accountStatus.toUpperCase() === "PENDING"
                ? `${message} Your account is pending administrator approval.`
                : `${message} You can now sign in.`,
        });
      },
      onError: (error) => {
        const message = getApiErrorMessage(error);
        const normalizedMessage = message.toLowerCase();

        if (normalizedMessage.includes("email is already registered")) {
          setErrors((current) => ({ ...current, email: message }));
          setFeedback(null);
          return;
        }

        if (normalizedMessage.includes("phone number is already registered")) {
          setErrors((current) => ({ ...current, phone: message }));
          setFeedback(null);
          return;
        }

        setFeedback({ kind: "error", message });
      },
    },
  });

  function updateField<Field extends RegistrationField>(
    field: Field,
    value: RegistrationValues[Field],
  ) {
    setValues((current) => ({ ...current, [field]: value }));
    setErrors((current) => ({ ...current, [field]: undefined }));
    setFeedback(null);
  }

  function validate(): Partial<Record<RegistrationField, string>> {
    const nextErrors: Partial<Record<RegistrationField, string>> = {};
    const normalizedEmail = values.email.trim();

    if (!values.fullName.trim()) {
      nextErrors.fullName = "Enter your full name.";
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(normalizedEmail)) {
      nextErrors.email = "Enter a valid email address.";
    }
    if (!values.phone.trim()) {
      nextErrors.phone = "Enter your phone number.";
    } else if (!/^[+\d][\d\s()-]{6,19}$/.test(values.phone.trim())) {
      nextErrors.phone = "Enter a valid phone number.";
    }
    if (values.password.length < 6) {
      nextErrors.password = "Use at least 6 characters for your password.";
    }

    return nextErrors;
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const nextErrors = validate();
    setErrors(nextErrors);
    setFeedback(null);

    if (Object.keys(nextErrors).length > 0) {
      return;
    }

    const user: User = {
      fullName: values.fullName.trim(),
      email: values.email.trim(),
      phone: values.phone.trim(),
      password: values.password,
      role: values.role,
      ...(values.address.trim() ? { address: values.address.trim() } : {}),
    };

    registrationMutation.mutate({ data: user });
  }

  return (
    <main className="min-h-screen bg-[#f4f6fb] p-3 sm:p-5 lg:p-8">
      <div className="mx-auto flex min-h-[calc(100vh-1.5rem)] w-full max-w-[1240px] flex-col overflow-hidden rounded-2xl border border-border/70 bg-background shadow-[0_24px_70px_-35px_rgba(15,23,42,0.35)] sm:min-h-[calc(100vh-2.5rem)] lg:min-h-[calc(100vh-4rem)] lg:flex-row">
        <aside className="relative flex flex-col overflow-hidden bg-[#121b2d] px-7 py-8 text-white sm:px-10 lg:w-[35%] lg:px-11 lg:py-10">
          <div className="pointer-events-none absolute -right-36 bottom-[-8rem] size-96 rounded-full bg-primary/20 blur-3xl" />
          <div className="relative">
            <Brand />
            <div className="mt-12 inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/[0.07] px-3 py-1.5 text-[10px] font-semibold tracking-wide text-slate-100">
              <IconBuildingCommunity
                size={14}
                className="text-cyan-300"
                aria-hidden="true"
              />
              GREENWOOD HEIGHTS · TOWER A & B
            </div>
            <p className="mt-9 text-[11px] font-semibold tracking-[0.15em] text-cyan-300">
              COMMUNITY ACCESS
            </p>
            <h1 className="mt-2 text-3xl font-semibold leading-tight tracking-tight sm:text-[2.1rem]">
              A safer community
              <br />
              starts with you.
            </h1>
            <p className="mt-4 max-w-sm text-sm leading-relaxed text-slate-400">
              Create your SaveCMS profile to connect with your apartment
              community and its visitor access services.
            </p>
          </div>

          <div className="relative mt-10 rounded-xl border border-white/[0.06] bg-[#0b1220] p-5 shadow-xl lg:mt-auto">
            <div className="flex items-start gap-3">
              <span className="grid size-9 shrink-0 place-items-center rounded-lg bg-cyan-400/10 text-cyan-300">
                <IconShieldCheck size={19} aria-hidden="true" />
              </span>
              <div>
                <p className="text-sm font-semibold">
                  Your account, your access
                </p>
                <p className="mt-1 text-xs leading-relaxed text-slate-400">
                  Security, maintenance, and apartment manager accounts require
                  administrator approval.
                </p>
              </div>
            </div>
            <div className="mt-5 flex items-center gap-2 border-t border-white/10 pt-4 text-[10px] text-slate-400">
              <IconLock
                size={14}
                className="text-cyan-300"
                aria-hidden="true"
              />
              Your information is used to set up your community profile.
            </div>
          </div>

          <p className="relative mt-8 text-[10px] text-slate-500">
            256-bit Encrypted Gate Vault <span className="px-2">·</span> ISO
            27001 Certified
          </p>
        </aside>

        <section className="flex flex-1 flex-col px-6 py-7 sm:px-10 sm:py-9 lg:px-12 lg:py-10">
          <header className="flex flex-wrap items-center justify-between gap-4">
            <div>
              <p className="text-[10px] font-semibold tracking-[0.16em] text-primary">
                ACCOUNT REGISTRATION
              </p>
              <p className="mt-1 text-xs text-muted-foreground">
                Greenwood Heights community
              </p>
            </div>
            <Link
              className="inline-flex items-center gap-1.5 text-xs font-semibold text-muted-foreground transition-colors hover:text-foreground"
              href="/"
            >
              <IconArrowLeft size={15} aria-hidden="true" />
              Back to sign in
            </Link>
          </header>

          <div className="mx-auto flex w-full flex-1 items-center justify-center py-9 lg:py-6">
            <div className="w-full max-w-[560px]">
              <div className="mb-7">
                <h2 className="text-2xl font-semibold tracking-tight text-foreground sm:text-3xl">
                  Create your account
                </h2>
                <p className="mt-2 text-sm leading-relaxed text-muted-foreground">
                  Enter your details to get started. Fields marked with{" "}
                  <span className="font-semibold text-foreground">*</span> are
                  required.
                </p>
              </div>

              <form className="space-y-5" noValidate onSubmit={handleSubmit}>
                <div className="grid gap-5 sm:grid-cols-2">
                  <div className="space-y-2 sm:col-span-2">
                    <Label htmlFor="fullName" className="text-xs font-semibold">
                      Full name <span className="text-primary">*</span>
                    </Label>
                    <div className="relative">
                      <IconUser
                        className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground"
                        size={17}
                        aria-hidden="true"
                      />
                      <Input
                        autoComplete="name"
                        aria-invalid={Boolean(errors.fullName)}
                        className="h-11 rounded-lg border-border bg-background pl-10 text-sm"
                        id="fullName"
                        onChange={(event) =>
                          updateField("fullName", event.target.value)
                        }
                        disabled={registrationMutation.isPending}
                        placeholder="e.g. Arun Kumar"
                        value={values.fullName}
                      />
                    </div>
                    {errors.fullName && (
                      <FieldError>{errors.fullName}</FieldError>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="email" className="text-xs font-semibold">
                      Email address <span className="text-primary">*</span>
                    </Label>
                    <div className="relative">
                      <IconMail
                        className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground"
                        size={17}
                        aria-hidden="true"
                      />
                      <Input
                        autoComplete="email"
                        aria-invalid={Boolean(errors.email)}
                        className="h-11 rounded-lg border-border bg-background pl-10 text-sm"
                        id="email"
                        onChange={(event) =>
                          updateField("email", event.target.value)
                        }
                        disabled={registrationMutation.isPending}
                        placeholder="you@example.com"
                        type="email"
                        value={values.email}
                      />
                    </div>
                    {errors.email && <FieldError>{errors.email}</FieldError>}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="phone" className="text-xs font-semibold">
                      Phone number <span className="text-primary">*</span>
                    </Label>
                    <div className="relative">
                      <IconPhone
                        className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground"
                        size={17}
                        aria-hidden="true"
                      />
                      <Input
                        autoComplete="tel"
                        aria-invalid={Boolean(errors.phone)}
                        className="h-11 rounded-lg border-border bg-background pl-10 text-sm"
                        id="phone"
                        inputMode="tel"
                        onChange={(event) =>
                          updateField("phone", event.target.value)
                        }
                        disabled={registrationMutation.isPending}
                        placeholder="+91 98765 43210"
                        type="tel"
                        value={values.phone}
                      />
                    </div>
                    {errors.phone && <FieldError>{errors.phone}</FieldError>}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="password" className="text-xs font-semibold">
                      Password <span className="text-primary">*</span>
                    </Label>
                    <div className="relative">
                      <IconLock
                        className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground"
                        size={17}
                        aria-hidden="true"
                      />
                      <Input
                        autoComplete="new-password"
                        aria-invalid={Boolean(errors.password)}
                        className="h-11 rounded-lg border-border bg-background px-10 text-sm"
                        id="password"
                        minLength={6}
                        onChange={(event) =>
                          updateField("password", event.target.value)
                        }
                        disabled={registrationMutation.isPending}
                        placeholder="At least 6 characters"
                        type={showPassword ? "text" : "password"}
                        value={values.password}
                      />
                      <button
                        aria-label={
                          showPassword ? "Hide password" : "Show password"
                        }
                        className="absolute right-2 top-1/2 grid size-8 -translate-y-1/2 place-items-center rounded-md text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
                        disabled={registrationMutation.isPending}
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
                    {errors.password && (
                      <FieldError>{errors.password}</FieldError>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="role" className="text-xs font-semibold">
                      Account type
                    </Label>
                    <div className="relative">
                      <IconHome
                        className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground"
                        size={17}
                        aria-hidden="true"
                      />
                      <select
                        className="h-11 w-full appearance-none rounded-lg border border-border bg-background pl-10 pr-3 text-sm text-foreground outline-none transition focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/30"
                        id="role"
                        disabled={registrationMutation.isPending}
                        onChange={(event) => {
                          const selectedRole = accountRoles.find(
                            (role) => role.value === event.target.value,
                          );
                          if (selectedRole) {
                            updateField("role", selectedRole.value);
                          }
                        }}
                        value={values.role}
                      >
                        {accountRoles.map((role) => (
                          <option key={role.value} value={role.value}>
                            {role.label}
                          </option>
                        ))}
                      </select>
                    </div>
                    <p className="text-[11px] leading-relaxed text-muted-foreground">
                      Security guard, maintenance, and apartment manager
                      accounts require administrator approval.
                    </p>
                  </div>

                  <div className="space-y-2 sm:col-span-2">
                    <Label htmlFor="address" className="text-xs font-semibold">
                      Address{" "}
                      <span className="font-normal text-muted-foreground">
                        (optional)
                      </span>
                    </Label>
                    <div className="relative">
                      <IconMapPin
                        className="pointer-events-none absolute left-3 top-3 text-muted-foreground"
                        size={17}
                        aria-hidden="true"
                      />
                      <Input
                        autoComplete="street-address"
                        className="h-11 rounded-lg border-border bg-background pl-10 text-sm"
                        id="address"
                        onChange={(event) =>
                          updateField("address", event.target.value)
                        }
                        disabled={registrationMutation.isPending}
                        placeholder="Tower and flat number"
                        value={values.address}
                      />
                    </div>
                  </div>
                </div>

                <Button
                  className="h-11 w-full rounded-lg text-sm font-semibold shadow-sm"
                  disabled={registrationMutation.isPending}
                  type="submit"
                >
                  {registrationMutation.isPending ? (
                    "Creating account..."
                  ) : (
                    <>
                      Create SaveCMS account
                      <IconArrowRight size={17} aria-hidden="true" />
                    </>
                  )}
                </Button>

                {feedback && (
                  <output
                    aria-live="polite"
                    className={`flex items-start gap-2 rounded-lg border px-3 py-2.5 text-xs leading-relaxed ${
                      feedback.kind === "error"
                        ? "border-destructive/30 bg-destructive/5 text-destructive"
                        : "border-primary/20 bg-primary/5 text-foreground"
                    }`}
                  >
                    {feedback.kind === "success" && (
                      <IconCheck
                        className="mt-0.5 shrink-0 text-primary"
                        size={15}
                        aria-hidden="true"
                      />
                    )}
                    {feedback.message}
                  </output>
                )}
              </form>

              <p className="mt-6 text-center text-xs text-muted-foreground">
                Already have an account?{" "}
                <Link
                  className="font-semibold text-primary hover:underline"
                  href="/"
                >
                  Sign in
                </Link>
              </p>
            </div>
          </div>

          <footer className="flex flex-wrap items-center justify-between gap-2 border-t border-border/70 pt-4 text-[10px] text-muted-foreground">
            <span>SaveCMS · Smart Visitor & Complaint Suite</span>
            <span>Security Dispatch: Ext 901</span>
          </footer>
        </section>
      </div>
    </main>
  );
}
