# Weather Temperature Service

## Task 1 — Basic implementation

### Brief description

A Java 21 AWS Lambda that returns the current temperature in Wrocław together with a category band, using the [Open-Meteo](https://open-meteo.com/) API as the data source.

The Lambda calls Open-Meteo in two steps:

1. **Geocoding** (`/v1/search`) — resolves the city name to latitude/longitude.
2. **Forecast** (`/v1/forecast`) — returns `temperature_2m` for those coordinates.

The numeric value is mapped to one of five bands and returned as JSON:

```json
{
  "city": "Wrocław",
  "temperature": 7.4,
  "category": "Cold"
}
```

| Range       | Category  |
|-------------|-----------|
| < 0 °C      | Freezing  |
| 0 – 10 °C   | Cold      |
| 10 – 20 °C  | Mild      |
| 20 – 30 °C  | Warm      |
| > 30 °C     | Hot       |

### Key design decisions

- **Layered separation.** `WeatherHandler` (Lambda entry, orchestration only) → `WeatherService` (business logic) → `WeatherProvider` interface → `OpenMeteoWeatherProvider`. The handler contains no business logic.
- **WeatherProvider abstraction.** Adding another source (e.g. AccuWeather) means writing a new implementation; nothing else changes.
- **Classification kept out of the handler.** `TemperatureClassifier` is a separate business component. Each band owns its bounds and a `matches(double)` predicate inside the `TemperatureCategory` enum; the classifier just iterates over `values()` and returns the first match.
- **HTTP/JSON facade.** `HttpJsonClient` exposes a single `getJson(url)` operation over `HttpClient` + `ObjectMapper`, and stays independent of the Open-Meteo provider.
- **Timeouts.** Set on both the connection (3 s) and the request (5 s) so the Lambda doesn't hang on a slow upstream.
- **Custom exception.** `ExternalApiException` wraps IO and malformed-response errors from external calls.

### Unit testing without calling the real API

Each layer has a constructor-injected seam, so tests stub upstream dependencies. JUnit 5 + Mockito is enough.

**`TemperatureClassifier.classify(double t)`**

- **Setup.** None — pure logic.
- **Cases:**

| Input `t` | Expected         |
|-----------|------------------|
| `-0.01`   | `FREEZING`       |
| `0`       | `COLD`           |
| `9.99`    | `COLD`           |
| `10`      | `MILD`           |
| `20`      | `WARM`           |
| `29.99`   | `WARM`           |
| `30`      | `HOT`            |

**`WeatherService.getWeather(city)`**

- **Setup.** Stub `WeatherProvider` and `TemperatureClassifier`.
- **Cases:**

| Stub setup | Input | Expected |
|---|---|---|
| `provider.fetchTemperature("Wrocław") → 7.4`, `classifier.classify(7.4) → COLD` | `"Wrocław"` | `WeatherResponse("Wrocław", 7.4, "Cold")` |
| `provider.fetchTemperature(...)` throws `ExternalApiException` | any | exception propagates |

**`OpenMeteoWeatherProvider.fetchTemperature(city)`**

- **Setup.** Stub `HttpJsonClient.getJson(url)` to return prepared `JsonNode`s for both endpoints.
- **Cases:**

| Stub response | Expected |
|---|---|
| Geocoding returns coords + forecast returns `current.temperature_2m = 7.4` | `7.4` |
| Geocoding returns `{"results": []}` | `IllegalArgumentException("City not found")` |
| Geocoding result missing `latitude` / `longitude` | `ExternalApiException` |
| Forecast missing `current.temperature_2m` | `ExternalApiException` |

**`WeatherHandler.handleRequest(request, context)`**

- **Setup.** Stub `WeatherService`; pass a no-op `Context`.
- **Cases:**

| `request` | Expected |
|---|---|
| `new WeatherRequest("Wrocław")` | response from stub |
| `null` | `IllegalArgumentException` |
| `new WeatherRequest(null)` | `IllegalArgumentException` |
| `new WeatherRequest("   ")` | `IllegalArgumentException` |
| `new WeatherRequest(" Wrocław ")` | service is called with `"Wrocław"` (trim verified) |

### AWS evidence

The Lambda accepts `city` as an input parameter from the start (Tasks 1 and 2 share one codebase). The Task 1 demo invokes it with `{"city": "Wrocław"}`.

- [Lambda created in AWS](doc/task1/lambda-created.png)
- [Test execution with response](doc/task1/test-execution.png)

## Task 2 — City as input parameter

The Lambda now accepts the city name as an input parameter (`{"city": "..."}`) and returns the current temperature for that city.

Sample test events:

- [London](doc/task2/london.png)
- [New York](doc/task2/new-york.png)

## Task 3 — Lambda Function URL

The Lambda is exposed via an AWS Function URL and accepts the city name as the GET query parameter `city`.

**Endpoint:** `https://ymh73ss7bbtimx6xyjdraxaz4i0mfjzv.lambda-url.eu-north-1.on.aws/`
**Query parameter:** `city`

**Example request:**

```
GET https://ymh73ss7bbtimx6xyjdraxaz4i0mfjzv.lambda-url.eu-north-1.on.aws/?city=Wrocław
```

Sample requests and responses:

- [Function URL configuration in AWS Console](doc/task3/function-url.png)
- [Berlin](doc/task3/berlin.png)
- [Minsk](doc/task3/minsk.png)
- [City not found (404)](doc/task3/not-found.png)

## Task 4 — Design reflection

**Adding another provider.**

- **Domain isolation.** External API access is isolated behind the `WeatherProvider` interface; the business layer (`WeatherService`, `TemperatureClassifier`) and the HTTP/JSON facade (`HttpJsonClient`) stay provider-agnostic.
- **Cost of adding one.** Write one new `WeatherProvider` implementation (e.g. `AccuWeatherProvider`) and swap the injection in the handler.

**What I would improve with more time.**

- **Tests.** Write actual unit tests for the seams described above — the architecture supports them, only the tests themselves are missing.
- **Geocoding cache.** In a real deployment, cache the city → coordinates lookup to avoid two HTTP calls per request.
