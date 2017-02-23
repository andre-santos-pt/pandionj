package pt.iscte.pandionj;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.layouts.Filter;
import org.eclipse.zest.layouts.InvalidLayoutConfiguration;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutEntity;
import org.eclipse.zest.layouts.LayoutGraph;
import org.eclipse.zest.layouts.LayoutRelationship;
import org.eclipse.zest.layouts.progress.ProgressListener;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import pt.iscte.pandionj.model.ArrayModel;
import pt.iscte.pandionj.model.ModelElement;
import pt.iscte.pandionj.model.NullModel;
import pt.iscte.pandionj.model.ObjectModel;
import pt.iscte.pandionj.model.ObjectModel.SiblingVisitor;
import pt.iscte.pandionj.model.ValueModel;
import pt.iscte.pandionj.model.ReferenceModel;

public class PandionJLayoutAlgorithm implements LayoutAlgorithm {


	// index model -> layout
	private Map<ModelElement, LayoutEntity> map = new WeakHashMap<>();
	private Set<LayoutEntity> dirty = new HashSet<>();
	private double refY = Constants.MARGIN;

	private void setLocation(LayoutEntity e, double x, double y) {
		refY = Math.max(refY, e.getYInLayout() + e.getHeightInLayout() + Constants.MARGIN);
		if(dirty.contains(e)) {
			e.setLocationInLayout(x, y);
			dirty.remove(e);
		}
	}

	@Override
	public void applyLayout(LayoutEntity[] entitiesToLayout, LayoutRelationship[] relationshipsToConsider, double x,
			double y, double width, double height, boolean asynchronous, boolean continuous)
					throws InvalidLayoutConfiguration {

		for(LayoutEntity e : entitiesToLayout) {
			GraphNode node = (GraphNode) e.getGraphData();
			ModelElement element = (ModelElement) node.getData();
			if(!map.containsKey(element)) {
				map.put(element, e);
				if(element instanceof ReferenceModel)
					element.asReference().addObserver((o,a) -> dirty.add(e));

				dirty.add(e);
			}
		}

		
		for(LayoutEntity e : entitiesToLayout) {
			GraphNode node = (GraphNode) e.getGraphData();
			ModelElement element = (ModelElement) node.getData();
			if(element instanceof ValueModel || element instanceof ReferenceModel){
				setLocation(e, x, refY);

//				refY = Math.max(refY, e.getYInLayout() + e.getHeightInLayout());

				if(element instanceof ReferenceModel) {
					ModelElement target = ((ReferenceModel) element).getTarget();
					LayoutEntity targetE = map.get(target);
					if(targetE == null) {
						System.err.println("!! " + target);
						continue;
					}

//					refY = Math.max(refY, targetE.getYInLayout() + targetE.getHeightInLayout());
					
					if(target instanceof ObjectModel) {
						ObjectModel obj = (ObjectModel) target;
						if(obj.isBinaryTree()) {
							int yy = (int) refY;
							obj.infixTraverse(new SiblingVisitor() {
								int x = Constants.NODE_SPACING/4;
								@Override
								public void accept(ModelElement object, ModelElement parent, int index, int depth, String field) {
									setLocation(map.get(object), Constants.NODE_SPACING + x, yy + depth*Constants.NODE_SPACING/2);
									x += Constants.NODE_SPACING/4;
								}
							});
						}
						else {
							class Visitor implements SiblingVisitor {
								public void accept(ModelElement o, ModelElement parent, int index, int d, String field) {
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
											if(o instanceof NullModel)
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
					else {
						int dist = target instanceof NullModel ? Constants.NODE_SPACING / 2 : Constants.NODE_SPACING;
						setLocation(targetE, e.getXInLayout() + e.getWidthInLayout() + dist, e.getYInLayout());
					}
				}
			}
		}
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
		System.out.println("ADD " + entity);
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
