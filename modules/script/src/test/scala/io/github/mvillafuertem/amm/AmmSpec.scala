package io.github.mvillafuertem.amm

import ammonite.ops._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

final class AmmSpec extends AnyFlatSpecLike with Matchers with BeforeAndAfterAll {

  override def suiteName: String = "Ammonite-Ops"

  override protected def beforeAll(): Unit = {
    val f = pwd / Symbol("tmp") / "hoge.txt"
    if (exists ! f) rm ! f
  }

  it should "test 1" in {
    val directory: FilePath = root / Symbol("home")
    val file: FilePath      = root / Symbol("tmp") / "sample.scala"

    directory.toString shouldBe "/home"
    file.toString shouldBe "/tmp/sample.scala"
  }

  ignore should "test 2" in {
    val files: LsSeq = ls ! pwd

    ls.rec ! pwd
    mkdir ! pwd / Symbol("tmp") / "hoge"

    cp(pwd / Symbol("tmp") / "sample.txt", pwd / Symbol("tmp") / "sample2.txt")
    mv(pwd / Symbol("tmp") / "sample2.txt", pwd / Symbol("tmp") / "sample3.txt")
    rm ! pwd / Symbol("tmp") / "sample3.txt"
  }

  ignore should "test 3" in {
    // http://ammonite.io/#Operations

    read(pwd / Symbol("tmp") / "sample.txt") shouldBe "Scala de Scala\n"
    read.bytes ! pwd / Symbol("tmp") / "sample.txt" // Array[Byte]
    read.lines ! pwd / Symbol("tmp") / "sample.txt" // Vector[String]

    write(pwd / Symbol("tmp") / "hoge.txt", "Foo!")
    write.over(pwd / Symbol("tmp") / "foo.txt", "Yaa!")
    write.append(pwd / Symbol("tmp") / "foo.txt", "Boo!")
  }

  it should "test 4" in {
    // http://ammonite.io/#Extensions

    val seq = Seq(1, 2, 3)
    seq | (_ * 2) shouldBe seq.map(_ * 2)
    seq || (i => Seq(i, i * 2)) shouldBe seq.flatMap(i => Seq(i, i * 2))
    seq |? (_ % 2 == 0) shouldBe seq.filter(_ % 2 == 0)
    seq |& (_ + _) shouldBe seq.product
    seq |! println // foreach

    // Pipeable
    val func = (i: Int) => i * 2
    100 |> func shouldBe 200 // func(100)

    // Callable
    func ! 100 shouldBe 200 // func(100)

    ls.rec ! pwd |? (_.ext == "scala") | read
  }

  it should "test 5" in {
    import ammonite.ops.ImplicitWd._

    // http://www.ne.jp/asahi/hishidama/home/tech/scala/dynamic.html
    % ls
    val res = %%(Symbol("echo"), "foo!")
    res.exitCode shouldBe 0
    res.out.string shouldBe "foo!\n"
  }
}
