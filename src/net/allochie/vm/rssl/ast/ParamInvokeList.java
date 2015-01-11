package net.allochie.vm.rssl.ast;

import java.util.ArrayList;

import net.allochie.vm.rssl.ast.expression.Expression;

public class ParamInvokeList extends ArrayList<Expression> {
	/**
	 *
	 */
	private static final long serialVersionUID = -5349043874124784066L;

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
