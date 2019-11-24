package de.nightara.solaris.joy.game;

import de.nightara.solaris.core.pokemon.*;
import java.util.function.*;

public class GameModifier<T extends Game> extends AbstractItem
{
  private final Function<Integer, Integer> baseRewardModifier;
  private final Consumer<QuicktimeSpawner<T>> qteModifier;

  public GameModifier(String name, int price,
          String description,
          Function<Integer, Integer> baseRewardModifier,
          Consumer<QuicktimeSpawner<T>> qteModifier)
  {
    super(name, price, description);
    this.qteModifier = qteModifier;
    this.baseRewardModifier = baseRewardModifier;
  }

  public Function<Integer, Integer> getBaseRewardModifier()
  {
    return baseRewardModifier;
  }

  public Consumer<QuicktimeSpawner<T>> getQteModifier()
  {
    return qteModifier;
  }
}
