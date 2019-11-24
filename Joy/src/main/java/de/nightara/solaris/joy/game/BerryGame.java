package de.nightara.solaris.joy.game;

import de.nightara.solaris.core.pokemon.*;
import de.nightara.solaris.core.util.*;
import de.nightara.solaris.core.util.database.*;
import discord4j.core.object.entity.*;
import gnu.trove.set.hash.*;
import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static de.nightara.solaris.core.DiscordCommand.*;
import static de.nightara.solaris.core.util.Util.*;

public class BerryGame extends Game<BerryGame>
{
  public static final String CMD = "beerenfeld";
  public static final String QTE_CMD = "verteidigen";
  public static final String NAME = "Beeren pflanzen";
  public static final GameModifier<BerryGame> BOOST_MULCH = new GameModifier<BerryGame>("Wuchermulch", 200,
                                                                                        "Ein feuchter Mulch, der die Ausbeute bei der Beerenernte erhöht.",
                                                                                        reward -> (int) (reward * 1.5),
                                                                                        spawner -> spawner.modifyDelay(dur -> dur.dividedBy(3)));
  public static final GameModifier<BerryGame> STABLE_MULCH = new GameModifier<BerryGame>("Stabilmulch", 200,
                                                                                         "Ein feuchter Mulch, der wilde Pokémon von der Beerenernte vertreibt.",
                                                                                         reward -> (int) (reward / 2),
                                                                                         spawner -> spawner.modifySpawnRate(rate -> rate / 2));

  private static final List<GameModifier<BerryGame>> MODIFIERS = Arrays.asList(BOOST_MULCH, STABLE_MULCH);
  private static final int REWARD_FACTOR = 12;
  private static final double MOD_NEUTRAL = 0.0625;
  private static final double BASE_QTE_LOSS = 0.8;
  private static final double MOD_EFFECTIVE = 0.0;
  private static final double MOD_INEFFECTIVE = 0.25;
  private static final Duration QTE_TIMEOUT = Duration.ofSeconds(20);
  private static final Duration DURATION_FACTOR = Duration.ofSeconds(3);
  private static final Function<BerryGame, Double> SUPPLIER = game -> DBConnector.createRandomPokemon().map(p ->
  {
    synchronized (game)
    {
      User u = game.getUser();
      MessageChannel c = game.getChannel();
      Set<Move> m = new THashSet<>();
      while(m.size() < 4)
      {
        m.add(Util.getRandomMove());
      }
      game.setMoves(m);
      game.setAnswer((Move) null);
      game.setAttackers(p);

      sendMessage(c, u.getMention() + " Dein Beerenfeld wird von einer Horde **" + p.getName() + "** angegriffen!\n"
                  + "Setz schnell eine möglichst effektive Attacke ein, um sie zu vertreiben! Du hast folgende Attacken zur Auswahl:\n"
                  + IntStream.range(0, m.size()).mapToObj(x -> "**" + game.getMoves().get(x).getName() + "** mit dem Befehl `" + PREFIX + QTE_CMD + ' ' + x + '`').collect(Collectors.joining("\n")));

      try
      {
        game.wait(QTE_TIMEOUT.toMillis());
      }
      catch(InterruptedException ex)
      {
      }

      if(game.isActive())
      {
        Optional<Move> answer = game.getAnswer();
        double eff = game.getAnswer()
                .map(a -> DBConnector.getTypeEffectivity(a.getType(), p.getTypes()))
                .orElse(0.0);

        if(answer.isPresent())
        {
          sendMessage(c, u.getMention() + " Die Attacke " + answer.get().getName() + " war " + (eff > 1 ? "sehr" : eff < 1 ? "nicht sehr" : "normal") + " effektiv.\n"
                      + "Du hast die wilden " + p.getName() + " vertrieben, bevor sie allzu viel Schaden anrichten konnten.");
        }
        else
        {
          sendMessage(c, u.getMention() + " Leider hast du nicht schnell genug reagiert.\n"
                      + "Die wilden " + p.getName() + " haben dein Beerenfeld verwüstet.");
        }

        return 1 - BASE_QTE_LOSS * answer
                .map(a -> eff > 1 ? MOD_EFFECTIVE : eff < 1 ? MOD_INEFFECTIVE : MOD_NEUTRAL)
                .orElse(1.0);
      }
      return 1.0;
    }
  }).orElse(1.0);

  private final Berry berry;
  private final List<Move> moves;

  private volatile Move answer;
  private volatile Pokemon attackers;

  public BerryGame(Berry berry, User user, MessageChannel channel, Optional<GameModifier<BerryGame>> mulch)
  {
    super(CMD, NAME, user, channel, DURATION_FACTOR.multipliedBy(Math.min(berry.getPrice(), 150)), berry.getPrice() * REWARD_FACTOR, new QuicktimeSpawner(SUPPLIER));
    this.berry = berry;
    this.moves = Collections.synchronizedList(new LinkedList<>());
    mulch.ifPresent(m -> this.addEventModifier(m));
  }

  public Berry getBerry()
  {
    return berry;
  }

  public List<Move> getMoves()
  {
    return moves;
  }

  public void setMoves(Collection<Move> moves)
  {
    this.moves.clear();
    this.moves.addAll(moves);
  }

  public Optional<Pokemon> getAttackers()
  {
    return Optional.ofNullable(attackers);
  }

  public void setAttackers(Pokemon attackers)
  {
    this.attackers = attackers;
  }

  public Optional<Move> getAnswer()
  {
    return Optional.ofNullable(answer);
  }

  protected synchronized void setAnswer(Move answer)
  {
    if(answer == null || getMoves().contains(answer))
    {
      this.answer = answer;
      this.notifyAll();
    }
  }

  @Override
  public void setAnswer(Message msg)
  {
    parseInt(splitAndPad(msg.getContent().orElse(""), " ", 2, "5").get(1))
            .filter(i -> i < getMoves().size())
            .map(getMoves()::get)
            .ifPresent(this::setAnswer);
  }

  public static boolean checkInit(Message msg)
  {
    String[] split = msg.getContent().orElse("").toLowerCase().split(" ");
    return (split.length < 2 || Berry.stream()
            .anyMatch(berry -> berry.getName().toLowerCase().equals(split[1])))
            && (split.length < 3 || MODIFIERS.stream()
                    .filter(mulch -> mulch.getName().toLowerCase().equals(split[2]))
                    .allMatch(mulch -> DBConnector.buyItem(msg.getAuthor().get(), mulch)));
  }

  public static BerryGame createGame(Message msg)
  {
    List<String> split = splitAndPad(msg.getContent().orElse("").toLowerCase(), " ", 3);
    Berry b = Berry.stream()
            .filter(berry -> berry.getName().toLowerCase().equals(split.get(1)))
            .findAny().orElse(Berry.RAZZBERRY);
    Optional<GameModifier<BerryGame>> mod = MODIFIERS.stream()
            .filter(mulch -> mulch.getName().toLowerCase().equals(split.get(2)))
            .findAny();
    sendMessage(msg.getChannel().block(), msg.getAuthor().get().getMention() + " Du hast " + b + mod.map(AbstractItem::getName).map(mulch -> " mit " + mulch).orElse(" ohne Mulch") + " eingepflanzt.\n"
    + "Der Duft scheint wilde Pokémon anzuziehen. Pass auf!");
    return new BerryGame(b, msg.getAuthor().get(), msg.getChannel().block(), mod);
  }
}
