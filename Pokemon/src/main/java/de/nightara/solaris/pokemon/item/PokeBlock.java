package de.nightara.solaris.pokemon.item;

import de.nightara.solaris.core.pokemon.*;
import de.nightara.solaris.core.pokemon.Pokemon.Type;
import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static de.nightara.solaris.core.util.Util.biFunctionIdentitySecond;

public class PokeBlock extends EncounterModifier implements Item
{
  public enum BlockType
  {
    DEFAULT(500, 1.0, Duration.ofMinutes(2), "normaler"),
    LUXURY(2000, 2.0, Duration.ofMinutes(5), "edler");

    private final int price;
    private final Duration duration;
    private final String description;
    private final double shinyImprovement;

    BlockType(int price, double shinyImprovement, Duration duration, String description)
    {
      this.price = price;
      this.duration = duration;
      this.description = description;
      this.shinyImprovement = shinyImprovement;
    }

    public int getPrice()
    {
      return price;
    }

    public Duration getDuration()
    {
      return duration;
    }

    public String getDescription()
    {
      return description;
    }

    public double getShinyImprovement()
    {
      return shinyImprovement;
    }
  };

  private static final List<PokeBlock> BLOCKS = new LinkedList<>();
  public static final PokeBlock RED = new PokeBlock("Pokériegel", "Rot", Type.FIRE, BlockType.DEFAULT);
  public static final PokeBlock GRAY = new PokeBlock("Pokériegel", "Grau", Type.STEEL, BlockType.DEFAULT);
  public static final PokeBlock BLUE = new PokeBlock("Pokériegel", "Blau", Type.WATER, BlockType.DEFAULT);
  public static final PokeBlock ROSE = new PokeBlock("Pokériegel", "Rosa", Type.FAIRY, BlockType.DEFAULT);
  public static final PokeBlock GREEN = new PokeBlock("Pokériegel", "Grün", Type.GRASS, BlockType.DEFAULT);
  public static final PokeBlock OCHER = new PokeBlock("Pokériegel", "Ocker", Type.ROCK, BlockType.DEFAULT);
  public static final PokeBlock WHITE = new PokeBlock("Pokériegel", "Weiß", Type.NORMAL, BlockType.DEFAULT);
  public static final PokeBlock TERRA = new PokeBlock("Pokériegel", "Lehm", Type.GROUND, BlockType.DEFAULT);
  public static final PokeBlock LILAC = new PokeBlock("Pokériegel", "Lila", Type.DRAGON, BlockType.DEFAULT);
  public static final PokeBlock PINK = new PokeBlock("Pokériegel", "Pink", Type.PSYCHIC, BlockType.DEFAULT);
  public static final PokeBlock LBLUE = new PokeBlock("Pokériegel", "Hellblau", Type.ICE, BlockType.DEFAULT);
  public static final PokeBlock BLACK = new PokeBlock("Pokériegel", "Schwarz", Type.DARK, BlockType.DEFAULT);
  public static final PokeBlock YGREEN = new PokeBlock("Pokériegel", "Gelbgrün", Type.BUG, BlockType.DEFAULT);
  public static final PokeBlock YELLOW = new PokeBlock("Pokériegel", "Gelb", Type.ELECTRIC, BlockType.DEFAULT);
  public static final PokeBlock BLILAC = new PokeBlock("Pokériegel", "Blaulila", Type.GHOST, BlockType.DEFAULT);
  public static final PokeBlock BROWN = new PokeBlock("Pokériegel", "Braun", Type.FIGHTNING, BlockType.DEFAULT);
  public static final PokeBlock MBLUE = new PokeBlock("Pokériegel", "Mattblau", Type.FLYING, BlockType.DEFAULT);
  public static final PokeBlock VIOLET = new PokeBlock("Pokériegel", "Violett", Type.POISON, BlockType.DEFAULT);

  private final int price;
  private final Type type;
  private final String name;
  private final String color;
  private final String description;
  private final double shinyImprovement;

  public PokeBlock(String name, String color, Type type, BlockType blockType)
  {
    this(name + " (" + color + ")", color, type, blockType.getPrice(), blockType.getDuration(), blockType.getShinyImprovement(), "Ein " + blockType.getDescription() + " Pokériegel, der shiny Pokémon anlockt. "
            + "Pokémon vom Typ " + type.getName() + " scheinen ihn besonders zu mögen.");
  }

  public PokeBlock(String name, String color, Type type, int price, Duration duration, double shinyImprovement, String description)
  {
    super(duration);
    this.name = name;
    this.type = type;
    this.price = price;
    this.description = description;
    this.color = color.toLowerCase();
    this.shinyImprovement = shinyImprovement;
    BLOCKS.add(this);
  }

  @Override
  public BiFunction<Pokemon, Double, Double> fleeRateModifier()
  {
    return biFunctionIdentitySecond();
  }

  @Override
  public BiFunction<Pokemon, Double, Double> catchRateModifier()
  {
    return biFunctionIdentitySecond();
  }

  @Override
  public BiFunction<Pokemon, Double, Double> shinyRateModifier()
  {
    if(isActive())
    {
      return (p, d) -> d * (1 + getShinyImprovement(p) * (1 - d));
    }
    else
    {
      return biFunctionIdentitySecond();
    }
  }

  public double getShinyImprovement(Pokemon target)
  {
    return target.getTypes().contains(getType()) ? shinyImprovement : 0;
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

  public String getColor()
  {
    return color;
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
    return "einen " + getName();
  }

  public static Stream<PokeBlock> stream()
  {
    return BLOCKS.stream();
  }

  public static Optional<PokeBlock> parse(String color)
  {
    String lowerColor = color.toLowerCase();
    return stream()
            .filter(block -> block.getColor().equals(lowerColor))
            .findAny();
  }
}
