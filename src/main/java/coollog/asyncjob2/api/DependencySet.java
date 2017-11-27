package coollog.asyncjob2.api;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/** Encapsulates a set of dependency tasks. */
class DependencySet<T> {

  private Set<T> dependencies = new HashSet<>();

  private T lastAdded;

  void add(T dependency) {
    if (dependencies.add(dependency)) {
      lastAdded = dependency;
    }
  }

  boolean isEmpty() {
    return dependencies.isEmpty();
  }

  boolean hasOne() {
    return 1 == dependencies.size();
  }

  T getLastAdded() {
    return lastAdded;
  }

  Stream<T> stream() {
    return dependencies.stream();
  }
}
