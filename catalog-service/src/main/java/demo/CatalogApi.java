package demo;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.math.BigDecimal;
import java.util.Map;

@Path("/catalog")
public class CatalogApi {

  @GET
  @Path("/list")
  public Multi<Product> list() {
    return Product.all();
  }

  @POST
  @Transactional
  public Uni<Product> register(Product product) {
    return product.persistAndFlush().replaceWith(product);
  }

  @GET
  @Path("/products/{name}")
  public Uni<Product> find(@PathParam("name") String name) {
    return Product.findByName(name);
  }

  // Inject some data ------------------------------------------------------ //

  @Inject
  @ConfigProperty(name = "catalog-service.prepopulate", defaultValue = "true")
  boolean prePopulate;

  @PostConstruct
  void populateDatabase() {
    if (prePopulate) {
      Map.of(
        "bread", new BigDecimal("0.95"),
        "chocolate", new BigDecimal("4.75"),
        "speaker", new BigDecimal("399.90"),
        "mouse", new BigDecimal("49.99")
      ).forEach((name, price) -> {
        var product = new Product();
        product.name = name;
        product.price = price;
        product.persistAndFlush();
      });
    }
  }
}
