package de.nightara.solaris.core;

import de.nightara.solaris.core.util.Util;
import java.util.*;
import java.util.stream.*;
import org.apache.commons.lang3.*;
import org.junit.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class UtilTest
{
  @Test
  public void testBreakString()
  {
    String m1 = "qwe rtzuiopppppü asdfghjklöäyxcvbn\nm qwert zu iopü\nasd fghj  klöä";
    String m1Stitched = StringUtils.remove(StringUtils.remove(m1, '\n'), ' ');
    for(int x = 5; x < 30; x++)
    {
      List<String> broken = Util.breakString(m1, x);
      String brokenStitched = StringUtils.remove(StringUtils.remove(String.join("", broken), '\n'), ' ');
      
      assertEquals(m1Stitched, brokenStitched);
      for(int y = 0; y < broken.size(); y++)
      {
        String fragment = broken.get(y);
        assertThat(fragment.length(), allOf(greaterThan(0), lessThanOrEqualTo(x)));
        if(y > 0)
        {
          assertThat(broken.get(y - 1).length() + fragment.length(), greaterThanOrEqualTo(x));
        }
        if(y < broken.size() - 1)
        {
          assertThat(fragment.length() + broken.get(y + 1).length(), greaterThanOrEqualTo(x));
        }
      }
    }
  }
}
