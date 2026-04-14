package org.jakarta.samples.petclinic.vet;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/vets.html")
public class VetServlet extends HttpServlet {

    @Inject
    EntityManager em;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Vet> vets = em.createQuery("SELECT v FROM Vet v", Vet.class).getResultList();
        req.setAttribute("vets", vets);
        req.getRequestDispatcher("/WEB-INF/jsp/vetList.jsp").forward(req, resp);
    }
}
