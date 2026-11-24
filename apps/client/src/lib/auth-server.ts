import { cookies } from "next/headers";

export async function auth() {
  const cookieStore = await cookies();
  const token = cookieStore.get("omnicart_token")?.value || null;

  let userId: string | null = null;
  let sessionClaims: any = null;

  if (token) {
    try {
      const parts = token.split(".");
      if (parts.length >= 2 && parts[1]) {
        const payload = JSON.parse(
          Buffer.from(parts[1], "base64").toString("utf-8")
        );
        userId = payload.userId || payload.sub || payload.id || null;
        sessionClaims = {
          metadata: {
            role: payload.role || payload.metadata?.role || "user",
          },
        };
      }
    } catch (e) {
      // Ignored
    }
  }

  return {
    userId,
    sessionClaims,
    getToken: async () => token,
  };
}
