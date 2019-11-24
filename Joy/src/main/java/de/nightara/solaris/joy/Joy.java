package de.nightara.solaris.joy;

import de.nightara.solaris.core.*;
import de.nightara.solaris.core.event.*;
import de.nightara.solaris.core.pokemon.*;
import de.nightara.solaris.core.util.*;
import de.nightara.solaris.core.util.database.*;
import de.nightara.solaris.joy.game.*;
import discord4j.core.object.entity.*;
import gnu.trove.map.hash.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static de.nightara.solaris.core.util.Util.*;

public class Joy extends DiscordBot
{
  private final Map<User, Tuple<MessageChannel, Game>> CURRENT_GAMES;

  public static void main(String... args)
  {
    Joy bot = new Joy(args[0]);
  }

  public Joy(String configFile)
  {
    super(configFile);
    CURRENT_GAMES = new THashMap<>();

    addDiscordCommand(new DiscordCommand("kontostand", "Lass deinen aktuellen Kontostand anzeigen.",
                                         msg -> sendAnswer(msg, "Dein Kontostand beträgt " + DBConnector.getMoney(msg.getAuthor().get()) + " Pokédollar."),
                                         "konto", "pokedollar", "dollar"));
    addDiscordCommand(new DiscordCommand("box", "Lass deine Box anzeigen.",
                                         msg -> sendMessage(msg.getChannel().block(), getPokemonBox(msg.getAuthor().get()))));
    addDiscordCommand(new DiscordCommand("inventar", "Lass dein Inventar anzeigen.",
        msg -> sendAnswer(msg, getInventory(msg.getAuthor().get()))));
    addDiscordCommand(new DiscordCommand("vorhersage", "Ruf die aktuelle Wettervorhersage auf.",
                                         msg -> sendAnswer(msg, "Dieses Feature existiert leider noch nicht."),
                                         "wetter"));
    addDiscordCommand(new DiscordCommand(BerryGame.CMD, "(<Beere>) (<Mulch>)", "Starte das Minispiel \"" + BerryGame.NAME + "\"",
                                         msg -> canLaunchGame(msg, BerryGame.class, BerryGame::checkInit),
                                         msg -> launchGame(msg, BerryGame::createGame)));
    addDiscordCommand(new DiscordCommand(BerryGame.QTE_CMD, "<Angriff>", "Verteidige deine Beeren im Minispiel \"" + BerryGame.NAME + "\"",
                                         msg -> getGame(msg, BerryGame.class).isPresent(),
                                         msg -> getGame(msg).get().setAnswer(msg),
                                         "vert"));

    launch();
  }

  public final boolean canLaunchGame(Message msg, Class<? extends Game> gameClass, Function<Message, Boolean> gameCheck)
  {
    return !getGame(msg, gameClass).isPresent()
            && gameCheck.apply(msg);
  }

  public final <T extends Game> void launchGame(Message msg, Function<Message, T> gameSource)
  {
    T game = gameSource.apply(msg);
    CURRENT_GAMES.put(msg.getAuthor().get(), new Tuple<>(msg.getChannel().block(), game));
    ThreadManager.submit(game::play);
  }

  public final Optional<Game> getGame(Message msg)
  {
    return getGame(msg.getAuthor().get(), msg.getChannel().block(), Game.class);
  }

  public final <T extends Game> Optional<T> getGame(Message msg, Class<T> gameClass)
  {
    return getGame(msg.getAuthor().get(), msg.getChannel().block(), gameClass);
  }

  public final <T extends Game> Optional<T> getGame(User user, MessageChannel channel, Class<T> gameClass)
  {
    return Optional.ofNullable(CURRENT_GAMES.get(user))
            .filter(tuple -> tuple.getT1().equals(channel))
            .map(Tuple::getT2)
            .filter(Game::isActive)
            .filter(gameClass::isInstance)
            .map(gameClass::cast);
  }

  public final StringBuilder getPokemonBox(User user)
  {
    StringBuilder sb = new StringBuilder(user.getMention())
            .append(" In deiner Box liegen folgende Pokémon:\n```");
    DBConnector.loadAllPokemon(user).stream()
            .map(Util::getAsTextLine)
            .forEach(sb::append);
    return sb.append("```");
  }

  public final StringBuilder getInventory(User user)
  {
    StringBuilder sb = new StringBuilder(user.getMention())
        .append(" Du besitzt folgende Items:\n```");
    DBConnector.getInventory(user).entrySet().stream()
    .map(e -> e.getKey() + ": " + e.getValue())
    .forEach(sb::append);
    return sb.append("```");
  }

  @Override
  public Stream<Item> streamItems()
  {
    return Stream.of(BerryGame.STABLE_MULCH, BerryGame.BOOST_MULCH);
  }
}
