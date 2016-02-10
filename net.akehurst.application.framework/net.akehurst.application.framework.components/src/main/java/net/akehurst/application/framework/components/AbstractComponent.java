package net.akehurst.application.framework.components;

import java.util.HashSet;
import java.util.Set;

import net.akehurst.application.framework.os.AbstractActiveObject;

abstract
public class AbstractComponent extends AbstractActiveObject implements IComponent {

	public AbstractComponent(String id) {
		super(id);
		this.ports = new HashSet<>();
	}

	Set<Port> ports;
	@Override
	public void afAddPort(Port value) {
		this.ports.add(value);
	}
	
	@Override
	public void afStart() {
		for(Port p: this.ports) {
			for(Class<?>interfaceType : p.getRequired()) {
				if (null==p.out(interfaceType)) {
					System.out.println("Warn: Port "+p+" has not been provided with "+interfaceType);
				} else {
					// ok
				}
			}
		}
		
		super.afStart();
	}
}
