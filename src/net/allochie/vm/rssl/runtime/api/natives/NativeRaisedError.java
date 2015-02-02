package net.allochie.vm.rssl.runtime.api.natives;

import net.allochie.vm.rssl.runtime.VMUserCodeException;

public class NativeRaisedError extends VMUserCodeException {

	/**
	 *
	 */
	private static final long serialVersionUID = -8539312282997579071L;

	public NativeRaisedError(String reason) {
		super(null, reason);
	}

}
