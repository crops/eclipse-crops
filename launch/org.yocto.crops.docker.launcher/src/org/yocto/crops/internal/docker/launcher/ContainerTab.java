/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.yocto.crops.internal.docker.launcher;

import java.util.ArrayList;
import java.util.Arrays;

import org.yocto.crops.docker.launcher.DockerLaunchUIPlugin;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerConnectionManagerListener;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerContainerListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.prefs.Preferences;

public class ContainerTab extends AbstractLaunchConfigurationTab implements
		IDockerConnectionManagerListener, /* IDockerImageListener, */ IDockerContainerListener {

	private Text workspaceDirectory;
	private Text cropsBindMount;
	private List directoriesList;
	private String containerName;
	private String connectionName;
	private String connectionUri;
	private Boolean keepValue;
	private Boolean stdinValue;
	private IDockerConnection connection;
	private IDockerConnection[] connections;
	private IDockerContainerListener containerTab;
	//private IDockerImageListener containerTab;

	private Button newButton;
	private Button removeButton;
	private Button keepButton;
	private Button stdinButton;
	private Combo containerCombo;
	private Combo connectionSelector;

	private ModifyListener connectionModifyListener = new ModifyListener() {

		@Override
		public void modifyText(ModifyEvent e) {
			int index = connectionSelector.getSelectionIndex();
			if (connection != null)
				connection.removeContainerListener(containerTab);
			connection = connections[index];
			if (!connectionName.equals(connection.getName()))
				updateLaunchConfigurationDialog();
			connectionName = connection.getName();
			connectionUri = connection.getUri();
			connection.addContainerListener(containerTab);
		}

	};

	public ContainerTab() {
		super();
		containerTab = this;
	}

	@Override
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		Composite mainComposite = createComposite(parent, 3, 1,
				GridData.FILL_HORIZONTAL);
		mainComposite.setFont(font);
		setControl(mainComposite);

		Label connectionSelectorLabel = new Label(mainComposite, SWT.NULL);
		connectionSelectorLabel
				.setText(Messages.ContainerTab_Connection_Selector_Label);

		connectionSelector = new Combo(mainComposite, SWT.BORDER
				| SWT.READ_ONLY);
		initializeConnectionSelector();
		connectionSelector.addModifyListener(connectionModifyListener);
		// Following is a kludge so that on Linux the Combo is read-only but
		// has a white background.
		connectionSelector.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				e.doit = false;
			}
		});
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		connectionSelector.setLayoutData(gd);

		Label containerSelectorLabel = new Label(mainComposite, SWT.NULL);
		containerSelectorLabel.setText(Messages.ContainerTab_Container_Selector_Label);
		containerCombo = new Combo(mainComposite, SWT.DROP_DOWN);
		containerCombo.setLayoutData(gd);

		initializeContainerCombo();

		containerCombo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!containerName.equals(containerCombo.getText()))
					updateLaunchConfigurationDialog();
				containerName = containerCombo.getText();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

		});

		createWorkspaceToCropsDirectoryBindMount(mainComposite);
		createDirectoryList(mainComposite);
		createButtons(mainComposite);
		createOptions(mainComposite);
	}

	/**
	 * CROPS relies on the toolchain container having a bind mount
	 * from the host workspace to the "/crops" directory as seen
	 * by the container.
	 * 
	 * @param parent
	 */
	private void createWorkspaceToCropsDirectoryBindMount(Composite parent) {
		Composite comp = createComposite(parent, 1, 3, GridData.FILL_BOTH);
		/* Host workspace path -> Container bind mount path */
		Group group = new Group(comp, SWT.NONE);
		Font font = parent.getFont();
		group.setFont(font);
		group.setText(Messages.ContainerTab_BindMount_Group_Name);
		
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace = true;
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayoutData(gd);
		group.setLayout(layout);
		
		Label label = new Label(group, SWT.NULL);
		label.setFont(font);
		label.setText(Messages.ContainerTab_Host_Path_Label);
		label.setLayoutData(new GridData());
		workspaceDirectory = new Text(group, SWT.SINGLE | SWT.BORDER);
		workspaceDirectory.setFont(font);
		workspaceDirectory.setText( ResourcesPlugin
				.getWorkspace().getRoot().getRawLocation().toOSString() );
		workspaceDirectory.setLayoutData(new GridData());
		
		label = new Label(group, SWT.NULL);
		label.setFont(font);
		label.setText(Messages.ContainerTab_Container_Path_Label);
		label.setLayoutData(new GridData(SWT.LEAD, SWT.CENTER, false, false, 1, 1));
		cropsBindMount = new Text(group, SWT.SINGLE | SWT.BORDER);
		cropsBindMount.setFont(font);
		cropsBindMount.setText( ILaunchConstants.ATTR_CROPS_DIRECTORY_DEFAULT);
		cropsBindMount.setLayoutData(new GridData(SWT.NONE, SWT.CENTER, true, false, 1, 1));
		
	}
	
	private void createDirectoryList(Composite parent) {
		Composite comp = createComposite(parent, 2, 2, GridData.FILL_BOTH);

		Group group = new Group(comp, SWT.NONE);
		Font font = parent.getFont();
		group.setFont(font);
		group.setText(Messages.ContainerTab_Group_Name);

		GridData gd2 = new GridData(GridData.FILL_BOTH);
		group.setLayoutData(gd2);

		group.setLayout(new GridLayout());

		directoriesList = new List(group, SWT.SINGLE | SWT.V_SCROLL);
		GridData gd3 = new GridData(GridData.FILL_BOTH);
		directoriesList.setLayoutData(gd3);
		directoriesList.setFont(font);
		directoriesList.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				removeButton.setEnabled(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
	}

	private void createButtons(Composite parent) {
		Font font = parent.getFont();
		Composite composite = createComposite(parent, 1, 1,
				GridData.VERTICAL_ALIGN_BEGINNING
						| GridData.HORIZONTAL_ALIGN_END);
		composite.setFont(font);
		newButton = createPushButton(composite,
				Messages.ContainerTab_New_Button, null); //$NON-NLS-1$
		newButton.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridData gdb = new GridData(GridData.VERTICAL_ALIGN_CENTER);
		gdb.grabExcessHorizontalSpace = false;
		gdb.horizontalAlignment = SWT.FILL;
		gdb.minimumWidth = 120;
		newButton.setLayoutData(gdb);
		newButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				handleNewButtonSelected(directoriesList);
			}
		});

		removeButton = createPushButton(composite,
				Messages.ContainerTab_Remove_Button, null); //$NON-NLS-1$
		removeButton.setLayoutData(new GridData(GridData.FILL_BOTH));
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				handleRemoveButtonSelected(directoriesList);
			}
		});
		removeButton.setEnabled(false);
	}

	private void createOptions(Composite parent) {
		Font font = parent.getFont();
		Composite comp = createComposite(parent, 1, 3, GridData.FILL_BOTH);

		Group group = new Group(comp, SWT.NONE);
		group.setFont(font);
		group.setText(Messages.ContainerTab_Option_Group_Name);

		GridData gd2 = new GridData(GridData.FILL_BOTH);
		group.setLayoutData(gd2);

		group.setLayout(new GridLayout());
		Preferences prefs = InstanceScope.INSTANCE.getNode(DockerLaunchUIPlugin.PLUGIN_ID);
		keepButton = createCheckButton(group, Messages.ContainerTab_Keep_Label);
		keepButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Boolean keepPref = prefs.getBoolean(
				PreferenceConstants.KEEP_CONTAINER_AFTER_LAUNCH, false);
		keepButton.setSelection(keepPref);
		keepValue = keepPref;
		keepButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!keepValue.equals(keepButton.getSelection()))
					updateLaunchConfigurationDialog();
				keepValue = keepButton.getSelection();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

		});
		stdinButton = createCheckButton(group,
				Messages.ContainerTab_Stdin_Support_Label);
		stdinButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		stdinValue = false;
		stdinButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!stdinValue.equals(stdinButton.getSelection()))
					updateLaunchConfigurationDialog();
				stdinValue = stdinButton.getSelection();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

		});
	}

	private Composite createComposite(Composite parent, int columns, int hspan,
			int fill) {
		Composite g = new Composite(parent, SWT.NONE);
		g.setLayout(new GridLayout(columns, false));
		g.setFont(parent.getFont());
		GridData gd = new GridData(fill);
		gd.horizontalSpan = hspan;
		g.setLayoutData(gd);
		return g;
	}

	/**
	 * A New entry button has been pressed for the given text field. Prompt the
	 * user for a directory to add and enter the result in the given field.
	 */
	protected void handleNewButtonSelected(List list) {
		String directory = getDirectory();
		if (directory != null) {
			list.add(directory);
			updateLaunchConfigurationDialog();
		}
	}

	/**
	 * Prompts the user to choose and configure a variable and returns the
	 * resulting string, suitable to be used as an attribute.
	 */
	private String getDirectory() {
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		return dialog.open();
	}

	/**
	 * The remove entry button has been pressed for the given text field. Remove
	 * the currently selected directory.
	 */
	protected void handleRemoveButtonSelected(List list) {
		int index = list.getSelectionIndex();
		list.remove(index);
		updateLaunchConfigurationDialog();
		removeButton.setEnabled(false);
	}

	private void initializeConnectionSelector() {
		int defaultIndex = -1;
		connections = DockerConnectionManager.getInstance().getConnections();
		if (connections.length == 0) {
			setErrorMessage(Messages.ContainerTab_Error_No_Connections);
			return;
		}
		String[] connectionNames = new String[connections.length];
		for (int i = 0; i < connections.length; ++i) {
			connectionNames[i] = connections[i].getName();
			if (connections[i].getUri().equals(connectionUri))
				defaultIndex = i;
		}
		if (defaultIndex < 0) {
			setWarningMessage(Messages.bind(
					Messages.ContainerTab_Warning_Connection_Not_Found,
					connectionUri, connections[0].getName()));
			defaultIndex = 0;
		}
		connectionSelector.setItems(connectionNames);
		if (connections.length > 0) {
			connectionSelector.setText(connectionNames[defaultIndex]);
			connection = connections[defaultIndex];
			connectionUri = connection.getUri();
		}
	}

	private void initializeContainerCombo() {
		if (connection != null) {
			java.util.List<IDockerContainer> containers = connection.getContainers();
			if (containers == null || containers.size() == 0) {
				setErrorMessage(Messages.ContainerTab_Error_No_Containers);
				return;
			}
			connection.removeContainerListener(containerTab);
			ArrayList<String> containerNames = new ArrayList<String>();
			for (IDockerContainer container : containers) {
				// java.util.List<String> tags = image.repoTags();
				java.util.List<String> tags = container.names();
				if (tags != null) {
					for (String tag : tags) {
						if (!tag.equals("<none>:<none>")) //$NON-NLS-1$
							containerNames.add(tag);
					}
				}
			}
			containerCombo.setItems(containerNames.toArray(new String[0]));
			if (containerName != null)
				containerCombo.setText(containerName);
			connection.addContainerListener(containerTab);
		}
	}

	public void addControlAccessibleListener(Control control, String controlName) {
		// Strip mnemonic (&)
		String[] strs = controlName.split("&"); //$NON-NLS-1$
		StringBuffer stripped = new StringBuffer();
		for (int i = 0; i < strs.length; i++) {
			stripped.append(strs[i]);
		}
		control.getAccessible().addAccessibleListener(
				new ControlAccessibleListener(stripped.toString()));
	}

	private class ControlAccessibleListener extends AccessibleAdapter {
		private String controlName;

		ControlAccessibleListener(String name) {
			controlName = name;
		}

		@Override
		public void getName(AccessibleEvent e) {
			e.result = controlName;
		}
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ILaunchConstants.ATTR_ADDITIONAL_DIRS,
				(String) null);
		configuration.setAttribute(ILaunchConstants.ATTR_CONNECTION_URI, ""); //$NON-NLS-1$
		Preferences prefs = InstanceScope.INSTANCE
				.getNode(DockerLaunchUIPlugin.PLUGIN_ID);
		String image = prefs.get(PreferenceConstants.DEFAULT_IMAGE, ""); //$NON-NLS-1$
		configuration.setAttribute(ILaunchConstants.ATTR_IMAGE, image);
		Boolean keepContainer = prefs.getBoolean(
				PreferenceConstants.KEEP_CONTAINER_AFTER_LAUNCH, false);
		configuration.setAttribute(ILaunchConstants.ATTR_KEEP_AFTER_LAUNCH,
				keepContainer);
		configuration.setAttribute(ILaunchConstants.ATTR_STDIN_SUPPORT, false);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			java.util.List<String> additionalDirs = configuration.getAttribute(
					ILaunchConstants.ATTR_ADDITIONAL_DIRS,
					(java.util.List<String>) null);

			if (additionalDirs != null)
				directoriesList.setItems(additionalDirs.toArray(new String[0]));
			connectionUri = configuration.getAttribute(
					ILaunchConstants.ATTR_CONNECTION_URI, (String) "");
			int defaultIndex = 0;
			connections = DockerConnectionManager.getInstance()
					.getConnections();
			if (connections.length > 0) {
				if (!connectionUri.equals("")) { //$NON-NLS-1$
					String[] connectionNames = new String[connections.length];
					for (int i = 0; i < connections.length; ++i) {
						connectionNames[i] = connections[i].getName();
						if (connections[i].getUri().equals(connectionUri))
							defaultIndex = i;
					}
					connectionSelector.select(defaultIndex);
				} else {
					connectionUri = connections[0].getUri();
				}
			}
			containerName = configuration.getAttribute(ILaunchConstants.ATTR_IMAGE,
					"");
			containerCombo.setText(containerName);
			keepValue = configuration.getAttribute(
					ILaunchConstants.ATTR_KEEP_AFTER_LAUNCH, false);
			keepButton.setSelection(keepValue);
			stdinValue = configuration.getAttribute(
					ILaunchConstants.ATTR_STDIN_SUPPORT, false);
			stdinButton.setSelection(stdinValue);
		} catch (CoreException e) {
			setErrorMessage(Messages.bind(
					Messages.ContainerTab_Error_Reading_Configuration, e
							.getStatus().getMessage())); //$NON-NLS-1$
			DockerLaunchUIPlugin.log(e);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		String[] dirs = directoriesList.getItems();
		configuration.setAttribute(ILaunchConstants.ATTR_ADDITIONAL_DIRS,
				Arrays.asList(dirs));
		String container = containerCombo.getText();
		configuration.setAttribute(ILaunchConstants.ATTR_CONTAINER, container);
		configuration.setAttribute(ILaunchConstants.ATTR_CONNECTION_URI,
				connectionUri);
		configuration.setAttribute(ILaunchConstants.ATTR_KEEP_AFTER_LAUNCH,
				keepButton.getSelection());
		configuration.setAttribute(ILaunchConstants.ATTR_STDIN_SUPPORT,
				stdinButton.getSelection());
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		try {
			return launchConfig.getAttribute(ILaunchConstants.ATTR_IMAGE,
					(String) null) != null;
		} catch (CoreException e) {
			return false;
		}
	}

	@Override
	public String getName() {
		return Messages.ContainerTab_Name;
	}

	@Override
	public Image getImage() {
		return SWTImagesFactory.get(SWTImagesFactory.IMG_CONTAINER);
	}

	@Override
	public void changeEvent(int type) {
		String currUri = null;
		int currIndex = 0;
		connections = DockerConnectionManager.getInstance().getConnections();
		if (connection != null) {
			currUri = connection.getUri();
			currIndex = connectionSelector.getSelectionIndex();
		}
		String[] connectionNames = new String[connections.length];
		int index = 0;
		for (int i = 0; i < connections.length; ++i) {
			connectionNames[i] = connections[i].getName();
			if (connections[i].getUri().equals(currUri))
				index = i;
		}
		if (type == IDockerConnectionManagerListener.RENAME_EVENT) {
			index = currIndex; // no change in connection displayed
		}
		connectionSelector.removeModifyListener(connectionModifyListener);
		connectionSelector.setItems(connectionNames);
		if (connectionNames.length > 0) {
			connectionSelector.setText(connectionNames[index]);
			connection = connections[index];
			connectionUri = connection.getUri();
		} else {
			connection = null;
			connectionUri = "";
			connectionSelector.setText("");
		}
		connectionSelector.addModifyListener(connectionModifyListener);
	}

	@Override
	public void listChanged(IDockerConnection c,
			java.util.List<IDockerContainer> list) {
		final IDockerContainer[] finalList = list.toArray(new IDockerContainer[0]);
		if (c.getName().equals(connection.getName())) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					connection.removeContainerListener(containerTab);
					ArrayList<String> containerNames = new ArrayList<String>();
					for (IDockerContainer container : finalList) {
						//java.util.List<String> tags = image.repoTags();
						java.util.List<String> tags = container.names();
						if (tags != null) {
							for (String tag : tags) {
								containerNames.add(tag);
							}
						}
					}
					if (!containerCombo.isDisposed())
						containerCombo.setItems(containerNames.toArray(new String[0]));
					connection.addContainerListener(containerTab);
				}

			});
		}
	}

	@Override
	public void dispose() {
		if (connection != null)
			connection.removeContainerListener(containerTab);
		super.dispose();
	}

}
