package io.github.mvillafuertem.foundations

// https://medium.com/@alexander.zaidel/composing-functions-with-reader-monad-f3e471958e2a
// https://vimeo.com/44502327
// https://wiki.haskell.org/wikiupload/8/85/TMR-Issue13.pdf
// Explicado en 6 pasos
object Reader {

  type ConnectionPool = String
  type User           = String

  /**
   * 1. Repositorio con colaboradores (usando injeccion de dependencias)
   *
   * class UserRepository(pool: ConnectionPool) {
   * def createUser(userName: String, age: Int): User = ???
   * }
   */

  /**
   * 2. Bajamos el nivel de injección a la función, quedaría algo así
   *
   * class UserRepository {
   * def createUser(userName: String, age: Int, pool: ConnectionPool): User = ???
   * }
   */

  /**
   * 3. Currificamos nuestra función
   *
   * class UserRepository {
   * def createUser(userName: String, age: Int)(pool: ConnectionPool): User = ???
   * }
   */

  /**
   * 4. Cambiamos el tipo de retorno a función
   *
   * class UserRepository {
   * def createUser(userName: String, age: Int): ConnectionPool => User = { pool => ??? }
   * }
   */

  /**
   * 5. Creamos un wrapper para el tipo
   *
   * case class Reader(fun: ConnectionPool => User)
   *
   * class UserRepository {
   * def createUser(userName: String, age: Int): Reader = Reader { pool => ??? }
   * }
   */

  /** 6. Aqui empieza lo interesante, generalizamos nuestra función parametrizando los tipos y añadimos los operadores map y flatmap */

  // Piensa que A no solo puede ser un colaborador, sino que puede verse algo así
  // A = colaborador1 && colaborador2 && colaborador3
  // esto formaría un entorno
  case class Reader[A, B](fun: A => B) {

    def map[C](f: B => C): Reader[A, C] = Reader { a =>
      f(fun(a))
    }

    def flatMap[C](f: B => Reader[A, C]): Reader[A, C] = Reader { a =>
      f(fun(a)).fun(a)
    }

  }

  class UserRepository {
    def createUser(userName: String, age: Int): Reader[ConnectionPool, User] = Reader[ConnectionPool, User](pool => ???)
  }

}
