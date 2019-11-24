package de.nightara.solaris.core.pokemon;

import java.time.*;
import java.util.function.*;

public abstract class EncounterModifier
{
  private final Instant start;
  private final Duration duration;

  public EncounterModifier()
  {
    this(Duration.ZERO);
  }

  public EncounterModifier(Duration duration)
  {
    this.duration = duration;
    this.start = Instant.now();
  }

  public abstract BiFunction<Pokemon, Double, Double> fleeRateModifier();

  public abstract BiFunction<Pokemon, Double, Double> catchRateModifier();

  public abstract BiFunction<Pokemon, Double, Double> shinyRateModifier();

  public Instant getStart()
  {
    return start;
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
    return getDuration() != Duration.ZERO
            && Instant.now().isAfter(getStart().plus(getDuration()));
  }

  public static EncounterModifier create(BiFunction<Pokemon, Double, Double> catchRateModifier,
          BiFunction<Pokemon, Double, Double> fleeRateModifier,
          BiFunction<Pokemon, Double, Double> shinyRateModifier,
          String desc)
  {
    return new EncounterModifier()
    {
      @Override
      public BiFunction<Pokemon, Double, Double> catchRateModifier()
      {
        return catchRateModifier;
      }

      @Override
      public BiFunction<Pokemon, Double, Double> fleeRateModifier()
      {
        return fleeRateModifier;
      }

      @Override
      public BiFunction<Pokemon, Double, Double> shinyRateModifier()
      {
        return shinyRateModifier;
      }
      
      @Override
      public String toString()
      {
        return desc;
      }
    };
  }

  @Override
  public abstract String toString();
}
