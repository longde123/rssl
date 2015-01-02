package net.allochie.vm.jass.ast.constant;

import java.util.HashMap;

import net.allochie.vm.jass.parser.Token;

public class StringConst extends Constant {

	private static HashMap<String, StringConst> map = new HashMap<String, StringConst>();

	public final String identity;

	private StringConst(String identity) {
		this.identity = identity;
	}

	public static StringConst fromToken(Token stringtoken) {
		if (!map.containsKey(stringtoken.image))
			map.put(stringtoken.image, new StringConst(stringtoken.image));
		return map.get(stringtoken.image);
	}

	@Override
	public String toString() {
		return "\"" + identity + "\"";
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof StringConst))
			return false;
		return ((StringConst) o).identity.equals(identity);
	}

}
