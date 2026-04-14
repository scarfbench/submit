package spring.tutorial.roster.rest;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import spring.tutorial.roster.request.Request;
import spring.tutorial.roster.util.LeagueDetails;
import spring.tutorial.roster.util.PlayerDetails;
import spring.tutorial.roster.util.TeamDetails;

@Path("/roster")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RosterResource {

    @Inject
    Request request;

    // ---- League ----

    @POST
    @Path("/league")
    public Response createLeague(LeagueDetails details) {
        try {
            request.createLeague(details);
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("/league/{id}")
    public Response getLeague(@PathParam("id") String id) {
        LeagueDetails d = request.getLeague(id);
        return d != null ? Response.ok(d).build() : Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("/league/{id}")
    public Response removeLeague(@PathParam("id") String id) {
        request.removeLeague(id);
        return Response.ok().build();
    }

    @GET
    @Path("/league/{id}/teams")
    public List<TeamDetails> getTeamsOfLeague(@PathParam("id") String id) {
        return request.getTeamsOfLeague(id);
    }

    // ---- Team ----

    @POST
    @Path("/team/league/{leagueId}")
    public Response createTeamInLeague(TeamDetails details,
                                       @PathParam("leagueId") String leagueId) {
        request.createTeamInLeague(details, leagueId);
        return Response.ok().build();
    }

    @GET
    @Path("/team/{id}")
    public Response getTeam(@PathParam("id") String id) {
        TeamDetails d = request.getTeam(id);
        return d != null ? Response.ok(d).build() : Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("/team/{id}")
    public Response removeTeam(@PathParam("id") String id) {
        request.removeTeam(id);
        return Response.ok().build();
    }

    @GET
    @Path("/team/{id}/players")
    public List<PlayerDetails> getPlayersOfTeam(@PathParam("id") String id) {
        return request.getPlayersOfTeam(id);
    }

    // ---- Player ----

    @POST
    @Path("/player")
    public Response createPlayer(@QueryParam("id") String id,
                                  @QueryParam("name") String name,
                                  @QueryParam("position") String position,
                                  @QueryParam("salary") double salary) {
        request.createPlayer(id, name, position, salary);
        return Response.ok().build();
    }

    @GET
    @Path("/player/{id}")
    public Response getPlayer(@PathParam("id") String id) {
        PlayerDetails d = request.getPlayer(id);
        return d != null ? Response.ok(d).build() : Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("/player/{id}")
    public Response removePlayer(@PathParam("id") String id) {
        request.removePlayer(id);
        return Response.ok().build();
    }

    @POST
    @Path("/player/{pid}/team/{tid}")
    public Response addPlayerToTeam(@PathParam("pid") String pid,
                                     @PathParam("tid") String tid) {
        request.addPlayer(pid, tid);
        return Response.ok().build();
    }

    @DELETE
    @Path("/player/{pid}/team/{tid}")
    public Response dropPlayerFromTeam(@PathParam("pid") String pid,
                                        @PathParam("tid") String tid) {
        request.dropPlayer(pid, tid);
        return Response.ok().build();
    }

    @GET
    @Path("/player/{id}/leagues")
    public List<LeagueDetails> getLeaguesOfPlayer(@PathParam("id") String id) {
        return request.getLeaguesOfPlayer(id);
    }

    @GET
    @Path("/player/{id}/sports")
    public List<String> getSportsOfPlayer(@PathParam("id") String id) {
        return request.getSportsOfPlayer(id);
    }

    // ---- Queries ----

    @GET
    @Path("/players")
    public List<PlayerDetails> getAllPlayers() {
        return request.getAllPlayers();
    }

    @GET
    @Path("/players/position/{pos}")
    public List<PlayerDetails> getPlayersByPosition(@PathParam("pos") String pos) {
        return request.getPlayersByPosition(pos);
    }

    @GET
    @Path("/players/salary/higher/{name}")
    public List<PlayerDetails> getPlayersByHigherSalary(@PathParam("name") String name) {
        return request.getPlayersByHigherSalary(name);
    }

    @GET
    @Path("/players/salary/range")
    public List<PlayerDetails> getPlayersBySalaryRange(@QueryParam("low") double low,
                                                        @QueryParam("high") double high) {
        return request.getPlayersBySalaryRange(low, high);
    }

    @GET
    @Path("/players/league/{id}")
    public List<PlayerDetails> getPlayersByLeagueId(@PathParam("id") String id) {
        return request.getPlayersByLeagueId(id);
    }

    @GET
    @Path("/players/sport/{sport}")
    public List<PlayerDetails> getPlayersBySport(@PathParam("sport") String sport) {
        return request.getPlayersBySport(sport);
    }

    @GET
    @Path("/players/city/{city}")
    public List<PlayerDetails> getPlayersByCity(@PathParam("city") String city) {
        return request.getPlayersByCity(city);
    }

    @GET
    @Path("/players/not-on-team")
    public List<PlayerDetails> getPlayersNotOnTeam() {
        return request.getPlayersNotOnTeam();
    }

    @GET
    @Path("/players/position/{pos}/name/{name}")
    public List<PlayerDetails> getPlayersByPositionAndName(@PathParam("pos") String pos,
                                                            @PathParam("name") String name) {
        return request.getPlayersByPositionAndName(pos, name);
    }
}
