package net.allochie.vm.rssl.natives;

import net.allochie.vm.rssl.VMUserCodeException;

public class NativeRaisedError extends VMUserCodeException {

	/**
	 *
	 */
	private static final long serialVersionUID = -8539312282997579071L;

	public NativeRaisedError(String reason) {
		super(null, reason);
	}

}
