package net.allochie.vm.rssl.natives;

import net.allochie.vm.rssl.RSSLThread;
import net.allochie.vm.rssl.VMCallFrame;
import net.allochie.vm.rssl.VMException;
import net.allochie.vm.rssl.VMStackFrame;
import net.allochie.vm.rssl.VMUserCodeException;
import net.allochie.vm.rssl.global.Callout;
import net.allochie.vm.rssl.global.NativeMethod;

public class NativeUtils {

	@NativeMethod(name = "LastError")
	public static String lastError(Callout c) throws VMException {
		RSSLThread what = c.thread;
		for (int i = what.callStack.size() - 1; i >= 0; i--) {
			VMStackFrame frame = what.callStack.get(i);
			if (frame != null && frame instanceof VMCallFrame) {
				VMCallFrame box = (VMCallFrame) frame;
				VMUserCodeException ex = box.getException();
				if (ex != null)
					return ex.findCodePoint() + ": " + ex.getMessage();

			}
		}
		return "";
	}
}
