package de.nightara.solaris.pokemon;

import de.nightara.solaris.core.*;
import de.nightara.solaris.core.event.*;
import de.nightara.solaris.core.pokemon.*;
import de.nightara.solaris.core.util.*;
import de.nightara.solaris.core.util.database.*;
import de.nightara.solaris.pokemon.item.*;
import discord4j.core.event.domain.lifecycle.*;
import discord4j.core.event.domain.message.*;
import discord4j.core.object.entity.*;
import discord4j.core.object.reaction.*;
import discord4j.core.object.util.*;
import gnu.trove.set.hash.*;
import java.util.*;
import java.util.stream.*;
import reactor.core.publisher.*;

import static de.nightara.solaris.core.util.Util.*;

public class Solaris extends DiscordBot
{
  private final Set<Encounter> encounters;

  public static void main(String... args)
  {
    Solaris bot = new Solaris(args[0]);
  }

  public Solaris(String configFile)
  {
    super(configFile);
    this.encounters = new THashSet<>();
    addDiscordCommand(new DiscordCommand("pokeball", "Wirf einen Pokéball.",
                                         msg -> getEncounter(msg.getChannel().block()).map(e -> e.canCatch(msg.getAuthor().get())).orElse(false)
                                         && DBConnector.buyItem(msg.getAuthor().get(), Ball.POKEBALL),
                                         msg -> sendAnswer(msg, "Du kannst dir das im Moment nicht leisten."),
                                         msg -> sendMessage(msg.getChannel().block(), getEncounter(msg.getChannel().block()).map(e -> e.attemptCatch(msg.getAuthor().get(), Ball.POKEBALL))),
                                         "ball"));
    addDiscordCommand(new DiscordCommand("superball", "Wirf einen Superball.",
                                         msg -> getEncounter(msg.getChannel().block()).map(e -> e.canCatch(msg.getAuthor().get())).orElse(false)
                                         && DBConnector.buyItem(msg.getAuthor().get(), Ball.SUPERBALL),
                                         msg -> sendAnswer(msg, "Du kannst dir das im Moment nicht leisten."),
                                         msg -> sendMessage(msg.getChannel().block(), getEncounter(msg.getChannel().block()).map(e
                                                            -> e.attemptCatch(msg.getAuthor().get(), Ball.SUPERBALL)))));
    addDiscordCommand(new DiscordCommand("hyperball", "Wirf einen Hyperball.",
                                         msg -> getEncounter(msg.getChannel().block()).map(e -> e.canCatch(msg.getAuthor().get())).orElse(false)
                                         && DBConnector.buyItem(msg.getAuthor().get(), Ball.HYPERBALL),
                                         msg -> sendAnswer(msg, "Du kannst dir das im Moment nicht leisten."),
                                         msg -> sendMessage(msg.getChannel().block(), getEncounter(msg.getChannel().block()).map(e
                                                            -> e.attemptCatch(msg.getAuthor().get(), Ball.HYPERBALL)))));
    addDiscordCommand(new DiscordCommand("himmihbeere", "Wirf eine Himmihbeere.",
                                         msg -> getEncounter(msg.getChannel().block()).isPresent()
                                         && DBConnector.buyItem(msg.getAuthor().get(), Berry.RAZZBERRY),
                                         msg -> sendAnswer(msg, "Du kannst dir das im Moment nicht leisten."),
                                         msg -> sendMessage(msg.getChannel().block(), getEncounter(msg.getChannel().block()).map(e
                                                            -> e.addEncounterModifier(msg.getAuthor().get(), Berry.RAZZBERRY))),
                                         "beere"));
    addDiscordCommand(new DiscordCommand("nanabbeere", "Wirf eine Nanabbeere.",
                                         msg -> getEncounter(msg.getChannel().block()).isPresent()
                                         && DBConnector.buyItem(msg.getAuthor().get(), Berry.NANABBERRY),
                                         msg -> sendAnswer(msg, "Du kannst dir das im Moment nicht leisten."),
                                         msg -> sendMessage(msg.getChannel().block(), getEncounter(msg.getChannel().block()).map(e
                                                            -> e.addEncounterModifier(msg.getAuthor().get(), Berry.NANABBERRY)))));
    addDiscordCommand(new DiscordCommand("pokeriegel", "<Riegelfarbe>", "Wirf einen Pokériegel.",
                                         msg -> getEncounter(msg.getChannel().block()).isPresent()
                                         && DBConnector.buyItem(msg.getAuthor().get(), PokeBlock.parse(msg.getContent().orElse("").split(" ")[1])),
                                         msg -> sendAnswer(msg, "Du kannst dir das im Moment nicht leisten."),
                                         msg -> sendMessage(msg.getChannel().block(), getEncounter(msg.getChannel().block()).flatMap(e
                                                            -> e.addEncounterModifier(msg.getAuthor().get(), PokeBlock.parse(msg.getContent().orElse("").split(" ")[1])))),
                                         "riegel"));
    addDiscordCommand(new DiscordCommand("entwickeln", "<Datenbank-ID> (<Dex-ID der Entwicklung>)", "Entwickle ein Pokémon.",
                                         msg -> loadPokemonByMsg(msg).isPresent(),
                                         msg -> sendAnswer(msg, "Dieses Pokémon befindet sich nicht in deinem Besitz."),
                                         msg -> sendAnswer(msg, attemptEvolution(msg)
                                                 .map(p -> "Dein Pokémon hat sich zu " + p.getName() + " weiterentwickelt!")
                                                 .orElse("Dein Pokémon kann sich anscheinend im Moment nicht entwickeln."))));
    addDiscordCommand(new DiscordCommand("verschicken", "<Datenbank-ID>", "Schick ein Pokémon an den Professor, um Sonderbonbons dafür zu erhalten",
                                        msg -> loadPokemonByMsg(msg).isPresent(),
                                        msg -> sendAnswer(msg, "Dieses Pokémon befindet sich nicht in deinem Besitz."),
                                        msg -> sendAnswer(msg, sendToProfessor(msg))));
    addDiscordCommand(new DiscordCommand("levelup", "<Datenbank-ID> (<Anzahl der Level>)", "Level ein Pokémon mit Hilfe von Sonderbonbons.",
                                        msg -> loadPokemonByMsg(msg).isPresent(),
                                        msg -> sendAnswer(msg, "Dieses Pokémon befindet sich nicht in deinem Besitz."),
                                        msg -> sendAnswer(msg, levelPokemon(msg))));

    discord.getEventDispatcher().on(GuildSelectEvent.class).subscribe(evt -> {
      evt.getGuild().getChannels().toStream()
          .filter(gch -> gch instanceof GuildMessageChannel)
          .map(gch -> (GuildMessageChannel) gch)
          .filter(gch -> gch.getEffectivePermissions(discord.getSelfId().get()).block().contains(Permission.SEND_MESSAGES))
          .forEach(this::addSpawnRoutine);
    });

    launch();
  }

