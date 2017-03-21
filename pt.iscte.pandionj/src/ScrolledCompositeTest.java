import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/**
 * This class demonstrates ScrolledComposite
 */
public class ScrolledCompositeTest {
  public void run() {
    Display display = new Display();
    Shell shell = new Shell(display);
    createContents(shell);
    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
    display.dispose();
  }

  private void createContents(Composite parent) {
    parent.setLayout(new FillLayout());

    // Create the ScrolledComposite to scroll horizontally and vertically
    ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL
        | SWT.V_SCROLL);

    // Create a child composite to hold the controls
    Composite child = new Composite(sc, SWT.NONE);
    child.setLayout(new FillLayout());

    // Create the buttons
    new Button(child, SWT.PUSH).setText("One");
    new Button(child, SWT.PUSH).setText("Two");
    /*
     * // Set the absolute size of the child child.setSize(400, 400);
     */
    // Set the child as the scrolled content of the ScrolledComposite
    sc.setContent(child);

    // Set the minimum size
    sc.setMinSize(400, 400);

    // Expand both horizontally and vertically
    sc.setExpandHorizontal(true);
    sc.setExpandVertical(true);
  }

  public static void main(String[] args) {
    new ScrolledCompositeTest().run();
  }
}