package pt.iscte.pandionj.figures;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.debug.core.DebugException;
import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.Button;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.zest.core.widgets.Graph;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.model.ObjectModel;
import pt.iscte.pandionj.parser.MethodInfo;

public class ObjectFigure extends Figure {
	private Graph graph;
	private ObjectModel model;
	private Map<String, Label> fieldLabels;
	private Label label;

	public ObjectFigure(ObjectModel model, Graph graph) {
		this.model = model;
		this.graph = graph;
		GridLayout layout = new GridLayout(1, false);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		setLayoutManager(layout);
		RoundedRectangle fig = new RoundedRectangle();
		fig.setCornerDimensions(new Dimension(10, 10));
		fig.setLayoutManager(layout);
		fig.setBorder(new MarginBorder(Constants.OBJECT_PADDING));
		fig.setBackgroundColor(Constants.OBJECT_COLOR);

		fieldLabels = new HashMap<String, Label>();
		//		for (String f : model.getFields()) {
		//			Label label = new Label(f + " = " + model.getValue(f));
		//			fig.add(label);
		//			fieldLabels.put(f, label);
		//		}

		label = new Label();
		label.setFont(new Font(null, Constants.FONT_FACE, Constants.VALUE_FONT_SIZE, SWT.NONE));
		fig.add(label);
		add(fig);


		setBorder(new MarginBorder(Constants.OBJECT_PADDING));
		//		setBorder(new LineBorder(ColorConstants.black, Constants.ARROW_LINE_WIDTH));
		setOpaque(false);
		setSize(-1, -1);


		//		setPreferredSize(Constants.POSITION_WIDTH, Math.max(Constants.POSITION_WIDTH, model.getFields().size()*30));

		//		model.addObserver(new Observer() {
		//			
		//			@Override
		//			public void update(Observable o, Object arg) {
		//				String name = (String) arg;
		//				Display.getDefault().syncExec(() -> {
		//					fieldLabels.get(name).setText(name + " = " + model.getValue(name));
		//				});
		//			}
		//		});

		model.registerObserver(new Observer() {
			public void update(Observable o, Object arg) {
				Display.getDefault().asyncExec(() -> {
					label.setText(model.toStringValue());
				});
			}
		});

		try {
			setToolTip(new Label(":" + model.getContent().getJavaType().getName()+ "\n" + "FIELD VALUES"));
		} catch (DebugException e) {
			e.printStackTrace();
		}
		label.setText(model.toStringValue());

		Iterator<MethodInfo> methods = model.getMethods();

		while(methods.hasNext()) {
			Figure methodFig = new Figure();
			methodFig.setLayoutManager(new FlowLayout());
			MethodInfo m = methods.next();
			Button but = new Button(m.getName());
			Constants.SET_FONT(but, Constants.BUTTON_FONT_SIZE);
			but.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					Rectangle r = but.getBounds().getCopy();
				    org.eclipse.swt.graphics.Point p = graph.toDisplay(r.x,r.y);
					IJavaValue ret = null;
					if(m.getNumberOfParameters() == 0)
						ret = model.invoke(m);
					else {
						ParamsDialog prompt = new ParamsDialog(Display.getDefault().getActiveShell(), p.x + r.width, p.y, m.getName());
						prompt.open();
						IJavaDebugTarget debugTarget = (IJavaDebugTarget) model.getStackFrame().getStackFrame().getDebugTarget();
						
						ret = model.invoke(m, debugTarget.newValue(Integer.parseInt(prompt.getValue())));
					}
					System.out.println("!!! " + ret);
					if(ret instanceof IJavaObject)
						model.getStackFrame().getObject((IJavaObject) ret, true);
					else if(ret instanceof IJavaPrimitiveValue )
						try {
							MessageDialog.openInformation(Display.getDefault().getActiveShell(), m.getName(), ret.getValueString());
						} catch (DebugException e1) {
							e1.printStackTrace();
						}
				}
			});
			methodFig.add(but);
			fig.add(methodFig);
		}

		setPreferredSize(getPreferredSize().expand(Constants.OBJECT_PADDING, Constants.OBJECT_PADDING));


	}
	
	private class ParamsDialog {
		Shell shell;
		Text text;
		String value;
		public ParamsDialog(Shell parent, int x, int y, String methodName) {
			shell = new Shell(parent, SWT.PRIMARY_MODAL);
			shell.setText(methodName);
			shell.setLayout(new FillLayout());

			text = new org.eclipse.swt.widgets.Text(shell, SWT.BORDER);
			text.setText("!!!");
			text.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if(e.keyCode == SWT.CR) {
						value = text.getText();
						shell.close();
					}
				}
			});
			shell.setLocation(x, y);
			
			shell.pack();
		}

		public void open() {
			shell.open();
			while(!shell.isDisposed())
				if(!shell.getDisplay().readAndDispatch())
					shell.getDisplay().sleep();
		}
		public String getValue() {
			return value;
		}


	}
	//	private void updateSize() {
	//		Dimension textExtents = TextUtilities.INSTANCE.getTextExtents(label.getText(), label.getFont());
	//		System.out.println(getPreferredSize());
	//		setPreferredSize(textExtents.expand(Constants.OBJECT_PADDING, Constants.OBJECT_PADDING));
	//		layout();
	//	}


}
