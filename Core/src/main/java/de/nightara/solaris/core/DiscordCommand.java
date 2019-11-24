package de.nightara.solaris.core;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import discord4j.core.object.entity.*;
import discord4j.core.event.domain.message.*;

import static de.nightara.solaris.core.util.Util.*;

public class DiscordCommand implements Predicate<MessageCreateEvent>
{
  public static final char PREFIX = '!';

  private final String desc;
  private final String usage;
  private final boolean hasArgs;
  private final List<String> cmd;
  private final Consumer<Message> onError;
  private final Consumer<Message> consumer;
  private final Predicate<Message> predicate;

  public DiscordCommand(String cmd, String desc, Consumer<Message> consumer, String... alias)
  {
    this(cmd, desc, msg -> true, consumer, alias);
  }

  public DiscordCommand(String cmd, String desc, Predicate<Message> predicate, Consumer<Message> consumer, String... alias)
  {
    this(cmd, desc, predicate, getNOP(), consumer, alias);
  }

  public DiscordCommand(String cmd, String desc, Predicate<Message> predicate, Consumer<Message> onError, Consumer<Message> consumer, String... alias)
  {
    this(cmd, "", desc, predicate, onError, consumer, alias);
  }

  public DiscordCommand(String cmd, String usage, String desc, Consumer<Message> consumer, String... alias)
  {
    this(cmd, usage, desc, msg -> true, consumer, alias);
  }

  public DiscordCommand(String cmd, String usage, String desc, Predicate<Message> predicate, Consumer<Message> consumer, String... alias)
  {
    this(cmd, usage, desc, predicate, getNOP(), consumer, alias);
  }

  public DiscordCommand(String cmd, String usage, String desc, Predicate<Message> predicate, Consumer<Message> onError, Consumer<Message> consumer, String... alias)
  {
    this.desc = desc;
    this.usage = usage;
    this.onError = onError;
    this.consumer = consumer;
    this.predicate = predicate;
    this.hasArgs = usage != null && !usage.isEmpty();
    this.cmd = Arrays.stream(alias)
            .map(String::toLowerCase)
            .collect(Collectors.toList());
    this.cmd.add(0, cmd.toLowerCase());
  }

  public boolean hasArgs()
  {
    return hasArgs;
  }

  public boolean testArgs(int argCount)
  {
    String[] split = usage.split(" ");
    long minArgCount = Arrays.stream(split)
            .filter(arg -> !arg.startsWith("(") && !arg.endsWith(")"))
            .count();
    return argCount == split.length
            || argCount >= minArgCount;
  }

  public String getDesc()
  {
    return desc;
  }

  public String getUsage()
  {
    return usage;
  }

  public StringBuilder getHelp()
  {
    StringBuilder sb = new StringBuilder(cmd.stream()
            .map(name -> PREFIX + name)
            .collect(Collectors.joining(", ")));
    sb.append("\nBenutzung: ")
            .append(PREFIX)
            .append(cmd.get(0))
            .append(' ')
            .append(usage)
            .append('\n')
            .append(desc)
            .append("\n\n");
    return sb;
  }

  public void handle(MessageCreateEvent event)
  {
    if(test(event))
    {
      deleteMessage(event.getMessage());
      consumer.accept(event.getMessage());
    }
  }

  @Override
  public boolean test(MessageCreateEvent event)
  {
    if(testGeneral(event))
    {
      if(predicate.test(event.getMessage()))
      {
        return true;
      }
      else
      {
        onError.accept(event.getMessage());
      }
    }
    return false;
  }

  private boolean testGeneral(MessageCreateEvent event)
  {
    String[] message = event.getMessage().getContent().orElse("").toLowerCase().split(" ");
    return !event.getMessage().getAuthor().map(User::isBot).orElse(true)
            && message.length > 0
            && message[0].length() > 1
            && message[0].charAt(0) == PREFIX
            && cmd.contains(message[0].substring(1))
            && testArgs(message.length);
  }

  public String toString()
  {
    return cmd.get(0);
  }
}
