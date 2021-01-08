package demo;

import io.smallrye.reactive.messaging.annotations.Broadcast;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;

import static org.eclipse.microprofile.reactive.messaging.Acknowledgment.Strategy.PRE_PROCESSING;

@ApplicationScoped
public class Stats {

  // ---------------------------------------------------------------------------------------------------------------- //

  private long invoiceCount = 0L;

  @Incoming("invoices")
  @Outgoing("web-invoice-count")
  @Broadcast
  @Acknowledgment(PRE_PROCESSING)
  public long invoiceCount(JsonObject invoice) {
    return ++invoiceCount;
  }

  // ---------------------------------------------------------------------------------------------------------------- //

  private HashMap<String, Long> perCityCount = new HashMap<>();

  @Incoming("invoices")
  @Outgoing("city-stats")
  @Acknowledgment(PRE_PROCESSING)
  public JsonObject perCityCount(JsonObject invoice) {
    var city = invoice.getString("city");
    var count = perCityCount.getOrDefault(city, 0L) + 1L;
    perCityCount.put(city, count);
    return new JsonObject()
      .put("city", city)
      .put("count", count);
  }

  // ---------------------------------------------------------------------------------------------------------------- //

  // NOTE: this won't be needed in future releases

  @Incoming("per-city-stats")
  @Outgoing("web-city-stats")
  @Broadcast
  @Acknowledgment(PRE_PROCESSING)
  public JsonObject cityStats(JsonObject stat) {
    return stat;
  }

  @Incoming("invoices")
  @Outgoing("web-invoices")
  @Broadcast
  @Acknowledgment(PRE_PROCESSING)
  public JsonObject invoices(JsonObject invoice) {
    return invoice;
  }

  // ---------------------------------------------------------------------------------------------------------------- //
}
