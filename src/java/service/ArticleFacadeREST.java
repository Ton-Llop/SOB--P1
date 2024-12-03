/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import model.entities.Article;
import model.entities.User;
import authn.Secured;


@Stateless
@Path("article")
public class ArticleFacadeREST extends AbstractFacade<Article> {

    @PersistenceContext(unitName = "Homework1PU")
    private EntityManager em;

    public ArticleFacadeREST() {
        super(Article.class);
    }
   
@GET
@Path("filter")
@Produces(MediaType.APPLICATION_JSON)
public Response getArticles(@QueryParam("topic") List<String> topics, @QueryParam("author") String author) {
    StringBuilder queryBuilder = new StringBuilder("SELECT a FROM Article a WHERE 1=1");

    if (topics != null && !topics.isEmpty()) {
        if (topics.size() > 2) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Només es permeten un màxim de dos tòpics.")
                    .build();
        }
        queryBuilder.append(" AND a.topic IN :topics");
    }

    if (author != null && !author.isEmpty()) {
        queryBuilder.append(" AND a.author.username = :author");
    }

    // Ordenar els resultats per popularitat (descendent)
    queryBuilder.append(" ORDER BY a.popularity DESC");

    // Crear la consulta
    var query = em.createQuery(queryBuilder.toString(), Article.class);

    // Configurar paràmetres si són proporcionats
    if (topics != null && !topics.isEmpty()) {
        query.setParameter("topics", topics);
    }
    if (author != null && !author.isEmpty()) {
        query.setParameter("author", author);
    }

    // Obtenir resultats
    List<Article> articles = query.getResultList();

    // Retornar els articles en format JSON
    return Response.ok(articles).build();
}

    @Override
    protected EntityManager getEntityManager() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
