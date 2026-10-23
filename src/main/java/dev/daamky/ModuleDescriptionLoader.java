package dev.daamky;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

final class ModuleDescriptionLoader {
    private static final String p317bbb00 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
    private static final String II1Il11l111II11IIl = "/assets/daamky/descriptions.json";
// https://t.me/pasterplace

    private ModuleDescriptionLoader() {
    }
// https://t.me/pasterplace

    static void II1Il11l111II11IIl(List<ModuleStructure> items) {
        JsonObject jsonObject = II1Il11l111II11IIl();
        if (jsonObject != null) {
            for (Object module : items) {
                JsonElement jsonElement = jsonObject.get(((ModuleStructure)module).getName());
                if (jsonElement != null && jsonElement.isJsonObject()) {
                    JsonObject asJsonObject = jsonElement.getAsJsonObject();
// https://t.me/pasterplace

                    for (Object setting : ((ModuleStructure)module).getSettings()) {
                        if (((Setting)setting).getDescription().isEmpty()) {
                            for (java.util.Map.Entry<?, ?> descriptionEntry : asJsonObject.entrySet()) {
                                if (((String)descriptionEntry.getKey()).trim().equalsIgnoreCase(((Setting)setting).getName().trim())) {
                                    ((Setting)setting).withDescription(((JsonElement)descriptionEntry.getValue()).getAsString());
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
// https://t.me/pasterplace

    private static JsonObject II1Il11l111II11IIl() {
        try (InputStream resourceAsStream = ModuleDescriptionLoader.class.getResourceAsStream("/assets/daamky/descriptions.json")) {
            if (resourceAsStream != null) {
                try (InputStreamReader inputStreamReader = new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8)) {
                    return JsonParser.parseReader(inputStreamReader).getAsJsonObject();
                }
            } else {

                return null;
            }
        } catch (Exception loadFailure) {
            return null;
        }
    }
}

