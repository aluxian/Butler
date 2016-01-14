package com.butler.cloud.databases

import com.butler.cloud.databases.types.{NodeTypes, RelTypes}
import com.butler.cloud.utils.Configured
import org.neo4j.graphdb._
import org.neo4j.rest.graphdb.RestGraphDatabase

import scala.language.implicitConversions

/**
 * Provides access to the Neo4j graph database.
 */
trait GraphDB extends Configured {

  lazy val neo4j: RestGraphDatabase = configured[RestGraphDatabase]

  /**
   * Helper block for running code inside a transaction.
   */
  def withTx(operation: => Unit) = {
    val tx = neo4j.beginTx()
    try {
      operation
      tx.success()
    } finally {
      tx.close()
    }
  }

  implicit class ResourceIterableUtils[T](iterable: ResourceIterable[T]) {
    def each(operation: (ResourceIterator[T]) => Unit): Unit = {
      val iterator = iterable.iterator()
      try {
        operation(iterator)
      } finally {
        iterator.close()
      }
    }
  }

  implicit class IterableUtils[T](iterable: java.lang.Iterable[T]) {
    def each(operation: (java.util.Iterator[T]) => Unit): Unit = {
      val iterator = iterable.iterator
      operation(iterator)
      while (iterator.hasNext) {
        iterator.next()
      }
    }
  }

  /**
   * Update a node if it exists, otherwise create it. Also set additional properties on it.
   */
  def upsertNode(label: Label, propertyKey: String, propertyValue: Any, otherProperties: Map[String, Any] = Map()): Node = {
    var node: Option[Node] = None

    neo4j.findNodesByLabelAndProperty(label, propertyKey, propertyValue).each { iterator =>
      if (iterator.hasNext) {
        node = Some(iterator.next)
      } else {
        node = Some(neo4j.createNode(label))
      }
    }

    (otherProperties + (propertyKey -> propertyValue)).foreach {
      case (key, value) => node.get.setProperty(key, value)
    }

    node.get
  }

  /**
   * Update a relationship if it exists, otherwise create it.
   */
  def upsertRelationship(fromNode: Node, toNode: Node, relType: RelationshipType): Relationship = {
    var alreadyExists: Boolean = false

    fromNode.getRelationships(relType, Direction.OUTGOING).each { outgoingIterator =>
      while (outgoingIterator.hasNext && !alreadyExists) {
        val rel = outgoingIterator.next()
        if (rel.getEndNode.getId == toNode.getId) {
          alreadyExists = true
          return rel
        }
      }
    }

    fromNode.createRelationshipTo(toNode, relType)
  }

  implicit def string2Label(name: String): Label = DynamicLabel.label(name)
  implicit def nodeLabel2Label(label: NodeTypes.NodeLabel): Label = DynamicLabel.label(label.toString)
  implicit def relationshipLabel2RelationshipType(label: RelTypes.RelationshipLabel): RelationshipType = new RelationshipType {
    override def name(): String = label.toString
  }

}
