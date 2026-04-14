package org.quarkus.samples.petclinic.vet;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = "/app/vets.html")
public class VetsServlet extends HttpServlet {

    @PersistenceContext
    private EntityManager em;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Vet> vets = em.createQuery("SELECT v FROM Vet v", Vet.class).getResultList();
        request.setAttribute("vets", vets);
        request.getRequestDispatcher("/WEB-INF/jsp/vetList.jsp").forward(request, response);
    }
}
