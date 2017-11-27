package coollog.asyncjob2.api;

import coollog.asyncjob.AsyncAsyncJob;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionException;

public class AsyncTaskBuilder {

  /**
   * Creates a new {@link SingleAsyncTask}.
   *
   * @param taskClass a subclass of {@link SingleAsyncTask} to instantiate
   * @param dependencies the task must run after all dependencies are completed
   * @return the new {@link SingleAsyncTask}
   */
  public static <R> SingleAsyncTask<R> newSingleAsyncTask(
      Class<? extends SingleAsyncTask<R>> taskClass,
      AsyncTask<?>... dependencies)
      throws CompletionException {

    List<AsyncTask> dependenciesCopy = Arrays.asList(dependencies);
    try {
      return taskClass
          .getDeclaredConstructor(List.class)
          .newInstance(dependenciesCopy);
    } catch (InstantiationException
        | IllegalAccessException
        | InvocationTargetException
        | NoSuchMethodException ex) {
      throw new CompletionException(ex);
    }
  }
}
