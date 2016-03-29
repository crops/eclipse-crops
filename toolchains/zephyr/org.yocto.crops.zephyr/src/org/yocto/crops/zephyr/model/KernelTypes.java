package org.yocto.crops.zephyr.model;

import java.util.Set;

import org.yocto.crops.zephyr.model.KernelType;

public class KernelTypes {
	private Set<KernelType> kernelTypes;

	public void setKernelTypes(Set<KernelType> kernelTypes) {
		this.kernelTypes = kernelTypes;
	}
	
	public Set<KernelType> getKernelTypes() {
		return kernelTypes;
	}	
}
