package de.nightara.solaris.pokemon;

import de.nightara.solaris.core.event.*;
import de.nightara.solaris.core.pokemon.*;
import de.nightara.solaris.core.pokemon.Pokemon.*;
import de.nightara.solaris.core.util.database.*;
import de.nightara.solaris.pokemon.item.*;
import discord4j.core.object.entity.*;
import gnu.trove.set.hash.*;
import org.w3c.dom.*;

import java.time.*;
import java.util.*;

import static de.nightara.solaris.core.util.Util.*;
import static de.nightara.solaris.core.pokemon.Pokemon.Stat.*;

public class Encounter extends Event
{
  public static final Duration DEFAULT_DURATION = Duration.ofMinutes(10);

  private final Pokemon template;
  private final double baseFleeRate;
  private final double baseCatchRate;
  private final double baseShinyRate;
  private final Set<User> alreadyCaught;
  private final List<EncounterModifier> encounterModifiers;

  private boolean fled;

  public Encounter(Pokemon pokemon, MessageChannel channel)
  {
    this(pokemon, channel, DEFAULT_DURATION, Pokemon.DEFAULT_LEVEL);
  }

  public Encounter(Pokemon template, MessageChannel channel, Duration duration, int level)
  {
    super(channel, duration);
    this.fled = false;
    this.template = template;
    this.baseShinyRate = 1.0 / 4096;
    this.alreadyCaught = new THashSet<>();
    this.encounterModifiers = new LinkedList<>();
    this.baseFleeRate = 1.0 / (10 + template.getBase(HP));
    this.baseCatchRate = 2000.0 / Arrays.stream(Stat.values())
            .mapToInt(template::getBase)
            .map(i -> i * i)
            .sum();
    sendMessage(getChannel(), "Eine Horde wilder " + template.getName() + " ist erschienen!");
    ((TextChannel) getChannel()).edit(spec -> spec.setTopic("Eine Horde wilder " + template.getName() + " ist erschienen!")).block();
  }

  public Pokemon getTemplate()
  {
    return template;
  }

  public Pokemon createPokemon(boolean shiny)
  {
    return getTemplate().createNewPokemon(shiny);
  }

  public Optional<String> addEncounterModifier(User user, Optional<? extends EncounterModifier> mod)
  {
    return mod.map(m -> addEncounterModifier(user, m));
  }

  public String addEncounterModifier(User user, EncounterModifier mod)
  {
    addEncounterModifier(mod);
    return user.getMention() + " hat den Fangversuch durch " + mod.toString() + " einfacher gemacht!";
  }

  protected void addEncounterModifier(EncounterModifier mod)
  {
    encounterModifiers.add(mod);
  }

  public double getFleeRate()
  {
    return isOver() ? 1 : encounterModifiers.stream()
            .map(EncounterModifier::fleeRateModifier)
            .reduce((f1, f2) -> f1.andThen(d -> f2.apply(template, d)))
            .orElse(biFunctionIdentitySecond())
            .apply(template, baseFleeRate);
  }

  public double getCatchRate()
  {
    return isOver() ? 0 : encounterModifiers.stream()
            .map(EncounterModifier::catchRateModifier)
            .reduce((f1, f2) -> f1.andThen(d -> f2.apply(template, d)))
            .orElse(biFunctionIdentitySecond())
            .apply(template, baseCatchRate);
  }

  public double getShinyRate()
  {
    return encounterModifiers.stream()
            .map(EncounterModifier::shinyRateModifier)
            .reduce((f1, f2) -> f1.andThen(d -> f2.apply(template, d)))
            .orElse(biFunctionIdentitySecond())
            .apply(template, baseShinyRate);
  }

  @Override
  public boolean isOver()
  {
    return super.isOver() || fled;
  }

  public boolean canCatch(User user)
  {
    return !alreadyCaught.contains(user);
  }

  public synchronized String attemptCatch(User user, Ball ball)
  {
    if(Math.random() - (ball.getCatchModifier() / 100.0) < getCatchRate())
    {
      alreadyCaught.add(user);
      boolean shiny = Math.random() < getShinyRate();
      DBConnector.storePokemon(user, createPokemon(shiny));
      return user.getMention() + " hat ein " + (shiny ? "shiny " : "") + getTemplate().getName() + " gefangen!";
    }
    else if(Math.random() < getFleeRate())
    {
      this.fled = true;
      if(getChannel() instanceof TextChannel)
      {
        ((TextChannel) getChannel()).edit(spec -> spec.setTopic("")).block();
      }
      return "Die wilden Pokémon sind geflüchtet!";
    }
    else
    {
      return "Das " + getTemplate().getName() + " ist aus dem Ball von " + user.getMention() + " ausgebrochen!";
    }
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 53 * hash + super.hashCode();
    hash = 53 * hash + Objects.hashCode(this.template);
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
    final Encounter other = (Encounter) obj;
    if(!super.equals(other))
    {
      return false;
    }
    return Objects.equals(this.template, other.template);
  }
}
