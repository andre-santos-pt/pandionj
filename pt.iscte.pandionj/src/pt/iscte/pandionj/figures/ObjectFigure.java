package pt.iscte.pandionj.figures;

import static pt.iscte.pandionj.Constants.OBJECT_PADDING;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.Button;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.FigureProvider;
import pt.iscte.pandionj.FontManager;
import pt.iscte.pandionj.ParamsDialog;
import pt.iscte.pandionj.RuntimeViewer;
import pt.iscte.pandionj.Utils;
import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IObjectModel;
import pt.iscte.pandionj.extensibility.IObjectModel.InvocationResult;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.IRuntimeModel;
import pt.iscte.pandionj.extensibility.IRuntimeModel.Event;
import pt.iscte.pandionj.extensibility.IStackFrameModel;
import pt.iscte.pandionj.extensibility.IVariableModel;
import pt.iscte.pandionj.extensibility.IVisibleMethod;
import pt.iscte.pandionj.extensibility.PandionJUI;
import pt.iscte.pandionj.model.ModelObserver;
import pt.iscte.pandionj.model.RuntimeModel;

public class ObjectFigure extends PandionJFigure<IObjectModel> {
	
	// TODO refactor to constants
	private static final GridData COLLAPSE = new GridData(0, 0);
	private static final GridData RIGHT_ALLIGN = new GridData(SWT.RIGHT, SWT.FILL, true, false);
	private static final GridData FILL = new GridData(SWT.FILL, SWT.FILL, true, true);
	
	
	private RoundedRectangle fig;
	private StackContainer stack;
	private ObjectContainer objectContainer;
	private List<MethodWidget> methodWidgets;
	private RuntimeViewer runtimeViewer;
	private FigureProvider figureProvider;
	private FieldsContainer fieldsContainer;
	private Figure methodsFig;
	private boolean terminated;

