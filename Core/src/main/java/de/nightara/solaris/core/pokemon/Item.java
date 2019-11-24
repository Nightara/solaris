package de.nightara.solaris.core.pokemon;

import de.nightara.solaris.core.util.database.*;
import discord4j.core.object.entity.*;

public interface Item
{
  public int getPrice();
  public String getName();
  public String getDesc();
  
  public default boolean buy(User user)
  {
    return DBConnector.buyItem(user, this);
  }
  
  public default StringBuilder toShopLine()
  {
    return new StringBuilder("+ ").append(getName()).append(" (").append(getPrice()).append(" Pokédollar)\n")
            .append("  ").append(getDesc()).append("\n");
  }
}
