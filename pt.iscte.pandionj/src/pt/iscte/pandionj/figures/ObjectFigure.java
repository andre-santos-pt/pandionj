package pt.iscte.pandionj.figures;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
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
import pt.iscte.pandionj.model.PrimitiveType;
import pt.iscte.pandionj.model.RuntimeModel;

public class ObjectFigure extends PandionJFigure<IObjectModel> {
	
	// TODO refactor to constants
	private static final GridData COLLAPSE = new GridData(0, 0);
	private static final GridData CLOSE = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
	static {
		CLOSE.heightHint = 5;
	}
	private static final GridData RIGHT_ALIGN = new GridData(SWT.RIGHT, SWT.FILL, true, false);
	private static final GridData FILL = new GridData(SWT.FILL, SWT.FILL, true, true);
	
	
	private RoundedRectangle fig;
	private StackContainer stack;
	private ObjectContainer objectContainer;
	private List<MethodWidget> methodWidgets;
	private RuntimeViewer runtimeViewer;
	private FigureProvider figureProvider;
	private FieldsContainer fieldsContainer;
	private Figure methodsFig;
	private boolean methodsEnabled;

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
//					getLayoutManager().layout(ObjectFigure.this);
				}
				else if(e.type == IRuntimeModel.Event.Type.TERMINATION) {
					methodsEnabled = false;
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
		methodsEnabled = enabled;
		if(methodsFig != null) {
			for(Object c : methodsFig.getChildren()) {
				MethodWidget w = (MethodWidget) c;
				w.setEnabled(enabled);
				w.clear();
			}
			methodsFig.invalidate();
		}
	}

	
	
	class FieldsContainer extends Figure {
		Figure visibleFields;
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
			if(!hiddenFields.getChildren().isEmpty())
				getLayoutManager().setConstraint(hiddenFields, show ? new GridData(SWT.FILL, SWT.DEFAULT, true, true) : CLOSE);

			for (IVariableModel<?> var : model.getFields()) {
				if(var instanceof IReferenceModel && !var.isVisible())
					runtimeViewer.showPointer((IReferenceModel) var, show);
			}
			fig.getLayoutManager().layout(fig);
		}

		void addFields(IObjectModel model, FigureProvider figureProvider) {
			visibleFields = new Figure();
			visibleFields.setLayoutManager(new GridLayout(1, false));

			hiddenFields = new Figure();
			hiddenFields.setOpaque(true);
			hiddenFields.setBackgroundColor(ColorConstants.lightGray);
			hiddenFields.setLayoutManager(new GridLayout(1, false));
//			getLayoutManager().setConstraint(hiddenFields, new GridData(SWT.FILL, SWT.DEFAULT, true, false));
			
			add(visibleFields);
			add(hiddenFields);

//			hiddenFields.addMouseListener(new MouseListener() {
//				public void mouseReleased(MouseEvent me) {}
//				public void mousePressed(MouseEvent me) {}
//				public void mouseDoubleClicked(MouseEvent me) {
//					visibilityOpen = !visibilityOpen;
//					showPrivateFields(visibilityOpen);
//				}
//			});
			
			List<IVariableModel<?>> fields = model.getFields();
			for(IVariableModel<?> v : fields) {
				PandionJFigure<?> fieldFig = figureProvider.getFigure(v, false);
				
				if(!v.isVisible()) {
					hiddenFields.add(fieldFig);
					hiddenFields.getLayoutManager().setConstraint(fieldFig, RIGHT_ALIGN);
				}
				else {
					visibleFields.add(fieldFig);
					visibleFields.getLayoutManager().setConstraint(fieldFig, RIGHT_ALIGN);
				}
				if(v instanceof IReferenceModel) {
					IReferenceModel ref = (IReferenceModel) v;
					IEntityModel target = ref.getModelTarget();
					PandionJFigure<?> targetFig = null;
					if(!target.isNull())
						targetFig = objectContainer.addObject(target);
					RuntimeViewer.getInstance().addPointer(ref, (ReferenceFigure) fieldFig, targetFig, objectContainer, this);
					objectContainer.updateIllustration(ref, null);
				}
			}
			if(hiddenFields.getChildren().isEmpty())
				getLayoutManager().setConstraint(hiddenFields, COLLAPSE);
			
			Dimension size = hiddenFields.getPreferredSize();
			dim = new GridData(size.width, size.height);
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

		MethodWidget(IMethod method) {
			setLayoutManager(new FlowLayout());
			button = new Button(shortSig(method));
			button.setToolTip(new Label(longSig(method)));
			FontManager.setFont(button, Constants.BUTTON_FONT_SIZE);
			button.setEnabled(methodsEnabled);
			add(button);
			resultLabel = new Label();
			add(resultLabel);
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					invoke(model, method, resultLabel);
				}
			});
		}

		private String shortSig(IMethod method) {
			return method.getElementName() + (method.getNumberOfParameters() == 0 ? "()" : "(...)");
		}

		private String longSig(IMethod method) {
			try {
				return method.getElementName() + "(" + String.join(", ", method.getParameterNames()) + ")";
			} catch (JavaModelException e) {
				return method.getElementName() + "(...)";
			}
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
		for(IMethod m : model.getVisibleMethods()) {
			MethodWidget w = new MethodWidget(m);
			methodWidgets.add(w);
			methodsFig.add(w);
		}
		fig.add(methodsFig);
		RuntimeViewer.getInstance().updateLayout();
	}


	private static String[] NO_PARAMS = new String[0];
	
	private void invoke(IObjectModel model, IMethod m, Label resultLabel) {

		PandionJUI.InvocationAction action = new PandionJUI.InvocationAction() {
			public void invoke(String expression, String[] paramValues) {
				model.invoke(m.getElementName(), new InvocationResult() {
					public void valueReturn(Object o) {
						PandionJUI.executeUpdate(() -> {
							for(MethodWidget w : methodWidgets)
								w.clear();
							try {
								if(PrimitiveType.isPrimitive(Signature.getSignatureSimpleName(m.getReturnType()))) {
									String val = o.toString();
									if(m.getReturnType().equals("char"))
										val = "'" + val + "'";
									resultLabel.setText(" = " + val);
								}
								else if(!m.getReturnType().equals("V")) {
									RuntimeViewer.getInstance().addObject((IEntityModel) o);
								}
							} catch (JavaModelException e) {
								e.printStackTrace();
							}
							getModel().getRuntimeModel().evaluationNotify();
						});
					}
				}, paramValues);
			}
		};
		
		if(m.getNumberOfParameters() != 0)
			PandionJUI.openInvocation(m, action);
		else
			action.invoke("", NO_PARAMS);
	}

	public ConnectionAnchor getIncommingAnchor() {
		// TODO fig.getInsets()
		return new ChopboxAnchor(fig);
	}
}
