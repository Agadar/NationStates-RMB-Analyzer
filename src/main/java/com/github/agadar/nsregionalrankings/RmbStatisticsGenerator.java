package com.github.agadar.nsregionalrankings;

import com.github.agadar.nationstates.NationStates;
import com.github.agadar.nationstates.domain.region.MostLikedRank;
import com.github.agadar.nationstates.domain.region.MostLikesRank;
import com.github.agadar.nationstates.domain.region.MostPostsRank;
import com.github.agadar.nationstates.domain.region.Region;
import com.github.agadar.nationstates.query.RegionQuery;
import com.github.agadar.nationstates.shard.RegionShard;

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
public final class RmbStatisticsGenerator {

    /**
     * Generates a report containing the rankings of the nations with the most
     * RMB post likes received.
     *
     * @param region The region to generate a report of.
     * @param maxResults The maximum number of results per list to return.
     * @return The generated report.
     */
    private String mostLikesReceived(Region region, int maxResults) {
        String toReturn = "--------Most Likes Received--------%n";

        for (int i = 0; i < Math.min(region.mostLikesRanks.size(), maxResults); i++) {
            final MostLikesRank rank = region.mostLikesRanks.get(i);
            toReturn += String.format("%s. %s with %s likes received.%n", i + 1,
                    wrapInNationTags(rank.name), rank.likes);
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
    private String mostLikesGiven(Region region, int maxResults) {
        String generatedString = "%n--------Most Likes Given--------%n";

        for (int i = 0; i < Math.min(region.mostLikedRanks.size(), maxResults); i++) {
            final MostLikedRank rank = region.mostLikedRanks.get(i);
            generatedString += String.format("%s. %s with %s likes given.%n", i + 1,
                    wrapInNationTags(rank.name), rank.liked);
        }
        return generatedString;
    }

    /**
     * Generates a report containing the rankings of the nations with the most
     * RMB posts made.
     *
     * @param region The region to generate a report of.
     * @param maxResults The maximum number of results per list to return.
     * @return The generated report.
     */
    private String mostPosts(Region region, int maxResults) {
        String toReturn = "%n--------Most Posts Total--------%n";
        for (int i = 0; i < Math.min(region.mostPostsRanks.size(), maxResults); i++) {
            final MostPostsRank rank = region.mostPostsRanks.get(i);
            toReturn += String.format("%s. %s with %s posts total.%n", i + 1,
                    wrapInNationTags(rank.name), rank.posts);
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
    private String mostLikesPerPost(Region region, int maxResults) {
        final int minimumNrOfPostsRequired = 10;
        String toReturn = "%n--------Most Likes Per Post On Average--------%n";
        toReturn += "(Includes only nations with at least " + minimumNrOfPostsRequired + " posts)%n";
        final Map<String, Integer> likes = new HashMap<>();
        final Map<String, Float> avgLikesPerPost = new HashMap<>();

        // Start by mapping the likes.
        region.mostLikesRanks.stream().forEach((rank) -> {
            likes.put(rank.name, rank.likes);
        });

        // Now iterate over the number of posts and fill avgLikesPerPost, making
        // sure to place only nations in avgLikesPerPost that satisfy the minimum
        // number of posts criterium.
        region.mostPostsRanks.stream()
                .filter((rank) -> rank.posts >= minimumNrOfPostsRequired)
                .filter((rank) -> likes.containsKey(rank.name))
                .forEach((rank) -> {
                    float likesReceived = likes.get(rank.name);
                    float numberOfPosts = rank.posts;
                    avgLikesPerPost.put(rank.name, likesReceived / numberOfPosts);
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
            toReturn += String.format("%s. %s with %s likes per post on average.%n",
                    i + 1, wrapInNationTags(entry.getKey()), rounded.toString());
        }
        return toReturn;
    }

    private String wrapInNationTags(String nationName) {
        return String.format("[nation]%s[/nation]", nationName);
    }

    /**
     * Generates a report containing the rankings of the nations with: - the
     * most RMB post likes received; - the most RMB post likes given; - the most
     * RMB posts made; - the most likes per RMB post on average; - the most
     * endorsements given. in the specified region in between the specified
     * dates.
     *
     * If any exception is caught at all, then the returned report contains the
     * error message.
     *
     * @param region The region to generate a report of.
     * @param maxResults The maximum number of results per list to return.
     * Always at least 1.
     * @param epochStart The lower bound of the time frame to report on. If 0,
     * then there is no lower bound.
     * @param epochEnd The upper bound of the time frame to report on. If 0,
     * then there is no upper bound.
     * @return The generated report.
     */
    public String generateReport(String region, int maxResults,
            long epochStart, long epochEnd) {
        if (region == null || region.isEmpty()) {
            return "No region specified!";
        }
        if (maxResults < 1) {
            maxResults = 1;
        }
        final RegionQuery regionQuery = NationStates.region(region).shards(RegionShard.MOST_LIKED,
                RegionShard.MOST_LIKES, RegionShard.MOST_POSTS);

        if (epochStart > 0) {
            regionQuery.postsFrom(epochStart);
        }

        if (epochEnd > 0) {
            regionQuery.postsTo(epochEnd);
        }

        Region regionQueryResult;
        try {
            regionQueryResult = regionQuery.execute();
        } catch (Exception ex) {
            return ex.getMessage();
        }

        if (regionQueryResult == null) {
            return "Region does not exist!";
        }

        String toReturn = mostLikesReceived(regionQueryResult, maxResults);
        toReturn += mostLikesGiven(regionQueryResult, maxResults);
        toReturn += mostPosts(regionQueryResult, maxResults);
        toReturn += mostLikesPerPost(regionQueryResult, maxResults);
        return String.format(toReturn);
    }
}
