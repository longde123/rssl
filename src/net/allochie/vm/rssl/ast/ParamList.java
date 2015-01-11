package net.allochie.vm.rssl.ast;

import java.util.ArrayList;

public class ParamList extends ArrayList<Param> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1299743137084275206L;

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < size(); i++) {
			result.append(get(i));
			if (i != size() - 1)
				result.append(", ");
		}
		return result.toString();
	}

}
