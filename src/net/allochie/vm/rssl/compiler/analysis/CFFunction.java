package net.allochie.vm.rssl.compiler.analysis;

import java.util.ArrayList;

import net.allochie.vm.rssl.ast.Function;
import net.allochie.vm.rssl.compiler.analysis.CFGraphNode.JumpLabel;

public class CFFunction {

	public Function src;
	public CFGraphNode atop_exception_frame;
	public CFGraphNode atop_return_frame;

	public ArrayList<CFGraphNode> top_nodes;

	public CFFunction(Function src, CFGraphNode atop_exception_frame, CFGraphNode atop_return_frame,
			ArrayList<CFGraphNode> top_nodes) {
		this.src = src;
		this.atop_exception_frame = atop_exception_frame;
		this.atop_return_frame = atop_return_frame;
		this.top_nodes = top_nodes;
	}

	public void dump() {
		CFGraphNode entry = top_nodes.get(0);
		System.out.println("function_" + entry.hashCode() + " [label=\"<f0>Function `" + src.sig.id.image + "`\"];");
		System.out.println("function_" + entry.hashCode() + " -> node_" + entry.hashCode());
		System.out.println(recurseTree(entry, new ArrayList<String>()));
	}

	private String recurseTree(CFGraphNode node, ArrayList<String> mu_table) {
		StringBuilder d0 = new StringBuilder();
		int qq = node.hashCode();

		StringBuilder flags = new StringBuilder();
		for (CFGraphNodeFlag flag : node.flags)
			flags.append(flag.toString().substring(0, 1));

		if (node.image == null) {
			emit(d0,
					"node_" + qq + " [label=\"<f0>Chunk " + qq + "|<f1>" + node.statements.size() + "|<f2>"
							+ flags.toString() + "\"];");
		} else {
			emit(d0, "node_" + qq + " [label=\"<f0>" + node.image + " " + qq + "|<f1>" + node.statements.size()
					+ "|<f2>" + flags.toString() + "\"];");
		}
		mu_table.add("node_" + qq);

		if (node.exit_nodes != null && node.exit_nodes.size() != 0) {

			for (int i = 0; i < node.exit_nodes.size(); i++) {
				JumpLabel l0 = node.exit_nodes.get(i);

				if (i == 0)
					emit(d0, "node_" + qq + ":f0 -> node_" + l0.expr.hashCode() + ":f0;");
				emit(d0, "node_" + l0.expr.hashCode() + " [label=\"<f0>Cond " + l0.expr.hashCode()
						+ "|<f1>T|<f2>F\"];");

				emit(d0, "node_" + l0.expr.hashCode() + ":f1 -> node_" + l0.next.hashCode() + ":f0;");
				if (!mu_table.contains("node_" + l0.next.hashCode()))
					d0.append(recurseTree(l0.next, mu_table));

				if (i == node.exit_nodes.size() - 1) {
					emit(d0, "node_" + l0.expr.hashCode() + ":f2 -> node_" + node.next_node.hashCode() + ":f0;");
					if (!mu_table.contains("node_" + node.next_node.hashCode()))
						d0.append(recurseTree(node.next_node, mu_table));
				} else {
					emit(d0,
							"node_" + l0.expr.hashCode() + ":f2 -> node_"
									+ node.exit_nodes.get(i + 1).expr.hashCode() + ":f0;");
				}
			}

		} else {
			if (node.next_node != null) {
				emit(d0, "node_" + qq + ":f0 -> node_" + node.next_node.hashCode() + ":f0;");
				if (!mu_table.contains("node_" + node.next_node.hashCode()))
					d0.append(recurseTree(node.next_node, mu_table));
			}
		}

		return d0.toString();
	}

	private void emit(StringBuilder b0, String data) {
		b0.append(data);
		b0.append("\r\n");
	}

}
