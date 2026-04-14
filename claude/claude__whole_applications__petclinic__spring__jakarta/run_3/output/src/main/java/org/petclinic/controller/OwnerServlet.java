package org.petclinic.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.petclinic.model.Owner;
import org.petclinic.repository.OwnerRepository;
import org.thymeleaf.context.WebContext;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/owners/*")
public class OwnerServlet extends BaseServlet {

    @Inject
    private OwnerRepository ownerRepository;

    private static final int PAGE_SIZE = 5;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            // /owners - list owners
            listOwners(request, response);
        } else if (pathInfo.equals("/find")) {
            // /owners/find
            showFindForm(request, response);
        } else if (pathInfo.equals("/new")) {
            // /owners/new
            showCreateForm(request, response);
        } else if (pathInfo.matches("/\\d+")) {
            // /owners/{id}
            showOwner(request, response, pathInfo);
        } else if (pathInfo.matches("/\\d+/edit")) {
            // /owners/{id}/edit
            showEditForm(request, response, pathInfo);
        } else if (pathInfo.matches("/\\d+/pets/new")) {
            // Forward to PetServlet
            request.getRequestDispatcher("/pets" + pathInfo).forward(request, response);
        } else if (pathInfo.matches("/\\d+/pets/\\d+/edit")) {
            request.getRequestDispatcher("/pets" + pathInfo).forward(request, response);
        } else if (pathInfo.matches("/\\d+/pets/\\d+/visits/new")) {
            request.getRequestDispatcher("/visits" + pathInfo).forward(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        if (pathInfo != null && pathInfo.equals("/new")) {
            processCreateForm(request, response);
        } else if (pathInfo != null && pathInfo.matches("/\\d+/edit")) {
            processEditForm(request, response, pathInfo);
        } else if (pathInfo != null && pathInfo.matches("/\\d+/pets/new")) {
            request.getRequestDispatcher("/pets" + pathInfo).forward(request, response);
        } else if (pathInfo != null && pathInfo.matches("/\\d+/pets/\\d+/edit")) {
            request.getRequestDispatcher("/pets" + pathInfo).forward(request, response);
        } else if (pathInfo != null && pathInfo.matches("/\\d+/pets/\\d+/visits/new")) {
            request.getRequestDispatcher("/visits" + pathInfo).forward(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void showFindForm(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WebContext ctx = createWebContext(request, response);
        ctx.setVariable("owner", new Owner());
        processTemplate("owners/findOwners", ctx, response);
    }

    private void listOwners(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int page = getIntParam(request, "page", 1);
        String lastName = getParam(request, "lastName", "");

        List<Owner> owners = ownerRepository.findByLastNameStartingWith(lastName, page, PAGE_SIZE);
        long totalItems = ownerRepository.countByLastNameStartingWith(lastName);

        if (owners.isEmpty()) {
            // No owners found - show find form with error
            WebContext ctx = createWebContext(request, response);
            Owner owner = new Owner();
            owner.setLastName(lastName);
            ctx.setVariable("owner", owner);
            ctx.setVariable("lastNameError", "not found");
            processTemplate("owners/findOwners", ctx, response);
            return;
        }

        if (totalItems == 1) {
            // Single owner - redirect to details
            response.sendRedirect(request.getContextPath() + "/owners/" + owners.get(0).getId());
            return;
        }

        // Multiple owners - show list
        int totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);
        WebContext ctx = createWebContext(request, response);
        ctx.setVariable("listOwners", owners);
        ctx.setVariable("currentPage", page);
        ctx.setVariable("totalPages", totalPages);
        ctx.setVariable("totalItems", totalItems);
        processTemplate("owners/ownersList", ctx, response);
    }

    private void showOwner(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws IOException {
        int ownerId = Integer.parseInt(pathInfo.substring(1));
        Optional<Owner> optOwner = ownerRepository.findById(ownerId);

        if (optOwner.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Owner not found");
            return;
        }

        Owner owner = optOwner.get();
        WebContext ctx = createWebContext(request, response);
        ctx.setVariable("owner", owner);

        // Flash message support
        String message = (String) request.getSession().getAttribute("flash_message");
        String error = (String) request.getSession().getAttribute("flash_error");
        if (message != null) {
            ctx.setVariable("message", message);
            request.getSession().removeAttribute("flash_message");
        }
        if (error != null) {
            ctx.setVariable("error", error);
            request.getSession().removeAttribute("flash_error");
        }

        processTemplate("owners/ownerDetails", ctx, response);
    }

    private void showCreateForm(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WebContext ctx = createWebContext(request, response);
        ctx.setVariable("owner", new Owner());
        processTemplate("owners/createOrUpdateOwnerForm", ctx, response);
    }

    private void processCreateForm(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Owner owner = new Owner();
        owner.setFirstName(getParam(request, "firstName"));
        owner.setLastName(getParam(request, "lastName"));
        owner.setAddress(getParam(request, "address"));
        owner.setCity(getParam(request, "city"));
        owner.setTelephone(getParam(request, "telephone"));

        // Validate
        List<Map<String, String>> errors = validateOwner(owner);
        if (!errors.isEmpty()) {
            WebContext ctx = createWebContext(request, response);
            ctx.setVariable("owner", owner);
            ctx.setVariable("errors", errors);
            processTemplate("owners/createOrUpdateOwnerForm", ctx, response);
            return;
        }

        ownerRepository.save(owner);
        request.getSession().setAttribute("flash_message", "New Owner Created");
        response.sendRedirect(request.getContextPath() + "/owners/" + owner.getId());
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws IOException {
        String idStr = pathInfo.replaceAll("/edit", "").substring(1);
        int ownerId = Integer.parseInt(idStr);
        Optional<Owner> optOwner = ownerRepository.findById(ownerId);

        if (optOwner.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Owner not found");
            return;
        }

        WebContext ctx = createWebContext(request, response);
        ctx.setVariable("owner", optOwner.get());
        processTemplate("owners/createOrUpdateOwnerForm", ctx, response);
    }

    private void processEditForm(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws IOException {
        String idStr = pathInfo.replaceAll("/edit", "").substring(1);
        int ownerId = Integer.parseInt(idStr);
        Optional<Owner> optOwner = ownerRepository.findById(ownerId);

        if (optOwner.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Owner not found");
            return;
        }

        Owner owner = optOwner.get();
        owner.setFirstName(getParam(request, "firstName"));
        owner.setLastName(getParam(request, "lastName"));
        owner.setAddress(getParam(request, "address"));
        owner.setCity(getParam(request, "city"));
        owner.setTelephone(getParam(request, "telephone"));

        List<Map<String, String>> errors = validateOwner(owner);
        if (!errors.isEmpty()) {
            WebContext ctx = createWebContext(request, response);
            ctx.setVariable("owner", owner);
            ctx.setVariable("errors", errors);
            processTemplate("owners/createOrUpdateOwnerForm", ctx, response);
            return;
        }

        ownerRepository.save(owner);
        request.getSession().setAttribute("flash_message", "Owner Values Updated");
        response.sendRedirect(request.getContextPath() + "/owners/" + ownerId);
    }

    private List<Map<String, String>> validateOwner(Owner owner) {
        List<Map<String, String>> errors = new ArrayList<>();
        if (!hasText(owner.getFirstName())) {
            errors.add(createError("firstName", "must not be blank"));
        }
        if (!hasText(owner.getLastName())) {
            errors.add(createError("lastName", "must not be blank"));
        }
        if (!hasText(owner.getAddress())) {
            errors.add(createError("address", "must not be blank"));
        }
        if (!hasText(owner.getCity())) {
            errors.add(createError("city", "must not be blank"));
        }
        if (!hasText(owner.getTelephone())) {
            errors.add(createError("telephone", "must not be blank"));
        } else if (!owner.getTelephone().matches("\\d{10}")) {
            errors.add(createError("telephone", "Telephone must be a 10-digit number"));
        }
        return errors;
    }

    private Map<String, String> createError(String field, String message) {
        Map<String, String> error = new HashMap<>();
        error.put("field", field);
        error.put("message", message);
        return error;
    }
}
