package de.nightara.solaris.core.event;

import discord4j.core.object.entity.*;

import java.time.*;
import java.util.*;

public abstract class Event
{
  private final Instant start;
  private final MessageChannel channel;
  private final Duration duration;

  public Event(MessageChannel channel, Duration duration)
  {
    this.channel = channel;
    this.duration = duration;
    this.start = Instant.now();
  }

  public Instant getStart()
  {
    return start;
  }

  public MessageChannel getChannel()
  {
    return channel;
  }

  public Duration getDuration()
  {
    return duration;
  }

  public boolean isActive()
  {
    return !isOver();
  }

  public boolean isOver()
  {
    return Instant.now().isAfter(getStart().plus(getDuration()));
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 71 * hash + Objects.hashCode(this.start);
    hash = 71 * hash + Objects.hashCode(this.channel);
    hash = 71 * hash + Objects.hashCode(this.duration);
    return hash;
  }

  @Override
  public boolean equals(Object obj)
  {
    if(this == obj)
    {
      return true;
    }
    if(obj == null) {
      return false;
    }
    if(getClass() != obj.getClass())
    {
      return false;
    }
    final Event other = (Event) obj;
    if(!Objects.equals(this.start, other.start))
    {
      return false;
    }
    if(!Objects.equals(this.channel, other.channel))
    {
      return false;
    }
    return Objects.equals(this.duration, other.duration);
  }
}
