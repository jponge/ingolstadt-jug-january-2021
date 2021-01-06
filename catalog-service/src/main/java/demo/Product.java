package demo;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import javax.persistence.Entity;
import java.math.BigDecimal;

@Entity
public class Product extends PanacheEntity {

  public String name;
  public BigDecimal price;

  public static Multi<Product> all() {
    return findAll().stream();
  }

  public static Uni<Product> findByName(String name) {
    return find("name", name).firstResult();
  }
}
