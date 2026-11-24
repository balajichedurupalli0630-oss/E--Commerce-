import z from "zod";

export interface CustomJwtSessionClaims {
  metadata?: {
    role?: "user" | "admin";
  };
}

export type UserType = {
  id: string;
  username: string;
  firstName?: string;
  lastName?: string;
  imageUrl?: string;
  email?: string;
  emailAddresses: Array<{ emailAddress?: string; email_address?: string; id?: string }>;
  phoneNumbers?: Array<{ phoneNumber?: string; id?: string }>;
  publicMetadata: { role?: "user" | "admin" };
  banned?: boolean;
  createdAt?: string | number | Date;
  updatedAt?: string | number | Date;
};

export const UserFormSchema = z.object({
  firstName: z
    .string({ message: "First name is required!" })
    .min(2, { message: "First name must be at least 2 characters!" })
    .max(50),
  lastName: z
    .string({ message: "Last name is required!" })
    .min(2, { message: "Last name must be at least 2 characters!" })
    .max(50),
  username: z
    .string({ message: "Username is required!" })
    .min(2, { message: "Username must be at least 2 characters!" })
    .max(50),
  emailAddress: z.array(z.string({ message: "Email address is required!" })),
  password: z
    .string({ message: "Password is required!" })
    .min(8, { message: "Password must be at least 8 characters!" })
    .max(50),
});
