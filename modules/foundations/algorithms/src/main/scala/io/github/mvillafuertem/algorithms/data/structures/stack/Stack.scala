package io.github.mvillafuertem.algorithms.data.structures.stack

import io.github.mvillafuertem.algorithms.data.structures.node.Node

class Stack[A] extends Equals {

  private var front: Node[A]   = _
  private var numElements: Int = 0

  def isEmpty: Boolean = front == null

  def push(value: A): this.type = {
    if (this.isEmpty) front = Node(value, null)
    else {
      val nuevo = Node(value, front)
      front = nuevo
    }
    numElements += 1
    this
  }

  def pop: A = {
    var value: A = ???
    if (isEmpty)
      System.out.println("Error, la pila está vacía")
    else {
      value = front.value
      front = front.next
      numElements -= 1
    }
    value
  }

  def peek: A = {
    var value: A = ???
    if (isEmpty)
      System.out.println("Error, la pila está vacía")
    else value = front.value
    value
  }

  def length: Int = numElements

  def show(): String = {
    var result = ""
    var aux    = front
    while (aux != null) {
      result += s"${aux.value}\n"
      aux = aux.next
    }
    result
  }

  override def canEqual(that: Any): Boolean = that.isInstanceOf[Stack[A]]

  override def equals(obj: Any): Boolean =
    obj match {
      case that: Stack[A] => that.canEqual(this) && this.hashCode == that.hashCode
      case _              => false
    }

  override def hashCode(): Int =
    show().hashCode()

}
