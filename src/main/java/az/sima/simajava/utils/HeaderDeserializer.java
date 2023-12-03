package az.sima.simajava.utils;

import az.sima.simajava.dto.sima.simaContract.Header;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Base64;

public class HeaderDeserializer implements JsonDeserializer<Header> {

    @Override
    public Header deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String algorithmName = jsonObject.get("AlgName").getAsString();
        String base64Signature = jsonObject.get("Signature").getAsString();
        byte[] signature = Base64.getDecoder().decode(base64Signature);
        return Header.builder()
                .AlgorithmName(algorithmName)
                .Signature(signature)
                .build();

    }
}
