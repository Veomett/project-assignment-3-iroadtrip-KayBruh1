import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class IRoadTrip {
    private HashMap<String, List<String>> countryBorders = new HashMap<>();
    private HashMap<Integer, capitalsInfo> capdistMap = new HashMap<>();
    private HashMap<String, stateInfo> stateInfoMap = new HashMap<>();

    public IRoadTrip(String[] args) {
        try {
            String bordersFile = readFile(args[0]);
            handleBorders(countryBorders, bordersFile);

            String capdistFile = readFile(args[1]);
            processCapDist(capdistFile);

            String stateNameFile = readFile(args[2]);
            processStateName(stateNameFile);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private String readFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readString(path);
    }

    private void handleBorders(HashMap<String, List<String>> countryBorders, String fileContent) {
        Scanner scan = new Scanner(fileContent);

        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            String[] parts = line.split("=");

            String countryName = parts[0].trim();
            String borderInfo = parts[1].trim();

            List<String> countryBounds = addBorders(borderInfo);

            countryBorders.put(countryName, countryBounds);
        }
        scan.close();
    }

    private List<String> addBorders(String borderInfo) {
        List<String> neighboringCountries = new ArrayList<>();

        String[] borders = borderInfo.split(";");

        for (String border : borders) {
            String neighbor = border.trim().split(":")[0];
            neighboringCountries.add(neighbor);
        }

        return neighboringCountries;
    }

    public static class capitalsInfo {
        private int countryAId;
        private String countryACode;
        private int countryBId;
        private String countryBCode;
        private int kmDistance;

        public capitalsInfo(int countryAId, String countryACode, int countryBId, String countryBCode, int kmDistance) {
            this.countryAId = countryAId;
            this.countryACode = countryACode;
            this.countryBId = countryBId;
            this.countryBCode = countryBCode;
            this.kmDistance = kmDistance;
        }

        public int getCountryAId() {
            return countryAId;
        }

        public String getCountryACode() {
            return countryACode;
        }

        public int getCountryBId() {
            return countryBId;
        }

        public String getCountryBCode() {
            return countryBCode;
        }

        public int getKMDistance() {
            return kmDistance;
        }
    }

    private Map<Integer, capitalsInfo> processCapDist(String fileContent) {
        Scanner scan = new Scanner(fileContent);

        if (scan.hasNextLine()) {
            scan.nextLine();
        }

        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            String[] fields = line.split(",");

            int countryAId = Integer.parseInt(fields[0]);
            String countryACode = fields[1];
            int countryBId = Integer.parseInt(fields[2]);
            String countryBCode = fields[3];
            int kmDistance = Integer.parseInt(fields[4]);

            capitalsInfo distanceInfo = new capitalsInfo(countryAId, countryACode, countryBId, countryBCode,
                    kmDistance);

            capdistMap.put(countryAId, distanceInfo);
        }
        scan.close();

        return capdistMap;
    }

    public class stateInfo {
        private int statenum;
        private String stateid;
        private String countryname;
        private String start;
        private String end;

        public stateInfo(int statenum, String stateid, String countryname, String start, String end) {
            this.statenum = statenum;
            this.stateid = stateid;
            this.countryname = countryname;
            this.start = start;
            this.end = end;
        }

        public int getStatenum() {
            return statenum;
        }

        public String getStateid() {
            return stateid;
        }

        public String getCountryname() {
            return countryname;
        }

        public String getStart() {
            return start;
        }

        public String getEnd() {
            return end;
        }

    }

    private void processStateName(String fileContent) {
        Scanner scan = new Scanner(fileContent);

        if (scan.hasNextLine()) {
            scan.nextLine();
        }

        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            String[] fields = line.split("\t");

            int statenum = Integer.parseInt(fields[0]);
            String stateid = fields[1];
            String countryname = fields[2];
            String start = fields[3];
            String end = fields[4];

            stateInfo stateInfo = new stateInfo(statenum, stateid, countryname, start, end);

            if (end.equals("2020-12-31")) {
                stateInfoMap.put(stateid, stateInfo);
            }
        }
        scan.close();
    }

    public int getDistance(String country1, String country2) {
        // Replace with your code
        return -1;
    }

    public List<String> findPath(String country1, String country2) {
        // Replace with your code
        return null;
    }

    public void acceptUserInput() {
        // Replace with your code
        System.out.println("IRoadTrip - skeleton");
    }

    public static void main(String[] args) {
        IRoadTrip a3 = new IRoadTrip(args);

        a3.acceptUserInput();
    }

}
