package net.allochie.vm.rssl.natives;

import net.allochie.vm.rssl.VMException;

public class NativeMethodException extends VMException {

	/**
	 *
	 */
	private static final long serialVersionUID = 3395622652388966109L;

	public NativeMethodException(String reason) {
		super(null, reason);
	}

}
