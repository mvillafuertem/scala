package io.github.mvillafuertem.algorithms.data.structures.stack

import java.util

import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable

final class TwoSumSpec extends AnyFlatSpecLike with Matchers {

  behavior of "Two Sum"

  /**
   * Given an array of integers, return indices of the two numbers such that they add up to a specific target.
   *
   * You may assume that each input would have exactly one solution, and you may not use the same element twice.
   *
   * Example:
   *
   * Given nums = [2, 7, 11, 15], target = 9,
   *
   * Because nums[0] + nums[1] = 2 + 7 = 9, return [0, 1].
   */
  it should "two sum" in {

    val nums   = Array[Int](3, 3)
    val target = 6

    val actual = twoSum(nums, target)

    actual shouldBe Array[Int](0, 1)

  }

  def twoSum(nums: Array[Int], target: Int): Array[Int] = {

    val map: mutable.Map[Int, Int] = mutable.HashMap()
    for (i <- nums.indices) {
      val complement = target - nums(i)
      map
        .get(complement)
        .fold(map += ((nums(i), i)))(_ => return Array[Int](map(complement), i))
    }
    throw new IllegalArgumentException("No two sum solution")

  }

  def twoSumImperative(nums: Array[Int], target: Int): Array[Int] = {
    val prevMap = new util.HashMap[Integer, Integer]();
    for (i <- nums.indices) {
      val diff = target - nums(i)
      if (prevMap.containsKey(diff)) return Array[Int](prevMap.get(diff), i)
      prevMap.put(nums(i), i)
    }
    new Array[Int](0) // Guranteed solution, no need for return

  }

}
