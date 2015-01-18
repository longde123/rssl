package net.allochie.vm.rssl.natives;

import net.allochie.vm.rssl.VMUserCodeException;

public class NativeRaisedError extends VMUserCodeException {

	public NativeRaisedError(String reason) {
		super(null, reason);
	}

}
