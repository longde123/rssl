package net.allochie.vm.rssl.compiler;

public class RSSLCompilerException extends Exception {

	public RSSLCompilerException(String reason) {
		super(reason);
	}

	public RSSLCompilerException(String reason, Exception cause) {
		super(reason, cause);
	}

}
