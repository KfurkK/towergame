import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Scanner;

public class tools {

    public static void main(String[] args) throws FileNotFoundException {
        // Example usage: readCoordinates("path/to/level2.txt");
    }

    public static ArrayList<int[]> readCoordinates(String filePath) throws FileNotFoundException {
        ArrayList<int[]> coordinates = new ArrayList<>(); // actual storage
        boolean startReading = false;

        try (Scanner scanner = new Scanner((new File(filePath))) ) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();

                if (line.startsWith("HEIGHT:")) {
                    // Start reading coordinates after HEIGHT marker
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
        // Debug print loop removed

        return coordinates;
    }
    // 5 enemies, 1 seconds between enemies, 2 seconds delay before start
    public static double[][] getWaveData(int level) {
        if (level == 1) {
            return new double[][] {
                    {5, 1, 2},
                    {8, 0.5, 5},
                    {12, 0.3, 5}
            };
        } else if (level == 2) {
            return new double[][] {
                    {5, 1, 2},
                    {8, 0.5, 5},
                    {12, 0.3, 5},
                    {20, 0.3, 5}
            };
        } else if (level == 3) {
            return new double[][] {
                    {5, 1, 2},
                    {8, 0.5, 5},
                    {12, 0.3, 5},
                    {20, 0.3, 5},
                    {25, 0.3, 5}
            };
        } else if (level == 4) {
            return new double[][] {
                    {5, 1, 2},
                    {8, 0.5, 5},
                    {12, 0.3, 5},
                    {20, 0.3, 5},
                    {25, 0.3, 5}
            };
        } else if (level == 5) {
            return new double[][] {
                    {5, 1, 2},
                    {8, 0.5, 5},
                    {12, 0.3, 5},
                    {20, 0.3, 5},
                    {25, 0.3, 5}
            };
        }

        // Default return for undefined levels
        return new double[][] {
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0}
        };
    }

	public static int getMapSize(String filePath) throws FileNotFoundException {
		try (Scanner scanner = new Scanner(new File(filePath))) {
	        while (scanner.hasNextLine()) {
	            String line = scanner.nextLine().trim();
	            if (line.startsWith("WIDTH:")) {
	                return Integer.parseInt(line.split(":")[1].trim());
	            }
	        }
	    }
	    return 10;
	}


}

