package demos;

import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.kafka.client.producer.KafkaProducer;
import io.vertx.mutiny.kafka.client.producer.KafkaProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

@SuppressWarnings("DuplicatedCode")
public class DemoGenerator extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(DemoGenerator.class);

  private KafkaProducer<String, JsonObject> producer;

  @Override
  public void start() {
    logger.info("Demo generator starting");

    var producerConfig = new HashMap<String, String>();
    producerConfig.put("bootstrap.servers", "localhost:9092");
    producerConfig.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    producerConfig.put("value.serializer", "io.vertx.kafka.client.serialization.JsonObjectSerializer");
    producerConfig.put("acks", "1");

    producer = KafkaProducer.create(vertx, producerConfig);

    vertx.setPeriodic(1000, id -> generate());
  }

  private static final List<String> availableItems = List.of("bread", "chocolate", "speaker", "mouse");
  private static final List<String> cities = List.of("Lyon", "Ingolstadt", "Nevers");
  private final Random random = new Random();

  private void generate() {
    logger.info("Generating a purchase request");

    var request = new JsonObject();
    request.put("city", cities.get(random.nextInt(cities.size())));

    var items = new JsonArray();
    for (var item : availableItems) {
      if (random.nextBoolean()) {
        items.add(new JsonObject()
          .put("name", item)
          .put("count", random.nextInt(10) + 1));
      }
    }
    if (items.isEmpty()) {
      return;
    }
    request.put("items", items);

    var record = KafkaProducerRecord.create("purchases", "demo", request);
    producer.writeAndForget(record);
  }
}
