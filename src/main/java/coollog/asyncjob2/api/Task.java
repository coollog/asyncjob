package coollog.asyncjob2.api;

public interface Task<T> extends Runnable {

  /**
   * Only valid if the task is completed.
   *
   * @return the task result
   */
  T getResult();
}