  public void addSpawnRoutine(MessageChannel c)
  {
    ThreadManager.submitRoutine(() ->
    {
      if(!getEncounter(c).isPresent())
      {
        DBConnector.createRandomSpawn(c)
                .map(p -> new Encounter(p, c))
                .ifPresent(this::addEncounter);
      }
    }, Encounter.DEFAULT_DURATION.dividedBy(1 + new Random().nextInt(5)), Encounter.DEFAULT_DURATION);
  }

  public Optional<Encounter> getEncounter(MessageChannel c)
  {
    return encounters.stream()
            .filter(Encounter::isActive)
            .filter(e -> e.getChannel().equals(c))
            .findAny();
  }

  public boolean addEncounter(Encounter e)
  {
    return !getEncounter(e.getChannel()).isPresent()
            && encounters.add(e);
  }

  public Optional<Pokemon> attemptEvolution(Message msg)
  {
    String[] split = msg.getContent().orElse("").split(" ");
    Optional<Integer> evoId = Optional.of(split)
        .filter(s -> s.length > 2)
        .flatMap(s -> Util.parseInt(s[2]));
    return loadPokemonByMsg(msg)
        .filter(Pokemon::canEvolve)
        .map(p ->
        {
          List<Integer> evoList = p.getEvolutions();
          LOG.info("{}", evoId);
          LOG.info("{}", evoList);
          Integer actualEvoId = evoId
              .filter(evoList::contains)
              .orElse(evoList.size() == 1 ? evoList.get(0) : -1);
          LOG.info("actual evo id: " + actualEvoId);
          return new Tuple<>(p, actualEvoId);
        })
        .filter(t -> t.getT2() >= 0)
        .flatMap(t -> DBConnector.evolvePokemon(t.getT1(), t.getT2()));
  }

