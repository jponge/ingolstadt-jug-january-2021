package demo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class CatalogApiTest {

  @Test
  @DisplayName("Smoke test to check a product registration")
  public void register() {
    JsonPath jsonPath = given()
      .when()
      .request()
      .contentType(ContentType.JSON)
      .body(new JsonObject().put("name", "abc").put("price", "10.25").encode())
      .post("/catalog")
      .then()
      .statusCode(200)
      .extract().body().jsonPath();

    Assertions.assertEquals("abc", jsonPath.getString("name"));
    Assertions.assertEquals("10.25", jsonPath.getString("price"));
  }
}
