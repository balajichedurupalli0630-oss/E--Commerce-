"use client";

import {
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "./ui/form";
import { Input } from "./ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Button } from "./ui/button";
import { Textarea } from "./ui/textarea";
import { Checkbox } from "./ui/checkbox";
import { ScrollArea } from "./ui/scroll-area";
import {
  CategoryType,
  clothingSizes,
  colors,
  ProductFormSchema,
  shoeSizes,
  sizes,
} from "@repo/types";
import { useMutation, useQuery } from "@tanstack/react-query";
import { toast } from "react-toastify";
import { useAuth } from "@clerk/nextjs";
import { useRouter } from "next/navigation";

// const categories = [
//   "T-shirts",
//   "Shoes",
//   "Accessories",
//   "Bags",
//   "Dresses",
//   "Jackets",
//   "Gloves",
// ] as const;

const fetchCategories = async () => {
  const res = await fetch(
    `${process.env.NEXT_PUBLIC_PRODUCT_SERVICE_URL}/categories`
  );

  if (!res.ok) {
    throw new Error("Failed to fetch categories!");
  }

  return await res.json();
};

const AddProduct = () => {
  const form = useForm<z.infer<typeof ProductFormSchema>>({
    resolver: zodResolver(ProductFormSchema),
    defaultValues: {
      name: "",
      shortDescription: "",
      description: "",
      price: 0,
      categorySlug: "",
      sizes: [],
      colors: [],
      images: {},
    },
  });

  const { isPending, error, data } = useQuery({
    queryKey: ["categories"],
    queryFn: fetchCategories,
  });

  const { getToken } = useAuth();
  const router = useRouter();

  const mutation = useMutation({
    mutationFn: async (data: z.infer<typeof ProductFormSchema>) => {
      const token = await getToken();
      const res = await fetch(
        `${process.env.NEXT_PUBLIC_PRODUCT_SERVICE_URL}/products`,
        {
          method: "POST",
          body: JSON.stringify(data),
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        }
      );
      if (!res.ok) {
        throw new Error("Failed to create product!");
      }
    },
    onSuccess: () => {
      toast.success("Product created successfully");
      router.refresh();
    },
    onError: (error) => {
      toast.error(error.message);
    },
  });

  return (
    <SheetContent>
      <ScrollArea className="h-screen">
        <SheetHeader>
          <SheetTitle className="mb-4">Add Product</SheetTitle>
          <SheetDescription asChild>
            <Form {...form}>
              <form
                className="space-y-8"
                onSubmit={form.handleSubmit((data) => mutation.mutate(data))}
              >
                <FormField
                  control={form.control}
                  name="name"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Name</FormLabel>
                      <FormControl>
                        <Input {...field} />
                      </FormControl>
                      <FormDescription>
                        Enter the name of the product.
                      </FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="shortDescription"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Short Description</FormLabel>
                      <FormControl>
                        <Input {...field} />
                      </FormControl>
                      <FormDescription>
                        Enter the short description of the product.
                      </FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="description"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Description</FormLabel>
                      <FormControl>
                        <Textarea {...field} />
                      </FormControl>
                      <FormDescription>
                        Enter the description of the product.
                      </FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="price"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Price (₹)</FormLabel>
                      <FormControl>
                        <Input
                          type="number"
                          placeholder="e.g. 2999"
                          value={field.value === 0 ? "" : field.value}
                          onChange={(e) =>
                            field.onChange(
                              e.target.value === "" ? 0 : Number(e.target.value)
                            )
                          }
                        />
                      </FormControl>
                      <FormDescription>
                        Enter the price of the product in Rupees (₹).
                      </FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                {data && (
                  <FormField
                    control={form.control}
                    name="categorySlug"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Category</FormLabel>
                        <FormControl>
                          <Select
                            onValueChange={(val) => {
                              field.onChange(val);
                              form.setValue("sizes", []);
                            }}
                            value={field.value}
                          >
                            <SelectTrigger>
                              <SelectValue placeholder="Select a category" />
                            </SelectTrigger>
                            <SelectContent>
                              {data.map((cat: CategoryType) => (
                                <SelectItem key={cat.id} value={cat.slug}>
                                  {cat.name}
                                </SelectItem>
                              ))}
                            </SelectContent>
                          </Select>
                        </FormControl>
                        <FormDescription>
                          Enter the category of the product.
                        </FormDescription>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                )}
                <FormField
                  control={form.control}
                  name="sizes"
                  render={({ field }) => {
                    const selectedCategory = form.watch("categorySlug") || "";
                    const isShoes =
                      selectedCategory.toLowerCase().includes("shoe") ||
                      selectedCategory.toLowerCase().includes("footwear") ||
                      selectedCategory.toLowerCase().includes("sneaker") ||
                      selectedCategory.toLowerCase().includes("boot");

                    const availableSizes = isShoes ? shoeSizes : clothingSizes;

                    return (
                      <FormItem>
                        <FormLabel>
                          Sizes{" "}
                          {isShoes
                            ? "(UK / India Shoe Sizes)"
                            : "(Clothing Sizes)"}
                        </FormLabel>
                        <FormControl>
                          <div className="grid grid-cols-3 gap-4 my-2">
                            {availableSizes.map((size) => (
                              <div
                                className="flex items-center gap-2"
                                key={size}
                              >
                                <Checkbox
                                  id={`size-${size.replace(/\s+/g, "_")}`}
                                  checked={field.value?.includes(size)}
                                  onCheckedChange={(checked) => {
                                    const currentValues = field.value || [];
                                    if (checked) {
                                      field.onChange([...currentValues, size]);
                                    } else {
                                      field.onChange(
                                        currentValues.filter((v) => v !== size)
                                      );
                                    }
                                  }}
                                />
                                <label
                                  htmlFor={`size-${size.replace(/\s+/g, "_")}`}
                                  className="text-xs uppercase font-medium cursor-pointer"
                                >
                                  {size}
                                </label>
                              </div>
                            ))}
                          </div>
                        </FormControl>
                        <FormDescription>
                          {isShoes
                            ? "Select UK / India standard shoe sizes (UK 5 to UK 12)."
                            : "Select apparel sizes (XS to 3XL)."}
                        </FormDescription>
                        <FormMessage />
                      </FormItem>
                    );
                  }}
                />
                <FormField
                  control={form.control}
                  name="colors"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Colors</FormLabel>
                      <FormControl>
                        <div className="space-y-4">
                          <div className="grid grid-cols-3 gap-4 my-2">
                            {colors.map((color) => (
                              <div
                                className="flex items-center gap-2"
                                key={color}
                              >
                                <Checkbox
                                  id={`color-${color}`}
                                  checked={field.value?.includes(color)}
                                  onCheckedChange={(checked) => {
                                    const currentValues = field.value || [];
                                    if (checked) {
                                      field.onChange([...currentValues, color]);
                                    } else {
                                      field.onChange(
                                        currentValues.filter((v) => v !== color)
                                      );
                                    }
                                  }}
                                />
                                <label
                                  htmlFor={`color-${color}`}
                                  className="text-xs flex items-center gap-2 cursor-pointer capitalize"
                                >
                                  <div
                                    className="w-2 h-2 rounded-full border border-gray-300"
                                    style={{ backgroundColor: color }}
                                  />
                                  {color}
                                </label>
                              </div>
                            ))}
                          </div>
                        </div>
                      </FormControl>
                      <FormDescription>
                        Select the available colors for the product.
                      </FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="images"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Images</FormLabel>
                      <FormControl>
                        <div className="">
                          {form.watch("colors")?.map((color) => (
                            <div
                              className="mb-4 flex items-center gap-4"
                              key={color}
                            >
                              <div className="flex items-center gap-2">
                                <div
                                  className="w-4 h-4 rounded-full"
                                  style={{ backgroundColor: color }}
                                />
                                <span className="text-sm font-medium min-w-[80px]">
                                  {color}:
                                </span>
                              </div>
                              <Input
                                type="file"
                                accept="image/*"
                                onChange={async (e) => {
                                  const file = e.target.files?.[0];
                                  if (file) {
                                    const cloudName =
                                      process.env
                                        .NEXT_PUBLIC_CLOUDINARY_CLOUD_NAME;
                                    const hasCloudinary =
                                      cloudName &&
                                      cloudName !== "your_cloud_name";

                                    if (hasCloudinary) {
                                      try {
                                        const formData = new FormData();
                                        formData.append("file", file);
                                        formData.append(
                                          "upload_preset",
                                          "ecommerce"
                                        );

                                        const res = await fetch(
                                          `https://api.cloudinary.com/v1_1/${cloudName}/image/upload`,
                                          {
                                            method: "POST",
                                            body: formData,
                                          }
                                        );
                                        const data = await res.json();

                                        if (data.secure_url) {
                                          const currentImages =
                                            form.getValues("images") || {};
                                          form.setValue("images", {
                                            ...currentImages,
                                            [color]: data.secure_url,
                                          });
                                          toast.success(
                                            `Image uploaded for ${color}`
                                          );
                                          return;
                                        }
                                      } catch (error) {
                                        console.log(
                                          "Cloudinary error, falling back to local base64:",
                                          error
                                        );
                                      }
                                    }

                                    // Fallback: Read file as Base64 Data URL (works immediately locally)
                                    const reader = new FileReader();
                                    reader.onloadend = () => {
                                      const base64String =
                                        reader.result as string;
                                      const currentImages =
                                        form.getValues("images") || {};
                                      form.setValue("images", {
                                        ...currentImages,
                                        [color]: base64String,
                                      });
                                      toast.success(
                                        `Image selected for ${color}`
                                      );
                                    };
                                    reader.readAsDataURL(file);
                                  }
                                }}
                              />
                              {field.value?.[color] ? (
                                <span className="text-green-600 text-sm">
                                  Image selected
                                </span>
                              ) : (
                                <span className="text-red-600 text-sm">
                                  Image required
                                </span>
                              )}
                            </div>
                          ))}
                        </div>
                      </FormControl>
                    </FormItem>
                  )}
                />
                <Button
                  type="submit"
                  disabled={mutation.isPending}
                  className="disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {mutation.isPending ? "Submitting..." : "Submit"}
                </Button>
              </form>
            </Form>
          </SheetDescription>
        </SheetHeader>
      </ScrollArea>
    </SheetContent>
  );
};

export default AddProduct;
