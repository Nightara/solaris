package de.nightara.solaris.core.pokemon;

import de.nightara.solaris.core.util.database.DBConnector;
import de.nightara.solaris.core.pokemon.Pokemon.Type;
import de.nightara.solaris.core.util.*;
import gnu.trove.map.hash.*;
import java.util.*;

import static de.nightara.solaris.core.pokemon.Move.MoveType.*;
import static de.nightara.solaris.core.pokemon.Pokemon.Stat.*;
import static de.nightara.solaris.core.pokemon.Pokemon.Type.*;

public class Move
{
  public enum MoveType
  {
    PHYSICAL, SPECIAL;
  }

  public static final Move ERROR_SPECIAL = new Move(UNTYPED, "Error", SPECIAL);
  public static final Move ERROR_PHYSICAL = new Move(UNTYPED, "Error", PHYSICAL);
  public static final TwinTuple<Move> ERROR_MOVES = new TwinTuple<>(ERROR_PHYSICAL, ERROR_SPECIAL);

  private static final Map<Type, TwinTuple<Move>> MOVES = new THashMap<>();

  private final Type type;
  private final String name;
  private final int basePower;
  private final MoveType moveType;

  public Move(Type type, String name, MoveType moveType)
  {
    this.type = type;
    this.name = name;
    this.basePower = 90;
    this.moveType = moveType;
  }

  public String getName()
  {
    return name;
  }

  public MoveType getMoveType()
  {
    return moveType;
  }

  public Type getType()
  {
    return type;
  }

  public int getBasePower()
  {
    return basePower;
  }

  public int calcDamage(Pokemon atk, Pokemon def)
  {
    double attack = atk.getStat(getMoveType() == PHYSICAL ? Atk : SpA);
    double defense = def.getStat(getMoveType() == PHYSICAL ? Def : SpD);
    return (int) Math.ceil((2 + atk.getLevel() * 0.4 * getBasePower() / 50) * (attack / defense) * DBConnector.getTypeEffectivity(getType(), def.getTypes()));
  }

  public static TwinTuple<Move> get(Type type)
  {
    if(!MOVES.containsKey(type))
    {
      MOVES.put(type, DBConnector.getMoves(type).orElse(ERROR_MOVES));
    }
    return MOVES.get(type);
  }

  public static Optional<Move> parse(String name)
  {
    String lowerName = name.toLowerCase();
    Optional<Move> cached = MOVES.values().stream()
            .flatMap(Collection::stream)
            .filter(move -> move.getName().toLowerCase().equals(lowerName))
            .findAny();
    return cached.isPresent() ? cached : DBConnector.getMoveByName(name);
  }
}
