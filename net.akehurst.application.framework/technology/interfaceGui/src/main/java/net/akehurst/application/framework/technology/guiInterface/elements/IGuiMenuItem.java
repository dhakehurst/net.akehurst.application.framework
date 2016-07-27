package net.akehurst.application.framework.technology.guiInterface.elements;

public interface IGuiMenuItem extends IGuiElement {

	interface EventSelected {
		void execute();
	}
	void onSelected(EventSelected event);
	
}
