package legacy;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class Inn {
  private List<Item> items;

  public Inn() {
    items = new ArrayList<Item>();
    items.add(new Item("Lolcat 1", 10, 20));
    items.add(new Item("Lolcat 2", 2, 0));
    items.add(new Item("Lolcat 3", 5, 7));
    items.add(new Item("Lolcat 4", 0, 80));
    items.add(new Item("Lolcat 5", 15, 20));
    items.add(new Item("Lolcat 6", 3, 6));
  }

  public List<Item> getItems() {
    return items;
  }

  public void updateQuality() {
    for (Item item : items) {
      new ItemUpdater(item).updateQuality();
    }
  }

  public static void main(String[] args) throws Exception {
    int port = Integer.parseInt(System.getenv("PORT"));

    HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
    server.createContext("/", new HttpHandler() {
      final Inn inn = new Inn();

      @Override
      public void handle(HttpExchange exchange) throws IOException {
        String body = "";

        if ("/update".equals(exchange.getRequestURI().getPath())) {
          inn.updateQuality();
        } else {
          body = itemsAsJson();

          String query = exchange.getRequestURI().getQuery();
          if (null != query) {
            String callback = query.split("[&=]")[1];
            body = callback + "(" + body + ")";
          }
        }

        byte[] response = body.getBytes();
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
      }

      private String itemsAsJson() {
        List<String> json = Lists.newArrayList();
        for (Item item : inn.getItems()) {
          json.add(String.format("{\"name\":\"%s\", \"quality\":\"%d\", \"sellIn\":\"%d\"}", item.getName(), item.getQuality(), item.getSellIn()));
        }
        return "[" + Joiner.on(",").join(json) + "]";
      }
    });
    server.start();

  }
}
