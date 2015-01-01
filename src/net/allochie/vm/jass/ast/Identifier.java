package net.allochie.vm.jass.ast;

import java.util.HashMap;

import net.allochie.vm.jass.parser.Token;

public class Identifier {

	public static HashMap<String, Identifier> map = new HashMap<String, Identifier>();

	public final String image;

	private Identifier(String image) {
		this.image = image;
	}

	public static Identifier fromToken(Token identoken) {
		if (!map.containsKey(identoken.image))
			map.put(identoken.image, new Identifier(identoken.image));
		return map.get(identoken.image);
	}

	
	@Override
	public String toString() {
		return image;
	}
}
