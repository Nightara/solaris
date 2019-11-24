package de.nightara.solaris.pokemon.item;

import de.nightara.solaris.core.pokemon.*;
import java.util.*;
import java.util.stream.*;

public class Ball extends AbstractItem
{
  private static final List<Ball> BALLS = new LinkedList<>();
  public static final Ball POKEBALL = new Ball("Pokéball", 50, 1, "Ein einfacher Pokéball, um wilde Pokémon einzufangen.");
  public static final Ball SUPERBALL = new Ball("Superball", 200, 5, "Ein verbesserter Pokéball mit einer höheren Fangchance.");
  public static final Ball HYPERBALL = new Ball("Hyperball", 500, 10, "Ein verbesserter Pokéball mit einer deutlich höheren Fangchance.");

  private final int catchModifier;

  private Ball(String name, int price, int catchModifier, String description)
  {
    super(name, price, description);
    this.catchModifier = catchModifier;
    BALLS.add(this);
  }

  public int getCatchModifier()
  {
    return catchModifier;
  }

  public static Stream<Ball> stream()
  {
    return BALLS.stream();
  }
}
