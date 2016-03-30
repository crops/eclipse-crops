package org.yocto.crops.zephyr;

import java.util.Map.Entry;

import org.eclipse.cdt.core.templateengine.SharedDefaults;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.yocto.crops.core.CropsCorePlugin;
import org.yocto.crops.zephyr.preferences.PreferenceConstants;
import org.yocto.crops.zephyr.ZephyrConstants;
import org.yocto.crops.zephyr.ZephyrConstants.Arches;
import org.yocto.crops.zephyr.ZephyrConstants.Arches.arch_id;
import org.yocto.crops.zephyr.ZephyrConstants.Boards;
import org.yocto.crops.zephyr.ZephyrConstants.Boards.board_id;

public class WizardPage extends MBSCustomPage {

	private Composite composite;
	private boolean finish = false;
	private Text pathText;
	private Combo boardCombo;
	private Combo archCombo;
	private Boolean expertBoolean;
	private Text gccVariantText;
	private Text installDirText;
	private Text zephyrBaseText;
	
	public static final String PAGE_ID = "org.yocto.crops.cdt.core.WizardPage"; //$NON-NLS-1$
	
	public static final String CROPS_PROJECT_NAME = "cropsProjectName"; //$NON-NLS-1$
	public static final String CEED_COMMAND_PATH = "ceedCommandPath"; //$NON-NLS-1$
	public static final String ZEPHYR_BOARD = "zephyrBoard"; //$NON-NLS-1$
	public static final String ZEPHYR_ARCH = "zephyrArch"; //$NON-NLS-1$
	public static final String EXPERT_MODE = "expertMode"; //$NON-NLS-1$
	public static final String GCC_VARIANT = "gccVariant"; //$NON-NLS-1$
	public static final String INSTALL_DIR = "installDir"; //$NON-NLS-1$
	public static final String ZEPHYR_BASE = "zephyrBase"; //$NON-NLS-1$
	
	static final String SHARED_DEFAULTS_PATH_KEY = "path"; //$NON-NLS-1$
	static final String SHARED_DEFAULTS_BOARD_KEY = "board"; //$NON-NLS-1$
	static final String SHARED_DEFAULTS_ARCH_KEY = "arch"; //$NON-NLS-1$
	static final String SHARED_DEFAULTS_EXPERT_MODE_KEY = "expert"; //$NON-NLS-1$
	static final String SHARED_DEFAULTS_GCC_VARIANT_KEY = "gccVariant"; //$NON-NLS-1$
	static final String SHARED_DEFAULTS_INSTALL_DIR_KEY = "installDir"; //$NON-NLS-1$
	static final String SHARED_DEFAULTS_ZEPHYR_BASE_KEY = "zephyrBase"; //$NON-NLS-1$
	
	public WizardPage() {
		pageID = PAGE_ID;
		
		IPreferenceStore store = CropsCorePlugin.getDefault().getPreferenceStore();
		
		MBSCustomPageManager.addPageProperty(PAGE_ID, ZEPHYR_BOARD, store.getDefaultString(PreferenceConstants.P_ZEPHYR_BOARD));
		MBSCustomPageManager.addPageProperty(PAGE_ID, ZEPHYR_ARCH, store.getDefaultString(PreferenceConstants.P_ZEPHYR_ARCH));
		MBSCustomPageManager.addPageProperty(PAGE_ID, GCC_VARIANT, store.getDefaultString(PreferenceConstants.P_ZEPHYR_GCC_VARIANT));
		MBSCustomPageManager.addPageProperty(PAGE_ID, INSTALL_DIR, store.getDefaultString(PreferenceConstants.P_ZEPHYR_INSTALL_DIR));
		MBSCustomPageManager.addPageProperty(PAGE_ID, ZEPHYR_BASE, store.getDefaultString(PreferenceConstants.P_ZEPHYR_BASE));
	}

