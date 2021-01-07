package demos;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import io.vertx.mutiny.ext.web.client.predicate.ResponsePredicate;
import io.vertx.mutiny.ext.web.codec.BodyCodec;
import io.vertx.mutiny.kafka.client.consumer.KafkaConsumer;
import io.vertx.mutiny.kafka.client.consumer.KafkaConsumerRecord;
import io.vertx.mutiny.kafka.client.producer.KafkaProducer;
import io.vertx.mutiny.kafka.client.producer.KafkaProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/*
  Process orders of the form:
  {
    "city": "Lyon",
    "items": [
      {
        "name": "foo",
        "count": 3
      },
      {
        "name": "bar",
        "count": 1
      },
    ]
  }
*/

@SuppressWarnings("DuplicatedCode")
public class PurchaseService extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(PurchaseService.class);

  private KafkaProducer<String, JsonObject> producer;
  private WebClient deliveryService;
  private WebClient catalogService;

  @Override
  public Uni<Void> asyncStart() {

    logger.info("Starting");

    deliveryService = WebClient.create(vertx, new WebClientOptions()
      .setDefaultHost("localhost")
      .setDefaultPort(3000));

    catalogService = WebClient.create(vertx, new WebClientOptions()
      .setDefaultHost("localhost")
      .setDefaultPort(4000));

    var producerConfig = new HashMap<String, String>();
    producerConfig.put("bootstrap.servers", "localhost:9092");
    producerConfig.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    producerConfig.put("value.serializer", "io.vertx.kafka.client.serialization.JsonObjectSerializer");
    producerConfig.put("acks", "1");

    producer = KafkaProducer.create(vertx, producerConfig);

    var consumerConfig = new HashMap<String, String>();
    consumerConfig.put("bootstrap.servers", "localhost:9092");
    consumerConfig.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    consumerConfig.put("value.deserializer", "io.vertx.kafka.client.serialization.JsonObjectDeserializer");
    consumerConfig.put("auto.offset.reset", "earliest");
    consumerConfig.put("enable.auto.commit", "true");
    consumerConfig.put("group.id", "purchase-service");

    var consumer = KafkaConsumer.<String, JsonObject>create(vertx, consumerConfig);

    consumer
      .toMulti()
      .onItem().transformToUniAndMerge(this::assembleInvoice)
      .subscribe().with(this::publishInvoice, failure -> logger.error("Woops", failure));

    return consumer
      .subscribe("purchases")
      .invoke(() -> logger.info("Up and running"));
  }

  private Uni<JsonObject> assembleInvoice(KafkaConsumerRecord<String, JsonObject> request) {
    var payload = request.value();

    // Extract root values
    var city = payload.getString("city");
    var items = payload.getJsonArray("items");

    // Prepare HTTP requests for all items
    var itemUnis = new ArrayList<Uni<JsonObject>>();
    for (int i = 0; i < items.size(); i++) {
      var item = items.getJsonObject(i);

      var uni = catalogService.get("/catalog/products/" + item.getString("name"))
        .as(BodyCodec.jsonObject())
        .expect(ResponsePredicate.SC_OK)
        .send()
        .onItem().transform(HttpResponse::body)
        .onItem().transform(json -> json.put("count", item.getInteger("count")));

      itemUnis.add(uni);
    }

    // Prepare delivery quote HTTP request
    var deliveryUni = deliveryService.get("/quote/" + city)
      .as(BodyCodec.jsonObject())
      .expect(ResponsePredicate.SC_OK)
      .send()
      .onItem().transform(HttpResponse::body);

    // Requests to the catalog in parallel, followed by a request to the delivery service, and a final payload is assembled
    return Uni.combine().all().unis(itemUnis)
      .combinedWith(JsonArray::new)
      .chain(array -> deliveryUni.onItem().transform(quote -> new JsonObject()
        .put("items", array)
        .put("city", city)
        .put("shippingCost", quote.getInteger("price"))));
  }

  private void publishInvoice(JsonObject invoice) {
    var uuid = UUID.randomUUID().toString();
    var record = KafkaProducerRecord.create("invoices", uuid, invoice);
    producer.write(record).subscribe().with(
      ok -> logger.info("Published invoice {} with payload {}", uuid, invoice.encodePrettily()),
      err -> logger.error("Woops", err)
    );
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new PurchaseService());
    vertx.deployVerticle(new DemoGenerator());
  }
}
