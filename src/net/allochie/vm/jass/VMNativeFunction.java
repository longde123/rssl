package net.allochie.vm.jass;

import java.lang.reflect.Method;

import net.allochie.vm.jass.ast.Param;
import net.allochie.vm.jass.ast.dec.NativeFuncDef;

public class VMNativeFunction extends VMFunction {

	public NativeFuncDef qd;

	public VMNativeFunction(NativeFuncDef qd) {
		super();
		this.qd = qd;
		this.sig = qd.def;
	}

	public VMValue executeNative(VMClosure closure) throws VMException {
		Object[] params = new Object[qd.def.params.size()];
		Class<?>[] args = new Class<?>[qd.def.params.size()];
		for (int i = 0; i < params.length; i++) {
			Param pq = qd.def.params.get(i);
			params[i] = closure.getVariable(pq.name).safeValue().value;
			args[i] = params[i].getClass();
		}

		try {
			Method m = getClass().getMethod(qd.def.id.image, args);
			return new VMValue(m.invoke(null, params));
		} catch (Throwable t) {
			throw new VMException("Problem calling method!", t);
		}
	}

	public static void PrintInteger(Integer i) {
		System.out.println("PrintInteger: " + i);
	}

}
