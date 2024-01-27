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
package net.degoes.algorithms;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

final class SocialNetwork {
  List<Person> people;

  public SocialNetwork(List<Person> people) {
    this.people = people;
  }

  List<Integer> getFriendsOf(int id) {
    // Retrieve all the people that $id follows:
    List<Integer> follows = people.get(id).follows;

    // Return only the people that follow $id back ("friends"):
    return follows.stream().filter(candidateId ->
      people.get(candidateId).follows.contains(id)
    ).collect(Collectors.toList());
  }

  public Optional<Integer> findMostPopularFriend() {
    List<int[]> personAndFriendCount =
      people.stream().map(person ->
        // Map to array of person id and number of friends:
        new int[]{person.id, getFriendsOf(person.id).size()}
      ).collect(Collectors.toList());

    Optional<int[]> mostPopular = personAndFriendCount.stream().max(Comparator.comparingInt(array -> array[1]));

    return mostPopular.map(array -> array[0]);
  }
  
  // Deterministic RNG:
  private static Random rng = new Random(0L);

  public static SocialNetwork random(int people, int friendsPerPerson) {
    List<Person> members = IntStream.range(0, people).boxed().map(id -> {
      var friends = IntStream.range(0, friendsPerPerson).map(x -> rng.nextInt(people)).boxed().collect(Collectors.toList());

      return new Person(id, rng.nextInt(100), "Person "+id, friends);
    }).collect(Collectors.toList());

    return new SocialNetwork(members);
  }
}

final class Person {
  int id;
  int age;
  String name;
  List<Integer> follows;

  public Person(int id, int age, String name, List<Integer> follows) {
    this.id = id;
    this.age = age;
    this.name = name;
    this.follows = follows;
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