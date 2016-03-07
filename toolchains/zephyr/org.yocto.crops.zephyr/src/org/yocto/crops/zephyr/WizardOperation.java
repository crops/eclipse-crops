package org.yocto.crops.zephyr;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.core.templateengine.SharedDefaults;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * An operation that runs when the new project wizard finishes for the CROPS toolchain.
 * It reuses the information from {@link WizardPage} to set build options (ceed path).
 * It also clears and reruns scanner discovery to account for the modified command. 
 *
 */
public class WizardOperation implements IRunnableWithProgress {

	public WizardOperation() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

		String projectName = (String) MBSCustomPageManager.getPageProperty(WizardPage.PAGE_ID, WizardPage.CROPS_PROJECT_NAME);
		String path = (String) MBSCustomPageManager.getPageProperty(WizardPage.PAGE_ID, WizardPage.CEED_COMMAND_PATH);
		String board = (String) MBSCustomPageManager.getPageProperty(WizardPage.PAGE_ID, WizardPage.ZEPHYR_BOARD);
		Boolean expert = (Boolean) MBSCustomPageManager.getPageProperty(WizardPage.PAGE_ID, WizardPage.EXPERT_MODE);
		String gccVariant = (String) MBSCustomPageManager.getPageProperty(WizardPage.PAGE_ID, WizardPage.GCC_VARIANT);
		String installDir = (String) MBSCustomPageManager.getPageProperty(WizardPage.PAGE_ID, WizardPage.INSTALL_DIR);
		String zephyrBase = (String) MBSCustomPageManager.getPageProperty(WizardPage.PAGE_ID, WizardPage.ZEPHYR_BASE);
		
		SharedDefaults.getInstance().getSharedDefaultsMap().put(WizardPage.SHARED_DEFAULTS_PATH_KEY, path);
		SharedDefaults.getInstance().getSharedDefaultsMap().put(WizardPage.SHARED_DEFAULTS_BOARD_KEY, board);
		SharedDefaults.getInstance().getSharedDefaultsMap().put(WizardPage.SHARED_DEFAULTS_EXPERT_MODE_KEY, expert.toString());
		SharedDefaults.getInstance().getSharedDefaultsMap().put(WizardPage.SHARED_DEFAULTS_GCC_VARIANT_KEY, gccVariant);
		SharedDefaults.getInstance().getSharedDefaultsMap().put(WizardPage.SHARED_DEFAULTS_INSTALL_DIR_KEY, installDir);
		SharedDefaults.getInstance().getSharedDefaultsMap().put(WizardPage.SHARED_DEFAULTS_ZEPHYR_BASE_KEY, zephyrBase);
		SharedDefaults.getInstance().updateShareDefaultsMap(SharedDefaults.getInstance().getSharedDefaultsMap());
		
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (!project.exists())
			return;
		
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo == null)
			return;
		
		IConfiguration[] configs = buildInfo.getManagedProject().getConfigurations();
		for (IConfiguration config : configs) {
			IToolChain toolchain = config.getToolChain();
			IOption option = toolchain.getOptionBySuperClassId("org.yocto.crops.cdt.core.ceed.project"); //$NON-NLS-1$
			ManagedBuildManager.setOption(config, toolchain, option, projectName);
			option = toolchain.getOptionBySuperClassId("org.yocto.crops.cdt.core.option.ceed.path"); //$NON-NLS-1$
			ManagedBuildManager.setOption(config, toolchain, option, path);
			option = toolchain.getOptionBySuperClassId("crops.cdt.managedbuild.option.zephyr_install_dir"); //$NON-NLS-1$
			ManagedBuildManager.setOption(config, toolchain, option, installDir);
		}
		
		ManagedBuildManager.saveBuildInfo(project, true);
	}

}
