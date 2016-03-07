package org.yocto.crops.zephyr.builder;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.runtime.IAdapterFactory;
import org.yocto.crops.core.CropsCorePlugin;

public class MakefileBuildConfigurationFactory implements IAdapterFactory {

	private static Map<IBuildConfiguration, MakefileBuildConfiguration> cache = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adapterType.equals(MakefileBuildConfiguration.class) && adaptableObject instanceof IBuildConfiguration) {
			IBuildConfiguration config = (IBuildConfiguration) adaptableObject;
			MakefileBuildConfiguration makeConfig = cache.get(config);
			if (makeConfig == null) {
				makeConfig = new MakefileBuildConfiguration(config);
				cache.put(config, makeConfig);
			}
			return (T) makeConfig;
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class<?>[] { MakefileBuildConfiguration.class };
	}
}
