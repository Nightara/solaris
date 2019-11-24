package de.nightara.solaris.joy.game.mine;

import gnu.trove.map.hash.*;
import java.util.*;
import java.util.stream.*;
import java.util.stream.Stream.Builder;

import static de.nightara.solaris.joy.game.mine.Position.Direction.*;

public class Position
{
  public enum Direction
  {
    LEFT, RIGHT, CENTER;
  }

  private final int index;
  private final Direction direction;
  private final Map<Direction, Position> nextNodes;

  public Position(int index, Direction direction)
  {
    this.index = index;
    this.direction = direction;
    this.nextNodes = new THashMap<>();
  }

  public void addNext(Position pos)
  {
    nextNodes.put(pos.getDirection(), pos);
  }

  public int getIndex()
  {
    return index;
  }

  public Direction getDirection()
  {
    return direction;
  }

  public boolean isSafe()
  {
    return getDirection() == CENTER;
  }

  public boolean isBattle()
  {
    return !isSafe();
  }

  public boolean hasNextNode()
  {
    return !nextNodes.isEmpty();
  }

  public Optional<Position> getNextNode(Direction dir)
  {
    return Optional.ofNullable(nextNodes.get(dir));
  }

  public Stream<Position> stream()
  {
    Builder<Position> builder = Stream.builder();
    builder.add(this);
    nextNodes.values().stream()
            .flatMap(Position::stream)
            .forEach(builder);
    return builder.build().distinct();
  }

  @Override
  public int hashCode()
  {
    int hash = 5;
    hash = 79 * hash + this.index;
    hash = 79 * hash + Objects.hashCode(this.direction);
    return hash;
  }

  @Override
  public boolean equals(Object obj)
  {
    if(this == obj) {
      return true;
    }
    if(obj == null) {
      return false;
    }
    if(getClass() != obj.getClass()) {
      return false;
    }
    final Position other = (Position) obj;
    if(this.index != other.index) {
      return false;
    }
    return this.direction == other.direction;
  }
}
