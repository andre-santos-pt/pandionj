package pt.iscte.pandionj.perspective.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (java).
 */

public class NewFolderWizardPage extends WizardPage {

	private Text fileText;

	private IStructuredSelection selection;

	
	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public NewFolderWizardPage(IStructuredSelection selection) {
		super("wizardPage");
		setTitle("New Java package");
		setDescription("Java packages typically do not use uppercase characters.");
		this.selection = selection;
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);

		Object s = selection.getFirstElement();
		
		Label label = new Label(container, SWT.NULL);
		label.setText("&Package name:");

		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fileText.setLayoutData(gd);
		fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		initialize();
		dialogChanged();
		setControl(container);
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1)
				return;
			Object obj = ssel.getFirstElement();
		}
		fileText.setText("pack");
		fileText.setSelection(0, "pack".length());
	}


	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {
		String fileName = fileText.getText();

		if (fileName.length() == 0) {
			updateStatus("Package name must be specified");
			return;
		}
		if (!fileName.matches("[a-zA-Z]+(\\.[a-zA-Z]+)*")) {
			updateStatus("Package name must be valid");
			return;
		}
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String[] getPath() {
		return fileText.getText().split("\\.");
	}
}