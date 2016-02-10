package net.akehurst.application.framework.os;

public interface IActiveObject {

	String afId();
	
	void afRun();
	void afStart();
	void afJoin() throws InterruptedException;
	
}
