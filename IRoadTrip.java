
// imports
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class IRoadTrip {
    // used data structures
    private HashMap<String, List<String>> countryBorders = new HashMap<>();
    private HashMap<String, Integer> capdistMap = new HashMap<>();
    private HashMap<String, stateInfo> stateInfoMap = new HashMap<>();
    private HashMap<String, String> countryId = new HashMap<>();
    private HashMap<String, HashMap<String, Integer>> graph = new HashMap<>();
    private Map<String, Integer> distances = new HashMap<>();

    /**
     * read country borders, capital distances, and state information files
     * create graph representation of country borders and capdist
     *
     * @param args three file paths representing country borders, capital distances,
     *             and state information
     */
    public IRoadTrip(String[] args) {
        try {
            // read files and build the graph
            String bordersFile = readFile(args[0]);
            handleBorders(countryBorders, bordersFile);

            String capdistFile = readFile(args[1]);
            processCapDist(capdistFile);

            String stateNameFile = readFile(args[2]);
            processStateName(stateNameFile);

            buildGraph();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * feads the content of a file given file path.
     *
     * @param filePath path of the file to be read
     * @return string containing the content of the file.
     * @throws IOException if an I/O error occurs while parsing the file
     */
    private String readFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readString(path);
    }

    /**
     * parses the border information creates a list of neighboring countries
     *
     * @param borderInfo string containing border information for a country
     * @return list of neighboring countries
     */
    private List<String> addBorders(String borderInfo) {
        List<String> neighboringCountries = new ArrayList<>();

        String[] borders = borderInfo.split(";");

        for (String border : borders) {
            String[] parts = border.trim().split("\\s+");
            if (parts.length > 0) {
                neighboringCountries.add(parts[0]);
            }
        }

        return neighboringCountries;
    }

    /**
     * reads and processes the border information
     * populates countryBorders map with country names and their neighboring
     * countries
     *
     * @param countryBorders map to store the country names and neighboring
     *                       countries
     * @param fileContent    content of the file containing border information
     */
    private void handleBorders(HashMap<String, List<String>> countryBorders, String fileContent) {
        Scanner scan = new Scanner(fileContent);

        while (scan.hasNextLine()) {
            String line = scan.nextLine();

            // remove "km" and numbers
            line = line.replaceAll("\\b[\\d,\\.]+\\s*km\\b", "");
            line = removeParentheses(line);

            String[] parts = line.split("=");

            if (parts.length >= 2) {
                String countryName = parts[0].trim();
                String borderInfo = parts[1].trim();

                // extract only the country name
                List<String> countryBounds = addBorders(borderInfo)
                        .stream()
                        .map(neighbor -> neighbor.split("\\s+")[0])
                        .collect(Collectors.toList());

                countryBorders.put(countryName, countryBounds);
            }
        }

        scan.close();
    }

    /**
     * removes content enclosed within parentheses from the input string
     *
     * @param input input string with content in the parentheses to be removed
     * @return input string with content removed
     */
    private static String removeParentheses(String input) {
        // remove anything between parentheses
        String regex = "\\([^)]*\\)";
        Pattern parens = Pattern.compile(regex);
        Matcher matcher = parens.matcher(input);
        return matcher.replaceAll("");
    }

    public static class capitalsInfo {
        private int countryAId;
        private String countryACode;
        private int countryBId;
        private String countryBCode;
        private int kmDistance;

        /**
         * constructs an instance of the capitalsInfo class
         *
         * @param countryAId   identifier of the first country
         * @param countryACode code of the first country
         * @param countryBId   identifier of the second country
         * @param countryBCode code of the second country
         * @param kmDistance   distance between the capitals of the two countries in
         *                     kilometers
         */
        public capitalsInfo(int countryAId, String countryACode, int countryBId, String countryBCode, int kmDistance) {
            this.countryAId = countryAId;
            this.countryACode = countryACode;
            this.countryBId = countryBId;
            this.countryBCode = countryBCode;
            this.kmDistance = kmDistance;
        }
    }

    /**
     * processes the content the capdist file
     *
     * @param fileContent content of the file containing capital distances
     */
    private void processCapDist(String fileContent) {
        Scanner scan = new Scanner(fileContent);

        if (scan.hasNextLine()) {
            scan.nextLine();
        }

        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            String[] fields = line.split(",");

            String countryAID = fields[1];
            String countryBID = fields[3];
            int kmDistance = Integer.parseInt(fields[4]);
            String combCode = countryAID + countryBID;

            capdistMap.put(combCode, kmDistance);
        }
        scan.close();
    }

    public class stateInfo {
        private int stateNum;
        private String stateID;
        private String countryName;
        private String start;
        private String end;

        /**
         * constructs a stateInfo object
         *
         * @param stateNum    state number
         * @param stateID     state identifier
         * @param countryName associated country name
         * @param start       start date
         * @param end         end date
         */
        public stateInfo(int stateNum, String stateID, String countryName, String start, String end) {
            this.stateNum = stateNum;
            this.stateID = stateID;
            this.countryName = countryName;
            this.start = start;
            this.end = end;
        }
    }

    /**
     * processes the content of the state name file
     * 
     * @param fileContent content of the state name file to be processed
     */
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
            String countryName = fields[2];
            countryName = removeParentheses(countryName).trim();
            String start = fields[3];
            String end = fields[4];

            stateInfo stateInfo = new stateInfo(statenum, stateid, countryName, start, end);

            if (end.equals("2020-12-31")) {
                stateInfoMap.put(stateid, stateInfo);
            }

            // add country ID to the map
            countryId.put(countryName, stateid);
        }
        scan.close();
    }

    /**
     * finds and returns the country ID associated with the country name
     *
     * @param countryName name of the country to find the ID for
     * @return country ID or null if the country name is not found in the map
     */
    private String findCountryId(String countryName) {
        for (Map.Entry<String, String> entry : countryId.entrySet()) {
            if (entry.getKey().trim().equalsIgnoreCase(countryName.trim())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * builds the graph representing the countries, their borders, and capdist
     */
    private void buildGraph() {
        graph = new HashMap<>();
        distances = new HashMap<>();

        // iterate through countryBorders and capdistMap to build the graph
        for (Map.Entry<String, List<String>> entry : countryBorders.entrySet()) {
            String sourceCountry = entry.getKey();
            graph.put(sourceCountry, new HashMap<>());
            distances.put(sourceCountry, Integer.MAX_VALUE);

            List<String> neighboringCountries = entry.getValue();

            // create the inner HashMap for neighboring countries and distances
            HashMap<String, Integer> neighbors = new HashMap<>();

            // add neighboring countries based on capdistMap
            for (String neighbor : neighboringCountries) {
                String countryAId = findCountryId(sourceCountry);
                String countryBId = findCountryId(neighbor);

                // check countryAId and countryBId are not null
                if (countryAId != null && countryBId != null) {
                    String combCodeAB = countryAId + countryBId;
                    String combCodeBA = countryBId + countryAId;

                    Integer kmDistanceAB = capdistMap.getOrDefault(combCodeAB, -1);
                    Integer kmDistanceBA = capdistMap.getOrDefault(combCodeBA, -1);

                    if (kmDistanceAB >= 0) {
                        neighbors.put(neighbor, kmDistanceAB);
                    } else if (kmDistanceBA >= 0) {
                        neighbors.put(neighbor, kmDistanceBA);
                    }
                }
            }

            // put neighbors into the graph
            graph.put(sourceCountry, neighbors);
        }

        // fill edge cases
        HashMap<String, Integer> inner = graph.get("Mali");
        inner.put("Burkina Faso", 706);
        inner.put("Cote d'Ivoire", 704);

        inner = graph.get("Panama");
        inner.put("Costa Rica", 523);

        inner = graph.get("Laos");
        inner.put("Burma", 701);

        inner = graph.get("Zambia");
        inner.put("Congo, Democratic Republic of the", 1893);
        inner.put("Tanzania ", 1543);

        inner = graph.get("Namibia");
        inner.put("South Africa", 1187);

        inner = graph.get("Yemen");
        inner.put("Saudi Arabia", 1040);

        inner = graph.get("Malawi");
        inner.put("Tanzania ", 996);

        inner = graph.get("Bulgaria");
        inner.put("Macedonia ", 151);

        inner = graph.get("Jordan");
        inner.put("Saudi Arabia", 1323);

        inner = graph.get("Gambia, The");
        inner.put("Senegal", 144);

        inner = graph.get("United Arab Emirates");
        inner.put("Saudi Arabia", 778);

        inner = graph.get("Kenya");
        inner.put("Tanzania", 1120);

        inner = graph.get("Cameroon");
        inner.put("Central African Republic", 802);
        inner.put("Congo, Democratic Republic of the", 898);
        inner.put("Equatorial Guinea", 205);

        inner = graph.get("Oman");
        inner.put("Saudi Arabia", 1194);
        inner.put("United Arab Emirates", 422);

        inner = graph.get("Gabon");
        inner.put("Congo, Democratic Republic of the", 790);
        inner.put("Equatorial Guinea", 225);

        inner = graph.get("Brazil");
        inner.put("Suriname", 2467);

        inner = graph.get("Honduras");
        inner.put("El Salvador", 201);

        inner = graph.get("Italy");
        inner.put("Austria", 16178);
        inner.put("France", 1127);
        inner.put("Slovenia", 566);
        inner.put("Switzerland", 703);

        inner = graph.get("Korea, North");
        inner.put("China", 820);
        inner.put("Korea, South", 238);
        inner.put("Russia", 6443);

        inner = graph.get("Haiti");
        inner.put("Dominican Republic", 246);

        inner = graph.get("Burundi");
        inner.put("Congo, Democratic Republic of the", 1557);
        inner.put("Tanzania", 1158);

        inner = graph.get("Russia");
        inner.put("Korea, North", 6443);

        inner = graph.get("China");
        inner.put("Burma", 3222);
        inner.put("Korea, North", 820);

        inner = graph.get("Kyrgyzstan");
        inner.put("China", 3497);
        inner.put("Tajikistan", 668);
        inner.put("Kazakhstan", 230);
        inner.put("Uzbekistan", 458);

        inner = graph.get("Togo");
        inner.put("Burkina Faso", 730);

        inner = graph.get("Cote d'Ivoire");
        inner.put("Burkina Faso", 784);
        inner.put("Guinea", 975);
        inner.put("Mali", 726);
        inner.put("Ghana", 619);

        inner = graph.get("Uzbekistan");
        inner.put("Kyrgyzstan", 458);

        inner = graph.get("Zimbabwe");
        inner.put("South Africa", 929);

        inner = graph.get("Montenegro");
        inner.put("Bosnia and Herzegovina", 96);

        inner = graph.get("Indonesia");
        inner.put("Papua New Guinea", 4472);
        inner.put("Timor-Leste", 2085);

        inner = graph.get("Benin");
        inner.put("Burkina Faso", 807);

        inner = graph.get("Angola");
        inner.put("Congo, Democratic Republic of the", 510);
        inner.put("Congo, Republic of the", 510);

        inner = graph.get("Sudan");
        inner.put("Central African Republic", 1961);

        inner = graph.get("Greece");
        inner.put("North Macedonia", 2669);

        inner = graph.get("Iraq");
        inner.put("Saudi Arabia", 1026);

        inner = graph.get("Tanzania");
        inner.put("Burundi", 1158);
        inner.put("Congo, Democratic Republic of the", 2666);
        inner.put("Kenya", 626);
        inner.put("Malawi", 996);
        inner.put("Mozambique", 2338);
        inner.put("Rwanda", 1157);
        inner.put("Uganda", 1018);
        inner.put("Zambia", 1543);

        inner = graph.get("Ghana");
        inner.put("Burkina Faso", 797);
        inner.put("Cote d'Ivoire", 619);

        inner = graph.get("India");
        inner.put("Burma", 2347);

        inner = graph.get("Canada");
        inner.put("United States", 731);

        inner = graph.get("Central African Republic");
        inner.put("Congo, Democratic Republic of the", 970);
        inner.put("Congo, Republic of the", 970);

        inner = graph.get("Guinea");
        inner.put("Cote d'Ivoire", 975);
        inner.put("Sierra Leone", 117);

        inner = graph.get("United States");
        inner.put("Canada", 731);
        inner.put("Mexico", 3024);

        inner = graph.get("Chad");
        inner.put("Central African Republic", 974);

        inner = graph.get("Thailand");
        inner.put("Burma", 573);

        inner = graph.get("United Kingdom");
        inner.put("Ireland", 496);

        inner = graph.get("Liberia");
        inner.put("Cote d'Ivoire", 608);
        inner.put("Sierra Leone", 335);

        inner = graph.get("Burkina Faso");
        inner.put("Cote d'Ivoire", 784);

        inner = graph.get("Austria");
        inner.put("Czechia", 259);

        inner = graph.get("Mozambique");
        inner.put("South Africa", 433);
        inner.put("Eswatini", 133);
        inner.put("Tanzania", 2338);

        inner = graph.get("Lesotho");
        inner.put("South Africa", 454);

        inner = graph.get("Congo, Republic of the");
        inner.put("Central African Republic", 970);
        inner.put("Angola", 510);
        inner.put("Cameroon", 898);
        inner.put("Congo, Republic of the", 0);
        inner.put("Gabon", 790);

        inner = graph.get("Korea, South");
        inner.put("Korea, North", 238);

        inner = graph.get("Czechia");
        inner.put("Germany", 523);
        inner.put("Austria", 259);
        inner.put("Slovakia", 295);
        inner.put("Poland", 517);

        inner = graph.get("Saudi Arabia");
        inner.put("United Arab Emirates", 778);

        inner = graph.get("Ireland");
        inner.put("United Kingdom", 496);

        inner = graph.get("Qatar");
        inner.put("Saudi Arabia", 474);

        inner = graph.get("Slovakia");
        inner.put("Czechia", 295);

        inner = graph.get("Bosnia and Herzegovina");
        inner.put("Croatia", 295);
        inner.put("Serbia", 196);
        inner.put("Montenegro", 96);

        inner = graph.get("Niger");
        inner.put("Burkina Faso", 413);

        inner = graph.get("Rwanda");
        inner.put("Congo, Democratic Republic of the", 1678);
        inner.put("Tanzania", 1157);

        inner = graph.get("Burma");
        inner.put("China", 3222);
        inner.put("India", 2347);
        inner.put("Lao", 701);
        inner.put("Thailand", 573);
        inner.put("Bangladesh", 978);

        inner = graph.get("Bangladesh");
        inner.put("Burma", 978);

        inner = graph.get("Nicaragua");
        inner.put("Costa Rica", 337);

        inner = graph.get("Botswana");
        inner.put("South Africa", 261);

        inner = graph.get("Mexico");
        inner.put("United States", 3024);

        inner = graph.get("Uganda");
        inner.put("Congo, Democratic Republic of the", 1959);
        inner.put("Tanzania", 1018);

        inner = graph.get("Suriname");
        inner.put("Guyana", 341);
        inner.put("Brazil", 2467);

        inner = graph.get("Kyrgyzstan");
        inner.put("Kazakhstan", 230);
        inner.put("Tajikistan", 668);
        inner.put("Uzbekistan", 458);
        inner.put("China", 3497);

    }

    /**
     * finds and returns the distance of the shortest path between two countries
     *
     * @param startCountry name of the starting country
     * @param endCountry   name of the destination country
     * @return distance of the shortest path between the start and end countries
     *         returns -1 if the countries do not share borders
     */
    private int findShortestPathDistance(String startCountry, String endCountry) {
        // implement dijkstras
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(node -> node.distance));
        Map<String, Integer> distances = new HashMap<>();

        for (String country : graph.keySet()) {
            distances.put(country, Integer.MAX_VALUE);
        }

        distances.put(startCountry, 0);
        priorityQueue.add(new Node(startCountry, 0));

        while (!priorityQueue.isEmpty()) {
            Node current = priorityQueue.poll();

            if (current.country.equals(endCountry)) {
                Integer distance = distances.get(endCountry);
                return distance != null ? distance : -1;
            }

            if (current.distance > distances.get(current.country)) {
                continue;
            }

            for (Map.Entry<String, Integer> neighbor : graph.get(current.country).entrySet()) {
                int newDistance = current.distance + neighbor.getValue();

                if (newDistance < distances.get(neighbor.getKey())) {
                    distances.put(neighbor.getKey(), newDistance);
                    priorityQueue.add(new Node(neighbor.getKey(), newDistance));
                }
            }
        }

        return -1;
    }

    /**
     * finds and returns the shortest path between two countries
     *
     * @param startCountry name of the starting country
     * @param endCountry   name of the destination country
     * @return list of country names representing the shortest path between the
     *         start and end countries
     *         empty list if no valid path is found
     */
    private List<String> findShortestPath(String startCountry, String endCountry) {
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(node -> node.distance));
        distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();

        for (String country : graph.keySet()) {
            distances.put(country, Integer.MAX_VALUE);
        }

        distances.put(startCountry, 0);
        priorityQueue.add(new Node(startCountry, 0));

        while (!priorityQueue.isEmpty()) {
            Node current = priorityQueue.poll();

            if (current.country.equals(endCountry)) {
                int distance = findShortestPathDistance(startCountry, endCountry);
                if (distance != -1) {
                    // reconstruct the path
                    List<String> path = reconstructPath(startCountry, endCountry, previous);

                    // print the shortest path
                    printShortestPath(startCountry, endCountry, previous);

                    return path;
                } else {
                    System.out.println("No valid path found between " + startCountry + " and " + endCountry);
                    return Collections.emptyList();
                }
            }

            if (current.distance > distances.get(current.country)) {
                continue;
            }

            for (Map.Entry<String, Integer> neighbor : graph.get(current.country).entrySet()) {
                int newDistance = current.distance + neighbor.getValue();

                if (newDistance < distances.get(neighbor.getKey())) {
                    distances.put(neighbor.getKey(), newDistance);
                    priorityQueue.add(new Node(neighbor.getKey(), newDistance));

                    previous.put(neighbor.getKey(), current.country);
                }
            }
        }

        // no path is found, return an empty list
        return Collections.emptyList();
    }

    /**
     * reconstructs and returns the path from start to end
     *
     * @param startCountry name of the starting country
     * @param endCountry   name of the destination country
     * @param previous     map containing the previous country for each country in
     *                     the path
     * @return list of country names representing the path
     */
    private List<String> reconstructPath(String startCountry, String endCountry, Map<String, String> previous) {
        List<String> path = new ArrayList<>();
        String current = endCountry;

        while (current != null) {
            path.add(current);
            current = previous.get(current);
        }

        Collections.reverse(path);
        return path;
    }

    private static class Node {
        private String country;
        private int distance;

        /**
         * constructs a Node with the country name and distance
         *
         * @param country  name of the country represented
         * @param distance distance from the start country to country
         */
        public Node(String country, int distance) {
            this.country = country;
            this.distance = distance;
        }
    }

    /**
     * prints the shortest path and details from the start country to end country
     *
     * @param startCountry name of the starting country
     * @param endCountry   name of the destination country
     * @param previous     map containing the previous country for each country in
     *                     the path
     */
    private void printShortestPath(String startCountry, String endCountry, Map<String, String> previous) {
        System.out.println("Route from " + startCountry + " to " + endCountry + ":");

        List<String> path = new ArrayList<>();
        String current = endCountry;

        while (current != null) {
            path.add(current);
            current = previous.get(current);
        }

        // print the path in reverse order
        for (int i = path.size() - 1; i > 0; i--) {
            String country = path.get(i);
            String nextCountry = path.get(i - 1);
            int distance = distances.get(nextCountry) - distances.get(country);

            System.out.println("* " + country + " --> " + nextCountry + " (" + distance + " km.)");
        }
    }

    /**
     * calculates and returns the shortest path distance between two countries
     *
     * @param country1 name of the first country
     * @param country2 name of the second country
     * @return shortest path distance between the two countries, or -1
     */
    public int getDistance(String country1, String country2) {
        return findShortestPathDistance(country1, country2);
    }

    /**
     * finds and returns the shortest path between two countries
     *
     * @param startCountry name of the starting country
     * @param endCountry   name of the destination country
     * @return list of country names representing the shortest path between the
     *         start and end countries
     *         empty list if no valid path is found
     */
    public List<String> findPath(String country1, String country2) {
        return findShortestPath(country1, country2);
    }

    /**
     * checks and standardizes the name of a country. Country name
     * corresponds to borders
     *
     * @param country input country name to be checked and standardized
     * @return name of the country, or the original name
     */
    String checkCountry(String country) {
        if (country.equals("United States of America")) {
            return "United States";
        } else if (country.equals("The Bahamas") || country.equals("Bahamas")) {
            return "Bahamas, The";
        } else if (country.equals("Surinam")) {
            return "Suriname";
        } else if (country.equals("German Federal Republic")) {
            return "Germany";
        } else if (country.equals("Czech Republic")) {
            return "Czechia";
        } else if (country.equals("Sardinia")) {
            return "Italy";
        } else if (country.equals("Macedonia") || country.equals("Former Yugoslav Republic of Macedonia")) {
            return "North Macedonia";
        } else if (country.equals("Bosnia-Herzegovina")) {
            return "Bosnia and Herzegovina";
        } else if (country.equals("Rumania")) {
            return "Romania";
        } else if (country.equals("Soviet Union")) {
            return "Russia";
        } else if (country.equals("Byelorussia")) {
            return "Belarus";
        } else if (country.equals("Cote D’Ivoire")) {
            return "Cote d'Ivoire";
        } else if (country.equals("Upper Volta")) {
            return "Burkina Faso";
        } else if (country.equals("Congo") || country.equals("Republic of the Congo")) {
            return "Congo, Republic of the";
        } else if (country.equals("Democratic Republic of Zaire") || country.equals("Zaire")
                || country.equals("Democratic Republic of the Congo")) {
            return "Congo, Democratic Republic of the";
        } else if (country.equals("Tanganyika")) {
            return "Tanzania";
        } else if (country.equals("Rhodesia")) {
            return "Zimbabwe";
        } else if (country.equals("Swaziland")) {
            return "Eswatini";
        } else if (country.equals("Persia")) {
            return "Iran";
        } else if (country.equals("Ottoman Empire") || country.equals("Turkiye")) {
            return "Turkey";
        } else if (country.equals("Arab Republic of Yemen")) {
            return "Yemen";
        } else if (country.equals("People's Republic of Korea") || country.equals("North Korea")) {
            return "Korea, North";
        } else if (country.equals("Republic of Korea") || country.equals("South Korea")) {
            return "Korea, South";
        } else if (country.equals("Myanmar ")) {
            return "Burma";
        } else if (country.equals("Ceylon")) {
            return "Sri Lanka";
        } else if (country.equals("Kampuchea")) {
            return "Cambodia";
        } else if (country.equals("Democratic Republic of Vietnam")) {
            return "Vietnam";
        } else if (country.equals("East Timor")) {
            return "Timor-Leste";
        }
        return country;
    }

    /**
     * accepts user input for the names of two countries and calculates the distance
     * checks if the entered countries are valid and share borders
     */
    public void acceptUserInput() {
        Scanner scan = new Scanner(System.in);

        while (true) {
            System.out.print("Enter the name of the first country (type EXIT to quit): ");
            String startCountry = scan.nextLine().trim();
            startCountry = checkCountry(startCountry);

            if (startCountry.equalsIgnoreCase("EXIT")) {
                break;
            }

            if (!countryBorders.containsKey(startCountry)) {
                System.out.println("Invalid country name. Please enter a valid country name.");
                continue;
            }

            System.out.print("Enter the name of the second country (type EXIT to quit: ");
            String endCountry = scan.nextLine().trim();
            endCountry = checkCountry(endCountry);

            if (endCountry.equalsIgnoreCase("EXIT")) {
                break;
            }

            if (!countryBorders.containsKey(endCountry)) {
                System.out.println("Invalid country name. Please enter a valid country name.");
                continue;
            }

            // checks for islands
            HashMap<String, Integer> checkIsland1 = graph.get(startCountry);
            HashMap<String, Integer> checkIsland2 = graph.get(endCountry);
            if ((checkIsland1 == null || checkIsland1.isEmpty()) || (checkIsland2 == null || checkIsland2.isEmpty())) {
                System.out.println("Countries do not share borders.");
                continue;
            }

            // check if the countries share borders
            int distance = getDistance(startCountry, endCountry);
            if (distance == -1) {
                System.out.println("Countries do not share borders.");
                continue;
            }

            List<String> path = findShortestPath(startCountry, endCountry);

            // check if a path is found
            if (!path.isEmpty()) {
            } else {
                System.out.println("No valid path found between " + startCountry + " and " + endCountry);
            }
        }

        scan.close();
    }

    public static void main(String[] args) {
        IRoadTrip a3 = new IRoadTrip(args);

        a3.acceptUserInput();
    }
}
