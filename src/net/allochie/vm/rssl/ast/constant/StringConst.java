package net.allochie.vm.rssl.ast.constant;

import java.util.HashMap;

import net.allochie.vm.rssl.ast.CodePlace;
import net.allochie.vm.rssl.parser.Token;

public class StringConst extends Constant {

	private static HashMap<String, StringConst> map = new HashMap<String, StringConst>();

	public final String identity;

	private StringConst(String identity) {
		this.identity = identity;
		where = new CodePlace("<system>", 0, 0);
	}

	public static StringConst fromToken(Token stringtoken, CodePlace place) {
		String what = stringtoken.image;
		what = what.substring(1, what.length() - 1);
		if (!map.containsKey(what))
			map.put(what, new StringConst(what));
		return map.get(what);
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
