package de.nightara.solaris.core.util;

import java.util.*;

public class AbstractObservable<T> implements Observable<T>
{

  private final List<ObserverTuple<T>> observers;

  public AbstractObservable()
  {
    this.observers = new LinkedList<>();
  }

  @Override
  public void attachObserver(Observer<T> obs)
  {
    attachUntypedObserver(Object.class, obs);
  }

  public <K extends T> void attachObserver(Class<K> clazz, Observer<K> obs)
  {
    attachUntypedObserver(clazz, obs);
  }

  private void attachUntypedObserver(Class clazz, Observer<? extends T> obs)
  {
    observers.add(new ObserverTuple<>(clazz, obs));
    obs.onAttach(this);
  }

  @Override
  public void detachObserver(Observer<T> obs)
  {
    observers.removeIf(t -> Objects.equals(t.getT2(), obs));
    obs.onDetach(this);
  }

  @Override
  public void fire(T event)
  {
    observers.stream()
            .filter(t -> t.matches(event))
            .forEach(t -> t.receive(event));
  }
}
