package com.github.agadar.test;

import com.github.agadar.nsapi.NSAPI;
import com.github.agadar.nsapi.domain.region.Region;
import com.github.agadar.nsapi.domain.region.RegionalMessage;
import com.github.agadar.nsapi.enums.shard.RegionShard;
import com.github.agadar.nsapi.query.RegionQuery;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Agadar <https://github.com/Agadar/>
 */
public class RMBAnalyzerMain
{
    /** The logger for this object. */
    private final static Logger logger = Logger.getLogger(RMBAnalyzerMain.class.getName());   
    private final static String USER_AGENT = "Agadar's RMB Analyzer";
    private final static String REGION = "the western isles";
    private final static String DIRECTORY = "C:\\Users\\Martin\\Desktop\\"
                    + "The Western Isles RMB posts\\";
    private final static String FILEFORMAT = DIRECTORY + "%s.to.%s.xml";
    
    /** Starting post id. Set to 1 to start reading from the very first post. */
    private final static long FROM_ID = 20444412;   
    private final static boolean Download = false;
    private final static DecimalFormat formatter = new DecimalFormat("0.00");
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException
    {
        if (Download)
        {
            NSAPI.setUserAgent(USER_AGENT);
            final RegionQuery baseQuery = NSAPI.region(REGION).shards(RegionShard.RegionalMessages);
            long fromId = FROM_ID;

            while(true)
            {
                final List<RegionalMessage> newMessages = 
                    baseQuery.messagesFromId(fromId).execute().RegionalMessages;               

                if (newMessages == null || newMessages.isEmpty())
                {
                    break;
                }

                final Region region = new Region();
                region.RegionalMessages = new ArrayList<>();

                for (RegionalMessage newMsg : newMessages)
                {
                    if (newMsg.Id >= fromId)
                    {
                        region.RegionalMessages.add(newMsg);
                    }
                }

                final long firstId = region.RegionalMessages.get(0).Id;
                fromId = region.RegionalMessages.get(region.RegionalMessages
                        .size() - 1).Id;
                final File file = new File(String.format(FILEFORMAT, firstId, fromId));

                try (FileOutputStream stream = new FileOutputStream(file))
                {
                    // if file doesn't exists, then create it
                    if (!file.exists()) 
                    {
                        file.createNewFile();
                    }

                    try (ByteArrayOutputStream bytesOut = NSAPI.objectToXml(region))
                    {
                        bytesOut.writeTo(stream);
                    }

                    fromId++;
                }
            }
        }
        
//Rankings
//- nations with most likes given;
//- nations with most likes received;
//- nations with most posts;
//- nations with highest average number of posts per day;
//- the most liked posts.
//
//Lists/graphs
//- posts per day
//- number of posting nations per day

        File[] files = (new File(DIRECTORY)).listFiles();
        System.out.println("Files to process: " + files.length);
        
        // Iterate over all created xml files.
        for (int i = 0; i < files.length; i++)
        {
            try (FileInputStream is = new FileInputStream(files[i]))
            {
                NSAPI.xmlToObject(is, Region.class).RegionalMessages.forEach(
                        (msg) -> Poster.Process(msg));
            }
            
            System.out.println("Processed " + (i + 1) + "/" + files.length);
        }
        
        String toPrint = "%n--------Most Likes Received--------%n";
        final List<Poster> likesReceivedRanks = Poster.mostLikesReceivedRanking().subList(0, 100);
        for (int i = 0; i < likesReceivedRanks.size(); i++)
        {
            final Poster poster = likesReceivedRanks.get(i);
            toPrint += String.format("%s. @%s with %s likes received.%n", i + 1, 
                poster.Name, poster.getTotalLikesReceived());
        }
        
        toPrint += "%n--------Most Likes Given--------%n";
        final List<Poster> likesGivenRanks = Poster.mostLikesGivenRanking().subList(0, 100);
        for (int i = 0; i < likesGivenRanks.size(); i++)
        {
            final Poster poster = likesGivenRanks.get(i);
            toPrint += String.format("%s. @%s with %s likes given.%n", i + 1, 
                poster.Name, poster.getTotalLikesGiven());
        }
        
        toPrint += "%n--------Most Posts Total--------%n";
        final List<Poster> postsTotalRanks = Poster.mostPostsRanking().subList(0, 100);
        for (int i = 0; i < postsTotalRanks.size(); i++)
        {
            final Poster poster = postsTotalRanks.get(i);
            toPrint += String.format("%s. @%s with %s posts total.%n", i + 1, 
                poster.Name, poster.getMessages());
        }
        
        toPrint += "%n--------Highest Average Posts Per Day--------%n";
        final List<Poster> avgPostsRanks = Poster.mostAvgPostsPerDayRanking().subList(0, 100);
        for (int i = 0; i < avgPostsRanks.size(); i++)
        {
            final Poster poster = avgPostsRanks.get(i);
            toPrint += String.format("%s. @%s with %s posts per day.%n", i + 1, 
                poster.Name, formatter.format(poster.getAvgPostsPerDay()));
        }
        
        System.out.println(String.format(toPrint));
    }
    
    /**
     * Closes the given InputStream. If an exception occurs, the exception is
     * logged, not thrown or returned. Use this if you don't particularly care
     * about catching exceptions thrown by closing InputStreams.
     * 
     * @param stream the InputStream to close
     */
    protected final static void closeInputStreamQuietly(InputStream stream)
    {
        if (stream == null) return;
        
        try
        {
            stream.close();
        }
        catch (IOException ex)
        {
            logger.log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Closes the given OutputStream. If an exception occurs, the exception is
     * logged, not thrown or returned. Use this if you don't particularly care
     * about catching exceptions thrown by closing OutputStreams.
     * 
     * @param stream the OutputStream to close
     */
    protected final static void closeOutputStreamQuietly(OutputStream stream)
    {
        if (stream == null) return;
        
        try
        {
            stream.close();
        }
        catch (IOException ex)
        {
            logger.log(Level.SEVERE, null, ex);
        }
    }
}