package net.allochie.vm.rssl.ast.constant;

import java.util.HashMap;

import net.allochie.vm.rssl.ast.CodePlace;
import net.allochie.vm.rssl.parser.ParseException;
import net.allochie.vm.rssl.parser.Token;

public class RealConst extends Constant {

	private static HashMap<Double, RealConst> map = new HashMap<Double, RealConst>();

	public final double identity;

	private RealConst(Double identity) {
		this.identity = identity;
	}

	public static RealConst fromToken(Token realtoken, CodePlace place) throws ParseException {
		try {
			Double val = Double.parseDouble(realtoken.image);
			if (!map.containsKey(val))
				map.put(val, new RealConst(val));
			return map.get(val);
		} catch (Throwable t) {
			throw new ParseException("Invalid number format at line " + place.line + ", column " + place.column);
		}
	}

	@Override
	public String toString() {
		return Double.toString(identity);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof RealConst))
			return false;
		return ((RealConst) o).identity == identity;
	}

}