  public String sendToProfessor(Message msg)
  {
    int candyCount = loadPokemonByMsg(msg)
        .filter(this::sendConfirmation)
        .filter(p -> DBConnector.deletePokemon(msg.getAuthor().get(), p))
        .map(Pokemon::getLevel)
        .orElse(0);

    DBConnector.changeInventory(msg.getAuthor().get(), "Rare Candy", candyCount);
    return candyCount > 0 ? "Du hast dein Pokémon zum Professor geschickt und dafür " + candyCount + " Sonderbonbons erhalten."
        : "Anscheinend ist beim Transfer etwas schief gelaufen. Dein Pokémon konnte nicht verschickt werden.";
  }

  public boolean sendConfirmation(Pokemon p)
  {
    return DBConnector.getOwnerId(p)
        .map(Snowflake::of)
        .map(discord::getUserById)
        .map(Mono::block)
        .flatMap(u -> sendPM(u,"Willst du wirklich dein " + p.getName() + " Level " + p.getLevel() + " zum Professor schicken?"))
        .map(msg ->
        {
          Snowflake targetId = msg.getId();
          ReactionEmoji.Unicode confirm = ReactionEmoji.unicode("✅");
          ReactionEmoji.Unicode cancel = ReactionEmoji.unicode("❎");
          ReactionEmoji.Unicode other = ReactionEmoji.unicode("\uD83D\uDE00");

          msg.addReaction(confirm).block();
          msg.addReaction(cancel).block();

          ReactionEmoji.Unicode reaction = discord.getEventDispatcher().on(ReactionAddEvent.class)
              .filter(evt -> !evt.getUser().block().isBot())
              .filter(evt -> evt.getMessageId().equals(targetId))
              .map(ReactionAddEvent::getEmoji)
              .map(emoji -> emoji.asUnicodeEmoji().orElse(other))
              .filter(emoji -> confirm.equals(emoji) || cancel.equals(emoji))
              .blockFirst();

          return confirm.equals(reaction);
        }).orElse(false);
  }

  public String levelPokemon(Message msg)
  {
    User u = msg.getAuthor().get();
    String[] split = msg.getContent().orElse("").split(" ");
    int levelCount = split.length > 2 ? Util.parseInt(split[2]).orElse(1) : 1;
    return Util.loadPokemonByMsg(msg)
        .filter(p -> p.getLevel() + levelCount <= 100)
        .filter(p -> DBConnector.getInventory(u, "Rare Candy")
            .map(i -> i >= levelCount)
            .orElse(false))
        .map(p ->
        {
          for(int i = 0; i < levelCount; i++)
          {
            p.addExp(p.getExpForNextLevel() - p.getExp());
          }
          DBConnector.storePokemon(u, p);
          return "Dein " + p.getName() + " ist auf Level " + p.getLevel() + " gewachsen!";
        })
    .orElse("Anscheinend besitzt du nicht genug Sonderbonbons dafür.");
  }

  @Override
  public Stream<? extends Item> streamItems()
  {
    return concat(Ball.stream(), Berry.stream(), PokeBlock.stream());
  }
}
