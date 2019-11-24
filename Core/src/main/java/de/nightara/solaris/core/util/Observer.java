package de.nightara.solaris.core.util;

@FunctionalInterface
public interface Observer<T>
{

  public void receive(T event);

  default public void onAttach(Observable target)
  {
  }

  default public void onDetach(Observable target)
  {
  }
}
