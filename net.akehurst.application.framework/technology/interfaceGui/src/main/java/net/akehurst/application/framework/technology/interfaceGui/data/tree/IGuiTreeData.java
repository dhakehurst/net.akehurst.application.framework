package net.akehurst.application.framework.technology.guiInterface.data.tree;

import java.util.List;

public interface IGuiTreeData<T> {

	T root();

	boolean isLeaf(T item);

	List<T> children(T item);

	String style(T item);

	String label(T item);
}
