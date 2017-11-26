package coollog.asyncjob;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

abstract class AsyncJob<T> {
  private static final Map<AsyncJob<?>, Object> resultMap = new ConcurrentHashMap<>();

  static Object getResultFor(AsyncJob<?> target) {
    return resultMap.get(target);
  }

  private final Set<AsyncJob<?>> dependencies = new HashSet<>();

  /** Only valid if `dependencies.size() == 1`. */
  private AsyncJob<?> onlyDependency;

  private CompletableFuture<T> future;


  final void dependsOn(AsyncJob<?> other) {
    onlyDependency = other;
    dependencies.add(other);
  }

  void run() {
    getFuture().join();
  }

  final CompletableFuture<T> getFuture() {
    if (future == null) {
      switch (dependencies.size()) {
        case 0:
          future = CompletableFuture.supplyAsync(wrapMySupplier());
          break;
        case 1:
          future = onlyDependency.getFuture().thenApplyAsync(voidObject -> {
            try {
              return wrapSupplier().get();
            } catch (Exception ex) {
              throw new CompletionException(ex);
            }
          });
          break;
        default:
          List<CompletableFuture<?>> futureList =
              dependencies
                  .stream()
                  .map(AsyncJob::getFuture)
                  .collect(Collectors.toList());
          CompletableFuture<Void> futures = CompletableFuture.allOf(
              futureList.toArray(new CompletableFuture[futureList.size()]));

          return futures.thenApplyAsync(voidObject -> {
            try {
              return wrapSupplier().get();
            } catch (Exception ex) {
              throw new CompletionException(ex);
            }
          });
      }
    }
    return future;
  }

  protected abstract Supplier<T> getSupplier() throws Exception;

  protected Supplier<T> wrapSupplier() throws Exception {
    return wrapMySupplier();
  }

  final protected Supplier<T> wrapMySupplier() {
    return () -> {
      try {
        T result = getSupplier().get();
        resultMap.put(this, result);
        return result;
      } catch (Exception ex) {
        throw new CompletionException(ex);
      }
    };
  }
}
