package de.nightara.solaris.core.util;

import discord4j.core.*;
import discord4j.core.event.domain.*;
import discord4j.core.object.entity.*;

public class GuildSelectEvent extends Event
{
  private final Guild guild;

  public GuildSelectEvent(DiscordClient client, Guild guild)
  {
    super(client);
    this.guild = guild;
  }

  public Guild getGuild()
  {
    return this.guild;
  }
}
