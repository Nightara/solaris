package de.nightara.solaris.joy.game.mine;

import de.nightara.solaris.joy.game.*;
import java.time.*;
import java.util.*;
import discord4j.core.object.entity.*;

public class MineGame extends Game<MineGame>
{
  public static final String CMD = "untergrund";
  public static final String NAME = "Untergrundmine";
  public static final String WORKER_CMD = "minenarbeit";
  public static final String TRAPPER_CMD = "fallensteller";

  private static final List<Zone> ZONES = new LinkedList<>();

  public MineGame(String cmd, String name, User user, MessageChannel channel, Duration duration, int baseReward, QuicktimeSpawner<MineGame> qteSpawner)
  {
    super(cmd, name, user, channel, duration, baseReward, qteSpawner);
  }

  @Override
  public void setAnswer(Message msg)
  {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
}
