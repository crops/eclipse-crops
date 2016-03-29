package org.yocto.crops.core.jface.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class GitTextProgressDialog extends Dialog {
	  
	  public GitTextProgressDialog(Shell parentShell) {
		    super(parentShell);
		  }

		  @Override
		  protected Control createDialogArea(Composite parent) {
		    Composite container = (Composite) super.createDialogArea(parent);
		    Text text = new Text(container, SWT.NONE);

		    return container;
		  }

		  // overriding this methods allows you to set the
		  // title of the custom dialog
		  @Override
		  protected void configureShell(Shell newShell) {
		    super.configureShell(newShell);
		    newShell.setText("Git progress dialog");
		  }

		  @Override
		  protected Point getInitialSize() {
		    return new Point(450, 300);
		  }
}
