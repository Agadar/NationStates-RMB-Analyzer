package com.github.agadar.test;

import com.github.agadar.nsapi.domain.region.RegionalMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 *
 * @author Agadar <https://github.com/Agadar/>
 */
public final class Poster 
{
    private final static Map<String, Poster> Posters = new TreeMap<>();
    private final static long Now = System.currentTimeMillis() / 1000;
    
    public final String Name;
    
    private long FirstPostTimeStamp = Now;
    private int Messages = 0;
    private double AvgPostsPerDay;
    private int TotalLikesReceived = 0;
    private int TotalLikesGiven = 0;   
    private final Map<String, Integer> LikesReceivedBy = new TreeMap<>();
    private final Map<String, Integer> LikesGivenTo = new TreeMap<>();

    public static void Process(RegionalMessage msg)
    {
        Poster poster = GetOrCreatePoster(msg.Author);
        poster.Messages++;
        
        if (msg.Timestamp < poster.FirstPostTimeStamp)
        {
            poster.FirstPostTimeStamp = msg.Timestamp;
        }
        
        poster.AvgPostsPerDay = (double) poster.Messages / Math.max(1, 
            (double) SECONDS.toDays(Now - poster.FirstPostTimeStamp));
        
        if (msg.LikedBy != null)
        {
            for (String likerStr : msg.LikedBy)
            {
                GetOrCreatePoster(likerStr).putLikeGivenTo(msg.Author);
                poster.putLikeReceivedBy(likerStr);
            }
        }
    }
    
    public static List<Poster> mostLikesReceivedRanking()
    {
        List<Poster> posters = new ArrayList(Posters.values());
        Collections.sort(posters, (Poster t, Poster t1) ->
        {
            return Integer.compare(t1.TotalLikesReceived, t.TotalLikesReceived);
        });
        return posters;
    }
    
    public static List<Poster> mostLikesGivenRanking()
    {
        List<Poster> posters = new ArrayList(Posters.values());
        Collections.sort(posters, (Poster t, Poster t1) ->
        {
            return Integer.compare(t1.TotalLikesGiven, t.TotalLikesGiven);
        });
        return posters;
    }
    
    public static List<Poster> mostPostsRanking()
    {
        List<Poster> posters = new ArrayList(Posters.values());
        Collections.sort(posters, (Poster t, Poster t1) ->
        {
            return Integer.compare(t1.Messages, t.Messages);
        });
        return posters;
    }
    
    public static List<Poster> mostAvgPostsPerDayRanking()
    {
        List<Poster> posters = new ArrayList(Posters.values());
        Collections.sort(posters, (Poster t, Poster t1) ->
        {
            return Double.compare(t1.AvgPostsPerDay, t.AvgPostsPerDay);
        });
        return posters;
    }
    
    private static Poster GetOrCreatePoster(String name)
    {
        Poster poster = Posters.get(name);
        
        if (poster == null)
        {
            Posters.put(name, poster = new Poster(name));
        }
        
        return poster;
    }
    
    private Poster(String name)
    {
        this.Name = name;
    }

    private void putLikeReceivedBy(String name)
    {
        int likes = LikesReceivedBy.getOrDefault(name, 0) + 1;
        LikesReceivedBy.put(name, likes);
        TotalLikesReceived++;
    }

    private void putLikeGivenTo(String name)
    {
        int likes = LikesGivenTo.getOrDefault(name, 0) + 1;
        LikesGivenTo.put(name, likes);
        TotalLikesGiven++;
    }

    public int getTotalLikesReceived()
    {
        return TotalLikesReceived;
    }

    public int getTotalLikesGiven()
    {
        return TotalLikesGiven;
    }

    public int getMessages()
    {
        return Messages;
    }

    public double getAvgPostsPerDay()
    {
        return AvgPostsPerDay;
    }
}
