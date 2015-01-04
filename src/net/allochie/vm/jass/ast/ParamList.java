package net.allochie.vm.jass.ast;

import java.util.ArrayList;

public class ParamList extends ArrayList<Param> {

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
