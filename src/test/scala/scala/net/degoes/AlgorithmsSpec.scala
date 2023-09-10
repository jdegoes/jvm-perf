package net.degoes.algorithms

import zio.test._ 

object AlgorithmsSpec extends ZIOSpecDefault {
  def spec = 
    suite("AlgorithmsSpec")(
      suite("SocialNetwork")(
        test("findMostPopularFriend - 3 person social network") {
          val network = 
            SocialNetwork(
              List(
                Person(0, 20, "John", List(1)),
                Person(1, 30, "Jane", List(0, 2)),
                Person(2, 40, "Fred", List(1))
              )
            )
          val result = network.findMostPopularFriend

          assertTrue(result == Some(1))
        },
        test("findMostPopularFriend - random social network") {
          val network = SocialNetwork.random(1000, 10)
          val result = network.findMostPopularFriend

          assertTrue(result.isDefined)
        }
      )
    )
      
}