package net.allochie.vm.rssl.runtime;

public class VMUserCodeException extends VMException {

	/**
	 *
	 */
	private static final long serialVersionUID = 4732001427163504471L;

	public VMUserCodeException(Object what, String reason) {
		super(what, reason);
	}

	public VMUserCodeException(Object what, String reason, Throwable t) {
		super(what, reason, t);
	}

}
