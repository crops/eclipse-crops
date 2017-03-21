package org.yocto.crops.internal.sdk.ui;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

public class YoctoProjectPropertyPage extends PropertyPage {

	public static final String NODE_NAME = "yoctoBuildContainerConfig";
	
	private Combo connectionSelector;
	private IDockerConnection[] connections;
	//private IDockerImageListener containerTab;
	private String connectionUri = "";
	private IDockerConnection connection;
	private String connectionName;
	private Combo imageCombo;
	private List directoriesList;
	private Button newButton;
	private Button removeButton;
	private Button privilegedButton;
	private ProjectScope projectScope;

	public Control createContents(Composite parent) {
		
		IEclipsePreferences prefs = projectScope.getNode(NODE_NAME);
		Composite mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainComposite.setLayout(new GridLayout());

		Label connectionSelectorLabel = new Label(mainComposite, SWT.NULL);
		connectionSelectorLabel.setText("Connection:");

		connectionSelector = new Combo(mainComposite, SWT.BORDER | SWT.READ_ONLY);
		int defaultIndex = -1;
		connections = DockerConnectionManager.getInstance().getConnections();
		String[] connectionNames = new String[connections.length];
		for (int i = 0; i < connections.length; ++i) {
			connectionNames[i] = connections[i].getName();
			if (connections[i].getUri().equals(connectionUri))
				defaultIndex = i;
		}
		if (defaultIndex < 0) {
			defaultIndex = 0;
		}
		connectionSelector.setItems(connectionNames);
		if (connections.length > 0) {
			connectionSelector.setText(connectionNames[defaultIndex]);
			connection = connections[defaultIndex];
			connectionName = connection.getName();
			connectionUri = connection.getUri();
		}
		connectionSelector.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label imageSelectorLabel = new Label(mainComposite, SWT.NULL);
		imageSelectorLabel.setText("Image:");
		imageCombo = new Combo(mainComposite, SWT.DROP_DOWN);
		imageCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		if (connection != null) {
			java.util.List<IDockerImage> images = connection.getImages();
			ArrayList<String> imageNames = new ArrayList<String>();
			for (IDockerImage image : images) {
				java.util.List<String> tags = image.repoTags();
				if (tags != null) {
					for (String tag : tags) {
						if (!tag.equals("<none>:<none>")) //$NON-NLS-1$
							imageNames.add(tag);
					}
				}
			}
			imageCombo.setItems(imageNames.toArray(new String[0]));
		}

		Composite comp1 = createComposite(mainComposite, 1, 2, GridData.FILL_BOTH);
		
		Group group1 = new Group(comp1, SWT.NONE);
		group1.setText("Required host directories");
		
		group1.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		group1.setLayout(new GridLayout());
		
		directoriesList = new List(group1, SWT.SINGLE | SWT.V_SCROLL);
		directoriesList.setLayoutData(new GridData(GridData.FILL_BOTH));
		directoriesList.addSelectionListener(new SelectionListener() {
		
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeButton.setEnabled(true);
			}
		
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		Composite composite = createComposite(mainComposite, 1, 1,
				GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_END);
		newButton = createPushButton(composite, "New...", null); //$NON-NLS-1$
		newButton.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridData gdb = new GridData(GridData.VERTICAL_ALIGN_CENTER);
		gdb.grabExcessHorizontalSpace = false;
		gdb.horizontalAlignment = SWT.FILL;
		gdb.minimumWidth = 120;
		newButton.setLayoutData(gdb);
		newButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String directory = getDirectory();
				if (directory != null) 
					directoriesList.add(directory);
			}
		});
		
		removeButton = createPushButton(composite, "Remove", null); //$NON-NLS-1$
		removeButton.setLayoutData(new GridData(GridData.FILL_BOTH));
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				int index = directoriesList.getSelectionIndex();
				directoriesList.remove(index);
				removeButton.setEnabled(false);
			}
		});
		removeButton.setEnabled(false);
		Composite comp = createComposite(mainComposite, 1, 3, GridData.FILL_BOTH);
		
		Group group = new Group(comp, SWT.NONE);
		group.setText("Additional options");
		
		GridData gd2 = new GridData(GridData.FILL_BOTH);
		group.setLayoutData(gd2);
		
		group.setLayout(new GridLayout());
		privilegedButton = new Button(group, SWT.CHECK);
		privilegedButton.setText("Run in privileged mode");
		privilegedButton.setSelection(true);
		privilegedButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		setButtonDimensionHint(privilegedButton);
		return mainComposite;
	}

	private String getDirectory() {
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		return dialog.open();
	}

	private int getButtonWidthHint(Button button) {
		PixelConverter converter = new PixelConverter(button);
		int widthHint = converter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		return Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	}

	private void setButtonDimensionHint(Button button) {
		Object gd = button.getLayoutData();
		if (gd instanceof GridData) {
			((GridData) gd).widthHint = getButtonWidthHint(button);
			((GridData) gd).horizontalAlignment = GridData.FILL;
		}
	}

	private Button createPushButton(Composite parent, String label, Image image) {
		Button button = new Button(parent, SWT.PUSH);
		button.setFont(parent.getFont());
		if (image != null) {
			button.setImage(image);
		}
		if (label != null) {
			button.setText(label);
		}
		GridData gd = new GridData();
		button.setLayoutData(gd);
		setButtonDimensionHint(button);
		return button;

	}

	@Override
	public void setElement(IAdaptable element) {
		super.setElement(element);
		this.projectScope = new ProjectScope((IProject) element);
	}
	
	private Composite createComposite(Composite parent, int columns, int hspan, int fill) {
		Composite g = new Composite(parent, SWT.NONE);
		g.setLayout(new GridLayout(columns, false));
		g.setFont(parent.getFont());
		GridData gd = new GridData(fill);
		gd.horizontalSpan = hspan;
		g.setLayoutData(gd);
		return g;
	}

	public boolean performOk() {
		System.out.println("Ok");
		return true;
	}

}