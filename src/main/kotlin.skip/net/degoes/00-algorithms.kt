/**
 * ALGORITHMS
 *
 * It does not matter how aggressively you microptimize code, if the algorithms you are using are
 * pathological.
 *
 * In this introductory session, we will establish the importance of using correct algorithms, and
 * see how you can use benchmarking to identify pathological behavior.
 *
 * As we explore this important subject, you will gain familiarity with JMH, a benchmarking harness
 * that is commonly used on the JVM.
 *
 * The principles you will learn in this workshop apply uniformly to all JVM languages, and most of
 * them apply even more broadly, to other languages beyond the JVM.
 */
package net.degoes.algorithms

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

data class Person(val id: Int, val age: Int, val name: String, val follows: List<Int>)

final data class SocialNetwork(val people: List<Person>) {
  fun getFriendsOf(id: Int): List<Int> {
    // Retrieve all the people that $id follows:
    val follows = people[id].follows

    // Return only the people that follow $id back ("friends"):
    return follows.filter { candidateId ->
      val candidate = people[candidateId]

      candidate.follows.contains(id)
    }
  }

  fun findMostPopularFriend(): Int? {
    val personAndFriendCount: List<Pair<Int, Int>> =
      people.map { person ->
        // Map to tuple of person id and number of friends:
        Pair(person.id, getFriendsOf(person.id).size)
      }

    val mostPopular: Pair<Int, Int>? = personAndFriendCount.maxBy { it.second }

    return mostPopular?.let { it.first }
  }

  companion object {
    // Deterministic RNG:
    private val rng = scala.util.Random(0L)

    fun random(people: Int, friendsPerPerson: Int): SocialNetwork =
      SocialNetwork(
        List(people) { id ->
          Person(
            id,
            rng.nextInt(100),
            "Person "+id,
            List(friendsPerPerson) { _ ->
              rng.nextInt(people)
            }
          )
        }
      )
  }
}

/**
 * EXERCISE 1
 *
 * Make a real benchmark for the `findMostPopularFriend` method. Initially, just create a social
 * network inside the benchmark, and then call `findMostPopularFriend` on it.
 *
 * EXERCISE 2
 *
 * In your previous benchmark, the overhead of creating the social network is included in the
 * benchmark of the method. This is not ideal, because it means that the benchmark is not measuring
 * the performance of the method in isolation. Take advantage of the @Setup annotation to create the
 * social network outside the benchmark.
 *
 * EXERCISE 3
 *
 * When benchmarking algorithms, a single data point is not useful: it gives you no idea of how the
 * performance of the algorithm changes with the size of the input. Use the @Param annotation on a
 * new field, `networkSize`, to see how the algorithm performs with differing network sizes.
 *
 * EXERCISE 4
 *
 * In our case, the social network has two parameters: the size of the network, and the number of
 * friends per person. Use the @Param annotation to create a second parameter, `friendsPerPerson`,
 * and see how the algorithm performs with differing numbers of friends per person.
 *
 * EXERCISE 5
 *
 * At this point, you should have an idea of how the algorithm performs, both with different network
 * sizes, and different numbers of friends per person. Now you will need to analyze the algorithm,
 * paying attention to nested loops, in order to figure out why the algorithm performs the way it
 * does.
 *
 * EXERCISE 6
 *
 * Now that you have some idea of why the algorithm performs the way it does, it is time to
 * investigate alternative methods of solving the problem that have improved algorithmic
 * performance. Test your potential improvements using the benchmark, and do not stop iterating
 * until you have found a solution that scales better with both network size and friend count.
 */
open class FindMostPopularFriendBenchmark {
  @Benchmark
  fun findMostPopularFriend(blackHole: Blackhole): Unit {
  }
}
