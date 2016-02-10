package net.akehurst.application.framework.os;

abstract
public class AbstractActiveObject implements IActiveObject {
	
	public AbstractActiveObject(String id) {
		this.id = id;
	}
	
	String id;
	public String afId() {
		return this.id;
	}
	
	Thread thread;

	@Override
	public void afStart() {
		this.thread = new Thread((() -> this.afRun()), this.getClass().getSimpleName());
		this.thread.setName(this.afId());
		this.thread.start();
	}
	
	@Override
	public void afJoin() throws InterruptedException {
		this.thread.join();
	}
	
	
	@Override
	public int hashCode() {
		return this.afId().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IActiveObject) {
			IActiveObject other = (IActiveObject)obj;
			return this.afId().equals(other.afId());
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return this.afId();
	}
	
}
