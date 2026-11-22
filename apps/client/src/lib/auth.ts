import { cookies } from "next/headers";
import { UserType } from "@repo/types";

export type User = UserType;

function decodeJwt(token: string): any {
  try {
    const parts = token.split(".");
    if (parts.length < 2) return null;
    const base64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const json = JSON.parse(Buffer.from(base64, "base64").toString("utf-8"));
    return json;
  } catch {
    return null;
  }
}

export async function auth() {
  const cookieStore = await cookies();
  const token = cookieStore.get("omnicart_jwt")?.value || null;
  const claims = token ? decodeJwt(token) : null;

  return {
    userId: claims?.sub || null,
    getToken: async () => token,
  };
}
