package org.yocto.crops.zephyr.model;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class KernelTypesDeserializer implements JsonDeserializer<Object> {
	@Override
	public KernelTypes deserialize(final JsonElement json, final Type typeOf, final JsonDeserializationContext context)
		throws JsonParseException {
		
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(KernelType.class, new KernelTypeDeserializer());
		Gson gson = gsonBuilder.create();
		
		final JsonObject jsonObject = json.getAsJsonObject();
		Set<KernelType> kernelTypeSet = new HashSet<>();
		KernelType[] kernelTypeArray = gson.fromJson(jsonObject.getAsJsonObject(), KernelType.class);
		
		final KernelTypes kernelTypes = new KernelTypes();
		return kernelTypes;
	}
}
