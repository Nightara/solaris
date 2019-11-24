package de.nightara.solaris.core.util.database;

import com.mysql.cj.jdbc.*;
import de.nightara.solaris.core.pokemon.*;
import de.nightara.solaris.core.pokemon.Pokemon.*;
import de.nightara.solaris.core.util.*;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.*;
import gnu.trove.map.hash.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.jooq.*;
import org.jooq.impl.*;

import static de.nightara.solaris.core.pokemon.Move.MoveType.*;
import static org.jooq.impl.DSL.*;

public abstract class DBConnector
{
  private static final String DB = "pokebot";
  private static final String DB_USER = "pokebot";
  private static final String DB_HOST = "localhost";
  private static final String DB_PWD = "PassWD1337?";

  private static final Field<Long> UID = field("UID", Long.class);
  private static final Field<Long> CID = field("CID", Long.class);
  private static final Field<Integer> HP = field("hp", Integer.class);
  private static final Field<Integer> EXP = field("exp", Integer.class);
  private static final Field<Integer> ATK = field("atk", Integer.class);
  private static final Field<Integer> DEF = field("def", Integer.class);
  private static final Field<Integer> SPA = field("spa", Integer.class);
  private static final Field<Integer> SPD = field("spd", Integer.class);
  private static final Field<String> NAME = field("name", String.class);
  private static final Field<String> ITEM = field("item", String.class);
  private static final Field<Integer> INIT = field("init", Integer.class);
  private static final Field<Integer> LEVEL = field("level", Integer.class);
  private static final Field<Boolean> SHINY = field("shiny", Boolean.class);
  private static final Field<Integer> MONEY = field("money", Integer.class);
  private static final Field<String> MOVE_PHY = field("move_phy", String.class);
  private static final Field<String> MOVE_SPE = field("move_spe", String.class);
  private static final Field<Double> EFFECTIVITY = field("effectivity", Double.class);
  private static final Field<Stage> STAGE = field("stage", SQLDataType.INTEGER.asConvertedDataType(Converters.stageConverter));
  private static final Field<Type> TYPE_NAME = field("name", SQLDataType.VARCHAR.asConvertedDataType(Converters.typeConverter));
  private static final Field<Type> TYPE_ATK = field("type_atk", SQLDataType.VARCHAR.asConvertedDataType(Converters.typeConverter));
  private static final Field<Type> TYPE_DEF = field("type_def", SQLDataType.VARCHAR.asConvertedDataType(Converters.typeConverter));
  private static final Field<Type> TYPE_PRI = field("type_pri", SQLDataType.VARCHAR.asConvertedDataType(Converters.typeConverter));
  private static final Field<Type> TYPE_SEC = field("type_sec", SQLDataType.VARCHAR.asConvertedDataType(Converters.typeConverter));
  private static final Field<Integer> PID = field("PID", SQLDataType.INTEGERUNSIGNED.asConvertedDataType(Converters.uintConverter));
  private static final Field<Integer> BID = field("BID", SQLDataType.INTEGERUNSIGNED.asConvertedDataType(Converters.uintConverter));
  private static final Field<Integer> AMOUNT = field("amount", SQLDataType.INTEGERUNSIGNED.asConvertedDataType(Converters.uintConverter));
  private static final Field<Instant> JOIN_DATE = field("join_date", SQLDataType.TIMESTAMP.asConvertedDataType(Converters.timestampConverter));
  private static final Field<Integer> BASE_FORM = field("base_form", SQLDataType.INTEGERUNSIGNED.asConvertedDataType(Converters.uintConverter));
  private static final Field<Integer> EVOLUTION = field("evolution", SQLDataType.INTEGERUNSIGNED.asConvertedDataType(Converters.uintConverter));

  private static final Table POKEMON = table("pokemon");
  private static final Table BOX = table("pokemon_box");
  private static final Table INVENTORY = table("inventory");
  private static final Table TYPES = table("pokemon_types");
  private static final Table SPAWN_LISTS = table("pokemon_spawns");
  private static final Table DISCORD_USERS = table("discord_users");
  private static final Table EVOLUTIONS = table("pokemon_evolutions");
  private static final Table TYPE_EFFECTIVITIES = table("pokemon_type_effectivities");

  private static final Map<Tuple<Type, Collection<Type>>, Double> EFFECTIVITY_CACHE = new THashMap<>();

  private static DSLContext context;
  private static MysqlDataSource dataSource;

  protected static void initialize()
  {
    if(context == null)
    {
      dataSource = new MysqlDataSource();
      dataSource.setUser(DB_USER);
      dataSource.setPassword(DB_PWD);
      dataSource.setDatabaseName(DB);
      dataSource.setServerName(DB_HOST);
      try
      {
        dataSource.setServerTimezone("UTC");
      }
      catch(SQLException ex)
      {
      }
      context = using(dataSource, SQLDialect.MYSQL);
    }
  }

  public static boolean createUser(User user)
  {
    initialize();
    return user.getId().asLong() > 0
            && context.insertInto(DISCORD_USERS)
                    .columns(UID)
                    .values(user.getId().asLong())
                    .onDuplicateKeyIgnore().execute() > 0;
  }

