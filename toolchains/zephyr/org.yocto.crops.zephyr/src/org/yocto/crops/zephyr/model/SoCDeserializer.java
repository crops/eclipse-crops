package org.yocto.crops.zephyr.model;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class SoCDeserializer implements JsonDeserializer<SoC> {
	@Override
	public SoC deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		final JsonObject jsonObject = json.getAsJsonObject();
		// Delegate deserialization to the context
		final SoC soc = context.deserialize(jsonObject.getAsJsonObject(), SoC.class);
		return soc;
	}

}
