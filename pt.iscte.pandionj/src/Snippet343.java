import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class Snippet343 {

public static void main(String[] args) {
	final Display display = new Display();
	Shell shell = new Shell(display);
	shell.setLayout(new FillLayout());
	shell.setSize(400, 400);
	SashForm sashForm = new SashForm(shell, SWT.HORIZONTAL);
	Composite leftComposite = new Composite(sashForm, SWT.NONE);
	leftComposite.setLayout(new FillLayout());
	Composite rightComposite = new Composite(sashForm, SWT.NONE);
	rightComposite.setLayout(new FillLayout());
	ExpandBar expandBar = new ExpandBar(leftComposite, SWT.NONE);
	final ExpandItem expandItem1 = new ExpandItem(expandBar, SWT.NONE);
	expandItem1.setText("item 1");
	
	ExpandItem expandItem = new ExpandItem(expandBar, SWT.NONE);
	expandItem.setText("item 2"); /* expandItem2 */
	Composite cp = new Composite(expandBar, SWT.BORDER);
	cp.setBackground(new Color(null, 244, 2, 1));
	expandItem.setControl(cp);
	expandItem.setHeight(200);
	
	final StyledText text = new StyledText(expandBar, SWT.MULTI | SWT.WRAP);
	expandItem1.setControl(text);
	text.setText("initial text that will wrap if it's long enough");

	/* update the item's height if needed in response to changes in the text's size */
	final int TRIAL_WIDTH = 100;
	final int trimWidth = text.computeTrim(0, 0, TRIAL_WIDTH, 100).width - TRIAL_WIDTH;
	text.addListener(SWT.Modify, event -> {
		Point size = text.computeSize(text.getSize().x - trimWidth, SWT.DEFAULT);
		if (expandItem1.getHeight() != size.y) {
			expandItem1.setHeight(size.y);
		}
	});
	expandBar.addListener(SWT.Resize, event -> display.asyncExec(() -> {
		/*
		 * The following is done asynchronously to allow the Text's width
		 * to be changed before re-calculating its preferred height.
		 */
		if (text.isDisposed()) return;
		Point size = text.computeSize(text.getSize().x - trimWidth, SWT.DEFAULT);
		if (expandItem1.getHeight() != size.y) {
			expandItem1.setHeight(size.y);
		}
	}));

	shell.open();
	/* set the item's initial height */
	Point size = text.computeSize(expandBar.getClientArea().width, SWT.DEFAULT);
	expandItem1.setHeight(size.y);
	expandItem1.setExpanded(true);

	while (!shell.isDisposed()) {
		if (!display.readAndDispatch()) display.sleep();
	}
	display.dispose();
}

}