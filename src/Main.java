import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        String customersCSV = "./data/customers.csv";
        String productsCSV = "./data/products.csv";
        String ordersCSV = "./data/orders.csv";
        String order_pricesCSV = "./data/order_prices.csv";
        String product_customersCSV = "./data/product_customers.csv";
        String customer_rankingCSV = "./data/customer_ranking.csv";

        List<String[]> datosClientes = leerArchivoCSV(customersCSV);
        List<String[]> datosProductos = leerArchivoCSV(productsCSV);
        List<String[]> datosPedidos = leerArchivoCSV(ordersCSV);

        if (getPreciosPedidos(datosPedidos, datosProductos, order_pricesCSV)) {
            System.out.println("Se han escrito los datos en " + order_pricesCSV);
        } else {
            System.out.println("Ha habido algún error al generar los datos para " + order_pricesCSV);
        }

        if (getClientesInteresados(datosProductos, datosPedidos, product_customersCSV)) {
            System.out.println("Se han generado los datos en " + product_customersCSV);
        } else {
            System.out.println("Ha habido un error al generar los datos para " + product_customersCSV);
        }

        if (getRankingClientes(datosClientes, datosPedidos, datosProductos, customer_rankingCSV)) {
            System.out.println("Se ha generado el ranking de clientes en " + customer_rankingCSV);
        } else {
            System.out.println("Ha habido un error al generar el ranking de clientes para " + customer_rankingCSV);
        }
    }

    private static List<String[]> leerArchivoCSV(String filename) {
        List<String[]> datos = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean primeraLinea = true;
            while ((line = br.readLine()) != null) {
                if (primeraLinea) {
                    primeraLinea = false;
                    continue;
                }
                String[] parts = line.split(",");
                datos.add(parts);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return datos;
    }

    private static boolean getPreciosPedidos(List<String[]> pedidos, List<String[]> productos, String ruta) {
        List<String[]> preciosSumados = new ArrayList<>();
        for (String[] order : pedidos) {
            double costeTotal = 0;
            String[] productIDs = order[2].split(" ");
            for (String productID : productIDs) {
                int id = Integer.parseInt(productID);
                double cost = calcularCostePedido(productos, id);
                costeTotal += cost;
            }
            String[] orderPriceEntry = {order[0], String.valueOf(costeTotal)};
            preciosSumados.add(orderPriceEntry);
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(ruta))) {
            writer.println("id,euros");
            for (String[] entry : preciosSumados) {
                writer.println(entry[0] + "," + entry[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static double calcularCostePedido(List<String[]> productos, int id) {
        for (String[] producto : productos) {
            if (Integer.parseInt(producto[0]) == id) {
                return Double.parseDouble(producto[2]);
            }
        }
        return 0.0; // Default cost if product ID not found
    }

    private static boolean getClientesInteresados(List<String[]> productos, List<String[]> pedidos, String ruta) {
        List<List<String>> productCustomersLists = new ArrayList<>();

        for (int i = 0; i < productos.size(); i++) {
            productCustomersLists.add(new ArrayList<>());
        }

        for (String[] pedido : pedidos) {
            String clienteID = pedido[1];
            String[] productIDs = pedido[2].split(" ");
            for (String productID : productIDs) {
                int productIndex = Integer.parseInt(productID);
                List<String> clienteLista = productCustomersLists.get(productIndex);
                if (!clienteLista.contains(clienteID)) {
                    clienteLista.add(clienteID);
                }
            }
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(ruta))) {
            writer.println("id,customer_ids");
            for (int i = 0; i < productCustomersLists.size(); i++) {
                List<String> customerIDs = productCustomersLists.get(i);
                StringBuilder customerIDsString = new StringBuilder();
                for (int j = 0; j < customerIDs.size(); j++) {
                    customerIDsString.append(customerIDs.get(j));
                    if (j < customerIDs.size() - 1) {
                        customerIDsString.append(" ");
                    }
                }
                writer.println(i + "," + customerIDsString);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    private static boolean getRankingClientes(List<String[]> clientes, List<String[]> pedidos, List<String[]> productos, String ruta) {
        List<String[]> rankingClientes = new ArrayList<>();

        for (String[] cliente : clientes) {
            String idCliente = cliente[0];
            double totalEuros = calcularGastoCliente(pedidos, idCliente, productos);
            String[] entradaRanking = {idCliente, cliente[1], cliente[2], String.valueOf(totalEuros)};
            rankingClientes.add(entradaRanking);
        }

        rankingClientes.sort((a, b) -> Double.compare(Double.parseDouble(b[3]), Double.parseDouble(a[3])));

        try (PrintWriter escritor = new PrintWriter(new FileWriter(ruta))) {
            escritor.println("id,firstname,lastname,total_euros");
            for (String[] entrada : rankingClientes) {
                escritor.println(String.join(",", entrada));
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static double calcularGastoCliente(List<String[]> pedidos, String idCliente, List<String[]> productos) {
        double totalEuros = 0.0;
        for (String[] pedido : pedidos) {
            if (pedido[1].equals(idCliente)) {
                String[] idsProductos = pedido[2].split(" ");
                for (String idProducto : idsProductos) {
                    int id = Integer.parseInt(idProducto);
                    double costo = calcularCostePedido(productos, id);
                    totalEuros += costo;
                }
            }
        }
        return totalEuros;
    }

}
