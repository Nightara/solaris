package de.nightara.solaris.core.pokemon;

public abstract class AbstractItem implements Item
{

  private final int price;
  private final String name;
  private final String description;

  public AbstractItem(String name, int price, String description)
  {
    this.name = name;
    this.price = price;
    this.description = description;
  }

  @Override
  public int getPrice()
  {
    return price;
  }

  @Override
  public String getName()
  {
    return name;
  }
  
  @Override
  public String getDesc()
  {
    return description;
  }
}
