
[^1_11]: https://docs.mem0.ai/api-reference/project/create-project


---

# ja voor java 21, gradle, m0-HN5W5Pts2QGFLt27emKUejivDn4dVIRrvSmVPvSh api code en minecraft paper 1.20.6

Hier volgt een **voorbeeldcode** om Mem0 te gebruiken in een **Minecraft Paper 1.20.6 plugin met Java 21 en Gradle**. Je gebruikt jouw `api_key` (`m0-HN5W5Pts2QGFLt27emKUejivDn4dVIRrvSmVPvSh`) direct in de Authorization-header voor Mem0 API-calls.

**Gradle dependencies (build.gradle):**

```groovy
plugins {
    id 'java'
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Voeg deze toe voor HTTP requests
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'com.google.code.gson:gson:2.10.1'
}
```

**Java codevoorbeeld:**

```java
import okhttp3.*;
import com.google.gson.*;

public class Mem0API {
    private static final String API_KEY = "m0-HN5W5Pts2QGFLt27emKUejivDn4dVIRrvSmVPvSh";
    private static final String ADD_MEMORY_URL = "https://api.mem0.ai/v1/memories/";
    private static final Gson gson = new Gson();

    public static void addMemory(String userId, String userMsg, String assistantMsg) throws Exception {
        OkHttpClient client = new OkHttpClient();

        JsonObject body = new JsonObject();
        JsonArray messages = new JsonArray();

        JsonObject user = new JsonObject();
        user.addProperty("role", "user");
        user.addProperty("content", userMsg);

        JsonObject assistant = new JsonObject();
        assistant.addProperty("role", "assistant");
        assistant.addProperty("content", assistantMsg);

        messages.add(user);
        messages.add(assistant);

        body.add("messages", messages);
        body.addProperty("user_id", userId);
        body.addProperty("version", "v2");

        RequestBody requestBody = RequestBody.create(
            gson.toJson(body),
            MediaType.get("application/json")
        );

        Request request = new Request.Builder()
                .url(ADD_MEMORY_URL)
                .header("Authorization", "Token " + API_KEY)
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new RuntimeException("Unexpected: " + response);

            System.out.println(response.body().string());
        }
    }
}
```

**Hoe gebruik je deze class in je plugin?**

```java
// Voorbeeld bij een Minecraft command:
Mem0API.addMemory(player.getUniqueId().toString(), "Hoi, ik ben Joep!", "Hallo Joep! Wat wil je vandaag doen?");
```

- Gebruik `addMemory` voor elke interactie/speler die je wilt opslaan.
- Vervang eventueel de messages en het user_id met relevante data uit je Minecraft plugin.

**Opmerking:**
Dezelfde aanpak werkt voor zoeken in geheugen, alleen met een andere URL en JSON payload (zie Mem0 docs of geef aan als je hiervoor ook een voorbeeld wilt).

**Let goed op:** Zet je API-key nooit openbaar als je de plugin deelt!

Wil je ook een voorbeeld voor memory search of volledige command-handler integratie voor Paper?
<span style="display:none">[^2_1]</span>

<div style="text-align: center">⁂</div>

[^2_1]: https://app.mem0.ai/dashboard/get-started


---

# graag voorbeelden van alle mogelijk diingen

Hier zijn Java-codevoorbeelden voor **alle belangrijke functies van de Mem0 API** in een Minecraft Paper 1.20.6 plugin met Java 21 en Gradle:

***

### 1. **Add memory (herinnering toevoegen)**

