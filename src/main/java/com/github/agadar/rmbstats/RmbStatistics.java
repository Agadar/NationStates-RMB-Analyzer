package com.github.agadar.rmbstats;

import com.github.agadar.nsapi.NSAPI;
import com.github.agadar.nsapi.domain.region.MostLikedRank;
import com.github.agadar.nsapi.domain.region.MostLikesRank;
import com.github.agadar.nsapi.domain.region.MostPostsRank;
import com.github.agadar.nsapi.domain.region.Region;
import com.github.agadar.nsapi.enums.shard.RegionShard;
import com.github.agadar.nsapi.query.RegionQuery;

/**
 * Gateway between the GUI and the NationStates API wrapper.
 * 
 * @author Agadar (https://github.com/Agadar/)
 */
public final class RmbStatistics 
{
    /**
     * Static initializer that sets up the user agent.
     */
    static
    {
        NSAPI.setUserAgent("NationStates RMB Statistics (https://github.com/Agadar/NationStates-RMB-Analyzer)");
    }
    
    /**
     * Generates a report containing the rankings of the nations with the most
     * RMB post likes received, the most RMB post likes given, and the most
     * RMB posts made in the specified region.
     * 
     * If any exception is caught at all, then the returned report contains
     * the error message.
     * 
     * @param region 
     *      The region to generate a report of.
     * @param maxResults 
     *      The maximum number of results per list to return. Always at least 1.
     * @param epochStart 
     *      The lower bound of the timeframe to report on. If 0,
     *      then there is no lower bound.
     * @param epochEnd 
     *      The upper bound of the timeframe to report on. If 0,
     *      then there is no upper bound.
     * @return 
     *      The generated report.
     */
    public static String generateReport(String region, int maxResults, 
            long epochStart, long epochEnd)
    {
        try
        {       
            // Ensure region is properly supplied, otherwise return error.           
            if (region == null || region.isEmpty())
            {
                return "No region specified!";
            }

            // Start preparing query.
            final RegionQuery q = NSAPI.region(region).shards(RegionShard.MostLiked, 
                    RegionShard.MostLikes, RegionShard.MostPosts);

            if (maxResults < 1)
            {
                maxResults = 1;
            }
            
            if (epochStart > 0)
            {
                q.postsFrom(epochStart);
            }

            if (epochEnd > 0)
            {
                q.postsTo(epochEnd);
            }

            // Execute query and build return string.
            final Region r = q.execute();
            
            // Return error message if the region was not found.
            if (r == null)
            {
                return "Region does not exist!";
            }
            
            // Build the string using the retrieved data.
            String toReturn = "--------Most Likes Received--------%n";
            for (int i = 0; i < Math.min(r.MostLikesRanks.size(), maxResults); i++)
            {
                final MostLikesRank rank = r.MostLikesRanks.get(i);
                toReturn += String.format("%s. @%s with %s likes received.%n", i + 1, 
                    rank.Name, rank.Likes);
            }

            toReturn += "%n--------Most Likes Given--------%n";
            for (int i = 0; i < Math.min(r.MostLikedRanks.size(), maxResults); i++)
            {
                final MostLikedRank rank = r.MostLikedRanks.get(i);
                toReturn += String.format("%s. @%s with %s likes given.%n", i + 1, 
                    rank.Name, rank.Liked);
            }

            toReturn += "%n--------Most Posts Total--------%n";
            for (int i = 0; i < Math.min(r.MostPostsRanks.size(), maxResults); i++)
            {
                final MostPostsRank rank = r.MostPostsRanks.get(i);
                toReturn += String.format("%s. @%s with %s posts total.%n", i + 1, 
                                rank.Name, rank.Posts);
            }   

            return String.format(toReturn);
        }
        catch (Exception ex)
        {
            // Whatever exception is thrown, just return its message.
            return ex.getMessage();
        }
    }
}