	public ObjectFigure(IObjectModel model, IFigure extensionFigure) {
		super(model, true);
		assert extensionFigure != null;

		GridLayout layout = new GridLayout(1, false);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 2;
		layout.marginHeight = 5;
		layout.marginWidth = 5;

		extensionFigure.setOpaque(true);

		fig = new RoundedRectangle();
		fig.setLayoutManager(layout);
		fig.setCornerDimensions(Constants.OBJECT_CORNER);
		fig.setBorder(new MarginBorder(Constants.OBJECT_PADDING));
		fig.setBackgroundColor(Constants.Colors.OBJECT);
		fig.setOpaque(true);
		getLayoutManager().setConstraint(fig, new GridData(SWT.DEFAULT, SWT.BEGINNING, false, false));

		fig.add(extensionFigure);
		//		fig.setToolTip(new Label(model.getTypeName()));

		runtimeViewer = RuntimeViewer.getInstance();
		figureProvider = runtimeViewer.getFigureProvider();
		objectContainer = ObjectContainer.create(false);
//		objectContainer.setOpaque(true);
//		objectContainer.setBackgroundColor(ColorConstants.yellow);
		objectContainer.setFigProvider(figureProvider);

		addShowMethodListener();

		model.getRuntimeModel().registerDisplayObserver(new ModelObserver<IRuntimeModel.Event<IStackFrameModel>>() {

			@Override
			public void update(Event<IStackFrameModel> e) {
				if(e.type == IRuntimeModel.Event.Type.REMOVE_FRAME) {
					stack.removeFrame(e.arg);
				}
				else if(e.type == RuntimeModel.Event.Type.NEW_FRAME) {
					IStackFrameModel f = e.arg;
					if(f.isInstanceFrameOf(model)) {
						stack.addFrame(f, RuntimeViewer.getInstance(), objectContainer, false);
						setObjectContainerVisible(true);
					}
				}
				else if(e.type == IRuntimeModel.Event.Type.STEP) {
					IStackFrameModel f = e.arg;
					setMethodsEnabled(!f.isInstance());
					boolean visible = f.isInstanceFrameOf(model);
					fieldsContainer.showPrivateFields(visible);
					setObjectContainerVisible(visible);
				}
				else if(e.type == IRuntimeModel.Event.Type.TERMINATION) {
					terminated = true;
					setMethodsEnabled(false);
				}
			}
		});

		add(fig);
		add(objectContainer);

		fieldsContainer = new FieldsContainer();
		fig.add(fieldsContainer);
		layout.setConstraint(fieldsContainer, new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		
		stack = new StackContainer();
		fig.add(stack);
		layout.setConstraint(stack, new GridData(SWT.FILL, SWT.DEFAULT, true, false));

		setObjectContainerVisible(false);
	}

	private void setObjectContainerVisible(boolean visible) {
		for (IVariableModel<?> var : model.getFields()) {
			if(var instanceof IReferenceModel)
				objectContainer.setVisible((IReferenceModel) var, var.isVisible() || visible);
		}
		objectContainer.setPointersVisible(visible);
		Dimension dim = objectContainer.getVisibleBounds();
		getLayoutManager().setConstraint(objectContainer, new GridData(dim.width, dim.height)); 
//		getLayoutManager().setConstraint(objectContainer, visible ? FILL : COLLAPSE);
	}
	
	
	private void setMethodsEnabled(boolean enabled) {
		if(methodsFig != null) {
			for(Object c : methodsFig.getChildren()) {
				MethodWidget w = (MethodWidget) c;
				w.setEnabled(enabled);
			}
			methodsFig.invalidate();
		}
	}

	
	
	class FieldsContainer extends Figure {
		Figure hiddenFields;
		GridData dim;
		boolean visibilityOpen;
		
		public FieldsContainer() {
			setLayoutManager(new GridLayout(1, false)); 
			addFields(model, figureProvider);
			showPrivateFields(false);
			visibilityOpen = false;
		}
		
		void showPrivateFields(boolean show) {
//			if(!show && dim == null) {
//				Dimension size = hiddenFields.getPreferredSize();
//				dim = new GridData(size.width, size.height);
//			}
			getLayoutManager().setConstraint(hiddenFields, show ? new GridData(SWT.FILL, SWT.DEFAULT, true, true) : COLLAPSE);
//			for(Figure f : hiddenLinks)
//				f.setVisible(show);
			for (IVariableModel<?> var : model.getFields()) {
				if(var instanceof IReferenceModel && !var.isVisible())
					runtimeViewer.showPointer((IReferenceModel) var, show);
			}
//			runtimeViewer.showPointers(this, true);
			fig.getLayoutManager().layout(fig);
//			invalidate();
		}

		void addFields(IObjectModel model, FigureProvider figureProvider) {
			hiddenFields = new Figure();
			hiddenFields.setOpaque(true);
			hiddenFields.setBackgroundColor(ColorConstants.lightGray);
			hiddenFields.setLayoutManager(new GridLayout(1, false));
			getLayoutManager().setConstraint(hiddenFields, new GridData(SWT.FILL, SWT.DEFAULT, true, false));
			
//			hiddenFields.addMouseListener(new MouseListener() {
//				public void mouseReleased(MouseEvent me) {}
//				public void mousePressed(MouseEvent me) {}
//				public void mouseDoubleClicked(MouseEvent me) {
//					visibilityOpen = !visibilityOpen;
//					showPrivateFields(visibilityOpen);
//				}
//			});
			add(hiddenFields);
			
			List<IVariableModel<?>> fields = model.getFields();
			for(IVariableModel<?> v : fields) {
				PandionJFigure<?> fieldFig = figureProvider.getFigure(v, false);
				
				if(!v.isVisible()) {
					hiddenFields.add(fieldFig);
					hiddenFields.getLayoutManager().setConstraint(fieldFig, RIGHT_ALLIGN);
				}
				else {
					add(fieldFig);
					getLayoutManager().setConstraint(fieldFig, RIGHT_ALLIGN);
				}
				if(v instanceof IReferenceModel) {
					IReferenceModel ref = (IReferenceModel) v;
					IEntityModel target = ref.getModelTarget();
					PandionJFigure<?> targetFig = null;
					if(!target.isNull())
						targetFig = objectContainer.addObject(target);
					addPointer((ReferenceFigure) fieldFig, ref, target, targetFig);
					objectContainer.updateIllustration(ref, null);
				}
			}
			Dimension size = hiddenFields.getPreferredSize();
			dim = new GridData(size.width, size.height);
		}

		void addPointer(ReferenceFigure figure, IReferenceModel ref, IEntityModel target, PandionJFigure<?> targetFig) {
			PolylineConnection pointer = new PolylineConnection();
			pointer.setVisible(!target.isNull());
			pointer.setSourceAnchor(figure.getAnchor());
			if(target.isNull())
				pointer.setSourceAnchor(figure.getAnchor());
			else
				pointer.setTargetAnchor(targetFig.getIncommingAnchor());
			Utils.addArrowDecoration(pointer);
			addPointerObserver(ref, pointer);
			runtimeViewer.addPointer(ref, pointer, this);
		}

		void addPointerObserver(IReferenceModel ref, PolylineConnection pointer) {
			ref.registerDisplayObserver(new ModelObserver<IEntityModel>() {
				@Override
				public void update(IEntityModel arg) {
					IEntityModel target = ref.getModelTarget();
					pointer.setVisible(!target.isNull());
					if(!target.isNull()) {
						PandionJFigure<?> targetFig = objectContainer.addObject(target);
						pointer.setTargetAnchor(targetFig.getIncommingAnchor());
						Utils.addArrowDecoration(pointer);
					}
				}
			});
		}
	}
	
	
	private void addShowMethodListener() {
		methodWidgets = new ArrayList<ObjectFigure.MethodWidget>();
		fig.addMouseListener(new MouseListener() {
			public void mouseReleased(org.eclipse.draw2d.MouseEvent me) { }
			public void mousePressed(org.eclipse.draw2d.MouseEvent me) { }
			public void mouseDoubleClicked(org.eclipse.draw2d.MouseEvent me) {
				if(methodsFig == null)
					addMethods(getModel());
				else {
					fig.remove(methodsFig);
					methodsFig = null;
				}
				invalidate();
			}
		});
	}


	private class MethodWidget extends Figure {

		Button button;
		Label resultLabel;

		MethodWidget(IVisibleMethod method) {
			setLayoutManager(new FlowLayout());
			button = new Button(method.getName() + (method.getNumberOfParameters() == 0 ? "()" : "(...)"));
			FontManager.setFont(button, Constants.BUTTON_FONT_SIZE);
			button.setEnabled(!terminated);
			add(button);
			resultLabel = new Label();
			add(resultLabel);
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					invoke(model, method, resultLabel);
				}
			});
		}

		void clear() {
			resultLabel.setText("");
		}

		@Override
		public void setEnabled(boolean value) {
			button.setEnabled(value);
		}
	}

	


	private void addMethods(IObjectModel model) {
		methodsFig = new Figure();
		methodsFig.setLayoutManager(new GridLayout(1, false));
		for(IVisibleMethod m : model.getVisibleMethods()) {
			MethodWidget w = new MethodWidget(m);
			methodWidgets.add(w);
			methodsFig.add(w);
		}
		fig.add(methodsFig);
		RuntimeViewer.getInstance().updateLayout();
	}


	private void invoke(IObjectModel model, IVisibleMethod m, Label resultLabel) {
		String[] stringValues = new String[0];
		if(m.getNumberOfParameters() != 0) {
			// TODO replace
			ParamsDialog prompt = new ParamsDialog(Display.getDefault().getActiveShell(), m);
			prompt.setLocation(100, 100);
			if(prompt.open())
				stringValues = prompt.getValues();
			else
				return;
		}
		model.invoke(m.getName(), new InvocationResult() {
			public void valueReturn(Object o) {
				PandionJUI.executeUpdate(() -> {
					for(MethodWidget w : methodWidgets)
						w.clear();
					if(m.isPrimitiveValue()) {
						String val = o.toString();
						if(m.getReturnType().equals("char"))
							val = "'" + val + "'";
						resultLabel.setText(" = " + val);
						//						MessageDialog.openInformation(null, m.getSignatureText(), o.toString());
					}
					else if(!m.getReturnType().equals("void")) {
						RuntimeViewer.getInstance().addObject((IEntityModel) o);
					}
					//					getModel().getRuntimeModel().update();
					getModel().getRuntimeModel().evaluationNotify();
				});
			}
		}, stringValues);

	}

	public ConnectionAnchor getIncommingAnchor() {
		return new ChopboxAnchor(fig);
	}





	//	private class ResultDialog {
	//		Shell shell;
	//
	//		ResultDialog(Shell parent, int x, int y, String exp) {
	//			shell = new Shell(parent, SWT.PRIMARY_MODAL);
	//			shell.setLayout(new FillLayout());
	//			shell.setBackground(ColorConstants.white);
	//			org.eclipse.swt.widgets.Label label = new org.eclipse.swt.widgets.Label(shell, SWT.BORDER);
	//			FontManager.setFont(label, Constants.BUTTON_FONT_SIZE);
	//			label.setText(exp);
	//			label.addMouseListener(new MouseAdapter() {
	//				public void mouseDown(MouseEvent e) {
	//					shell.close();
	//				}
	//			});
	//			shell.addKeyListener(new KeyAdapter() {
	//				public void keyPressed(KeyEvent e) {
	//					if(e.keyCode == SWT.CR || e.keyCode == SWT.ESC) {
	//						shell.close();
	//					}
	//				}
	//			});
	//
	//			shell.setLocation(x, y);
	//			shell.pack();
	//		}
	//
	//		void open() {
	//			shell.open();
	//			while(!shell.isDisposed())
	//				if(!shell.getDisplay().readAndDispatch())
	//					shell.getDisplay().sleep();
	//		}
	//	}
}
