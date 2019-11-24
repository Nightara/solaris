package de.nightara.solaris.core;

import de.nightara.solaris.core.pokemon.*;
import de.nightara.solaris.core.util.*;
import java.util.stream.*;

import discord4j.core.object.entity.*;
import org.junit.*;

import static org.junit.Assert.*;

public class DiscordBotTest
{

  @Test
  public void testConnectivity()
  {
    DiscordBot bot = new DiscordBot("config.xml")
    {
      @Override
      public Stream<Item> streamItems()
      {
        return Stream.empty();
      }
    };
    bot.launch();
    Guild guild = bot.getGuild();
    assertTrue(guild != null);
    //assertTrue(Util.sendMessage(guild.getDefaultChannel(), "Hallo, " + guild.getName() + "!")
    //        .isPresent());
  }
}
