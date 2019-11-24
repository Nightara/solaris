package de.nightara.solaris.joy.game.mine;

import de.nightara.solaris.joy.game.mine.MineUser.Role;
import de.nightara.solaris.joy.game.mine.Position.Direction;
import discord4j.core.object.entity.*;
import gnu.trove.map.hash.*;
import java.util.*;

public class Zone
{
  private static final int LEVELS = 3;

  private final String name;
  private final Position root;
  private final Map<User, MineUser> participants;

  public Zone(String name)
  {
    this(name, LEVELS);
  }

  public Zone(String name, int levels)
  {
    this.name = name;
    this.participants = new THashMap<>();

    List<Position> tmp = new ArrayList<>();
    for(int x = 0; x < levels; x++)
    {
      Position center = new Position(x, Direction.CENTER);
      tmp.forEach(position -> position.addNext(center));

      Position left = new Position(x, Direction.LEFT);
      center.addNext(left);

      Position right = new Position(x, Direction.RIGHT);
      center.addNext(right);

      tmp = Arrays.asList(left, right);
    }

    root = getPosition(1, Direction.CENTER).get();
  }

  public String getName()
  {
    return name;
  }

  public boolean contains(User u)
  {
    return participants.containsKey(u);
  }

  public Optional<MineUser> getMineUser(User u)
  {
    return Optional.ofNullable(participants.get(u));
  }

  public double getWorkerRatio()
  {
    return participants.values().stream()
            .filter(Role.WORKER::equals)
            .count() / (double) participants.size();
  }

  public final Optional<Position> getPosition(int index, Direction dir)
  {
    return root.stream()
            .filter(p -> p.getIndex() == index)
            .filter(p -> p.getDirection() == dir)
            .findAny();
  }

  public boolean addUser(User user, MineUser role)
  {
    if(!participants.containsKey(user) || participants.get(user) != role)
    {
      removeUser(user);
      participants.put(user, role);
      return true;
    }
    return false;
  }

  public boolean removeUser(User user)
  {
    return participants.remove(user) != null;
  }

  public boolean startMining(User user)
  {
    return getMineUser(user)
            .filter(mu -> mu.getRole() == Role.WORKER)
            .map(mu -> mu.setPosition(root))
            .isPresent();
  }

  @Override
  public int hashCode()
  {
    int hash = 3;
    hash = 29 * hash + Objects.hashCode(this.name);
    return hash;
  }

  @Override
  public boolean equals(Object obj)
  {
    if(this == obj)
    {
      return true;
    }
    if(obj == null)
    {
      return false;
    }
    if(getClass() != obj.getClass())
    {
      return false;
    }
    final Zone other = (Zone) obj;
    return Objects.equals(this.name, other.name);
  }
}
