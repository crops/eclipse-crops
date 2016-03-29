package org.yocto.crops.internal.docker.launcher;

import org.yocto.crops.internal.docker.launcher.GdbDebuggerPage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

/**
 * The dynamic debugger tab for Docker Container launches using gdb server. The
 * gdbserver settings are used to start a gdbserver session in the Docker
 * Container and then to connect to it from the host.
 */
public class ContainerGdbDebuggerPage extends GdbDebuggerPage {

	protected Text fSysrootsPathText;
	
	protected Text fUsrBinPathText;
	
	protected Text fArchPrefixText;
	
	protected Text fGDBCommandText;

	@Override
	public String getName() {
		return Messages.Container_GDB_Debugger_Options;
	}
	
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		super.setDefaults(configuration);
		configuration.setAttribute(ILaunchConstants.ATTR_SYSROOTS_PATH, 
				ILaunchConstants.ATTR_SYSROOTS_PATH_DEFAULT);
		configuration.setAttribute(ILaunchConstants.ATTR_USR_BIN_PATH, 
				ILaunchConstants.ATTR_USR_BIN_PATH_DEFAULT);
		configuration.setAttribute(ILaunchConstants.ATTR_ARCH_PREFIX, 
				ILaunchConstants.ATTR_ARCH_PREFIX_DEFAULT);
		configuration.setAttribute(ILaunchConstants.ATTR_GDB_COMMAND, 
				ILaunchConstants.ATTR_GDB_COMMAND_DEFAULT);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);
		String sysrootsPath = null;
		String usrBinPath = null;
		String archPrefix = null;
		String gdbCommand = null;
		try {
			sysrootsPath = configuration.getAttribute(
					ILaunchConstants.ATTR_SYSROOTS_PATH,
					ILaunchConstants.ATTR_SYSROOTS_PATH_DEFAULT);
		} catch (CoreException e) {
		}
		try {
			usrBinPath = configuration.getAttribute(
					ILaunchConstants.ATTR_USR_BIN_PATH,
					ILaunchConstants.ATTR_USR_BIN_PATH_DEFAULT);
		} catch (CoreException e) {
		}
		try {
			archPrefix = configuration.getAttribute(
					ILaunchConstants.ATTR_ARCH_PREFIX,
					ILaunchConstants.ATTR_ARCH_PREFIX_DEFAULT);
		} catch (CoreException e) {
		}
		try {
			gdbCommand = configuration.getAttribute(
					ILaunchConstants.ATTR_GDB_COMMAND,
					ILaunchConstants.ATTR_GDB_COMMAND_DEFAULT);
		} catch (CoreException e) {
		}
		fSysrootsPathText.setText(sysrootsPath);
		fUsrBinPathText.setText(usrBinPath);
		fArchPrefixText.setText(archPrefix);
		fGDBCommandText.setText(gdbCommand);

	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		super.performApply(configuration);
		String str = fGDBCommandText.getText();
		str.trim();
		configuration
				.setAttribute(ILaunchConstants.ATTR_GDB_COMMAND, str);
	}
	
	protected void createGdbSettingsTab(TabFolder tabFolder) {
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(Messages.Gdb_Settings_Tab_Name);

		Composite comp = new Composite(tabFolder, SWT.NULL);
		comp.setLayout(new GridLayout(1, true));
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		((GridLayout) comp.getLayout()).makeColumnsEqualWidth = false;
		comp.setFont(tabFolder.getFont());
		tabItem.setControl(comp);

		Composite subComp = new Composite(comp, SWT.NULL);
		subComp.setLayout(new GridLayout(2, true));
		subComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		((GridLayout) subComp.getLayout()).makeColumnsEqualWidth = false;
		subComp.setFont(tabFolder.getFont());

	/* Sysroots Path */
		Label label = new Label(subComp, SWT.LEFT);
		label.setText(Messages.Sysroots_path_textfield_label);
		GridData gd = new GridData();
		label.setLayoutData(gd);
		fSysrootsPathText = new Text(subComp, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData();
		fSysrootsPathText.setLayoutData(data);
		fSysrootsPathText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
	
	/* /usr/bin path */
		label = new Label(subComp, SWT.LEFT);
		label.setText(Messages.Usr_bin_path_textfield_label);
		gd = new GridData();
		label.setLayoutData(gd);
		fUsrBinPathText = new Text(subComp, SWT.SINGLE | SWT.BORDER);
		data = new GridData();
		fUsrBinPathText.setLayoutData(data);
		fUsrBinPathText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
	
	/* Arch prefix */
		label = new Label(subComp, SWT.LEFT);
		label.setText(Messages.Arch_prefix_textfield_label);
		gd = new GridData();
		label.setLayoutData(gd);
		fArchPrefixText = new Text(subComp, SWT.SINGLE | SWT.BORDER);
		data = new GridData();
		fArchPrefixText.setLayoutData(data);
		fArchPrefixText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		
	/* Gdb Command */
		label = new Label(subComp, SWT.LEFT);
		label.setText(Messages.Gdb_name_textfield_label);
		gd = new GridData();
		label.setLayoutData(gd);
		fGDBCommandText = new Text(subComp, SWT.SINGLE | SWT.BORDER);
		data = new GridData();
		fGDBCommandText.setLayoutData(data);
		fGDBCommandText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.debug.mi.internal.ui.GDBDebuggerPage#createTabs(org.eclipse
	 * .swt.widgets.TabFolder)
	 */
	@Override
	public void createTabs(TabFolder tabFolder) {
		super.createTabs(tabFolder);
		createGdbSettingsTab(tabFolder);
	}
}
