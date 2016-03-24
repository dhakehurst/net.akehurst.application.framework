package net.akehurst.application.framework.technology.guiInterface;

/**
 * Used for async calls that need a response
 * @author akehurst
 *
 */
public interface IGuiCallback {

	void success(Object result);
	void error(Exception ex);
	
}
