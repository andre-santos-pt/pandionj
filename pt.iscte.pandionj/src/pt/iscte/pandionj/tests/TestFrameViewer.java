package pt.iscte.pandionj.tests;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import pt.iscte.pandionj.FrameViewer;
import pt.iscte.pandionj.extensibility.IVariableModel.Role;
import pt.iscte.pandionj.tests.mock.MockArray;
import pt.iscte.pandionj.tests.mock.MockReference;
import pt.iscte.pandionj.tests.mock.MockStackFrame;
import pt.iscte.pandionj.tests.mock.MockValue;

public class TestFrameViewer {
	public static void main(String[] args) {
		Shell shell = new Shell(new Display());
		shell.setSize(1200, 500);
		shell.setLayout(new FillLayout());
		shell.setLocation(100, 150);

		MockStackFrame frame = new MockStackFrame();
		frame.add(new MockValue("int", "a", Role.NONE, 7, false));


		MockArray array = new MockArray("int", 1,2,3,4,5);

		MockReference ref = new MockReference("int[]", "r", array, false);
		frame.add(ref);

		frame.add(new MockValue("String", "s", Role.NONE, 7, false));
		new FrameViewer(shell).setModel(frame, (v) -> true);
		

		Display display = shell.getDisplay();
		shell.open();
		while (!shell.isDisposed()) {
			while (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
}

