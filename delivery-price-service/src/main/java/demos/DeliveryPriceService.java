package demos;

import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeliveryPriceService {

  private static final Logger logger = LoggerFactory.getLogger(DeliveryPriceService.class);

  public static void main(String[] args) {
    logger.info("Starting the service");
    var vertx = Vertx.vertx();
    vertx.deployVerticle("demos.HttpApi");
    vertx.deployVerticle("demos.QuoteGenerator");
  }
}
