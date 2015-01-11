package net.allochie.vm.rssl;

import java.lang.reflect.Field;

import net.allochie.vm.rssl.ast.CodePlace;

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
		if (this.what != null) {
			what.append("\t").append("(failed operation: ").append(this.what.getClass().getSimpleName()).append(")\n");
			what.append(findCodePoint());
		}
		what.append(getClass().getSimpleName()).append(": ").append(getMessage()).append("\n");
		return what.toString();
	}

	public String generateMessage(RSSLMachine jassMachine, RSSLThread thread) {
		StringBuilder result = new StringBuilder();
		result.append(toString());
		result.append("\t").append("in thread: ").append(thread.name).append("\n");
		result.append("\t").append("in machine: ").append(jassMachine.getName()).append("\n");
		return result.toString();
	}

	public String findCodePoint() {
		if (this.what != null) {
			try {
				Class<?> thing = this.what.getClass();
				String place = "unknown, ??:??";
				for (Field f : thing.getFields()) {
					if (f.getType().equals(CodePlace.class)) {
						f.setAccessible(true);
						CodePlace what = (CodePlace) f.get(this.what);
						place = f.getName() + ", " + what;
						if (f.getName().equals("where"))
							return place;
					}
				}
				return place;
			} catch (Throwable t) {
				return "unknown, ??:??";
			}
		} else
			return "unknown, ??:??";
	}
}
