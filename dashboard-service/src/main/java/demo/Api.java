package demo;

import io.smallrye.mutiny.Multi;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.annotations.SseElementType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/api")
public class Api {

  // ---------------------------------------------------------------------------------------------------------------- //

  @Inject
  @Channel("web-invoice-count")
  Multi<Long> invoiceCount;

  @GET
  @Path("/invoice-count-stream")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  @SseElementType("text/plain")
  public Multi<Long> invoiceCountStream() {
    return invoiceCount;
  }

  // ---------------------------------------------------------------------------------------------------------------- //

  @Inject
  @Channel("web-city-stats")
  Multi<JsonObject> perCityStats;

  @GET
  @Path("/per-city-stats-stream")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  @SseElementType("application/json")
  public Multi<JsonObject> perCityStatsStream() {
    return perCityStats;
  }

  // ---------------------------------------------------------------------------------------------------------------- //

  @Inject
  @Channel("web-invoices")
  Multi<JsonObject> invoices;

  @GET
  @Path("/invoice-stream")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  @SseElementType("application/json")
  public Multi<JsonObject> invoiceStream() {
    return invoices;
  }

  // ---------------------------------------------------------------------------------------------------------------- //
}
