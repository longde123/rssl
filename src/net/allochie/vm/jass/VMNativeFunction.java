package net.allochie.vm.jass;

import net.allochie.vm.jass.ast.Param;
import net.allochie.vm.jass.ast.dec.NativeFuncDef;
import net.allochie.vm.jass.global.Callout;
import net.allochie.vm.jass.global.NativeMethodRegistry;

public class VMNativeFunction extends VMFunction {

	public NativeFuncDef qd;

	public VMNativeFunction(NativeFuncDef qd) {
		super();
		this.qd = qd;
		this.sig = qd.def;
	}

	public VMValue executeNative(JASSMachine machine, JASSThread thread, VMClosure closure) throws VMException {
		Object[] params = new Object[1 + qd.def.params.size()];
		Class<?>[] args = new Class<?>[1 + qd.def.params.size()];
		params[0] = new Callout(machine, thread, closure);
		for (int i = 0; i < qd.def.params.size(); i++) {
			Param pq = qd.def.params.get(i);
			params[1 + i] = closure.getVariable(pq.name).safeValue().value;
		}
		for (int i = 0; i < params.length; i++)
			args[i] = params[i].getClass();

		try {
			return new VMValue(machine, NativeMethodRegistry.findAndInvokeNative(qd.def.id.image, args, params));
		} catch (Throwable t) {
			if (t instanceof VMException)
				throw (VMException) t;
			throw new VMException("Uncaught exception from native method.", t);
		}
	}
}
