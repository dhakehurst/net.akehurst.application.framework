package net.akehurst.application.framework.technology.guiInterface.elements;

public interface IGuiMenuItem extends IGuiElement {

	interface EventSelected {
		void execute();
	}

	void onSelected(EventSelected event);

	interface EventVisibleWhen {
		boolean execute(Object context);
	}

	void visibleWhen(final EventVisibleWhen event);

	interface EventEnabledWhen {
		boolean execute(Object context);
	}

	void enabledWhen(final EventEnabledWhen event);
}
