package de.nightara.solaris.core.util;

public class ObserverTuple<T> extends Tuple<Class<T>, Observer<T>>
{ 
  public ObserverTuple(Class<T> t1, Observer<T> t2)
  {
    super(t1, t2);
  }
  
  public boolean matches(Object event)
  {
    return getT1().isInstance(event);
  }
  
  public void receive(T event)
  {
    getT2().receive(event);
  }
}
