package de.nightara.solaris.joy.game.mine;

import de.nightara.solaris.joy.game.mine.Position.Direction;
import java.time.*;
import java.util.*;

public class MineUser
{
  public static final Duration TRAP_DURATION = Duration.ofMinutes(5);

  public enum Role
  {
    WORKER, TRAPPER;
  }

  public enum Action
  {
    WAIT, RUN, DECOY, TRAP, ATTACK;
  }

  private final Role role;

  private Instant start;
  private Position position;

  public MineUser(Role job, Position position)
  {
    this.role = job;
    this.position = position;
    this.start = Instant.now();
  }

  public Role getRole()
  {
    return role;
  }

  public boolean isActive()
  {
    return !isOver();
  }

  public boolean isOver()
  {
    switch(getRole())
    {
      case WORKER:
        return getPosition().isPresent();
      case TRAPPER:
        return getDuration().minus(TRAP_DURATION).isNegative();
    }
    return true;
  }

  public Duration getDuration()
  {
    return Duration.between(start, Instant.now());
  }

  public Optional<Position> getPosition()
  {
    return Optional.ofNullable(position);
  }

  public boolean setPosition(Position pos)
  {
    start = Instant.now();
    position = pos;
    return pos == null;
  }

  public boolean advance(Direction dir)
  {
    if(position != null)
    {
      if(!position.hasNextNode())
      {
        setPosition(null);
        return true;
      }
      else if(position.getNextNode(dir).isPresent())
      {
        setPosition(position.getNextNode(dir).get());
        return true;
      }
    }
    return false;
  }
}
