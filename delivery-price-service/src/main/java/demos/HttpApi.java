package demos;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpApi extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(HttpApi.class);

  @Override
  public void start(Promise<Void> startPromise) {
    logger.info("Starting the HTTP API");

    var router = Router.router(vertx);
    router.get("/quote/:city").handler(this::quote);

    Future<HttpServer> serverFuture = vertx.createHttpServer()
      .requestHandler(router)
      .listen(3000);

    serverFuture
      .onSuccess(ok -> {
        logger.info("HTTP server started on port 3000");
        startPromise.complete();
      })
      .onFailure(failure -> {
        logger.error("Woops", failure);
        startPromise.fail(failure);
      });
  }

  private void quote(RoutingContext routingContext) {
    var city = routingContext.request().getParam("city");
    logger.info("Processing a quote request to {}", city);

    JsonObject requestJson = new JsonObject().put("city", city);
    Future<Message<JsonObject>> request = vertx.eventBus().request("quote.requests", requestJson);

    request.onSuccess(quote -> {
      logger.info("Quote for {}: {}", city, quote.body().getString("price"));
      routingContext.response()
        .putHeader("Content-Type", "application/json")
        .end(quote.body().encode());
    });

    request.onFailure(failure -> {
      logger.error("Woops", failure);
      routingContext.fail(500);
    });
  }
}
