package net.allochie.vm.rssl.runtime.api;

public interface IDebugger {

	public void trace(Object... args);

	public void fatal(Object... args);

}
