package de.nightara.solaris.core.pokemon;

import de.nightara.solaris.core.pokemon.Pokemon.Type;
import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static de.nightara.solaris.core.util.Util.*;

public class Berry extends EncounterModifier implements Item
{
  private static final List<Berry> BERRIES = new LinkedList<>();

  public static final Berry RAZZBERRY = new Berry("Himmihbeere", Type.NORMAL, 50, Duration.ofMinutes(2), 0.1,
          "Eine einfache Beere, die wilde Pokémon beruhigt und den Fangversuch für alle Spieler etwas leichter macht.");
  public static final Berry NANABBERRY = new Berry("Nanabbeere", Type.DRAGON, 200, Duration.ofMinutes(5), 0.2,
          "Eine etwas seltenere Beere, die wilde Pokémon beruhigt und den Fangversuch für alle Spieler leichter macht.");

  private final int price;
  private final Type type;
  private final String name;
  private final String description;
  private final double catchImprovement;

  public Berry(String name, Type type, int price, Duration duration, double catchImprovement, String description)
  {
    super(duration);
    this.name = name;
    this.type = type;
    this.price = price;
    this.description = description;
    this.catchImprovement = catchImprovement;
    BERRIES.add(this);
  }

  @Override
  public BiFunction<Pokemon, Double, Double> fleeRateModifier()
  {
    if(isActive())
    {
      return (p, d) -> 0.0;
    }
    else
    {
      return (p, d) -> d * 1.05;
    }
  }

  @Override
  public BiFunction<Pokemon, Double, Double> catchRateModifier()
  {
    if(isActive())
    {
      return (p, d) -> d * (1 + getCatchImprovement() * (1 - d));
    }
    else
    {
      return biFunctionIdentitySecond();
    }
  }

  @Override
  public BiFunction<Pokemon, Double, Double> shinyRateModifier()
  {
    return biFunctionIdentitySecond();
  }

  public double getCatchImprovement()
  {
    return catchImprovement;
  }

  @Override
  public String getName()
  {
    return name;
  }

  @Override
  public int getPrice()
  {
    return price;
  }
  
  public Type getType()
  {
    return type;
  }

  @Override
  public String getDesc()
  {
    return description;
  }

  @Override
  public String toString()
  {
    return "eine " + getName();
  }

  public static Stream<Berry> stream()
  {
    return BERRIES.stream();
  }
}
