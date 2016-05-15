package org.yocto.crops.zephyr.model;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class ToolchainsDeserializer implements JsonDeserializer<Toolchains> {

	@Override
	public Toolchains deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Toolchain.class, new ToolchainDeserializer());
		Gson gson = gsonBuilder.create();
		
		final JsonArray jsonArray = json.getAsJsonArray();
		Toolchain[] toolchainArray = (Toolchain[]) gson.fromJson(jsonArray.getAsJsonArray(), Toolchain[].class);
		Set<Toolchain> toolchainSet = new HashSet<>(Arrays.asList(toolchainArray));
		final Toolchains toolchains = new Toolchains();
		toolchains.setToolchains(toolchainSet);
		return toolchains;
	}

}
