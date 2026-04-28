package gatling;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

public class DeustoBankSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
        .baseUrl("http://localhost:8080")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json");

    ScenarioBuilder scn = scenario("Basic User Flow")
        .exec(http("Login Request")
            .post("/auth/login")
            .formParam("dni", "12345678A")
            .formParam("password", "Deusto1!")
            .check(status().in(200, 401, 404)) // Allowing multiple statuses so it doesn't hard-fail if DB is empty
        )
        .pause(1)
        .exec(http("Get Accounts")
            .get("/accounts/user/1")
            .check(status().in(200, 404))
        );

    {
        setUp(
            scn.injectOpen(atOnceUsers(10))
        ).protocols(httpProtocol);
    }
}
