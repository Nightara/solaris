package de.nightara.solaris.core.event;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

public abstract class ThreadManager
{
  private static final List<ScheduledFuture> ROUTINES = new LinkedList<>();
  private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(3);

  public static <T> Future<T> submit(Callable<T> thread)
  {
    return EXECUTOR.submit(thread);
  }

  public static ScheduledFuture submitRoutine(Runnable routine, Duration initial, Duration delay)
  {
    ScheduledFuture future = EXECUTOR.scheduleWithFixedDelay(routine, initial.toMillis(), delay.toMillis(), TimeUnit.MILLISECONDS);
    ROUTINES.add(future);
    return future;
  }
  
  public static boolean stopRoutine(ScheduledFuture routine)
  {
    return ROUTINES.remove(routine)
            && routine.cancel(false);
  }

  public static List<ScheduledFuture> getRoutines()
  {
    return ROUTINES;
  }
}
