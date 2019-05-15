package io.github.mvillafuertem.algorithms.data.structures.tree

import io.github.mvillafuertem.algorithms.data.structures.node.TreeNode
import io.github.mvillafuertem.algorithms.data.structures.queue.Queue

class Tree[A] {
//  private var raiz: TreeNode = _
//
//  def this(clave: Int) {
//    this()
//    raiz = new TreeNode(clave)
//  }
//
//  // Escribe las claves del árbol binario de raiz a en preorden.
//  private def preOrden(nodo: TreeNode): Unit = {
//    if (nodo != null) {
//      System.out.print(nodo.getClave + " ") // Nodo
//
//      preOrden(nodo.getIz) // Izquierda
//
//      preOrden(nodo.getDe) // Derecha
//
//    }
//  }
//
//  def preOrden(): Unit = {
//    System.out.print("Preorden: ")
//    preOrden(raiz)
//    System.out.println()
//  }
//
//  // Escribe las claves del árbol binario de raiz a en postorden.
//  private def postOrden(nodo: TreeNode): Unit = {
//    if (nodo != null) {
//      postOrden(nodo.getIz)
//      postOrden(nodo.getDe)
//      System.out.print(nodo.getClave + " ")
//    }
//  }
//
//  def postOrden(): Unit = {
//    System.out.print("Post orden: ")
//    postOrden(raiz)
//    System.out.println()
//  }
//
//  // Escribe las claves del árbol binario de raiz a en orden central.
//  private def ordenCentral(nodo: TreeNode): Unit = {
//    if (nodo != null) {
//      ordenCentral(nodo.getIz)
//      System.out.print(nodo.getClave + " ")
//      ordenCentral(nodo.getDe)
//    }
//  }
//
//  def ordenCentral(): Unit = {
//    System.out.print("Orden central: ")
//    ordenCentral(raiz)
//    System.out.println()
//  }
//
//  def listarAmplitud(): Unit = {
//    var p = raiz
//    val c = new Queue[A]
//    System.out.print("Amplitud: ")
//    if (p != null) c.enqueue(p)
//    while ( {
//      !c.isEmpty
//    }) {
//      p = c.dequeue
//      System.out.print(p.getClave + " ")
//      if (p.getIz != null) c.enqueue(p.getIz)
//      if (p.getDe != null) c.enqueue(p.getDe)
//    }
//    System.out.println()
//  }
//
//  private def busqueda(nodo: TreeNode, dato: Int) = {
//    var resul = false
//    if (nodo != null) if (nodo.getClave eq dato) resul = true
//    else {
//      resul = busqueda(nodo.getIz, dato)
//      if (!resul) resul = busqueda(nodo.getDe, dato)
//    }
//    resul
//  }
//
//  def busqueda(dato: Int): Boolean = busqueda(raiz, dato)
//
//  /**
//    * Obtener un árbol nuevo a partir de un dato, un árbol a1 (que será el
//    * subárbol izquierdo) y otro a2 (subárbol derecho) que deben ser distintos.
//    */
//  def juntar(clave: Int, a1: Tree, a2: Tree): Unit = {
//    if ((a1.raiz eq a2.raiz) && a1.raiz != null) System.out.println("no se pueden mezclar, a1 y a2 son iguales")
//    else { // Crear el nodo nuevo
//      raiz = new TreeNode(clave, a1.raiz, a2.raiz)
//      // Borrar los árboles a1 y a2
//      if (this ne a1) a1.raiz = null
//      if (this ne a2) a2.raiz = null
//    }
//  }
//
//  def listarAmplitudNiveles(): Unit = {
//    var p = null
//    var n = 0
//    val c = new Queue[Tree[A]]
//    System.out.println("Amplitud niveles: ")
//    p = raiz
//    if (p != null) {
//      var elem = new TreeNode(p, 1)
//      c.(elem)
//      while ( {
//        !c.isEmpty
//      }) {
//        elem = c.dequeue
//        p = elem.getNodoArbol
//        n = elem.getNivel
//        System.out.println("nivel: " + n + " " + p.getClave + " ")
//        if (p.getIz != null) {
//          elem = new TreeNode(p.getIz, n + 1)
//          c.encolar(elem)
//        }
//        if (p.getDe != null) {
//          elem = new TreeNode(p.getDe, n + 1)
//          c.encolar(elem)
//        }
//      }
//    }
//    System.out.println()
//  }
//
//  def getRaiz: TreeNode = raiz
//
//  def setRaiz(nodo: TreeNode): Unit = {
//    raiz = nodo
//  }
//
//  def mostrarHojas(): Unit = {
//    System.out.print("Claves en hojas: ")
//    mostrarHojas(raiz)
//    System.out.println("Fin")
//  }
//
//  private def mostrarHojas(nodo: TreeNode): Unit = {
//    if (nodo != null) if (nodo.getIz == null && nodo.getDe == null) System.out.print(nodo.getClave + ", ")
//    else {
//      mostrarHojas(nodo.getIz)
//      mostrarHojas(nodo.getDe)
//    }
//  }
}