	/**
	 * @param pageID
	 */
	public WizardPage(String pageID) {
		super(pageID);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#getName()
	 */
	@Override
	public String getName() {
		return Messages.WizardPage_name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		composite = new Composite(parent, SWT.NULL);
		
		composite.setLayout(new GridLayout(3, false));
		GridData layoutData = new GridData();
		composite.setLayoutData(layoutData);
		
		Label pathLabel = new Label(composite, SWT.NONE);
		pathLabel.setText(Messages.WizardPage_path);

		pathText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		String ceedCommandPath = SharedDefaults.getInstance().getSharedDefaultsMap().get(SHARED_DEFAULTS_PATH_KEY);
		if (ceedCommandPath != null) {
			pathText.setText(ceedCommandPath);
			updatePathProperty();
		}
		layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		pathText.setLayoutData(layoutData);
		pathText.addModifyListener(new ModifyListener(){
		
				public void modifyText(ModifyEvent event) {
					updatePathProperty();
				}
		});
		
		Button button = new Button(composite, SWT.NONE);
		button.setText(Messages.WizardPage_browse);
		button.addSelectionListener(new SelectionListener() {
			
			public void widgetDefaultSelected(SelectionEvent event) {
			}
			
			public void widgetSelected(SelectionEvent event) {
				// TODO: check for .exe extension or +x permissions?
				FileDialog fileDialog = new FileDialog(composite.getShell(), SWT.APPLICATION_MODAL);
				String browsedFile = fileDialog.open();
				if (browsedFile != null) {
					pathText.setText(browsedFile);
				}
			}
		});
		layoutData = new GridData(SWT.LEFT, SWT.TOP, false, false, 1 ,1);
		button.setLayoutData(layoutData);
		
		Label boardLabel = new Label(composite, SWT.NONE);
		boardLabel.setText(Messages.WizardPage_board);
		Combo boardCombo = new Combo(composite, SWT.DROP_DOWN);
		Boards boards = new Boards();
		for( board_id id : boards.getBoards().keySet()) {
			boardCombo.add(boards.getBoard(id).toString());
		}
		boardCombo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				updateBoardProperty();
			}
		});

		Label archLabel = new Label(composite, SWT.NONE);
		boardLabel.setText(Messages.WizardPage_arch);
		Combo archCombo = new Combo(composite, SWT.DROP_DOWN);
		Arches arches = new Arches();
		for( arch_id id : arches.getArches().keySet()) {
			archCombo.add(arches.getArch(id).toString());
		}
		boardCombo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				updateArchProperty();
			}
		});

		Label gccVariantLabel = new Label(composite, SWT.NONE);
		gccVariantLabel.setText(Messages.WizardPage_zephyr_gcc_variant);

		gccVariantText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		String gccVariant = SharedDefaults.getInstance().getSharedDefaultsMap().get(SHARED_DEFAULTS_GCC_VARIANT_KEY);
		if (gccVariant != null) {
			gccVariantText.setText(gccVariant);
			updateGccVariantProperty();
		}
		layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gccVariantText.setLayoutData(layoutData);
		gccVariantText.addModifyListener(new ModifyListener(){
		
				public void modifyText(ModifyEvent event) {
					updateGccVariantProperty();
				}
		});

		Label installDirLabel = new Label(composite, SWT.NONE);
		installDirLabel.setText(Messages.WizardPage_zephyr_install_dir);

		installDirText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		String installDir = SharedDefaults.getInstance().getSharedDefaultsMap().get(SHARED_DEFAULTS_INSTALL_DIR_KEY);
		if (installDir != null) {
			installDirText.setText(installDir);
			updateInstallDirProperty();
		}
		layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		installDirText.setLayoutData(layoutData);
		installDirText.addModifyListener(new ModifyListener(){
		
				public void modifyText(ModifyEvent event) {
					updateInstallDirProperty();
				}
		});

		Label zephyrBaseLabel = new Label(composite, SWT.NONE);
		zephyrBaseLabel.setText(Messages.WizardPage_zephyr_base);

		zephyrBaseText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		String zephyrBase = SharedDefaults.getInstance().getSharedDefaultsMap().get(SHARED_DEFAULTS_ZEPHYR_BASE_KEY);
		if (zephyrBase != null) {
			zephyrBaseText.setText(zephyrBase);
			updateZephyrBaseProperty();
		}
		layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		zephyrBaseText.setLayoutData(layoutData);
		zephyrBaseText.addModifyListener(new ModifyListener(){
		
				public void modifyText(ModifyEvent event) {
					updateZephyrBaseProperty();
				}
		});

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getControl()
	 */
	@Override
	public Control getControl() {
		return composite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getDescription()
	 */
	@Override
	public String getDescription() {
		return Messages.WizardPage_description;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getErrorMessage()
	 */
	@Override
	public String getErrorMessage() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getImage()
	 */
	@Override
	public Image getImage() {
		return wizard.getDefaultPageImage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getMessage()
	 */
	@Override
	public String getMessage() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getTitle()
	 */
	@Override
	public String getTitle() {
		return Messages.WizardPage_title;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#performHelp()
	 */
	@Override
	public void performHelp() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription(String description) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setImageDescriptor(org.eclipse.jface.resource.ImageDescriptor)
	 */
	@Override
	public void setImageDescriptor(ImageDescriptor image) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setTitle(java.lang.String)
	 */
	@Override
	public void setTitle(String title) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			finish = true;
		}
		composite.setVisible(visible);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage#isCustomPageComplete()
	 */
	@Override
	protected boolean isCustomPageComplete() {
		// Make sure that if the user goes back to the first page and changes the project
		// name, the property with be updated.
		updateProjectNameProperty();
		return finish;
	}

	/**
	 * MSBCustomPageManager and properties are used to pass things to CommandOperation
	 */
	private void updatePathProperty() {
		MBSCustomPageManager.addPageProperty(PAGE_ID, CEED_COMMAND_PATH, pathText.getText());
	}
	
	private void updateProjectNameProperty() {
		IWizardPage[] pages = getWizard().getPages();
		for (IWizardPage wizardPage : pages) {
			/* org.eclipse.ui.dialogs is in org.eclipse.ui.ide plugin */
			if (wizardPage instanceof WizardNewProjectCreationPage) {
				MBSCustomPageManager.addPageProperty(PAGE_ID, CROPS_PROJECT_NAME, ((WizardNewProjectCreationPage) wizardPage).getProjectName());
			}
		}
	}
	
	private void updateBoardProperty() {
		MBSCustomPageManager.addPageProperty(PAGE_ID, ZEPHYR_BOARD, boardCombo.getText());
	}
	
	private void updateArchProperty() {
		MBSCustomPageManager.addPageProperty(PAGE_ID,  ZEPHYR_ARCH, archCombo.getText());
	}
	private void updateGccVariantProperty() {
		MBSCustomPageManager.addPageProperty(PAGE_ID, GCC_VARIANT, gccVariantText.getText());
	}
	
	private void updateInstallDirProperty() {
		MBSCustomPageManager.addPageProperty(PAGE_ID, INSTALL_DIR, installDirText.getText());
	}
	
	private void updateZephyrBaseProperty() {
		MBSCustomPageManager.addPageProperty(PAGE_ID, ZEPHYR_BASE, zephyrBaseText.getText());
	}

}
