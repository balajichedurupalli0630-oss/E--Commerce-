import AppBarChart from "@/components/AppBarChart";
import CardList from "@/components/CardList";
import TodoList from "@/components/TodoList";
import { auth } from "@/lib/auth-server";

const Homepage = async () => {
  const { getToken } = await auth();
  const token = await getToken();
  const orderChartData = fetch(
    `${process.env.NEXT_PUBLIC_ORDER_SERVICE_URL || "http://localhost:8001"}/order-chart`,
    {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
      cache: "no-store",
    }
  )
    .then((res) => (res.ok ? res.json() : []))
    .catch(() => []);
  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 2xl:grid-cols-4 gap-4">
      <div className="bg-primary-foreground p-4 rounded-lg lg:col-span-2 2xl:col-span-3">
        <AppBarChart dataPromise={orderChartData} />
      </div>
      <div className="bg-primary-foreground p-4 rounded-lg">
        <CardList title="Latest Transactions" />
      </div>
      <div className="bg-primary-foreground p-4 rounded-lg lg:col-span-2">
        <TodoList />
      </div>
      <div className="bg-primary-foreground p-4 rounded-lg lg:col-span-2">
        <CardList title="Popular Products" />
      </div>
    </div>
  );
};

export default Homepage;
