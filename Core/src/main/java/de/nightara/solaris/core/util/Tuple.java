package de.nightara.solaris.core.util;

import java.util.*;

public class Tuple<T1, T2>
{
  private T1 t1;
  private T2 t2;

  public Tuple(T1 t1, T2 t2)
  {
    this.t1 = t1;
    this.t2 = t2;
  }

  public T1 getT1()
  {
    return t1;
  }

  public void setT1(T1 t1)
  {
    this.t1 = t1;
  }

  public T2 getT2()
  {
    return t2;
  }

  public void setT2(T2 t2)
  {
    this.t2 = t2;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 61 * hash + Objects.hashCode(this.t1);
    hash = 61 * hash + Objects.hashCode(this.t2);
    return hash;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
    {
      return true;
    }
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    final Tuple<?, ?> other = (Tuple<?, ?>) obj;
    if (!Objects.equals(this.t1, other.t1))
    {
      return false;
    }
    return Objects.equals(this.t2, other.t2);
  }
}
