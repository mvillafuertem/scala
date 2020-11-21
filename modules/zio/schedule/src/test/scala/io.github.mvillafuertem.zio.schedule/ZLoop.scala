package io.github.mvillafuertem.zio.schedule

import zio.{ ExitCode, UIO, ZIO }

object ZLoop extends zio.App {
  import zio.console._

  /**
   * Loops with the specified effectual function, collecting the results into a
   * list. The moral equivalent of:
   *
   * {{{
   * var s  = initial
   * var as = List.empty[A]
   *
   * while (cont(s)) {
   *   as = body(s) :: as
   *   s  = inc(s)
   * }
   *
   * as.reverse
   * }}}
   *
   * def loop[R, E, A, S](initial: S)(cont: S => Boolean, inc: S => S)(body: S => ZIO[R, E, A]): ZIO[R, E, List[A]] =
   * if (cont(initial))
   *   body(initial).flatMap(a => loop(inc(initial))(cont, inc)(body).map(as => a :: as))
   * else
   *   ZIO.succeedNow(List.empty[A])
   */
  /*
   * concepts
   */
  /*
   * result container
   */
  case class MyResult(a: Double, text: String)
  case class MyInfo(a: Int)
  /*
   * initial state of the business information
   */
  val initial: MyInfo                                                    = MyInfo(10)
  /*
   * function that determines the continuation of the processing loop
   */
  def cont(info: MyInfo): Boolean                                        = info.a > 0
  /*
   * function that gets the next status of business information
   */
  def dec(info: MyInfo): MyInfo                                          = MyInfo(info.a - 1)
  /*
   * business function
   */
  def body(info: MyInfo): UIO[MyResult]                                  =
    UIO(MyResult(info.a.toDouble, intTypeText(info.a)))
  /*
   * helper functions
   */
  def intTypeText(a: Int)                                                = if (a % 2 == 0) "even" else "odd"
  def prettyPrint[A](l: List[A]): String                                 = l.mkString("\n\t", "\n\t", "\n")
  /*
   * program
   */
  val program                                                            = for {
    _             <- putStrLn("zio loop demo:")
    evenOrOddList <- ZIO.loop(initial)(cont, dec)(body)
    _             <- putStrLn(s"-> evenOrOddList => ${prettyPrint(evenOrOddList)}")
  } yield ()
  // main function, needs exit = 0 [OK] or exit > 0 [ERROR]
  // Here the interpreter runs the program and perform side-effects
  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    (program as ExitCode.success)
      .catchAllCause(cause => putStrLn(s"${cause.prettyPrint}") as ExitCode.failure)
}
