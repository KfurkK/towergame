import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Scanner;

public class tools {

    public static void main(String[] args) throws FileNotFoundException {
        readCoordinates("C:/Users/kamil/IdeaProjects/towergame/src/levels/level2.txt");
    }

    public static ArrayList<int[]> readCoordinates(String filePath) throws FileNotFoundException {
        ArrayList<int[]> coordinates = new ArrayList<>(); // actual storage
        boolean startReading = false;

        try (Scanner scanner = new Scanner((new File(filePath))) ) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();

                if (line.startsWith("HEIGHT:")) {
                    // start reading
                    startReading = true;
                    continue;
                }

                if (line.equals("WAVE_DATA:")) {
                    break;
                }

                if (startReading) {
                    String[] parts = line.split(",");
                    int x = Integer.parseInt(parts[0].trim());
                    int y = Integer.parseInt(parts[1].trim());
                    coordinates.add(new int[]{x, y});
                }

            }

        }
        //for (int[] duo: coordinates) {
        //    System.out.println("|" + duo[0] + "," + duo[1]);
        //}

        return coordinates;
    }
}

