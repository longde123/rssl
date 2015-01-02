package net.allochie.vm.jass.ast;

import java.util.HashMap;

import net.allochie.vm.jass.parser.Token;

public class Identifier {

	public static HashMap<String, Identifier> map = new HashMap<String, Identifier>();

	/** Identifier image value */
	public final String image;

	private Identifier(String image) {
		this.image = image;
	}

	public static Identifier fromToken(Token identoken) {
		return fromString(identoken.image);
	}

	public static Identifier fromString(String name) {
		if (!map.containsKey(name))
			map.put(name, new Identifier(name));
		return map.get(name);
	}

	@Override
	public String toString() {
		return image;
	}
}
