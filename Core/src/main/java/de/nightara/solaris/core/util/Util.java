package de.nightara.solaris.core.util;

import de.nightara.solaris.core.pokemon.*;
import de.nightara.solaris.core.pokemon.Pokemon.Type;
import de.nightara.solaris.core.util.database.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import discord4j.core.object.entity.*;
import discord4j.core.object.util.*;
import org.jooq.tools.*;
import org.slf4j.*;

import javax.imageio.*;

public abstract class Util
{
  public static final int MAX_MSG_SIZE = 1980;
  public static final Logger LOG = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
  public static final String DEFAULT_NOTIFICATION = " Ich kann dir keine Privatnachrichten senden. "
          + "Bitte aktivier in den Einstellungen unter \"Datenschutz & Sicherheit\" die Option \"Erlaube Direktnachrichten von Servermitgliedern\".";

  public static List<String> breakString(String input, int lineLength)
  {
    return breakString(input, lineLength, false);
  }

  public static List<String> breakString(String input, int lineLength, boolean oldQuoted)
  {
    input = input.trim();
    List<String> list = new LinkedList<>();

    if(!input.isEmpty())
    {
      input = input + '\n';
      String tmp = input.substring(0, Math.min(lineLength + 1, input.length()));

      int breakpoint = Math.max(tmp.lastIndexOf(' '), tmp.lastIndexOf('\n'));
      breakpoint = breakpoint < 0 ? Math.min(lineLength, tmp.length()) : breakpoint;
      tmp = tmp.substring(0, breakpoint).trim();

      String qTmp = tmp;
      String header = "diff\n";
      boolean quoteChange = StringUtils.countMatches(tmp, "```") % 2 == 1;
      if(quoteChange)
      {
        if(oldQuoted)
        {
          qTmp = "```" + header + qTmp;
        }
        else
        {
          qTmp = qTmp + "```";
        }
        oldQuoted = !oldQuoted;
      }

      list.add(qTmp);
      list.addAll(breakString(input.substring(tmp.length()), lineLength, oldQuoted));
    }
    return list;
  }

  public static Optional<Pokemon> loadPokemonByMsg(Message msg)
  {
    try
    {
      Integer searchId = Integer.parseInt(msg.getContent().orElse("").split(" ")[1]);
      return DBConnector.loadAllPokemon(msg.getAuthor()).stream()
              .filter(p -> p.getBoxId().map(searchId::equals).orElse(false))
              .findAny();
    }
    catch(IndexOutOfBoundsException | NumberFormatException ex)
    {
      return Optional.empty();
    }
  }

  public static Optional<Message> sendPMOrNotify(User u, Optional<CharSequence> message, MessageChannel c)
  {
    return message.flatMap(msg -> sendPMOrNotify(u, msg, c));
  }

  public static Optional<Message> sendPMOrNotify(Optional<User> u, CharSequence message, MessageChannel c)
  {
    return u.flatMap(user -> sendPMOrNotify(user, message, c));
  }

  public static Optional<Message> sendPMOrNotify(User u, CharSequence message, MessageChannel c)
  {
    Optional<Message> msg = sendMessage(u.getPrivateChannel().block(), message);
    if(msg.isPresent())
    {
      return msg;
    }
    else
    {
      return sendMessage(c, u.getMention() + DEFAULT_NOTIFICATION);
    }
  }

  public static Optional<Message> sendPM(User u, Optional<? extends CharSequence> message)
  {
    return message.flatMap(msg -> sendPM(u, msg));
  }

  public static Optional<Message> sendPM(User u, CharSequence message)
  {
    return sendMessage(u.getPrivateChannel().block(), message);
  }

  public static Optional<Message> sendMessage(MessageChannel c, Optional<? extends CharSequence> message)
  {
    return message.flatMap(msg -> sendMessage(c, msg));
  }

  public static Optional<Message> sendMessage(MessageChannel c, CharSequence message)
  {
    return sendMessage(c, message, null);
  }

  public static Optional<Message> sendFile(MessageChannel c, Path file)
  {
    return sendMessage(c, null, file);
  }

  public static Optional<Message> sendMessage(MessageChannel c, CharSequence message, Path file)
  {
    if(message == null)
    {
      message = "";
    }
    return Util.breakString(message.toString(), MAX_MSG_SIZE).stream()
            .map(line ->
            {
              Optional<Message> value = Optional.empty();
              if(file != null)
              {
                value = c.createMessage(spec ->
                {
                  try
                  {
                    spec.addFile(file.getFileName().toString(), Files.newInputStream(file));
                    spec.setContent(line);
                  }
                  catch(IOException ex)
                  {
                    LOG.error("An error occurred while sending file {}: {}", file, ex);
                  }
                }).blockOptional();
              }
              else if(line.length() > 0)
              {
                value = c.createMessage(line).blockOptional();
              }
              return value;
            })
            .flatMap(Util::streamOptional)
            .max(Comparator.comparing(Message::getTimestamp));
  }

