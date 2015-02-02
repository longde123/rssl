package net.allochie.vm.rssl.runtime.natives;

import net.allochie.vm.rssl.runtime.RSSLThread;
import net.allochie.vm.rssl.runtime.VMException;
import net.allochie.vm.rssl.runtime.VMUserCodeException;
import net.allochie.vm.rssl.runtime.api.natives.Callout;
import net.allochie.vm.rssl.runtime.api.natives.NativeMethod;
import net.allochie.vm.rssl.runtime.frame.VMCallFrame;
import net.allochie.vm.rssl.runtime.frame.VMStackFrame;
import net.allochie.vm.rssl.runtime.value.VMType;

public class NativeUtils {

	@NativeMethod(name = "LastError")
	public static String lastError(Callout c) throws VMException {
		RSSLThread what = c.thread;
		for (int i = what.callStack.size() - 1; i >= 0; i--) {
			VMStackFrame frame = what.callStack.get(i);
			if (frame != null && frame instanceof VMCallFrame) {
				VMCallFrame box = (VMCallFrame) frame;
				VMUserCodeException ex = box.getCaughtException();
				if (ex != null)
					return ex.findCodePoint() + ": " + ex.getMessage();

			}
		}
		return "";
	}

	@NativeMethod(name = "What")
	public String getTypeof(Callout call, Object what) {
		return VMType.findType(call.machine, what).typename;
	}
}
