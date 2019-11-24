package de.nightara.solaris.joy.game;

import de.nightara.solaris.core.event.*;
import de.nightara.solaris.core.util.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import discord4j.core.object.entity.*;

import static de.nightara.solaris.core.util.Util.*;
import static de.nightara.solaris.core.util.database.DBConnector.*;

public abstract class Game<T extends Game> extends Event
{
  protected static final Duration QTE_DELAY = Duration.ofSeconds(15);

  private final User user;
  private final String cmd;
  private final String name;
  private final QuicktimeSpawner<T> qteSpawner;
  private final List<GameModifier<T>> modifiers;

  private int baseReward;
  private double rewardPercentage;

  public Game(String cmd, String name, User user, MessageChannel channel, Duration duration, int baseReward, QuicktimeSpawner<T> qteSpawner)
  {
    super(channel, duration);
    this.cmd = cmd;
    this.name = name;
    this.user = user;
    this.rewardPercentage = 1.0;
    this.qteSpawner = qteSpawner;
    this.baseReward = baseReward;
    this.modifiers = new LinkedList<>();
  }

  public String getCmd()
  {
    return cmd;
  }

  public String getName()
  {
    return name;
  }

  public User getUser()
  {
    return user;
  }


  public void addEventModifier(GameModifier<T> modifier)
  {
    modifiers.add(modifier);
    modifier.getQteModifier().accept(qteSpawner);
    baseReward = modifier.getBaseRewardModifier().apply(baseReward);
  }

  public int getReward()
  {
    return (int) (baseReward * rewardPercentage);
  }

  public int rewardUser()
  {
    int reward = -1;
    if(isOver())
    {
      reward = getReward();
      if(reward > 0)
      {
        changeMoney(getUser(), reward);
        sendMessage(getChannel(), getUser().getMention() + " Du hast " + reward + " Pokédollar vom Minispiel \"" + getName() + "\" erhalten!");
      }
      else
      {
        sendMessage(getChannel(), getUser().getMention() + " Du hast leider kein Geld vom Minispiel \"" + getName() + "\" erhalten.");
      }
    }
    return reward;
  }

  public void modifyRewardPercentage(Function<Double, Double> modifier)
  {
    rewardPercentage = modifier.apply(rewardPercentage);
  }

  public int play()
  {
    ScheduledFuture future = ThreadManager.submitRoutine(() ->
    {
      qteSpawner.generateQTE()
              .map(qte -> qte.apply(getThis()))
              .ifPresent(modifier -> modifyRewardPercentage(percent -> percent * modifier));
    }, QTE_DELAY.dividedBy(2), QTE_DELAY);
    Util.waitFor(this::isOver, QTE_DELAY);
    ThreadManager.stopRoutine(future);
    return rewardUser();
  }

  public T getThis()
  {
    return (T) this;
  }

  public abstract void setAnswer(Message msg);
}
