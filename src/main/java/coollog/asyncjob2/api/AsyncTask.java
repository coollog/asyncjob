package coollog.asyncjob2.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public abstract class AsyncTask<T> implements Task<T> {

  /** The future for running the asynchronous task. */
  private CompletableFuture<Void> future;

  /** This task runs only after all dependency tasks are completed. */
  private DependencySet<AsyncTask<?>> dependencies = new DependencySet<>();

  /** The result of running the task. */
  private T result;

  /** Runs the task to completion. */
  @Override
  public void run() {
    // Runs the future and ignores the result.
    getFuture().join();
  }

  /** The code to run asynchronously. */
  public abstract void execute();

  @Override
  public final T getResult() {
    return result;
  }

  protected final void setResult(T result) {
    this.result = result;
  }

  protected DependencySet<AsyncTask<?>> getDependencies() {
    return dependencies;
  }

  protected void addDependency(AsyncTask<?> dependency) {
    dependencies.add(dependency);
  }

  protected CompletableFuture<Void> getFuture() {
    if (future == null) {
      makeFutureFromDependencies();
    }

    return future;
  }

  /** Generates the future for this task based on the dependencies. */
  private void makeFutureFromDependencies() {
    if (dependencies.isEmpty()) {
      // No dependencies - create a new future.
      // TODO: Do exceptions get swallowed?
      future = CompletableFuture.runAsync(this::execute);

    } else if (dependencies.hasOne()) {
      // There is only one dependency, so run this task after that dependency.
      future =
          dependencies
              .getLastAdded()
              .getFuture()
              .thenRunAsync(this::execute);
    } else {
      // There are multiple dependencies, so run this task after all of them are complete.
      List<CompletableFuture<?>> futureList =
          dependencies
              .stream()
              .map(AsyncTask::getFuture)
              .collect(Collectors.toList());
      CompletableFuture<Void> afterAllDependenciesFuture =
          CompletableFuture.allOf(
              futureList.toArray(new CompletableFuture[futureList.size()]));

      future = afterAllDependenciesFuture.thenRunAsync(this::execute);
    }
  }
}
