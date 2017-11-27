package coollog.asyncjob2.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * An asynchronous task that depends on an {@link AsyncTask} with the
 * result as an {@link AsyncTask} and applies a function to the result
 * of the encapsulated task asynchronously.
 */
public abstract class PromisedAsyncTask<T, R>
    extends AsyncTask<AsyncTask<R>> {

  /** A function that applies to the result of the dependency. */
  private final Function<T, R> asyncFunction;

  PromisedAsyncTask(
      Function<T, R> asyncFunction,
      AsyncTask<AsyncTask<T>> dependency) {
    this.asyncFunction = asyncFunction;
    addDependency(dependency);
  }

  @Override
  public void execute() {
    AsyncTask<AsyncTask<T>> dependency =
        (AsyncTask<AsyncTask<T>>) getDependencies().getLastAdded();

    // Waits until the dependency starts running its task.
    dependency.getFuture().join();

    // Gets the task that was created by the dependency.
    AsyncTask<T> dependencyTask = dependency.getResult();

    // Creates a new task that applies the function to the result of the created task.
    SingleAsyncTask<R> newTask = new ApplyFunctionAsyncTask(dependencyTask);

    setResult(newTask);
  }

  /** The task that applies the 'asyncFunction' asynchronously. */
  private class ApplyFunctionAsyncTask extends SingleAsyncTask<R> {

    private final AsyncTask<T> dependency;

    private ApplyFunctionAsyncTask(AsyncTask<T> dependency) {
      super(Collections.singletonList(dependency));
      this.dependency = dependency;
    }

    @Override
    public void execute() {
      asyncFunction.apply(dependency.getResult());
    }
  }
}
