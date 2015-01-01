package net.allochie.vm.jass.ast;

import java.util.ArrayList;

import net.allochie.vm.jass.ast.expression.Expression;

public class ParamInvokeList extends ArrayList<Expression> {
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
