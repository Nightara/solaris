package de.nightara.solaris.pokemon;

import de.nightara.solaris.core.pokemon.*;
import de.nightara.solaris.pokemon.item.*;
import java.lang.reflect.*;
import java.time.*;
import org.junit.Test;

public class SolarisTest
{
  @Test
  public void testCommands() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException
  {
    boolean overridePrices = false;
    if(overridePrices)
    {
      Field f1 = AbstractItem.class.getDeclaredField("price");
      f1.setAccessible(true);
      f1.setInt(Ball.POKEBALL, 0);
      Field f2 = Berry.class.getDeclaredField("price");
      f2.setAccessible(true);
      f2.setInt(Berry.RAZZBERRY, 0);
      Field f3 = EncounterModifier.class.getDeclaredField("duration");
      f3.setAccessible(true);
      f3.set(Berry.RAZZBERRY, Duration.ofMinutes(1));
    }

    //Solaris bot = new Solaris("config.xml");
    //IChannel defaultChannel = bot.getGuild().getDefaultChannel();
    //Optional<Pokemon> bisa = DBConnector.createPokemonByName("Bisasam");
    //assertTrue(bisa.isPresent());
    //Encounter e = new Encounter(bisa.get(), defaultChannel);
    //assertTrue(e.isActive());
    //assertTrue(bot.addEncounter(e));
    //assertEquals(e, bot.getEncounter(defaultChannel).get());
    //Util.sendMessage(bot.getGuild().getDefaultChannel(),
    //                       e.addEncounterModifier(bot.getGuild().getUserByID(84637879459905536L), EncounterModifier.create(c -> 0.5, f -> 0.0, s -> 0.5, "einen Cheat")));
    //bot.joinThread();
  }
}
