package org.yocto.crops.zephyr.model;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class KernelTypeDeserializer implements JsonDeserializer<Object> {
	@Override
	public KernelType deserialize(final JsonElement json, final Type typeOf, final JsonDeserializationContext context)
		throws JsonParseException {
		
		final JsonObject jsonObject = json.getAsJsonObject();
		
		final KernelType kernelType = new KernelType();
		return kernelType;
	}
}
