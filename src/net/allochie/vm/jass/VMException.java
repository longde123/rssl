package net.allochie.vm.jass;

import java.lang.reflect.Field;

import net.allochie.vm.jass.ast.CodePlace;

public class VMException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = -4576283011705444688L;
	public Object what;

	public VMException(Object what, String reason) {
		super(reason);
		this.what = what;
	}

	public VMException(Object what, String reason, Throwable t) {
		super(reason, t);
		this.what = what;
	}

	@Override
	public String toString() {
		StringBuilder what = new StringBuilder();
		what.append(getClass().getSimpleName()).append(": ").append(getMessage()).append("\n");
		if (this.what != null) {
			what.append("\t").append("(failed operation: ").append(this.what.getClass().getSimpleName()).append(")\n");
			try {
				Class<?> thing = this.what.getClass();
				for (Field f : thing.getFields())
					if (f.getType().equals(CodePlace.class)) {
						f.setAccessible(true);
						CodePlace place = (CodePlace) f.get(this.what);
						if (place != null) {
							what.append("\t").append(f.getName()).append(": ");
							what.append(place).append("\n");
						} else {
							what.append("\t").append(f.getName()).append(": ");
							what.append("<no trace value>").append("\n");
						}
					}
			} catch (Throwable t) {
				what.append("No debug info available.");
			}
		} else
			what.append("\tno trace info found");
		return what.toString();
	}

	public String generateMessage(JASSMachine jassMachine, JASSThread thread) {
		StringBuilder result = new StringBuilder();
		result.append(toString());
		result.append("\t").append("in thread: ").append(thread.name).append("\n");
		result.append("\t").append("in machine: ").append(jassMachine.getName()).append("\n");
		return result.toString();
	}
}
