package de.nightara.solaris.core.util.database;

import de.nightara.solaris.core.pokemon.*;
import de.nightara.solaris.core.pokemon.Pokemon.Stat;
import de.nightara.solaris.core.util.TwinTuple;
import java.util.*;

import discord4j.core.object.entity.*;
import discord4j.core.object.util.*;
import org.junit.*;

import static de.nightara.solaris.core.pokemon.Move.MoveType.*;
import static de.nightara.solaris.core.pokemon.Pokemon.Stage.*;
import static de.nightara.solaris.core.pokemon.Pokemon.Type.*;
import static de.nightara.solaris.core.util.database.DBConnector.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DBConnectorTest
{
  @Test
  public void testGetTypeEffectivity()
  {
    assertEquals(4, DBConnector.getTypeEffectivity(FIRE, new TwinTuple<>(BUG, STEEL)), 0);
    assertEquals(0.25, DBConnector.getTypeEffectivity(FIRE, new TwinTuple<>(ROCK, WATER)), 0);
    assertEquals(1, DBConnector.getTypeEffectivity(ROCK, new TwinTuple<>(UNTYPED, UNTYPED)), 0);
  }

  @Test
  public void testGetMoney()
  {
    User nonExistingUser = mock(User.class);
    when(nonExistingUser.getId()).thenReturn(Snowflake.of(-1));
    assertEquals(0, DBConnector.getMoney(nonExistingUser));

    User existingUser = mock(User.class);
    when(existingUser.getId()).thenReturn(Snowflake.of(84637879459905536L));
    assertThat(DBConnector.getMoney(existingUser), greaterThanOrEqualTo(100));
  }

  @Test
  public void testGetMoves()
  {
    Optional<TwinTuple<Move>> moves = DBConnector.getMoves(FIRE);
    assertTrue(moves.isPresent());
    assertEquals(FIRE, moves.get().getT1().getType());
    assertEquals(FIRE, moves.get().getT2().getType());
    assertEquals(PHYSICAL, moves.get().getT1().getMoveType());
    assertEquals(SPECIAL, moves.get().getT2().getMoveType());
    assertEquals("Feuerschlag", moves.get().getT1().getName());
    assertEquals("Flammenwurf", moves.get().getT2().getName());
  }

  @Test
  public void testCreatePokemonById()
  {
    assertFalse(DBConnector.createPokemonById(-1).isPresent());
    Optional<Pokemon> bisa = DBConnector.createPokemonById(1);
    assertTrue(bisa.isPresent());
    assertEquals(45, bisa.get().getBase(Stat.HP));
    assertEquals(BASE, bisa.get().getStage());
  }

  @Test
  public void testCreatePokemonByName()
  {
    assertFalse(createPokemonByName("Agumon").isPresent());
    Optional<Pokemon> bisa = createPokemonByName("Bisasam");
    assertTrue(bisa.isPresent());
    assertEquals(45, bisa.get().getBase(Stat.HP));
    assertEquals(BASE, bisa.get().getStage());
  }

  @Test
  public void testBuyItem()
  {
    User nonExistingUser = mock(User.class);
    when(nonExistingUser.getId()).thenReturn(Snowflake.of(-1));
    User existingUser = mock(User.class);
    when(existingUser.getId()).thenReturn(Snowflake.of(84637879459905536L));
    Map<User, Integer> moneyBefore = getMoney(nonExistingUser, existingUser);
    int neMoneyBefore = moneyBefore.get(nonExistingUser);
    int eMoneyBefore = moneyBefore.get(existingUser);
    
    assertEquals(0, neMoneyBefore);
    assertTrue(changeMoney(existingUser, 100));
    assertEquals(eMoneyBefore + 100, getMoney(existingUser));
    assertTrue(changeMoney(existingUser, -100));
    assertEquals(eMoneyBefore, getMoney(existingUser));
    
    Item testItem = new AbstractItem("Test", 100, "Testitem"){};
    assertTrue(buyItem(existingUser, testItem));
    assertEquals(eMoneyBefore - testItem.getPrice(), getMoney(existingUser));
    assertTrue(changeMoney(existingUser, testItem.getPrice()));
    assertEquals(eMoneyBefore, getMoney(existingUser));
  }
  
  @Test
  public void testLoadPokemon()
  {
    User nonExistingUser = mock(User.class);
    when(nonExistingUser.getId()).thenReturn(Snowflake.of(-1));
    User existingUser = mock(User.class);
    when(existingUser.getId()).thenReturn(Snowflake.of(84637879459905536L));
    
    assertTrue(loadAllPokemon(nonExistingUser).isEmpty());
    List<Pokemon> pokis = loadAllPokemon(existingUser);
    assertFalse(pokis.isEmpty());
  }
}
