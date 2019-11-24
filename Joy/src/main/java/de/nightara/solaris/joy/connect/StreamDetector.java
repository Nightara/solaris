package de.nightara.solaris.joy.connect;

import java.net.*;
import java.util.*;
import com.google.gson.*;
import discord4j.core.object.entity.*;
import discord4j.core.object.presence.*;
import discord4j.core.object.util.*;
import gnu.trove.map.hash.*;
import reactor.core.publisher.*;

import java.io.*;
import java.time.*;

import static de.nightara.solaris.core.util.Util.*;

public class StreamDetector implements Runnable
{
  private static final JsonParser PARSER = new JsonParser();

  private final Snowflake guildId;
  private final List<User> streamSources;
  private final MessageChannel notificationChannel;
  private final Map<User, URL> videoSources;

  private Instant lastCheck;

  public StreamDetector(Snowflake guildId, MessageChannel notificationChannel)
  {
    this.guildId = guildId;
    this.lastCheck = Instant.MIN;
    this.notificationChannel = notificationChannel;
    this.videoSources = Collections.synchronizedMap(new THashMap<>());
    this.streamSources = Collections.synchronizedList(new LinkedList<>());
  }

  public boolean addVideoSource(User discordUser, String youtubeUser, String apiKey)
  {
    try
    {
      JsonObject channel = PARSER.parse(new InputStreamReader(new URL("https://www.googleapis.com/youtube/v3/channels?part=snippet,contentDetails&forUsername="
              + youtubeUser + "&key=" + apiKey).openStream())).getAsJsonObject();
      String uploads = channel.getAsJsonArray("items").get(0).getAsJsonObject()
              .getAsJsonObject("contentDetails")
              .getAsJsonObject("relatedPlaylists")
              .get("uploads").getAsString();
      videoSources.put(discordUser, new URL("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=1&playlistId=" + uploads + "&key=" + apiKey));
    }
    catch(IOException ex)
    {
      LOG.warn("Failed to add YouTube channel {} with API key {}", youtubeUser, apiKey);
      return false;
    }
    return true;
  }

  @Override
  public void run()
  {
    streamSources.stream()
        .map(user -> user.asMember(guildId)
            .flatMap(Member::getPresence)
            .map(Presence::getActivity)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(Activity::getStreamingUrl)
            .filter(Optional::isPresent)
            .map(Optional::get))
        .map(Mono::block)
        .map(url -> "@here\n" + "Wir sind live auf Twitch!\nKomm auf " + url + ", um zuzuschauen!")
        .forEach(msg -> sendMessage(notificationChannel, msg));
    videoSources.forEach((user, url) ->
    {
      try
      {
        JsonObject lastUpload = PARSER.parse(new InputStreamReader(url.openStream())).getAsJsonObject()
                .getAsJsonArray("items").get(0).getAsJsonObject()
                .getAsJsonObject("snippet");
        if(Instant.parse(lastUpload.get("publishedAt").getAsString()).isAfter(lastCheck))
        {
          sendMessage(notificationChannel, "@everyone\n" + user.getMention() + " hat gerade ein neues Video auf YouTube hochgeladen!\n"
                  + "Komm auf https://youtube.com/watch?v=" + lastUpload.getAsJsonObject("resourceId").get("videoId").getAsString()
                  + ", um \"" + lastUpload.get("title").getAsString() + "\" anzuschauen!");
        }
      }
      catch(IOException ex)
      {
        LOG.warn("Failed to read YouTube channel for {}", user.getUsername());
      }
    });
    lastCheck = Instant.now();
  }
}
