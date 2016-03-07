package org.yocto.crops.zephyr.builder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import org.yocto.crops.core.CropsCorePlugin;
import org.yocto.crops.core.CropsUtils;
import org.yocto.crops.core.preferences.CropsPreferences;

public class MakefileBuildConfiguration {

	private final IBuildConfiguration config;
	private static final String makefile = "Makefile";
			
	MakefileBuildConfiguration(IBuildConfiguration config) {
		this.config = config;
	}
	
	public static MakefileBuildConfiguration getConfig(IProject project, IProgressMonitor monitor) throws CoreException {

		/* config exists already? */
		for (IBuildConfiguration config : project.getBuildConfigs()) {
			if (!config.getName().equals(IBuildConfiguration.DEFAULT_CONFIG_NAME)) {
				MakefileBuildConfiguration makeConfig = config.getAdapter(MakefileBuildConfiguration.class);
				return makeConfig;
			}
		}
		/* we didn't find a config */
		// TODO: create a new one
		return null;
	}
	
	public void setActive(IProgressMonitor monitor) throws CoreException {
		IProject project = config.getProject();
		if (config.equals(project.getActiveBuildConfig())) {
			// already active
			return;
		}
		
		IProjectDescription projectDescription = project.getDescription();
		projectDescription.setActiveBuildConfig(config.getName());
		project.setDescription(projectDescription, monitor);
		
		// Re-index -- assuming for now each config has different builder/compiler settings
		CCorePlugin.getIndexManager().reindex(CoreModel.getDefault().create(project));
	}
	
	public IEclipsePreferences getSettings() {
		return (IEclipsePreferences) new ProjectScope(config.getProject()).getNode(CropsCorePlugin.getId()).node("config") //$NON-NLS-1$
				.node(config.getName());
	}
	
	public IFolder getBuildFolder() throws CoreException {
		IProject project = config.getProject();
		return project.getFolder("build"); //$NON-NLS-1$
	}
	
	public File getBuildDirectory() throws CoreException {
		return new File(getBuildFolder().getLocationURI());
	}
	
	public IFile getMakefile() throws CoreException {
		IFolder buildFolder = getBuildFolder();
		return buildFolder.getFile(makefile);
	}
	
	/* TODO: generate Makefile */
	
	public String getCeedCommand() {
		if (CropsUtils.isWin()) {
			Path ceedPath = CropsPreferences.getCropsHome().resolve("ceed/ceed.exe"); //$NON-NLS-1$
			if (!Files.exists(ceedPath)) {
				ceedPath = CropsPreferences.getCropsHome().resolve("ceed.exe"); //$NON-NLS-1$ 
			}
			return ceedPath.toString() + " -i ${codi_ip_addr}" + " -s ${codi_socket}" + "-g make -r ${MAKE_ARGS}";
		} else {
			return "ceed"; //$NON-NLS-1$ 
		}
	}
	
	public String getCodiIPAddressArg() {
		return " -i $(codi_ip_addr)";
	}
	public String getMakeCommand() {
		return getCeedCommand().concat(" -i $(codi_ip_addr)" + " -s $(codi_socket)" + " -d $(toolchain_container_id)" + " -g make");
	}
}
