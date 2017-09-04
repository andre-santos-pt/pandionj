package pt.iscte.pandionj.figures;

import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.swt.SWT;

import pt.iscte.pandionj.Constants;
import pt.iscte.pandionj.ExceptionType;
import pt.iscte.pandionj.FigureProvider;
import pt.iscte.pandionj.RuntimeViewer;
import pt.iscte.pandionj.Utils;
import pt.iscte.pandionj.extensibility.IArrayModel;
import pt.iscte.pandionj.extensibility.IEntityModel;
import pt.iscte.pandionj.extensibility.IReferenceModel;
import pt.iscte.pandionj.extensibility.PandionJUI;
import pt.iscte.pandionj.figures.PandionJFigure.Extension;
import pt.iscte.pandionj.model.ModelObserver;
import pt.iscte.pandionj.model.RuntimeModel;

public class ObjectContainer extends Figure {

	class Container2d extends Figure {

		Container2d(IArrayModel<?> a) {
			setOpaque(true);
			setLayoutManager(new GridLayout(1, false));

			Iterator<Integer> it = a.getValidModelIndexes(); 
			while(it.hasNext()) {
				add(new Label());
				it.next();
			}
		}

		public void addAt(int index, IFigure figure) {
			int i = Math.min(index, Constants.ARRAY_LENGTH_LIMIT-1);
			List children = getChildren();
			remove((IFigure) children.get(i)); 
			add(figure, i);
		}
	}


	FigureProvider figProvider;
	
	public ObjectContainer() {
		setBackgroundColor(ColorConstants.white);
		setOpaque(true);
		setLayoutManager(new GridLayout(1, true));
	}

	public void setFigProvider(FigureProvider figProvider) {
		this.figProvider = figProvider;
	}

	void setInput(RuntimeModel model) {
		PandionJUI.executeUpdate(() -> removeAll());
		model.registerDisplayObserver((e) -> {
			if(e.type == RuntimeModel.Event.Type.NEW_STACK)
				removeAll();
			else if(e.type == RuntimeModel.Event.Type.NEW_OBJECT)
				addObject((IEntityModel) e.arg);
		});
	}

	public PandionJFigure<?> addObject(IEntityModel e) {
		PandionJFigure<?> fig = figProvider.getFigure(e);
		if(!containsChild(fig)) {
			if(e instanceof IArrayModel && ((IArrayModel<?>) e).isReferenceType() && fig instanceof ArrayReferenceFigure) {
				Extension ext = new Extension(fig, e);
				GridLayout gridLayout = new GridLayout(2, false);
				gridLayout.horizontalSpacing = Constants.STACK_TO_OBJECTS_GAP;
				ext.setLayoutManager(gridLayout);
				org.eclipse.draw2d.GridData alignTop = new org.eclipse.draw2d.GridData(SWT.LEFT, SWT.TOP, false, false);
				gridLayout.setConstraint(fig, alignTop);

				Container2d container2d = new Container2d((IArrayModel<?>) e);
				ext.add(container2d);
				//					gridLayout.setConstraint(container2d, alignTop);

				IArrayModel<IReferenceModel> a = (IArrayModel<IReferenceModel>) e;
				Iterator<Integer> it = a.getValidModelIndexes();
				while(it.hasNext()) {
					Integer i = it.next();
					add2dElement(fig, a, i, container2d);
				}
				add(ext);
			}
			else {					
				add(fig);
			}
			getLayoutManager().layout(this);
		}
		return fig;
	}

	private void add2dElement(PandionJFigure<?> targetFig, IArrayModel<IReferenceModel> a, int i, Container2d container) {
		IReferenceModel e = a.getElementModel(i);
		IEntityModel eTarget = e.getModelTarget();
		PandionJFigure<?> eTargetFig = null;
		if(!eTarget.isNull()) {
			eTargetFig = figProvider.getFigure(eTarget);
			container.addAt(i, eTargetFig);
		}
		addPointer2D((ArrayReferenceFigure) targetFig, e, i, eTarget, eTargetFig, container);
	}


	private void addPointer2D(ArrayReferenceFigure figure, IReferenceModel ref, int index, IEntityModel target, PandionJFigure<?> targetFig, Container2d container) {
		PolylineConnection pointer = new PolylineConnection();
		pointer.setVisible(!target.isNull());
		pointer.setSourceAnchor(figure.getAnchor(index));
		if(target.isNull())
			pointer.setTargetAnchor(figure.getAnchor(index));
		else
			pointer.setTargetAnchor(targetFig.getIncommingAnchor());

		Utils.addArrowDecoration(pointer);
		addPointerObserver2d(ref, pointer, container, index);
		RuntimeViewer.getInstance().addPointer(ref, pointer);
	}

	private void addPointerObserver2d(IReferenceModel ref, PolylineConnection pointer, Container2d container, int index) {
		ref.registerDisplayObserver(new ModelObserver<IEntityModel>() {
			@Override
			public void update(IEntityModel arg) {
				IEntityModel target = ref.getModelTarget();
				pointer.setVisible(!target.isNull());
				if(!target.isNull()) {
					PandionJFigure<?> figure = figProvider.getFigure(target);
					container.addAt(index, figure);
					pointer.setTargetAnchor(figure.getIncommingAnchor());
					Utils.addArrowDecoration(pointer);
					container.getLayoutManager().layout(container);
				}
			}
		});
	}

	public void updateIllustration(IReferenceModel v, ExceptionType exception) {
		IEntityModel target = v.getModelTarget();
		PandionJFigure<?> fig = getDeepChild(target);
		if(fig != null) {
			if(handleIllustration(v, fig.getInnerFigure(), exception)) {
				//								xyLayout.setConstraint(fig, new Rectangle(fig.getBounds().getLocation(), fig.getPreferredSize()));
				//								xyLayout.layout(StackFrameViewer.this);
			}

			if(target instanceof IArrayModel && ((IArrayModel) target).isReferenceType()) {
				IArrayModel<IReferenceModel> a = (IArrayModel<IReferenceModel>) target;
				for (IReferenceModel e : a.getModelElements())
					updateIllustration(e, exception);
			}
		}
	}


	private boolean handleIllustration(IReferenceModel reference, IFigure targetFig, ExceptionType exception) {
		if(targetFig instanceof AbstractArrayFigure) {
			if(reference.hasIndexVars()) {
				IllustrationBorder b = new IllustrationBorder(reference, (AbstractArrayFigure<?>) targetFig, exception);
				targetFig.setBorder(b);
				return true;
			}
			else
				targetFig.setBorder(new MarginBorder(new Insets(0, Constants.POSITION_WIDTH, 20, Constants.POSITION_WIDTH))); // TODO temp margin
		}
		return false;
	} 

	private 	boolean containsChild(IFigure child) {
		for (Object object : getChildren()) {
			if(object == child)
				return true;
		}
		return false;
	}

	private PandionJFigure<?> getDeepChild(IEntityModel e) {
		return getDeepChildRef(this, e);
	}

	private PandionJFigure<?> getDeepChildRef(IFigure f, IEntityModel e) {
		for (Object object : f.getChildren()) {
			if(object instanceof PandionJFigure && ((PandionJFigure<?>) object).getModel() == e)
				return (PandionJFigure<?>) object;

			PandionJFigure<?> ret = getDeepChildRef((IFigure) object, e);
			if(ret != null)
				return ret;
		}
		return null;
	}
}