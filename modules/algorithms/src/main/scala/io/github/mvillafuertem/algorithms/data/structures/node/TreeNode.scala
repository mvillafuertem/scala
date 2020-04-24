package io.github.mvillafuertem.algorithms.data.structures.node

class TreeNode { // Constructor vacío
  private var clave        = 0
  private var iz: TreeNode = _
  private var de: TreeNode = _

  // Constructor con clave
  def this(clave: Int) {
    this()
    this.clave = clave
    iz = null
    de = null
  }

  def this(clave: Int, iz: TreeNode, de: TreeNode) {
    this()
    this.clave = clave
    this.iz = iz
    this.de = de
  }

  def getIz: TreeNode = iz

  def getDe: TreeNode = de

  def getClave: Int = clave

  def setIz(nuevoIz: TreeNode): Unit =
    iz = nuevoIz

  def setDe(nuevoDe: TreeNode): Unit =
    de = nuevoDe

  def setClave(nuevaClave: Int): Unit =
    clave = nuevaClave
}
