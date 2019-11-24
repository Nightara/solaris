package de.nightara.solaris.core.pokemon;

import de.nightara.solaris.core.util.*;
import java.util.*;

import static de.nightara.solaris.core.pokemon.Pokemon.Stat.*;

public class Pokemon
{
  public enum Stage
  {
    BASE(0), STAGE_ONE(1), STAGE_TWO(2);

    private final int intVal;

    private Stage(int intVal)
    {
      this.intVal = intVal;
    }

    public int intVal()
    {
      return intVal;
    }

    @Override
    public String toString()
    {
      return "" + intVal();
    }

    public static Stage get(int intVal)
    {
      for(Stage t : values())
      {
        if(t.intVal() == intVal)
        {
          return t;
        }
      }
      return BASE;
    }
  }

  public enum Type
  {
    UNTYPED("???"), GROUND("Boden"), ICE("Eis"),
    DRAGON("Drache"), FAIRY("Fee"), FIRE("Feuer"),
    FLYING("Flug"), ELECTRIC("Elektro"), GHOST("Geist"),
    ROCK("Gestein"), POISON("Gift"), FIGHTNING("Kampf"),
    BUG("Käfer"), NORMAL("Normal"), DARK("Unlicht"),
    GRASS("Pflanze"), PSYCHIC("Psycho"), STEEL("Stahl"),
    WATER("Wasser");

    private final String dbName;

    private Type(String dbName)
    {
      this.dbName = dbName;
    }

    public String getName()
    {
      return dbName;
    }

    @Override
    public String toString()
    {
      return getName();
    }

    public static Type get(String dbName)
    {
      for(Type t : values())
      {
        if(t.getName().equals(dbName))
        {
          return t;
        }
      }
      return UNTYPED;
    }
  };

  public enum Stat
  {
    HP, Atk, Def, SpA, SpD, Init;
  }

  public static final int MAX_LEVEL = 100;
  public static final int DEFAULT_EXP = 0;
  public static final int DEFAULT_LEVEL = 1;

  private int exp;
  private int boxId;
  private int level;
  private String nickname;

  private final int id;
  private final int baseHp;
  private final int baseAtk;
  private final int baseDef;
  private final int baseSpA;
  private final int baseSpD;
  private final int baseInit;

  private final String name;
  private final Stage stage;
  private final boolean shiny;
  private final Type primaryType;
  private final Type secondaryType;
  private final List<Integer> evolutions;

  public Pokemon(int id, String name, Stage stage, Type primaryType, Type secondaryType, Collection<Integer> evolutions,
          int baseHp, int baseAtk, int baseDef, int baseSpA, int baseSpD, int baseInit,
          boolean shiny, int level, int exp)
  {
    this(id, name, stage, primaryType, secondaryType, evolutions,
         baseHp, baseAtk, baseDef, baseSpA, baseSpD, baseInit,
         shiny, level, exp, -1, null);
  }

  public Pokemon(int id, String name, Stage stage, Type primaryType, Type secondaryType, Collection<Integer> evolutions,
          int baseHp, int baseAtk, int baseDef, int baseSpA, int baseSpD, int baseInit,
          boolean shiny, int level, int exp, int dbId, String nickname)
  {
    this.id = id;
    this.exp = exp;
    this.boxId = dbId;
    this.name = name;
    this.stage = stage;
    this.level = level;
    this.shiny = shiny;
    this.baseHp = baseHp;
    this.baseAtk = baseAtk;
    this.baseDef = baseDef;
    this.baseSpA = baseSpA;
    this.baseSpD = baseSpD;
    this.baseInit = baseInit;
    this.nickname = nickname;
    this.primaryType = primaryType;
    this.secondaryType = secondaryType;
    this.evolutions = new LinkedList<>(evolutions);
  }

  public int getId()
  {
    return id;
  }

  public String getName()
  {
    return name;
  }
  
  public Optional<String> getNickname()
  {
    return Optional.ofNullable(nickname);
  }
  
  public void setNickname(String nickname)
  {
    this.nickname = nickname;
  }

  public int getLevel()
  {
    return level;
  }

  public Type getPrimaryType()
  {
    return primaryType;
  }

  public Type getSecondaryType()
  {
    return secondaryType;
  }

  public int getStat(Stat stat)
  {
    return Pokemon.getStat(stat, getBase(stat), getLevel());
  }

  public boolean isShiny()
  {
    return shiny;
  }

  public TwinTuple<Type> getTypes()
  {
    return new TwinTuple<>(primaryType, secondaryType);
  }

  public Tuple<TwinTuple<Move>, TwinTuple<Move>> getMoves()
  {
    return new Tuple<>(Move.get(Type.UNTYPED), Move.get(getPrimaryType()));
  }

  public int getBase(Stat stat)
  {
    switch(stat)
    {
      case HP: return baseHp;
      case Atk: return baseAtk;
      case Def: return baseDef;
      case SpA: return baseSpA;
      case SpD: return baseSpD;
      case Init: return baseInit;
    }
    return -1;
  }

  public Stage getStage()
  {
    return stage;
  }

  public int getExp()
  {
    return exp;
  }

  public Optional<Integer> getBoxId()
  {
    return Optional.of(boxId).filter(bid -> bid > 0);
  }
  
  public void setBoxId(int boxId)
  {
    this.boxId = boxId;
  }

  public boolean hasBoxId()
  {
    return boxId > 0;
  }

  public List<Integer> getEvolutions()
  {
    return evolutions;
  }

  public boolean canEvolve()
  {
    return !getEvolutions().isEmpty()
            && (getStage() == Stage.BASE && getLevel() >= 30) || (getStage() == Stage.STAGE_ONE && getLevel() >= 50);
  }

  public boolean addExp(int exp)
  {
    if(exp > 0)
    {
      this.exp += exp;
      if((this.exp) >= getExpForNextLevel() && getLevel() < MAX_LEVEL)
      {
        this.exp %= getExpForNextLevel();
        level++;
        return true;
      }
    }
    return false;
  }

  public int getExpForNextLevel()
  {
    return getExpForNextLevel(getLevel());
  }

  public int getExpGainForKill()
  {
    return getExpGainForKill(getLevel());
  }

  public Pokemon createNewPokemon(boolean shiny)
  {
    return new Pokemon(getId(), getName(), getStage(), getPrimaryType(), getSecondaryType(), getEvolutions(),
                       getBase(HP), getBase(Atk), getBase(Def), getBase(SpA), getBase(SpD), getBase(Init),
                       isShiny() || shiny, getLevel(), getExp());
  }

  public static int getExpForNextLevel(int level)
  {
    return level < MAX_LEVEL ? level * 5 : 0;
  }

  public static int getExpGainForKill(int level)
  {
    return 10 + level;
  }

  public static int getStat(Stat stat, int base, int level)
  {
    return (int) Math.floor((stat == HP ? 10 + level : 5) + (base * 2) * (level * 0.01));
  }
}
