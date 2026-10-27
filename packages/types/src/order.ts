export type OrderProductType = {
  name: string;
  quantity: number;
  price: number;
};

export type OrderSchemaType = {
  userId: string;
  email: string;
  amount: number;
  status: "success" | "failed";
  products: OrderProductType[];
  createdAt?: string | Date;
  updatedAt?: string | Date;
};

export type OrderType = OrderSchemaType & {
  _id: string;
};

export type OrderChartType = {
  month: string;
  total: number;
  successful: number;
};
