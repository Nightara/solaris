package de.nightara.solaris.core;

import de.nightara.solaris.core.event.*;
import de.nightara.solaris.core.pokemon.*;
import de.nightara.solaris.core.util.*;
import discord4j.core.*;
import discord4j.core.event.domain.lifecycle.*;
import discord4j.core.event.domain.message.*;
import discord4j.core.object.data.stored.*;
import discord4j.core.object.entity.*;
import discord4j.core.object.presence.*;
import discord4j.core.object.util.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import static de.nightara.solaris.core.util.Util.*;

public abstract class DiscordBot
{
  public static final Path RESOURCES = Paths.get("resources");

  protected final Path configFile;
  protected final Properties config;
  protected final DiscordClient discord;
  private final List<DiscordCommand> commands;

  private Guild guild;

  public DiscordBot(String configFile)
  {
    this.config = new Properties();
    this.commands = new LinkedList<>();
    this.configFile = RESOURCES.resolve(configFile);
    try
    {
      this.config.loadFromXML(Files.newInputStream(this.configFile));
    }
    catch(IOException ex)
    {
      LOG.error("Critical IO error while reading config file {}", this.configFile);
      System.exit(1);
    }
    this.discord = new DiscordClientBuilder(config.getProperty("loginToken"))
            .build();

    addDiscordCommand(new DiscordCommand("help", "@<Bot>", "Zeigt diese Liste an.",
                                         msg -> msg.getUserMentions().any(discord.getSelf().block()::equals).block(),
                                         msg ->
                                 {
                                   StringBuilder content = new StringBuilder("```\n");
                                   commands.stream()
                                           .map(DiscordCommand::getHelp)
                                           .reduce(StringBuilder::append)
                                           .ifPresent(content::append);
                                   sendMessage(msg.getChannel().block(), content.append("```"));
                                 }, "info"));
    addDiscordCommand(new DiscordCommand("shop", "@<Bot>", "Durchsuche den Shop nach interessanten Items.",
                                         msg -> msg.getUserMentions().any(discord.getSelf().block()::equals).block(),
                                         msg ->
                                 {
                                   StringBuilder content = new StringBuilder("```diff\n- Ich habe folgende Items im Angebot:\n");
                                   streamItems()
                                           .map(Item::toShopLine)
                                           .forEach(content::append);
                                   sendPMOrNotify(msg.getAuthor(), content.append("```"), msg.getChannel().block());
                                 }));

    discord.getEventDispatcher().on(MessageCreateEvent.class)
        .filter(evt -> evt.getMessage().getContent().orElse("").startsWith("" + DiscordCommand.PREFIX))
        .subscribe(evt ->
        {
          Message msg = evt.getMessage();
          if(msg.getContent().orElse("").startsWith(DiscordCommand.PREFIX + "shutdown")
              && msg.getUserMentions().any(u -> u.equals(discord.getSelf().block())).block()
              && isAdmin(msg.getAuthor().get(), guild))
          {
            ThreadManager.getRoutines().forEach(ThreadManager::stopRoutine);
            discord.logout().block();
          }
        });

    discord.getEventDispatcher().on(ReadyEvent.class).subscribe(evt -> {
      System.out.println("test");
      PresenceBean bean = new PresenceBean();
      bean.setStatus(config.getProperty("statusMessage"));
      discord.updatePresence(new Presence(bean));
      setGuild(config.getProperty("guildId"));
    });
  }

  public void launch()
  {
    discord.login().block();
  }

  public abstract Stream<? extends Item> streamItems();

  public final Guild setGuild(String gid)
  {
    try
    {
      return setGuild(Long.parseLong(gid));
    }
    catch(NumberFormatException ex)
    {
      LOG.error("Guild ID {} is not valid.", gid);
    }
    return null;
  }

  public Guild setGuild(long gid)
  {
    try
    {
      guild = discord.getGuildById(Snowflake.of(gid)).block();
      LOG.info("Selected guild {}", guild.getName());
      discord.getEventDispatcher().publish(new GuildSelectEvent(discord, guild));
    }
    catch(NullPointerException ex)
    {
      LOG.error("Guild ID {} is not valid.", gid);
    }
    return guild;
  }

  public Guild getGuild()
  {
    return guild;
  }

  public final void addDiscordCommand(DiscordCommand cmd)
  {
    discord.getEventDispatcher().on(MessageCreateEvent.class).subscribe(cmd::handle);
    commands.add(cmd);
  }
}
