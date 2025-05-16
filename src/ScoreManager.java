//150123005 Ayberk SARAÇ / 150124035 Kamil Furkan KUNT / 150124075 Eren VURAL
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

 // It allows keeping scores and keeps track of the time and day the user made.
public class ScoreManager {
    private static final String FILE_PATH = "scores.txt";
    private static final int MAX_SCORES = 5;
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void saveScore(String username, int score) {
        List<ScoreEntry> entries = loadEntries();
        
        for (ScoreEntry entry : entries) {
            if (entry.username.equals(username) && entry.score == score) {
                return; 
            }
        }
        
        boolean duplicate = entries.stream().anyMatch(e -> e.score == score);
        if (duplicate) return;

        // Add the time.
        String time = formatter.format(new Date());
        entries.add(new ScoreEntry(username, score, time));
        
        // Sorts and lists scores

        
        entries.sort((a, b) -> Integer.compare(b.score, a.score));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (int i = 0; i < Math.min(MAX_SCORES, entries.size()); i++) {
                ScoreEntry entry = entries.get(i);
                writer.write(entry.username+ "@" + entry.score + "@" + entry.timestamp);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<ScoreEntry> loadEntries() {
        List<ScoreEntry> entries = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) return entries;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        	//writes scores to a file
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("@");
                if (parts.length == 3) {
                	String username = parts[0];
                    int score = Integer.parseInt(parts[1]);
                    String timestamp = parts[2];
                    entries.add(new ScoreEntry(username, score, timestamp));
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return entries;
    }

    
    public static class ScoreEntry {
    	public String username;
        public int score;
        public String timestamp;

        public ScoreEntry(String username, int score, String timestamp) {
        	this.username = username;
            this.score = score;
            this.timestamp = timestamp;
        }
    }
}