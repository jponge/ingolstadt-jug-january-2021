package demos;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class QuoteGenerator extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(QuoteGenerator.class);

  private final Random random = new Random();

  @Override
  public void start() {
    logger.info("Starting the quote generator");

    vertx.eventBus().consumer("quote.requests", this::makeQuote);
  }

  private void makeQuote(Message<JsonObject> message) {
    var city = message.body().getString("city");
    logger.info("Preparing a quote to deliver to {}", city);

    vertx.setTimer(random.nextInt(500), id -> {

      int price = random.nextInt(5) + 1;
      var payload = new JsonObject()
        .put("city", city)
        .put("price", price);

      logger.info("Deliver to {} for {} euros", city, price);
      message.reply(payload);
    });
  }
}
