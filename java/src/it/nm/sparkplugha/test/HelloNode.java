package it.nm.sparkplugha.test;

import it.nm.sparkplugha.SparkplugHANode;

public class HelloNode extends SparkplugHANode {

	public static void main(String[] args) throws Exception {
		HelloNode node = new HelloNode();
		node.connect();

	}

}
