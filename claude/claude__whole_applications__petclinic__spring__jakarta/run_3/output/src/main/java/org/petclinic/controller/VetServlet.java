package org.petclinic.controller;

import java.io.IOException;
import java.util.List;

import org.petclinic.model.Vet;
import org.petclinic.repository.VetRepository;
import org.thymeleaf.context.WebContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet({"/vets.html", "/vets"})
public class VetServlet extends BaseServlet {

    @Inject
    private VetRepository vetRepository;

    private static final int PAGE_SIZE = 5;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();

        if ("/vets".equals(servletPath)) {
            // JSON API
            showVetsJson(request, response);
        } else {
            // HTML page
            showVetsHtml(request, response);
        }
    }

    private void showVetsHtml(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int page = getIntParam(request, "page", 1);
        List<Vet> vets = vetRepository.findAll(page, PAGE_SIZE);
        long totalItems = vetRepository.count();
        int totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);

        WebContext ctx = createWebContext(request, response);
        ctx.setVariable("listVets", vets);
        ctx.setVariable("currentPage", page);
        ctx.setVariable("totalPages", totalPages);
        ctx.setVariable("totalItems", totalItems);
        processTemplate("vets/vetList", ctx, response);
    }

    private void showVetsJson(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<Vet> vets = vetRepository.findAll();
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        ArrayNode vetArray = mapper.createArrayNode();

        for (Vet vet : vets) {
            ObjectNode vetNode = mapper.createObjectNode();
            vetNode.put("id", vet.getId());
            vetNode.put("firstName", vet.getFirstName());
            vetNode.put("lastName", vet.getLastName());
            ArrayNode specs = mapper.createArrayNode();
            vet.getSpecialties().forEach(s -> {
                ObjectNode specNode = mapper.createObjectNode();
                specNode.put("id", s.getId());
                specNode.put("name", s.getName());
                specs.add(specNode);
            });
            vetNode.set("specialties", specs);
            vetNode.put("nrOfSpecialties", vet.getNrOfSpecialties());
            vetArray.add(vetNode);
        }
        root.set("vetList", vetArray);

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(mapper.writeValueAsString(root));
    }
}
