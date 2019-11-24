package de.nightara.solaris.joy.game;

import java.time.*;
import java.util.*;
import java.util.function.*;

public class QuicktimeSpawner<T extends Game>
{
  public static final double DEFAULT_SPAWNRATE = 1.0;
  public static final Duration DEFAULT_DELAY = Duration.ofMinutes(1);

  private final Function<T, Double> eventSupplier;

  private Duration delay;
  private Instant lastQTE;
  private double spawnRate;

  public QuicktimeSpawner(Function<T, Double> eventSupplier)
  {
    this(DEFAULT_DELAY, DEFAULT_SPAWNRATE, eventSupplier);
  }

  public QuicktimeSpawner(Duration delay, double spawnRate,
          Function<T, Double> eventSupplier)
  {
    this.delay = delay;
    this.lastQTE = Instant.MIN;
    this.spawnRate = spawnRate;
    this.eventSupplier = eventSupplier;
  }

  public void modifyDelay(Function<Duration, Duration> modifier)
  {
    delay = modifier.apply(delay);
  }
  
  public void modifySpawnRate(Function<Double, Double> modifier)
  {
    spawnRate = modifier.apply(spawnRate);
  }

  public Optional<Function<T, Double>> generateQTE()
  {
    if(Instant.now().isAfter(lastQTE.plus(delay))
            && Math.random() < spawnRate)
    {
      return Optional.of(eventSupplier);
    }
    return Optional.empty();
  }
}
