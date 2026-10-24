package dev.daamky;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

final class ModuleDescriptionLoader {
    private static final String II1Il11l111II11IIl = "/assets/daamky/descriptions.json";

    private ModuleDescriptionLoader() {
    }

    static void II1Il11l111II11IIl(List<ModuleStructure> items) {
        JsonObject jsonObject = II1Il11l111II11IIl();
        if (jsonObject != null) {
            for (Object module : items) {
                JsonElement jsonElement = jsonObject.get(((ModuleStructure)module).getName());
                if (jsonElement != null && jsonElement.isJsonObject()) {
                    JsonObject asJsonObject = jsonElement.getAsJsonObject();

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

