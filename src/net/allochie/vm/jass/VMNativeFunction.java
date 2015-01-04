package net.allochie.vm.jass;

import java.lang.reflect.Method;

import net.allochie.vm.jass.ast.Param;
import net.allochie.vm.jass.ast.dec.NativeFuncDef;
import net.allochie.vm.jass.global.NativeMethodRegistry;

public class VMNativeFunction extends VMFunction {

	public NativeFuncDef qd;

	public VMNativeFunction(NativeFuncDef qd) {
		super();
		this.qd = qd;
		this.sig = qd.def;
	}

	public VMValue executeNative(JASSMachine machine, VMClosure closure) throws VMException {
		Object[] params = new Object[2 + qd.def.params.size()];
		Class<?>[] args = new Class<?>[2 + qd.def.params.size()];
		params[0] = machine;
		params[1] = closure;
		for (int i = 0; i < qd.def.params.size(); i++) {
			Param pq = qd.def.params.get(i);
			params[2 + i] = closure.getVariable(pq.name).safeValue().value;
		}
		for (int i = 0; i < params.length; i++)
			args[i] = params[i].getClass();

		try {
			Method m = NativeMethodRegistry.findNativeMethod(qd.def.id.image);
			return new VMValue(machine, m.invoke(null, params));
		} catch (Throwable t) {
			if (t instanceof VMException)
				throw (VMException) t;
			throw new VMException("Uncaught exception from native method.", t);
		}
	}
}
