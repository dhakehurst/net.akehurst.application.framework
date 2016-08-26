package net.akehurst.application.framework.technology.guiInterface.data.tree;

import java.util.List;

public interface IGuiTreeParent<T> {

	List<IGuiTreeChild<T>> getChildren();

}
