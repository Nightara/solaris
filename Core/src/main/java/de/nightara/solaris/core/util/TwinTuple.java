package de.nightara.solaris.core.util;

import java.util.*;

public class TwinTuple<T> extends Tuple<T, T> implements Collection<T>
{
  public TwinTuple(T t1, T t2)
  {
    super(t1, t2);
  }
  
  @Override
  public int size()
  {
    return 2;
  }

  @Override
  public boolean isEmpty()
  {
    return false;
  }

  @Override
  public boolean contains(Object o)
  {
    return getT1().equals(o) || getT2().equals(o);
  }

  @Override
  public Iterator<T> iterator()
  {
    return new Iterator<T>()
    {
      private int remainder = 2;

      @Override
      public boolean hasNext()
      {
        return remainder > 0;
      }

      @Override
      public T next()
      {
        remainder--;
        if(remainder == 1)
        {
          return getT1();
        }
        if(remainder == 0)
        {
          return getT2();
        }
        return null;
      }
    };
  }

  @Override
  public Object[] toArray()
  {
    return new Object[]{getT1(), getT2()};
  }

  @Override
  public <T> T[] toArray(T[] ts)
  {
    return (T[]) toArray();
  }

  @Override
  public boolean add(T e)
  {
    throw new UnsupportedOperationException("Using Collections.add is not supported.");
  }

  @Override
  public boolean remove(Object o)
  {
    throw new UnsupportedOperationException("Using Collections.remove is not supported.");
  }

  @Override
  public boolean containsAll(Collection<?> c)
  {
    return c.stream().allMatch(this::contains);
  }

  @Override
  public boolean addAll(Collection<? extends T> c)
  {
    throw new UnsupportedOperationException("Using Collections.addAll is not supported.");
  }

  @Override
  public boolean removeAll(Collection<?> c)
  {
    throw new UnsupportedOperationException("Using Collections.removeAll is not supported.");
  }

  @Override
  public boolean retainAll(Collection<?> c)
  {
    throw new UnsupportedOperationException("Using Collections.retainAll is not supported.");
  }

  @Override
  public void clear()
  {
    throw new UnsupportedOperationException("Using Collections.retainAll is not supported.");
  }
}
