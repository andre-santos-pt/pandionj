package pt.iscte.pandionj;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.layouts.Filter;
import org.eclipse.zest.layouts.InvalidLayoutConfiguration;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutEntity;
import org.eclipse.zest.layouts.LayoutRelationship;
import org.eclipse.zest.layouts.progress.ProgressListener;

import pt.iscte.pandionj.figures.ArrayReferenceFigure;
import pt.iscte.pandionj.figures.BaseFigure;
import pt.iscte.pandionj.model.ArrayReferenceModel;
import pt.iscte.pandionj.model.EntityModel;
import pt.iscte.pandionj.model.ModelElement;
import pt.iscte.pandionj.model.NullModel;
import pt.iscte.pandionj.model.ObjectModel;
import pt.iscte.pandionj.model.ObjectModel.SiblingVisitor;
import pt.iscte.pandionj.model.ReferenceModel;
import pt.iscte.pandionj.model.ValueModel;
import pt.iscte.pandionj.model.VariableModel;

public class PandionJLayoutAlgorithm implements LayoutAlgorithm {

	// index model -> layout
	private Map<ModelElement<?>, LayoutEntity> map = new WeakHashMap<>();
	private Set<LayoutEntity> dirty = new HashSet<>();
	private double refY = Constants.MARGIN;

	private void setLocation(LayoutEntity e, double x, double y) {
		if(dirty.contains(e)) {
			e.setLocationInLayout(x, y);
			dirty.remove(e);
		}
		refY = Math.max(refY, e.getYInLayout() + e.getHeightInLayout() + Constants.MARGIN);
//		System.out.println(((GraphNode)e.getGraphData()).getData());
//		System.out.println(e);
	}

	@Override
	public void applyLayout(LayoutEntity[] entitiesToLayout, LayoutRelationship[] relationshipsToConsider, double x,
			double y, double width, double height, boolean asynchronous, boolean continuous)
					throws InvalidLayoutConfiguration {

		for(LayoutEntity e : entitiesToLayout) {
			GraphNode node = (GraphNode) e.getGraphData();
			ModelElement<?> element = (ModelElement<?>) node.getData();
			if(!map.containsKey(element)) {
				map.put(element, e);
				
				// TODO atualizar
				if(element instanceof ReferenceModel)
					element.registerObserver((o,a) -> dirty.add(e));

				dirty.add(e);
			}
		}


		for(LayoutEntity e : entitiesToLayout) {
			GraphNode node = (GraphNode) e.getGraphData();
			ModelElement<?> element = (ModelElement<?>) node.getData();
			// TODO values on top
			
			if(element instanceof VariableModel<?>){
				setLocation(e, Constants.MARGIN + x, refY);

				if(element instanceof ReferenceModel) {
					EntityModel<?> target = ((ReferenceModel) element).getModelTarget();
					LayoutEntity targetE = map.get(target);
					if(targetE == null) {
						continue;
					}

					if(target instanceof ObjectModel) {
						ObjectModel obj = (ObjectModel) target;
						if(obj.isBinaryTree()) {
							int yy = (int) refY;
							obj.infixTraverse(new SiblingVisitor() {
								int x = Constants.NODE_SPACING/4;
								@Override
								public void visit(EntityModel<?> object, ObjectModel parent, int index, int depth, String field) {
									setLocation(map.get(object), Constants.NODE_SPACING + x, yy + depth*Constants.NODE_SPACING/2);
									x += Constants.NODE_SPACING/4;
								}
							}, true);
						}
						else {
							class Visitor implements SiblingVisitor {
								public void visit(EntityModel<?> o, ObjectModel parent, int index, int d, String field) {
									if(map.containsKey(o)) {
										LayoutEntity n = map.get(o);
										switch(index) {
										case 1:
											setLocation(n, x + d * Constants.NODE_SPACING, e.getYInLayout() + Constants.NODE_SPACING/2);
											break;
										case 2:
											setLocation(n, x + d * Constants.NODE_SPACING, e.getYInLayout() - Constants.NODE_SPACING/2);
											break;
										default:
											int dist = (d+1) * Constants.NODE_SPACING;
											if(o == null)
												dist -= Constants.NODE_SPACING / 2;
											setLocation(n, x + dist, e.getYInLayout());
										}
									}
									else
										System.err.println("not " + o);
								}
							}
							setLocation(targetE, e.getXInLayout() + Constants.NODE_SPACING, e.getYInLayout());
							Visitor v = new Visitor();
							obj.traverseSiblings(v, true);
						}
					}
					else if(target instanceof ArrayReferenceModel && !((ArrayReferenceModel) target).hasWidgetExtension()) {
						setLocation(targetE, e.getXInLayout() + e.getWidthInLayout() + Constants.NODE_SPACING, e.getYInLayout());

						List<ReferenceModel> elements = ((ArrayReferenceModel) target).getModelElements();
						double yy = targetE.getYInLayout();
						int i = 0;
						for(ReferenceModel r : elements) {
							LayoutEntity ent = map.get(r.getModelTarget());
							if(ent != null) {
								yy += ent.getHeightInLayout() + Constants.OBJECT_PADDING;
								GraphNode n = (GraphNode) targetE.getGraphData();
								IFigure iFig = ((BaseFigure) n.getNodeFigure()).innerFig;
								if(iFig instanceof ArrayReferenceFigure) {
									Point location = ((ArrayReferenceFigure) iFig).getAnchor(i++).getLocation(null);								
									ent.setLocationInLayout(targetE.getXInLayout() + Constants.NODE_SPACING, targetE.getYInLayout()+ location.y);
								}
							}
						}
					}
					else {
						int dist = target instanceof NullModel ? Constants.NODE_SPACING / 2 : Constants.NODE_SPACING;
						setLocation(targetE, e.getXInLayout() + e.getWidthInLayout() + Constants.NODE_SPACING, e.getYInLayout());
					}
				}
			}
		}
		
		// loose entities
//		for(LayoutEntity e : entitiesToLayout) {
//			GraphNode node = (GraphNode) e.getGraphData();
//			ModelElement<?> element = (ModelElement<?>) node.getData();
//
//			if(element instanceof EntityModel<?>){
//				System.out.println(element);
//				setLocation(e, Constants.MARGIN + x + Constants.OBJECT_PADDING, refY);
//			}
//		}
	}

	@Override
	public boolean isRunning() {
		return true;
	}

	@Override
	public void setComparator(Comparator comparator) {

	}

	@Override
	public void setFilter(Filter filter) {

	}

	@Override
	public void setEntityAspectRatio(double ratio) {

	}

	@Override
	public double getEntityAspectRatio() {
		return 1.0;
	}

	@Override
	public void addProgressListener(ProgressListener listener) {

	}

	@Override
	public void removeProgressListener(ProgressListener listener) {

	}

	@Override
	public void stop() {

	}

	@Override
	public void setStyle(int style) {

	}

	@Override
	public int getStyle() {
		return SWT.NONE;
	}

	@Override
	public void addEntity(LayoutEntity entity) {

	}

	@Override
	public void addRelationship(LayoutRelationship relationship) {

	}

	@Override
	public void removeEntity(LayoutEntity entity) {

	}

	@Override
	public void removeRelationship(LayoutRelationship relationship) {

	}

	@Override
	public void removeRelationships(List relationships) {

	}


}
