package coollog.asyncjob2.api;

import java.util.List;

/** An asynchronous task that does not create other asynchronous tasks. */
public abstract class SingleAsyncTask<T> extends AsyncTask<T> {

  /** Instantiate with {@link AsyncTaskBuilder} */
  protected SingleAsyncTask(List<AsyncTask<?>> dependencies) {
    for (AsyncTask<?> dependency : dependencies) {
      addDependency(dependency);
    }
  }
}