  public static boolean changeMoney(User user, int money)
  {
    createUser(user);
    return context.update(DISCORD_USERS).set(MONEY, MONEY.plus(money))
            .where(UID.eq(user.getId().asLong())).and(value(money).gt(0).or(MONEY.gt(money)))
            .execute() > 0;
  }

  public static int getMoney(User user)
  {
    return getMoney(Arrays.asList(user)).get(user);
  }

  public static Map<User, Integer> getMoney(User... user)
  {
    return getMoney(Arrays.asList(user));
  }

  public static Map<User, Integer> getMoney(Collection<User> users)
  {
    users.forEach(DBConnector::createUser);
    List<Long> uids = users.stream()
            .map(User::getId)
            .map(Snowflake::asLong)
            .collect(Collectors.toList());
    Map<Long, Integer> results = context.select(UID, MONEY)
            .from(DISCORD_USERS).where(UID.in(uids))
            .fetchStream()
            .collect(Collectors.toMap(r -> r.get(UID),
                                      r -> r.get(MONEY)));
    return users.stream()
            .collect(Collectors.toMap(Function.identity(),
                                      user -> results.getOrDefault(user.getId().asLong(), 0)));
  }

  public static boolean buyItem(User user, Optional<? extends Item> item)
  {
    return item.map(i -> buyItem(user, i)).orElse(false);
  }

  public static boolean buyItem(User user, Item item)
  {
    createUser(user);
    return context.update(DISCORD_USERS).set(MONEY, MONEY.minus(item.getPrice()))
            .where(UID.eq(user.getId().asLong())).and(MONEY.ge(item.getPrice()))
            .execute() > 0;
  }

  public static double getTypeEffectivity(Type attack, Collection<Type> defense)
  {
    initialize();
    Tuple<Type, Collection<Type>> relation = new Tuple<>(attack, defense);
    if(!EFFECTIVITY_CACHE.containsKey(relation))
    {
      EFFECTIVITY_CACHE.put(relation, context.select(EFFECTIVITY)
                            .from(TYPE_EFFECTIVITIES).where(TYPE_ATK.eq(attack)).and(TYPE_DEF.in(defense))
                            .fetch(EFFECTIVITY).stream()
                            .reduce(1.0, (a, b) -> a * b));
    }
    return EFFECTIVITY_CACHE.get(relation);
  }

  public static Optional<Move> getMoveByName(String name)
  {
    initialize();
    String lowerName = name.toLowerCase();
    return context.select(TYPE_NAME, MOVE_PHY, MOVE_SPE)
            .from(TYPES).where(lower(MOVE_PHY).eq(lowerName)).or(lower(MOVE_SPE).eq(lowerName))
            .fetchOptional(rec -> new TwinTuple<>(new Move(rec.get(TYPE_NAME), rec.get(MOVE_PHY), PHYSICAL), new Move(rec.get(TYPE_NAME), rec.get(MOVE_SPE), SPECIAL)))
            .flatMap(t -> t.stream()
                    .filter(m -> m.getName().toLowerCase().equals(lowerName))
                    .findAny());
  }

  public static Optional<TwinTuple<Move>> getMoves(Type type)
  {
    initialize();
    return context.select(MOVE_PHY, MOVE_SPE)
            .from(TYPES).where(TYPE_NAME.eq(type))
            .fetchOptional(rec -> new TwinTuple<>(new Move(type, rec.get(MOVE_PHY), PHYSICAL), new Move(type, rec.get(MOVE_SPE), SPECIAL)));
  }

  public static Optional<Pokemon> createRandomSpawn(MessageChannel channel)
  {
    return createPokemon(PID.in(select(PID).from(SPAWN_LISTS).where(CID.eq(channel.getId().asLong()))),
                         false, Pokemon.DEFAULT_LEVEL, Pokemon.DEFAULT_EXP);
  }

  public static Optional<Pokemon> createRandomPokemon()
  {
    return createPokemon(trueCondition(), false, Pokemon.DEFAULT_LEVEL, Pokemon.DEFAULT_EXP);
  }

  public static Optional<Pokemon> createPokemonByName(String name)
  {
    return createPokemonByName(name, false, Pokemon.DEFAULT_LEVEL, Pokemon.DEFAULT_EXP);
  }

  public static Optional<Pokemon> createPokemonByName(String name, boolean shiny, int level, int exp)
  {
    return createPokemon(NAME.eq(name), shiny, level, exp);
  }

  public static Optional<Pokemon> createPokemonById(int id)
  {
    return createPokemon(PID.eq(id), false, Pokemon.DEFAULT_LEVEL, Pokemon.DEFAULT_EXP);
  }

