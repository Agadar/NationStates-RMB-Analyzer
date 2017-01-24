package com.github.agadar.rmbstats;

import com.github.agadar.nsapi.NSAPI;
import com.github.agadar.nsapi.domain.region.MostLikedRank;
import com.github.agadar.nsapi.domain.region.MostLikesRank;
import com.github.agadar.nsapi.domain.region.MostPostsRank;
import com.github.agadar.nsapi.domain.region.Region;
import com.github.agadar.nsapi.enums.shard.RegionShard;
import com.github.agadar.nsapi.query.RegionQuery;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gateway between the GUI and the NationStates API wrapper.
 *
 * @author Agadar (https://github.com/Agadar/)
 */
public final class RmbStatistics {

    /**
     * Static initializer that sets up the user agent.
     */
    static {
        NSAPI.setUserAgent("NationStates RMB Statistics (https://github.com/Agadar/NationStates-RMB-Analyzer)");
    }

    /**
     * Generates a report containing the rankings of the nations with the most
     * RMB post likes received.
     *
     * @param region The region to generate a report of.
     * @param maxResults The maximum number of results per list to return.
     * @return The generated report.
     */
    private static String mostLikesReceived(Region region, int maxResults) {
        String toReturn = "--------Most Likes Received--------%n";

        for (int i = 0; i < Math.min(region.MostLikesRanks.size(), maxResults); i++) {
            final MostLikesRank rank = region.MostLikesRanks.get(i);
            toReturn += String.format("%s. @%s with %s likes received.%n", i + 1,
                    rank.Name, rank.Likes);
        }
        return toReturn;
    }

    /**
     * Generates a report containing the rankings of the nations with the most
     * RMB post likes given.
     *
     * @param region The region to generate a report of.
     * @param maxResults The maximum number of results per list to return.
     * @return The generated report.
     */
    private static String mostLikesGiven(Region region, int maxResults) {
        String toReturn = "%n--------Most Likes Given--------%n";

        for (int i = 0; i < Math.min(region.MostLikedRanks.size(), maxResults); i++) {
            final MostLikedRank rank = region.MostLikedRanks.get(i);
            toReturn += String.format("%s. @%s with %s likes given.%n", i + 1,
                    rank.Name, rank.Liked);
        }
        return toReturn;
    }

    /**
     * Generates a report containing the rankings of the nations with the most
     * RMB posts made.
     *
     * @param region The region to generate a report of.
     * @param maxResults The maximum number of results per list to return.
     * @return The generated report.
     */
    private static String mostPosts(Region region, int maxResults) {
        String toReturn = "%n--------Most Posts Total--------%n";
        for (int i = 0; i < Math.min(region.MostPostsRanks.size(), maxResults); i++) {
            final MostPostsRank rank = region.MostPostsRanks.get(i);
            toReturn += String.format("%s. @%s with %s posts total.%n", i + 1,
                    rank.Name, rank.Posts);
        }
        return toReturn;
    }

    /**
     * Generates a report containing the rankings of the nations with the most
     * likes per RMB post on average.
     *
     * @param region The region to generate a report of.
     * @param maxResults The maximum number of results per list to return.
     * @return The generated report.
     */
    private static String mostLikesPerPost(Region region, int maxResults) {
        final int minimumNrOfPosts = 10;    // The minimum # of posts a nation must have in order
                                            // to be included in this ranking.                                           
        String toReturn = "%n--------Most Likes Per Post On Average--------%n";
        toReturn += "(Includes only nations with at least " + minimumNrOfPosts + " posts)%n";
        final Map<String, Integer> likes = new HashMap<>();
        final Map<String, Float> avgLikesPerPost = new HashMap<>();
        
        // Start by mapping the likes.
        region.MostLikesRanks.stream().forEach((rank) -> {
            likes.put(rank.Name, rank.Likes);
        });
        
        // Now iterate over the numer of posts and fill avgLikesPerPost, making
        // sure to place only nations in avgLikesPerPost that satisfy the minimum
        // number of posts criterium.
        region.MostPostsRanks.stream().forEach((rank) -> {
            if (rank.Posts >= minimumNrOfPosts) {
                float likesReceived = likes.get(rank.Name);
                float numberOfPosts = rank.Posts;
                avgLikesPerPost.put(rank.Name, likesReceived / numberOfPosts);
            }
        });                               

        // Now sort avgLikesPerPost by the avg likes per post.
        final List<Map.Entry<String, Float>> avgLikesPerPostSorted
                = avgLikesPerPost.entrySet().stream().sorted(
                        Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(Collectors.toList());

        // Trim the results to maxResults and build the string.
        for (int i = 0; i < Math.min(avgLikesPerPostSorted.size(), maxResults); i++) {
            final Map.Entry<String, Float> entry = avgLikesPerPostSorted.get(i);
            BigDecimal rounded = new BigDecimal(Float.toString(entry.getValue()));
            rounded = rounded.setScale(2, BigDecimal.ROUND_HALF_UP);
            toReturn += String.format("%s. @%s with %s likes per post on average.%n",
                    i + 1, entry.getKey(), rounded.toString());
        }
        return toReturn;
    }

    /**
     * Generates a report containing the rankings of the nations with the most
     * endorsements given.
     *
     * @param region The region to generate a report of.
     * @param maxResults The maximum number of results per list to return.
     * @return The generated report.
     */
    private static String mostEndorsementsGiven(Region region, int maxResults) {
        String toReturn = "%n--------Most Endorsements given--------%n";
        // TODO: code
        return toReturn;
    }

    /**
     * Generates a report containing the rankings of the nations with: - the
     * most RMB post likes received; - the most RMB post likes given; - the most
     * RMB posts made; - the most likes per RMB post on average; - the most
     * endorsements given. in the specified region inbetween the specified
     * dates.
     *
     * If any exception is caught at all, then the returned report contains the
     * error message.
     *
     * @param region The region to generate a report of.
     * @param maxResults The maximum number of results per list to return.
     * Always at least 1.
     * @param epochStart The lower bound of the timeframe to report on. If 0,
     * then there is no lower bound.
     * @param epochEnd The upper bound of the timeframe to report on. If 0, then
     * there is no upper bound.
     * @return The generated report.
     */
    public static String generateReport(String region, int maxResults,
            long epochStart, long epochEnd) {
        try {
            // Ensure region is properly supplied, otherwise return error.           
            if (region == null || region.isEmpty()) {
                return "No region specified!";
            }

            // Start preparing query.
            final RegionQuery q = NSAPI.region(region).shards(RegionShard.MostLiked,
                    RegionShard.MostLikes, RegionShard.MostPosts);

            if (maxResults < 1) {
                maxResults = 1;
            }

            if (epochStart > 0) {
                q.postsFrom(epochStart);
            }

            if (epochEnd > 0) {
                q.postsTo(epochEnd);
            }

            // Execute query and build return string.
            final Region r = q.execute();

            // Return error message if the region was not found.
            if (r == null) {
                return "Region does not exist!";
            }

            // Build the string using the retrieved data.
            String toReturn = mostLikesReceived(r, maxResults);
            toReturn += mostLikesGiven(r, maxResults);
            toReturn += mostPosts(r, maxResults);
            toReturn += mostLikesPerPost(r, maxResults);
            toReturn += mostEndorsementsGiven(r, maxResults);
            return String.format(toReturn);
        } catch (Exception ex) {
            // Whatever exception is thrown, just return its message.
            return ex.getMessage();
        }
    }
}
