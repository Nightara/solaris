package de.nightara.solaris.core.util.database;

import de.nightara.solaris.core.pokemon.Pokemon.Stage;
import de.nightara.solaris.core.pokemon.Pokemon.Type;
import java.sql.*;
import org.jooq.impl.*;
import java.time.*;
import org.jooq.*;
import org.jooq.types.*;

public abstract class Converters
{
  public static Converter<Integer, Stage> stageConverter = new AbstractConverter<Integer, Stage>(Integer.class, Stage.class)
  {
    @Override
    public Stage from(Integer databaseObject)
    {
      return databaseObject == null ? null : Stage.get(databaseObject);
    }

    @Override
    public Integer to(Stage userObject)
    {
      return userObject == null ? null : userObject.intVal();
    }
  };

  public static Converter<String, Type> typeConverter = new AbstractConverter<String, Type>(String.class, Type.class)
  {
    @Override
    public Type from(String databaseObject)
    {
      return databaseObject == null ? null : Type.get(databaseObject);
    }

    @Override
    public String to(Type userObject)
    {
      return userObject == null ? null : userObject.getName();
    }
  };

  public static Converter<Timestamp, Instant> timestampConverter = new AbstractConverter<Timestamp, Instant>(Timestamp.class, Instant.class)
  {
    @Override
    public Instant from(Timestamp databaseObject)
    {
      return databaseObject == null ? null : databaseObject.toInstant();
    }

    @Override
    public Timestamp to(Instant userObject)
    {
      return userObject == null ? null : new Timestamp(userObject.toEpochMilli());
    }
  };
  
  public static Converter<UInteger, Integer> uintConverter = new AbstractConverter<UInteger, Integer>(UInteger.class, Integer.class)
  {
    @Override
    public Integer from(UInteger databaseObject)
    {
      return databaseObject == null ? null : databaseObject.intValue();
    }

    @Override
    public UInteger to(Integer userObject)
    {
      return userObject == null ? null : UInteger.valueOf(userObject);
    }
  };
}
