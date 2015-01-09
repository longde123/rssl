package net.allochie.vm.jass.natives;

import net.allochie.vm.jass.JASSThread;
import net.allochie.vm.jass.VMCallFrame;
import net.allochie.vm.jass.VMException;
import net.allochie.vm.jass.VMStackFrame;
import net.allochie.vm.jass.VMUserCodeException;
import net.allochie.vm.jass.global.Callout;
import net.allochie.vm.jass.global.NativeMethod;

public class NativeUtils {

	@NativeMethod(name = "LastError")
	public static String lastError(Callout c) throws VMException {
		JASSThread what = c.thread;
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
