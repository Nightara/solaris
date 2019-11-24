package de.nightara.solaris.core.util;

import java.util.*;

public interface Observable<T>
{

  default public void fireEvents(T... events)
  {
    fire(Arrays.asList(events));
  }

  default public void fire(Collection<T> events)
  {
    events.forEach(this::fire);
  }

  public void fire(T event);

  public void attachObserver(Observer<T> obs);

  public void detachObserver(Observer<T> obs);
}
