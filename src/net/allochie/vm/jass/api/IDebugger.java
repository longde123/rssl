package net.allochie.vm.jass.api;

public interface IDebugger {
	
	public void trace(Object... args);
	
	public void fatal(Object... args);

}