  public static Optional<Message> sendAnswer(Message msg, Optional<? extends CharSequence> message)
  {
    return message.flatMap(m -> sendAnswer(msg, m));
  }

  public static Optional<Message> sendAnswer(Message msg, CharSequence message)
  {
    return sendMessage(msg.getChannel().block(), msg.getAuthor().map(User::getMention).orElse("") + ' ' + message);
  }

  public static void deleteMessage(Message msg)
  {
    msg.delete().doOnError(ex -> LOG.warn("Missing permissions to delete message in channel {}", msg.getChannelId()));
  }

  public static Move getRandomMove()
  {
    return Move.get(getRandomType(Type.UNTYPED)).stream()
            .skip(Math.random() < 0.5 ? 1 : 0)
            .findAny().get();
  }

  public static Type getRandomType(Type... exclude)
  {
    Type type;
    do
    {
      type = Type.values()[(int) (Math.random() * Type.values().length)];
    }
    while(Arrays.asList(exclude).contains(type));
    return type;
  }

  public static Optional<BufferedImage> getAvatar(User u)
  {
    return u.getAvatar(Image.Format.PNG)
        .map(Image::getData)
        .blockOptional()
        .map(url -> {
          try
          {
            return ImageIO.read(new URL(url));
          }
          catch(IOException ex)
          {
            return null;
          }
        });
  }

  public static boolean isAdmin(User user, Guild guild)
  {
    return user.equals(guild.getOwner().block()) || guild.getMemberById(user.getId())
        .flatMap(Member::getBasePermissions)
        .filter(perm -> perm.contains(Permission.ADMINISTRATOR))
        .blockOptional()
        .isPresent();
  }

  public static <T> Comparator<T> shuffle()
  {
    final Map<Object, UUID> uniqueIds = new IdentityHashMap<>();
    return Comparator.comparing(e -> uniqueIds.computeIfAbsent(e, k -> UUID.randomUUID()));
  }

  public static boolean waitFor(Supplier<Boolean> isTerminated, Duration delay)
  {
    return waitFor(isTerminated, delay, Duration.ZERO);
  }

  public static boolean waitFor(Supplier<Boolean> isTerminated, Duration delay, Duration timeout)
  {
    Instant end = Instant.now().plus(timeout);
    Supplier<Boolean> isTimeout = () -> !timeout.isZero() && Instant.now().isAfter(end);
    while(!isTerminated.get() && !isTimeout.get())
    {
      try
      {
        Thread.sleep(delay.toMillis());
      }
      catch(InterruptedException ex)
      {
      }
    }
    return !isTimeout.get();
  }

  public static List<String> splitAndPad(String input, String separator, int minSize)
  {
    return splitAndPad(input, separator, minSize, "FILL");
  }

  public static List<String> splitAndPad(String input, String separator, int minSize, String padding)
  {
    List<String> split = new LinkedList<>(Arrays.asList(input.split(" ")));
    while(split.size() < minSize)
    {
      split.add(padding);
    }
    return split;
  }

  public static Optional<Integer> parseInt(String s)
  {
    try
    {
      return Optional.of(Integer.parseInt(s));
    }
    catch(NullPointerException | NumberFormatException ex)
    {
      return Optional.empty();
    }
  }

  public static String getAsTextLine(Pokemon p)
  {
    return p.getBoxId().orElse(0) + ": " + p.getNickname().map(nick -> nick + " (" + p.getName() + ')').orElse(p.getName())
            + " - Level " + p.getLevel() + ", " + p.getExp() + " Exp\n";
  }

  public static <T> Consumer<T> getNOP()
  {
    return obj ->
    {
    };
  }

  public static <T, K> BiFunction<T, K, T> biFunctionIdentityFirst()
  {
    return (obj1, obj2) -> obj1;
  }

  public static <T, K> BiFunction<T, K, K> biFunctionIdentitySecond()
  {
    return (obj1, obj2) -> obj2;
  }

  public static <T> Stream<? extends T> concat(Stream<? extends T>... streams)
  {
    return Arrays.stream(streams)
            .reduce(Stream.empty(), Stream::concat);
  }

  public static <T> Stream<T> streamOptional(Optional<T> opt)
  {
    return opt.map(Stream::of).orElseGet(Stream::empty);
  }
}
