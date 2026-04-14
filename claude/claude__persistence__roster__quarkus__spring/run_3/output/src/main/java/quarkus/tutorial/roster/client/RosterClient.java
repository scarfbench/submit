package quarkus.tutorial.roster.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import quarkus.tutorial.roster.util.LeagueDetails;
import quarkus.tutorial.roster.util.PlayerDetails;
import quarkus.tutorial.roster.util.TeamDetails;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RosterClient {
    private static final String BASE_URI = "http://localhost:8080/roster";
    private final ObjectMapper mapper = new ObjectMapper();

    public RosterClient(String[] args) {
    }

    public static void main(String[] args) {
        RosterClient client = new RosterClient(args);
        try (HttpClient httpClient = HttpClient.newHttpClient()) {
            client.insertInfo(httpClient);
            client.getSomeInfo(httpClient);
            client.getMoreInfo(httpClient);
            client.removeInfo(httpClient);
            System.exit(0);
        } catch (Exception ex) {
            System.err.println("Caught an exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private HttpResponse<String> postJson(HttpClient client, String path, Object body) throws Exception {
        String json = mapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URI + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postForm(HttpClient client, String path, Map<String, String> formData) throws Exception {
        String form = formData.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URI + path))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postEmpty(HttpClient client, String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URI + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String get(HttpClient client, String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URI + path))
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private HttpResponse<String> delete(HttpClient client, String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URI + path))
                .DELETE()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private Map<String, String> playerForm(String id, String name, String position, String salary) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("id", id);
        form.put("name", name);
        form.put("position", position);
        form.put("salary", salary);
        return form;
    }

    private void insertInfo(HttpClient client) {
        try {
            // Leagues
            postJson(client, "/league", new LeagueDetails("L1", "Mountain", "Soccer"));
            postJson(client, "/league", new LeagueDetails("L2", "Valley", "Basketball"));
            postJson(client, "/league", new LeagueDetails("L3", "Foothills", "Soccer"));
            postJson(client, "/league", new LeagueDetails("L4", "Alpine", "Snowboarding"));

            // Teams
            postJson(client, "/team/league/L1", new TeamDetails("T1", "Honey Bees", "Visalia"));
            postJson(client, "/team/league/L1", new TeamDetails("T2", "Gophers", "Manteca"));
            postJson(client, "/team/league/L1", new TeamDetails("T5", "Crows", "Orland"));
            postJson(client, "/team/league/L2", new TeamDetails("T3", "Deer", "Bodie"));
            postJson(client, "/team/league/L2", new TeamDetails("T4", "Trout", "Truckee"));
            postJson(client, "/team/league/L3", new TeamDetails("T6", "Marmots", "Auburn"));
            postJson(client, "/team/league/L3", new TeamDetails("T7", "Bobcats", "Grass Valley"));
            postJson(client, "/team/league/L3", new TeamDetails("T8", "Beavers", "Placerville"));
            postJson(client, "/team/league/L4", new TeamDetails("T9", "Penguins", "Incline Village"));
            postJson(client, "/team/league/L4", new TeamDetails("T10", "Land Otters", "Tahoe City"));

            // Players, Team T1
            postForm(client, "/player", playerForm("P1", "Phil Jones", "goalkeeper", "100.00"));
            postEmpty(client, "/player/P1/team/T1");
            postForm(client, "/player", playerForm("P2", "Alice Smith", "defender", "505.00"));
            postEmpty(client, "/player/P2/team/T1");
            postForm(client, "/player", playerForm("P3", "Bob Roberts", "midfielder", "65.00"));
            postEmpty(client, "/player/P3/team/T1");
            postForm(client, "/player", playerForm("P4", "Grace Phillips", "forward", "100.00"));
            postEmpty(client, "/player/P4/team/T1");
            postForm(client, "/player", playerForm("P5", "Barney Bold", "defender", "100.00"));
            postEmpty(client, "/player/P5/team/T1");

            // Players, Team T2
            postForm(client, "/player", playerForm("P6", "Ian Carlyle", "goalkeeper", "555.00"));
            postEmpty(client, "/player/P6/team/T2");
            postForm(client, "/player", playerForm("P7", "Rebecca Struthers", "midfielder", "777.00"));
            postEmpty(client, "/player/P7/team/T2");
            postForm(client, "/player", playerForm("P8", "Anne Anderson", "forward", "65.00"));
            postEmpty(client, "/player/P8/team/T2");
            postForm(client, "/player", playerForm("P9", "Jan Wesley", "defender", "100.00"));
            postEmpty(client, "/player/P9/team/T2");
            postForm(client, "/player", playerForm("P10", "Terry Smithson", "midfielder", "100.00"));
            postEmpty(client, "/player/P10/team/T2");

            // Players, Team T3
            postForm(client, "/player", playerForm("P11", "Ben Shore", "point guard", "188.00"));
            postEmpty(client, "/player/P11/team/T3");
            postForm(client, "/player", playerForm("P12", "Chris Farley", "shooting guard", "577.00"));
            postEmpty(client, "/player/P12/team/T3");
            postForm(client, "/player", playerForm("P13", "Audrey Brown", "small forward", "995.00"));
            postEmpty(client, "/player/P13/team/T3");
            postForm(client, "/player", playerForm("P14", "Jack Patterson", "power forward", "100.00"));
            postEmpty(client, "/player/P14/team/T3");
            postForm(client, "/player", playerForm("P15", "Candace Lewis", "point guard", "100.00"));
            postEmpty(client, "/player/P15/team/T3");

            // Players, Team T4
            postForm(client, "/player", playerForm("P16", "Linda Berringer", "point guard", "844.00"));
            postEmpty(client, "/player/P16/team/T4");
            postForm(client, "/player", playerForm("P17", "Bertrand Morris", "shooting guard", "452.00"));
            postEmpty(client, "/player/P17/team/T4");
            postForm(client, "/player", playerForm("P18", "Nancy White", "small forward", "833.00"));
            postEmpty(client, "/player/P18/team/T4");
            postForm(client, "/player", playerForm("P19", "Billy Black", "power forward", "444.00"));
            postEmpty(client, "/player/P19/team/T4");
            postForm(client, "/player", playerForm("P20", "Jodie James", "point guard", "100.00"));
            postEmpty(client, "/player/P20/team/T4");

            // Players, Team T5
            postForm(client, "/player", playerForm("P21", "Henry Shute", "goalkeeper", "205.00"));
            postEmpty(client, "/player/P21/team/T5");
            postForm(client, "/player", playerForm("P22", "Janice Walker", "defender", "857.00"));
            postEmpty(client, "/player/P22/team/T5");
            postForm(client, "/player", playerForm("P23", "Wally Hendricks", "midfielder", "748.00"));
            postEmpty(client, "/player/P23/team/T5");
            postForm(client, "/player", playerForm("P24", "Gloria Garber", "forward", "777.00"));
            postEmpty(client, "/player/P24/team/T5");
            postForm(client, "/player", playerForm("P25", "Frank Fletcher", "defender", "399.00"));
            postEmpty(client, "/player/P25/team/T5");

            // Players, Team T9
            postForm(client, "/player", playerForm("P30", "Lakshme Singh", "downhill", "450.00"));
            postEmpty(client, "/player/P30/team/T9");
            postForm(client, "/player", playerForm("P31", "Mariela Prieto", "freestyle", "420.00"));
            postEmpty(client, "/player/P31/team/T9");

            // Players, Team T10
            postForm(client, "/player", playerForm("P32", "Soren Johannsen", "freestyle", "375.00"));
            postEmpty(client, "/player/P32/team/T10");
            postForm(client, "/player", playerForm("P33", "Andre Gerson", "freestyle", "396.00"));
            postEmpty(client, "/player/P33/team/T10");
            postForm(client, "/player", playerForm("P34", "Zoria Lepsius", "downhill", "431.00"));
            postEmpty(client, "/player/P34/team/T10");

            // Players, no team
            postForm(client, "/player", playerForm("P26", "Hobie Jackson", "pitcher", "582.00"));
            postForm(client, "/player", playerForm("P27", "Melinda Kendall", "catcher", "677.00"));

            // Players, multiple teams
            postForm(client, "/player", playerForm("P28", "Constance Adams", "substitute", "966.00"));
            postEmpty(client, "/player/P28/team/T1");
            postEmpty(client, "/player/P28/team/T3");

            // Adding existing players to second soccer league
            postEmpty(client, "/player/P24/team/T6");
            postEmpty(client, "/player/P21/team/T6");
            postEmpty(client, "/player/P9/team/T6");
            postEmpty(client, "/player/P7/team/T5");
        } catch (Exception ex) {
            System.err.println("Caught an exception: " + ex.getMessage());
        }
    }

    private void getSomeInfo(HttpClient client) {
        try {
            System.out.println("List all players in team T2:");
            String json = get(client, "/team/T2/players");
            List<PlayerDetails> playerList = mapper.readValue(json, new TypeReference<List<PlayerDetails>>() {});
            printDetailsList(playerList);
            System.out.println();

            System.out.println("List all teams in league L1:");
            json = get(client, "/league/L1/teams");
            List<TeamDetails> teamList = mapper.readValue(json, new TypeReference<List<TeamDetails>>() {});
            printDetailsList(teamList);
            System.out.println();

            System.out.println("List all defenders:");
            json = get(client, "/players/position/defender");
            playerList = mapper.readValue(json, new TypeReference<List<PlayerDetails>>() {});
            printDetailsList(playerList);
            System.out.println();

            System.out.println("List the leagues of player P28:");
            json = get(client, "/player/P28/leagues");
            List<LeagueDetails> leagueList = mapper.readValue(json, new TypeReference<List<LeagueDetails>>() {});
            printDetailsList(leagueList);
            System.out.println();
        } catch (Exception ex) {
            System.err.println("Caught an exception: " + ex.getMessage());
        }
    }

    private void getMoreInfo(HttpClient client) {
        try {
            System.out.println("Details of league L1:");
            String json = get(client, "/league/L1");
            LeagueDetails leagueDetails = mapper.readValue(json, LeagueDetails.class);
            System.out.println(leagueDetails.toString());
            System.out.println();

            System.out.println("Details of team T3:");
            json = get(client, "/team/T3");
            TeamDetails teamDetails = mapper.readValue(json, TeamDetails.class);
            System.out.println(teamDetails.toString());
            System.out.println();

            System.out.println("Details of player P20:");
            json = get(client, "/player/P20");
            PlayerDetails playerDetails = mapper.readValue(json, PlayerDetails.class);
            System.out.println(playerDetails.toString());
            System.out.println();

            System.out.println("List all teams in league L3:");
            json = get(client, "/league/L3/teams");
            List<TeamDetails> teamList = mapper.readValue(json, new TypeReference<List<TeamDetails>>() {});
            printDetailsList(teamList);
            System.out.println();

            System.out.println("List all players:");
            json = get(client, "/players");
            List<PlayerDetails> playerList = mapper.readValue(json, new TypeReference<List<PlayerDetails>>() {});
            printDetailsList(playerList);
            System.out.println();

            System.out.println("List all players not on a team:");
            json = get(client, "/players/not-on-team");
            playerList = mapper.readValue(json, new TypeReference<List<PlayerDetails>>() {});
            printDetailsList(playerList);
            System.out.println();

            System.out.println("Details of Jack Patterson, a power forward:");
            json = get(client, "/players/position/power forward/name/Jack Patterson");
            playerList = mapper.readValue(json, new TypeReference<List<PlayerDetails>>() {});
            printDetailsList(playerList);
            System.out.println();

            System.out.println("List all players in the city of Truckee:");
            json = get(client, "/players/city/Truckee");
            playerList = mapper.readValue(json, new TypeReference<List<PlayerDetails>>() {});
            printDetailsList(playerList);
            System.out.println();

            System.out.println("List all soccer players:");
            json = get(client, "/players/sport/Soccer");
            playerList = mapper.readValue(json, new TypeReference<List<PlayerDetails>>() {});
            printDetailsList(playerList);
            System.out.println();

            System.out.println("List all players in league L1:");
            json = get(client, "/players/league/L1");
            playerList = mapper.readValue(json, new TypeReference<List<PlayerDetails>>() {});
            printDetailsList(playerList);
            System.out.println();

            System.out.println("List all players making a higher salary than Ian Carlyle:");
            json = get(client, "/players/salary/higher/Ian Carlyle");
            playerList = mapper.readValue(json, new TypeReference<List<PlayerDetails>>() {});
            printDetailsList(playerList);
            System.out.println();

            System.out.println("List all players with a salary between 500 and 800:");
            json = get(client, "/players/salary/range?low=500.00&high=800.00");
            playerList = mapper.readValue(json, new TypeReference<List<PlayerDetails>>() {});
            printDetailsList(playerList);
            System.out.println();

            System.out.println("List all players of team T5:");
            json = get(client, "/team/T5/players");
            playerList = mapper.readValue(json, new TypeReference<List<PlayerDetails>>() {});
            printDetailsList(playerList);
            System.out.println();

            System.out.println("List all the leagues of player P28:");
            json = get(client, "/player/P28/leagues");
            List<LeagueDetails> leagueList = mapper.readValue(json, new TypeReference<List<LeagueDetails>>() {});
            printDetailsList(leagueList);
            System.out.println();

            System.out.println("List all the sports of player P28:");
            json = get(client, "/player/P28/sports");
            List<String> sportList = mapper.readValue(json, new TypeReference<List<String>>() {});
            printDetailsList(sportList);
            System.out.println();
        } catch (Exception ex) {
            System.err.println("Caught an exception: " + ex.getMessage());
        }
    }

    private void removeInfo(HttpClient client) {
        try {
            System.out.println("Removing team T6.");
            delete(client, "/team/T6");
            System.out.println();

            System.out.println("Removing player P24");
            delete(client, "/player/P24");
            System.out.println();
        } catch (Exception ex) {
            System.err.println("Caught an exception: " + ex.getMessage());
        }
    }

    private static void printDetailsList(List<?> list) {
        for (Object details : list) {
            System.out.println(details.toString());
        }
        System.out.println();
    }
}
