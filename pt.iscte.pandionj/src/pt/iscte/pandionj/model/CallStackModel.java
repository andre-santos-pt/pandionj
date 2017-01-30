package pt.iscte.pandionj.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.model.IStackFrame;



public class CallStackModel extends Observable {

	private static class Node {
		final Node parent;
		final StackFrameModel frame;
		List<Node> children;

		Node(IStackFrame frame) {
			this(null, frame);
		}

		Node(Node parent, IStackFrame frame) {
			assert frame != null;
			this.parent = parent;
			this.frame = new StackFrameModel(frame);
			children = Collections.emptyList();
		}

		boolean isRoot() {
			return parent == null;
		}

		Node addChild(IStackFrame child) {
			if(children.isEmpty())
				children = new ArrayList<>();
			else {
				for(Node n : children)
					if(n.frame.getStackFrame() == child)
						return n;
			}
			Node n = new Node(this, child);
			children.add(n);
			return n;
		}

		int getDepth() {
			Node n = this;
			int d = 1;
			while(n != null) {
				n = n.parent;
				d++;
			}
			return d;
		}

		void update() {
			Node n = this;
			while(n != null) {
				n.frame.update();
				n = n.parent;
			}
		}
	}


	private Node root;
	private Node current;


	public CallStackModel() {
		root = null;
		current = null;
	}


	public void handle(IStackFrame[] stack) {
		assert stack != null;

		for(IStackFrame f : stack) {
			if(!(f.getLaunch().getSourceLocator().getSourceElement(f) instanceof IFile)) {
				//				try {
				//					f.getDebugTarget().resume();
				//				} catch (DebugException e) {
				//					e.printStackTrace();
				//				}
				return;
			}

			IFile srcFile = (IFile) f.getLaunch().getSourceLocator().getSourceElement(f);
//			System.out.println(f + "   " + f.getThread() + "   " + srcFile + "  " + srcFile.getProject());
		}
		boolean notify = false;

		if(stack.length == 0) {
			if(root != null)
				notify = true;
			root = null;
			current = null;
		}
		else {
			if(root == null || root.frame.getStackFrame() !=  stack[stack.length-1]) {
				root = new Node(stack[stack.length-1]);
				current = root;
				notify = true;
			}
			else {
				Node n = root;
				for(int i = stack.length-2; i >= 0; i--) {
					n = n.addChild(stack[i]);	
				}
				if(current != n)
					notify = true;

				current = n;
			}
		}
		if(notify) {
			setChanged();
			notifyObservers();
		}
	}

	public StackFrameModel getTopFrame() {
		assert current != null;
		return current.frame;
	}

	public int getSize() {
		return current == null ? 0 : current.getDepth();
	}

	public void update() {
		assert current != null;
		current.update();
	}

	public List<StackFrameModel> getStackPath() {
		List<StackFrameModel> path = new ArrayList<>();
		Node n = current;
		while(n != null) {
			path.add(0, n.frame);
			n = n.parent;
		}
		return path;
	}

	public void simulateGC() {
		if(root != null)
			simulateGC(root);
	}

	private void simulateGC(Node n) {
		n.frame.simulateGC();
		for(Node c : n.children)
			simulateGC(c);
	}
}
