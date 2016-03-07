package org.yocto.crops.zephyr.model;

import java.util.Set;

public class Toolchains {
	private Set<Toolchain> toolchains;
	
	public void setToolchains(Set<Toolchain> toolchains) {
		this.toolchains = toolchains;
	}
	
	public Set<Toolchain> getToolchains() {
		return toolchains;
	}
}