```java
import okhttp3.*;
import com.google.gson.*;

public class Mem0API {
    private static final String API_KEY = "m0-HN5W5Pts2QGFLt27emKUejivDn4dVIRrvSmVPvSh";
    private static final String URL = "https://api.mem0.ai/v1/memories/";
    private static final Gson gson = new Gson();

    public static void addMemory(String userId, String userMsg, String assistantMsg) throws Exception {
        OkHttpClient client = new OkHttpClient();
        JsonObject payload = new JsonObject();
        JsonArray messages = new JsonArray();

        JsonObject user = new JsonObject();
        user.addProperty("role", "user");
        user.addProperty("content", userMsg);

        JsonObject assistant = new JsonObject();
        assistant.addProperty("role", "assistant");
        assistant.addProperty("content", assistantMsg);

        messages.add(user);
        messages.add(assistant);

        payload.add("messages", messages);
        payload.addProperty("user_id", userId);
        payload.addProperty("version", "v2");

        RequestBody body = RequestBody.create(gson.toJson(payload), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(URL)
                .header("Authorization", "Token " + API_KEY)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println(response.body().string());
        }
    }
}
```


***

### 2. **Herinneringen zoeken (search memories, v2)**

```java
public static void searchMemories(String query, String userId) throws Exception {
    OkHttpClient client = new OkHttpClient();
    String url = "https://api.mem0.ai/v2/memories/search/";

    JsonObject filters = new JsonObject();
    JsonArray orArr = new JsonArray();
    JsonObject uid = new JsonObject();
    uid.addProperty("user_id", userId);
    orArr.add(uid);
    filters.add("OR", orArr);

    JsonObject payload = new JsonObject();
    payload.addProperty("query", query);
    payload.add("filters", filters);

    RequestBody body = RequestBody.create(gson.toJson(payload), MediaType.get("application/json"));
    Request request = new Request.Builder()
            .url(url)
            .header("Authorization", "Token " + API_KEY)
            .header("Content-Type", "application/json")
            .post(body)
            .build();

    try (Response response = client.newCall(request).execute()) {
        System.out.println(response.body().string());
    }
}
```


***

### 3. **Herinneringen ophalen, bijwerken en verwijderen**

Zie deze methoden zoals beschreven in de Mem0 docs:

- **Get Memories**: `/v2/memories/`
- **Update Memory**: `PUT /v1/memories/{id}`
- **Delete Memory**: `DELETE /v1/memories/{id}`

Deze calls volgen hetzelfde patroon:

- Bouw een (optioneel) JSON-payload (voor update)
- Gebruik OkHttp `.put()` (voor update), `.delete()` (voor verwijderen) of `.get()` (voor ophalen)
- Zet steeds de Authorization-header op `Token ...`

***

### 4. **Memory History, Exports, Feedback**

Voor **history**:

```java
// /v1/memories/history
Request request = new Request.Builder()
        .url("https://api.mem0.ai/v1/memories/history")
        .header("Authorization", "Token " + API_KEY)
        .get()
        .build();
```

Voor **export** of **feedback**:

- Stuur respectievelijk een POST request naar `/v1/memories/create-memory-export` of `/v1/memories/feedback` met het juiste JSON-payload.

***

### 5. **Voorbeeldgebruik in een command**

```java
// Binnen je plugin:
Mem0API.addMemory(player.getUniqueId().toString(), "Player vraag", "AI antwoord");

// Geheugen zoeken:
Mem0API.searchMemories("Wat weet je over mij?", player.getUniqueId().toString());
```


***

### 6. **Wildcard-search, advanced filters**

Wil je ALLE memories van een speler:

```java
// Zet 'query' = "*" EN filters op user_id/andere keys.
payload.addProperty("query", "*");
```

Of complexere filters met AND/OR constructie zoals in de search-docs.

***

**Alles uitbreidbaar via dezelfde JSON/HTTP-patterns. Je hoeft enkel endpoints en formaten uit de Mem0 docs na te volgen voor meer complexiteit.** Wil je specifieke voorbeeld-implementaties voor get/update/delete voor een complete REST-CRUD plugin?
<span style="display:none">[^3_1]</span>

<div style="text-align: center">⁂</div>

[^3_1]: https://app.mem0.ai/dashboard/get-started

