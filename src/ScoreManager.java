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

public class ScoreManager {
    private static final String FILE_PATH = "scores.txt";
    private static final int MAX_SCORES = 5;
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void saveScore(int score) {
        List<ScoreEntry> entries = loadEntries();
        
        boolean duplicate = entries.stream().anyMatch(e -> e.score == score);
        if (duplicate) return;

        // Şu anki zamanı ekle
        String time = formatter.format(new Date());
        entries.add(new ScoreEntry(score, time));

        // Skorları büyükten küçüğe sırala
        entries.sort((a, b) -> Integer.compare(b.score, a.score));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (int i = 0; i < Math.min(MAX_SCORES, entries.size()); i++) {
                ScoreEntry entry = entries.get(i);
                writer.write(entry.score + "@" + entry.timestamp);
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
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("@");
                if (parts.length == 2) {
                    int score = Integer.parseInt(parts[0].trim());
                    String timestamp = parts[1].trim();
                    entries.add(new ScoreEntry(score, timestamp));
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return entries;
    }

    // Nested class for skor nesnesi
    public static class ScoreEntry {
        public int score;
        public String timestamp;

        public ScoreEntry(int score, String timestamp) {
            this.score = score;
            this.timestamp = timestamp;
        }
    }
}