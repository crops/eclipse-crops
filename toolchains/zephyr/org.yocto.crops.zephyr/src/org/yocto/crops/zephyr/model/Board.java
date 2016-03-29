package org.yocto.crops.zephyr.model;

public class Board {
	private String name;
	private Arch arch;
	private SoC soc;
	private KernelTypes kernelTypes;
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setArch(Arch arch) {
		this.arch = arch;
	}
	
	public Arch getArch() {
		return arch;
	}
	
	public void setSoC(SoC soc) {
		this.soc = soc;
	}
	
	public SoC getSoC() {
		return soc;
	}
	
	public void setKernelTypes(KernelTypes kernelTypes) {
		this.kernelTypes = kernelTypes;
	}
	
	public KernelTypes getKernelTypes() {
		return kernelTypes;
	}
}
