package com.github.agadar.nsregionalrankings;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.agadar.nationstates.INationStates;
import com.github.agadar.nationstates.domain.region.Region;
import com.github.agadar.nationstates.shard.RegionShard;

/**
 * Gateway between the GUI and the NationStates API wrapper.
 *
 * @author Agadar (https://github.com/Agadar/)
 */
public final class RmbStatisticsGenerator {

    private final INationStates nationStates;

    /**
     * Constructor.
     * 
     * @param nationStates The underlying NationStates agent to use.
     */
    public RmbStatisticsGenerator(INationStates nationStates) {
        this.nationStates = nationStates;
    }

    /**
     * Generates a report containing the rankings of the nations with the most RMB
     * post likes received.
     *
     * @param region     The region to generate a report of.
     * @param maxResults The maximum number of results per list to return.
     * @return The generated report.
     */
    private String mostLikesReceived(Region region, int maxResults) {
        String toReturn = "--------Most Likes Received--------%n";

        for (int i = 0; i < Math.min(region.getMostLikesRanks().size(), maxResults); i++) {
            var rank = region.getMostLikesRanks().get(i);
            toReturn += String.format("%s. %s with %s likes received.%n", i + 1, wrapInNationTags(rank.getName()),
                    rank.getLikes());
        }
        return toReturn;
    }

    /**
     * Generates a report containing the rankings of the nations with the most RMB
     * post likes given.
     *
     * @param region     The region to generate a report of.
     * @param maxResults The maximum number of results per list to return.
     * @return The generated report.
     */
    private String mostLikesGiven(Region region, int maxResults) {
        String generatedString = "%n--------Most Likes Given--------%n";

        for (int i = 0; i < Math.min(region.getMostLikedRanks().size(), maxResults); i++) {
            var rank = region.getMostLikedRanks().get(i);
            generatedString += String.format("%s. %s with %s likes given.%n", i + 1, wrapInNationTags(rank.getName()),
                    rank.getLiked());
        }
        return generatedString;
    }

    /**
     * Generates a report containing the rankings of the nations with the most RMB
     * posts made.
     *
     * @param region     The region to generate a report of.
     * @param maxResults The maximum number of results per list to return.
     * @return The generated report.
     */
    private String mostPosts(Region region, int maxResults) {
        String toReturn = "%n--------Most Posts Total--------%n";
        for (int i = 0; i < Math.min(region.getMostPostsRanks().size(), maxResults); i++) {
            var rank = region.getMostPostsRanks().get(i);
            toReturn += String.format("%s. %s with %s posts total.%n", i + 1, wrapInNationTags(rank.getName()),
                    rank.getPosts());
        }
        return toReturn;
    }

    /**
     * Generates a report containing the rankings of the nations with the most likes
     * per RMB post on average.
     *
     * @param region     The region to generate a report of.
     * @param maxResults The maximum number of results per list to return.
     * @return The generated report.
     */
    private String mostLikesPerPost(Region region, int maxResults) {
        int minimumNrOfPostsRequired = 10;
        String toReturn = "%n--------Most Likes Per Post On Average--------%n";
        toReturn += "(Includes only nations with at least " + minimumNrOfPostsRequired + " posts)%n";
        var likes = new HashMap<String, Integer>();

        region.getMostLikesRanks().stream().forEach((rank) -> {
            likes.put(rank.getName(), rank.getLikes());
        });

        var ranks = region.getMostPostsRanks().stream()
                .filter((rank) -> rank.getPosts() >= minimumNrOfPostsRequired)
                .filter((rank) -> likes.containsKey(rank.getName())).map((rank) -> {
                    float likesReceived = likes.get(rank.getName());
                    float numberOfPosts = rank.getPosts();
                    return new Pair<>(rank.getName(), likesReceived / numberOfPosts);
                }).sorted((rankLeft, rankRight) -> {
                    int compare = Float.compare(rankRight.b, rankLeft.b);
                    return compare == 0 ? (rankLeft.a).compareTo(rankRight.a) : compare;
                }).limit(maxResults).collect(Collectors.toList());

        for (int i = 0; i < ranks.size(); i++) {
            var rank = ranks.get(i);
            BigDecimal rounded = new BigDecimal(Float.toString(rank.b));
            rounded = rounded.setScale(2, RoundingMode.HALF_UP);
            toReturn += String.format("%s. %s with %s likes per post on average.%n", i + 1, wrapInNationTags(rank.a),
                    rounded.toString());
        }
        return toReturn;
    }

    private String wrapInNationTags(String nationName) {
        return String.format("[nation]%s[/nation]", nationName);
    }

    /**
     * Generates a report containing the rankings of the nations with: - the most
     * RMB post likes received; - the most RMB post likes given; - the most RMB
     * posts made; - the most likes per RMB post on average; - the most endorsements
     * given. in the specified region in between the specified dates.
     *
     * If any exception is caught at all, then the returned report contains the
     * error message.
     *
     * @param region     The region to generate a report of.
     * @param maxResults The maximum number of results per list to return. Always at
     *                   least 1.
     * @param epochStart The lower bound of the time frame to report on. If 0, then
     *                   there is no lower bound.
     * @param epochEnd   The upper bound of the time frame to report on. If 0, then
     *                   there is no upper bound.
     * @return The generated report.
     */
    public String generateReport(String region, int maxResults, long epochStart, long epochEnd) {
        if (region == null || region.isEmpty()) {
            return "No region specified!";
        }
        if (maxResults < 1) {
            maxResults = 1;
        }
        var regionQuery = nationStates.getRegion(region).shards(RegionShard.MOST_LIKED, RegionShard.MOST_LIKES,
                RegionShard.MOST_POSTS);

        if (epochStart > 0) {
            regionQuery.postsFrom(epochStart);
        }
        if (epochEnd > 0) {
            regionQuery.postsTo(epochEnd);
        }

        Optional<Region> regionQueryResultOptional;
        try {
            regionQueryResultOptional = regionQuery.execute();
        } catch (Exception ex) {
            return ex.getMessage();
        }

        if (regionQueryResultOptional.isEmpty()) {
            return "Region does not exist!";
        }
        var regionQueryResult = regionQueryResultOptional.get();

        String toReturn = mostLikesReceived(regionQueryResult, maxResults);
        toReturn += mostLikesGiven(regionQueryResult, maxResults);
        toReturn += mostPosts(regionQueryResult, maxResults);
        toReturn += mostLikesPerPost(regionQueryResult, maxResults);
        return String.format(toReturn);
    }
}
