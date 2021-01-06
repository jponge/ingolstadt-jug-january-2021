package demo;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/catalog")
public class CatalogApi {

  @GET
  @Path("/list")
  @Produces(MediaType.APPLICATION_JSON)
  public Multi<Product> list() {
    return Product.all();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<Product> register(Product product) {
    return product.persistAndFlush().replaceWith(product);
  }

  @GET
  @Path("/products/{name}")
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<Product> find(@PathParam("name") String name) {
    return Product.findByName(name);
  }
}
