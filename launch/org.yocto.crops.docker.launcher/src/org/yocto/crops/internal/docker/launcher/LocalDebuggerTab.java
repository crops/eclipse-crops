package org.yocto.crops.internal.docker.launcher;

import org.eclipse.cdt.debug.ui.ICDebuggerPage;
import org.eclipse.cdt.dsf.gdb.internal.ui.launching.CDebuggerTab;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

@SuppressWarnings("restriction")
public class LocalDebuggerTab extends CDebuggerTab {
	
	private final static String DEFAULTS_SET = "org.yocto.crops.docker.launcher.LocalDebuggerTab.DEFAULTS_SET"; //$NON-NLS-1$

	public LocalDebuggerTab() {
		super(SessionType.LOCAL, false);
	}
	/*
	 * When the launch configuration is created for Run mode, this Debugger tab
	 * is not created because it is not used for Run mode but only for Debug
	 * mode. When we then open the same configuration in Debug mode, the launch
	 * configuration already exists and initializeFrom() is called instead of
	 * setDefaults(). We therefore call setDefaults() ourselves and update the
	 * configuration. If we don't then the user will be required to press Apply
	 * to get the default settings saved.
	 */
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(DEFAULTS_SET, true);
		super.setDefaults(config);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		try {
			if (config.hasAttribute(DEFAULTS_SET) == false) {
				ILaunchConfigurationWorkingCopy wc;
				wc = config.getWorkingCopy();
				setDefaults(wc);
				wc.doSave();
			}
		} catch (CoreException e) {
		}

		super.initializeFrom(config);
	}

	@Override
	protected void loadDynamicDebugArea() {
		Composite dynamicTabHolder = getDynamicTabHolder();
		// Dispose of any current child widgets in the tab holder area
		Control[] children = dynamicTabHolder.getChildren();
		for (int i = 0; i < children.length; i++) {
			children[i].dispose();
		}
		setDynamicTab(new ContainerGdbDebuggerPage());

		ICDebuggerPage debuggerPage = getDynamicTab();
		if (debuggerPage == null) {
			return;
		}
		// Ask the dynamic UI to create its Control
		debuggerPage
				.setLaunchConfigurationDialog(getLaunchConfigurationDialog());
		debuggerPage.createControl(dynamicTabHolder);
		debuggerPage.getControl().setVisible(true);
		dynamicTabHolder.layout(true);
		contentsChanged();
	}

	@Override
	public String getId() {
		return "org.yocto.crops.docker.launch.debug.LocalCDSFDebuggerTab"; //$NON-NLS-1$
	}

}
