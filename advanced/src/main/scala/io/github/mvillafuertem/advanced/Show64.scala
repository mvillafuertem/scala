case class Device(id: Long, description: String)

trait Show64[A] {

  def encode(a: A): String
}

object Show64 {

  def apply[A](implicit sh: Show64[A]): Show64[A] = sh

  def encode[A: Show64](a: A): String = Show64[A].encode(a)

  implicit val deviceShow64: Show64[Device] =
    (a: Device) => Base64.encode(a.toString.getBytes)

  implicit class ShowOps[A: Show64](a: A) {
    def encode: String = Show64[A].encode(a)
  }
}


object MyApp extends App {

  import Show64._

  val device = Device(23L, "PEPE")

  deviceShow64.encode(device)

  println(encode(device))

  println(device.encode)

}
