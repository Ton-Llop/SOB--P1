/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import authn.Secured;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
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
    
    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response find(@PathParam("id") long id) {
        User usuari = super.find(id);
        if (usuari == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Excluir la contrasenya
        usuari.setPassword(null);
        return Response.status(Response.Status.OK).entity(usuari).build();
    }
    
    @PUT
    @Path("/{id}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Secured
    public Response updateCustomer(@PathParam("id") Long id, User UserCanviat) {
        User UserExistent = em.find(User.class, id);
        if (UserExistent == null) {
            // Si no existeix ,retornar un error 404
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Customer not found")
                           .build();
        }
        // Actualitza el username i email passats
        if (UserCanviat.getUsername() != null) {
            UserExistent.setUsername(UserCanviat.getUsername());
        }
        if (UserCanviat.getEmail() != null) {
            UserExistent.setEmail(UserCanviat.getEmail());
        }
        // Guardar els canvis
        em.merge(UserExistent);
        return Response.status(Response.Status.OK)
                       .entity(UserExistent)
                       .build();
    }

}
