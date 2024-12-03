/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import model.entities.Article;
import model.entities.User;


@Stateless
@Path("user")
public class UserFacadeREST extends AbstractFacade<User> {

    @PersistenceContext(unitName = "Homework1PU")
    private EntityManager em;

    public UserFacadeREST() {
        super(User.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @GET
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public Response getCustomers() {
        // Consultar users
    String query = "SELECT u FROM Usuari u";
    List<User> users = em.createQuery(query, User.class).getResultList();
    Collection<User> retorn = new ArrayList<>();

    // Recorrer users
    for (User u : users) {
        u.setPassword(null);
        String linkArticle = null;
        if (u.getArticles() != null && !u.getArticles().isEmpty()) {
            Article lastArticle = u.getArticles().get(u.getArticles().size() - 1);
            linkArticle = "/article/" + lastArticle.getId();
        }
        u.setCodiArticle(linkArticle);
        retorn.add(u);
    }
    return Response.status(Response.Status.OK)
                   .entity(retorn)
                   .build();
}
