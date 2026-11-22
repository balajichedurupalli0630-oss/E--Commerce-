import { NextResponse, type NextRequest } from "next/server";

const publicRoutes = ["/sign-in", "/unauthorized"];

function decodeJwtPayload(token: string): any {
  try {
    const parts = token.split(".");
    if (parts.length < 2) return null;
    const base64Url = parts[1];
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join("")
    );
    return JSON.parse(jsonPayload);
  } catch {
    return null;
  }
}

export function middleware(req: NextRequest) {
  const { pathname } = req.nextUrl;

  const isPublic = publicRoutes.some((route) => pathname.startsWith(route));
  if (isPublic) {
    return NextResponse.next();
  }

  const token = req.cookies.get("omnicart_jwt")?.value;

  if (!token) {
    const signInUrl = new URL("/sign-in", req.url);
    signInUrl.searchParams.set("redirect_url", req.url);
    return NextResponse.redirect(signInUrl);
  }

  const claims = decodeJwtPayload(token);
  if (!claims || (claims.exp && claims.exp * 1000 < Date.now())) {
    const response = NextResponse.redirect(new URL("/sign-in", req.url));
    response.cookies.delete("omnicart_jwt");
    return response;
  }

  const userRole = claims.role || claims.publicMetadata?.role;
  if (userRole && userRole !== "admin") {
    return NextResponse.redirect(new URL("/unauthorized", req.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    "/((?!_next|[^?]*\\.(?:html?|css|js(?!on)|jpe?g|webp|png|gif|svg|ttf|woff2?|ico|csv|docx?|xlsx?|zip|webmanifest)).*)",
    "/(api|trpc)(.*)",
  ],
};
