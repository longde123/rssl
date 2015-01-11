package net.allochie.vm.rssl.ast;

import java.util.ArrayList;

import net.allochie.vm.rssl.ast.dec.VarDec;

public class VarList extends ArrayList<VarDec> {

	/**
	 *
	 */
	private static final long serialVersionUID = -2743713924429685303L;

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
