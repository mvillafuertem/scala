package io.github.mvillafuertem.algorithms.data.structures.queue

import io.github.mvillafuertem.algorithms.data.structures.node.Node

class Queue[A] {
  private var cabeza: Node[A] = _
  private var fin: Node[A]    = _

  var numElem = 0

  def enqueue(value: A): Unit = {
    val nuevo = Node(value, null)
    if (cabeza != null)
      fin.next = nuevo
    else {
      cabeza = nuevo
      fin = nuevo
      numElem += 1
    }
  }

  def dequeue: A = { //Devuelve el elemento del cabeza de la cola y lo elimina Si la cola está vacía, devuelve como resultado 0 y un mensaje de error
    var nodo: Node[A] = ???
    var resul: A      = ???
    if (cabeza == null)
      println("Error, la cola está vacía")
    else {
      nodo = cabeza
      cabeza = nodo.next
      resul = nodo.value
      if (cabeza == null) fin = null
      numElem -= 1
    }
    resul
  }

  def isEmpty: Boolean = // Comprueba si la cola está vacía
    cabeza == null

  def peek: A = { //Devuelve el elemento del cabeza de la cola Si la cola está vacía, devuelve como resultado -999 y un mensaje de error
    var resul: A = ???
    if (!this.isEmpty) resul = cabeza.value
    else println("Error, la cola está vacía")
    resul
  }

  def show(): Unit = { // Muestra por pantalla el contenido de la cola
    var aux: Node[A] = ???
    println("Contenido de la cola: ")
    aux = cabeza
    while (aux != null) {
      println(s"${aux.value}  ")
      aux = aux.next
    }
  }

  def length: Int = numElem
}
