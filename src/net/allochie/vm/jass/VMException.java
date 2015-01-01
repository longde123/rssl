package net.allochie.vm.jass;

public class VMException extends Exception {

	public VMException(String reason) {
		super(reason);
	}

	public VMException(String reason, Throwable t) {
		super(reason, t);
	}

}