  public static Optional<Pokemon> createPokemon(Condition condition, boolean shiny, int level, int exp)
  {
    initialize();
    Set<Integer> evolutions = context.select(EVOLUTION).from(EVOLUTIONS.join(POKEMON).on(BASE_FORM.eq(PID))).where(condition).fetchSet(EVOLUTION);
    return context.select(PID, NAME, STAGE, TYPE_PRI, TYPE_SEC, HP, ATK, DEF, SPA, SPD, INIT)
            .from(POKEMON).where(condition)
            .orderBy(DSL.rand()).limit(1)
            .fetchOptional(rec -> new Pokemon(rec.get(PID), rec.get(NAME), rec.get(STAGE), rec.get(TYPE_PRI), rec.get(TYPE_SEC), evolutions,
                                              rec.get(HP), rec.get(ATK), rec.get(DEF), rec.get(SPA), rec.get(SPD), rec.get(INIT),
                                              shiny, level, exp));
  }

  public static Optional<Integer> getInventory(User user, String item)
  {
    createUser(user);
    return context.select(AMOUNT)
            .from(INVENTORY)
            .where(UID.eq(user.getId().asLong())).and(ITEM.eq(item))
            .fetchOptional(AMOUNT);
  }

  public static Map<String, Integer> getInventory(User user)
  {
    createUser(user);
    return context.select(ITEM, AMOUNT)
        .from(INVENTORY)
        .where(UID.eq(user.getId().asLong()))
        .fetchMap(ITEM, AMOUNT);
  }
  
  public static boolean changeInventory(User user, String item, int delta)
  {
    createUser(user);
    return context.insertInto(INVENTORY)
            .columns(UID, ITEM, AMOUNT)
            .values(user.getId().asLong(), item, delta)
            .onDuplicateKeyUpdate().set(AMOUNT, AMOUNT.plus(delta))
            .execute() > 0;
  }

  public static boolean storePokemon(User owner, Pokemon p)
  {
    createUser(owner);
    return context.insertInto(BOX)
            .columns(BID, UID, PID, LEVEL, EXP, SHINY, NAME)
            .values(p.getBoxId().orElse(null), owner.getId().asLong(), p.getId(), p.getLevel(), p.getExp(), p.isShiny(), p.getNickname().orElse(null))
            .onDuplicateKeyUpdate().set(UID, owner.getId().asLong()).set(PID, p.getId()).set(LEVEL, p.getLevel()).set(EXP, p.getExp()).set(NAME, p.getNickname().orElse(null))
            .execute() > 0;
  }

  public static boolean deletePokemon(User owner, Pokemon p)
  {
    createUser(owner);
    return p.getBoxId()
        .filter(bid -> context.deleteFrom(BOX)
            .where(UID.eq(owner.getId().asLong()).and(BID.eq(bid))).execute() > 0)
        .isPresent();
  }

  public static Optional<Pokemon> loadPokemon(int boxId)
  {
    initialize();
    Set<Integer> evolutions = context.select(EVOLUTION).from(BOX.join(EVOLUTIONS).on(tableField(BOX, PID).eq(tableField(EVOLUTIONS, BASE_FORM)))).where(BID.eq(boxId)).fetchSet(EVOLUTION);
    return context.select(BID, tableField(BOX, PID).as(PID), LEVEL, EXP, SHINY, tableField(BOX, NAME), tableField(POKEMON, NAME), STAGE, TYPE_PRI, TYPE_SEC, HP, ATK, DEF, SPA, SPD, INIT)
            .from(BOX.join(POKEMON).on(tableField(BOX, PID).eq(tableField(POKEMON, PID)))).where(BID.eq(boxId))
            .fetchOptional(rec -> new Pokemon(rec.get(PID), rec.get(tableField(POKEMON, NAME)), rec.get(STAGE), rec.get(TYPE_PRI), rec.get(TYPE_SEC), evolutions,
                                              rec.get(HP), rec.get(ATK), rec.get(DEF), rec.get(SPA), rec.get(SPD), rec.get(INIT),
                                              rec.get(SHINY), rec.get(LEVEL), rec.get(EXP), rec.get(BID), rec.get(tableField(BOX, NAME))));
  }

  public static Optional<Pokemon> evolvePokemon(Pokemon p, Integer evolution)
  {
    initialize();
    Integer boxId = p.getBoxId().orElse(-1);
    if(context.update(BOX).set(PID, evolution).where(BID.eq(boxId)).execute() > 0)
    {
      return loadPokemon(boxId);
    }
    return Optional.empty();
  }

  public static List<Pokemon> loadAllPokemon(Optional<User> user)
  {
    return user.map(DBConnector::loadAllPokemon).orElse(new LinkedList<>());
  }

  public static List<Pokemon> loadAllPokemon(User owner)
  {
    createUser(owner);
    Set<Integer> boxIds = context.select(BID).from(BOX).where(UID.eq(owner.getId().asLong())).fetchSet(BID);
    return boxIds.stream()
            .map(DBConnector::loadPokemon)
            .map(Optional::get)
            .collect(Collectors.toList());
  }

  //TODO: Test
  public static Optional<Long> getOwnerId(Pokemon p)
  {
    initialize();
    return p.getBoxId()
        .map(bid -> context.select(UID).from(BOX).where(BID.eq(bid)).fetchOne(UID));
  }

  private static <T> Field<T> tableField(Table table, Field<T> field)
  {
    return field(table.getName() + "." + field.getName(), field.getDataType());
  }
}
