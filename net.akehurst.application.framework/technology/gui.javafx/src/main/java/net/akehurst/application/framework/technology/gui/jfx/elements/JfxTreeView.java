package net.akehurst.application.framework.technology.gui.jfx.elements;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import net.akehurst.application.framework.technology.guiInterface.data.tree.IGuiTreeData;
import net.akehurst.application.framework.technology.guiInterface.data.tree.IGuiTreeView;

public class JfxTreeView<T> implements IGuiTreeView<T> {

	public JfxTreeView(final TreeView<T> jfx) {
		this.jfx = jfx;

	}

	TreeView<T> jfx;

	@Override
	public void setData(final IGuiTreeData<T> treeData) {
		Platform.runLater(() -> {
			// this.jfx.setCellFactory(param -> {
			// final TextFieldTreeCell<T> tc = new TextFieldTreeCell<T>() {
			// @Override
			// public void updateItem(final T item, final boolean empty) {
			// if (null != item) {
			// this.getStyleClass().add(treeData.style(item));
			// this.setText(treeData.label(item));
			// } else {
			// final int i = 0;
			// }
			// }
			// };
			//
			// return tc;
			// });
			final TreeItem<T> root = this.build(treeData, treeData.root());
			root.setExpanded(true);
			this.jfx.setRoot(root);
		});
	}

	TreeItem<T> build(final IGuiTreeData<T> treeData, final T value) {
		final TreeItem<T> ti = new TreeItem<T>(value) {

			@Override
			public boolean isLeaf() {
				return treeData.isLeaf(this.getValue());
			}

			@Override
			public ObservableList<TreeItem<T>> getChildren() {
				final List<T> list = treeData.children(this.getValue());
				if (null != list) {
					// remove from current children anything not in the list
					final List<TreeItem<T>> toRemove = new ArrayList<>();
					for (final TreeItem<T> ti : super.getChildren()) {
						if (list.contains(ti.getValue())) {
							// ok
						} else {
							toRemove.add(ti);
						}
					}
					super.getChildren().removeAll(toRemove);
					// add to current children things in the list but not in children
					for (final T tc : list) {

						if (super.getChildren().stream().anyMatch(el -> tc.equals(el.getValue()))) {

						} else {
							super.getChildren().add(JfxTreeView.this.build(treeData, tc));
						}
					}
				}
				return super.getChildren();
			}

		};
		ti.setExpanded(false);
		return ti;
	}

}
