package com.github.agadar.test;

import com.github.agadar.nsapi.NSAPI;
import com.github.agadar.nsapi.domain.region.MostLikedRank;
import com.github.agadar.nsapi.domain.region.MostLikesRank;
import com.github.agadar.nsapi.domain.region.MostPostsRank;
import com.github.agadar.nsapi.domain.region.Region;
import com.github.agadar.nsapi.enums.shard.RegionShard;
import com.github.agadar.nsapi.query.RegionQuery;

/**
 *
 * @author Agadar <https://github.com/Agadar/>
 */
public class RMBAnalyzerMain
{  
    private final static String USER_AGENT = "NationStates RMB Statistics (https://github.com/Agadar/NationStates-RMB-Analyzer)";
    private final static String REGION = "the western isles";
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        // Setup user agent and region query.
        NSAPI.setUserAgent(USER_AGENT);
        final RegionQuery q = NSAPI.region(REGION).shards(RegionShard.MostLiked, 
                RegionShard.MostLikes, RegionShard.MostPosts).postsFrom(1481140823).postsTo(1481142023);
        Region r;
        
        // Try executing the query. Log a warning and return if it fails.
        try {
            r = q.execute();
        }
        catch (Exception ex) {
            System.err.print(ex);
            return;
        }
        
        // Test prints
        System.out.println(r.MostLikedRanks.size());
        System.out.println(r.MostLikesRanks.size());
        System.out.println(r.MostPostsRanks.size());
        
        // Print it all out in the console for now.
        String toPrint = "%n--------Most Likes Received--------%n";
        
        for (int i = 0; i < r.MostLikedRanks.size(); i++)
        {
            final MostLikedRank rank = r.MostLikedRanks.get(i);
            toPrint += String.format("%s. @%s with %s likes received.%n", i + 1, 
                rank.Name, rank.Liked);
        }
        
        toPrint += "%n--------Most Likes Given--------%n";
        
        for (int i = 0; i < r.MostLikesRanks.size(); i++)
        {
            final MostLikesRank rank = r.MostLikesRanks.get(i);
            toPrint += String.format("%s. @%s with %s likes given.%n", i + 1, 
                rank.Name, rank.Likes);
        }
        
        toPrint += "%n--------Most Posts Total--------%n";

        for (int i = 0; i < r.MostPostsRanks.size(); i++)
        {
            final MostPostsRank rank = r.MostPostsRanks.get(i);
            toPrint += String.format("%s. @%s with %s posts total.%n", i + 1, 
                rank.Name, rank.Posts);
        }
        
        System.out.println(String.format(toPrint));
    }
}