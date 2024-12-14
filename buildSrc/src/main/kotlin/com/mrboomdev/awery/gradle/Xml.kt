package com.mrboomdev.awery.gradle

import groovy.util.Node
import groovy.util.NodeList
import org.w3c.dom.Element
import java.io.StringWriter
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

fun Element.toXmlString(): String {
	val transformer = TransformerFactory.newInstance().newTransformer().apply {
		setOutputProperty(OutputKeys.INDENT, "yes")
	}

	return StringWriter().apply {
		transformer.transform(DOMSource(this@toXmlString), StreamResult(this))
	}.toString()
}

/**
 * @param query Like an CSS query, but with limited functionality.
 * Allowed syntax: "parent1 parent2 children"
 * @return All matched nodes
 */
fun Node.queryAll(query: String): NodeList {
	val queryList = query.split(" ").map { it.trim() }
	val value = value()
	val result = NodeList()

	if(value is NodeList) {
		for(item in value) {
			if(item is Node) {
				if(item.name() == queryList) {
					if(queryList.size == 1) {
						result.add(item)
					}

					if(queryList.size > 1) {
						result.addAll(item.queryAll(queryList.toMutableList().apply {
							removeFirst()
						}.joinToString(" ")))
					}
				}
			}
		}
	}

	return result
}

/**
 * Iterate though all children nodes
 */
fun NodeList.forEachNode(action: (Node) -> Unit) {
	for(node in this) {
		if(node is Node) {
			action(node)
		}
	}
}

/**
 * @param query Like an CSS query, but with limited functionality.
 * Allowed syntax: "parent1 parent2 children"
 * @return A first match or null if no nodes were found
 */
fun Node.query(query: String): Node? = queryAll(query)[0] as Node?