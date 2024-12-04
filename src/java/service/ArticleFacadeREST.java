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
import jakarta.persistence.TypedQuery;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


@Stateless
@Path("article")
public class ArticleFacadeREST extends AbstractFacade<Article> {

    @PersistenceContext(unitName = "Homework1PU")
    private EntityManager em;

    public ArticleFacadeREST() {
        super(Article.class);
    }
   
@GET
@Produces(MediaType.APPLICATION_JSON)
public Response getArticles(@QueryParam("topic") List<String> topics, @QueryParam("author") String author) {
 
    if (topics != null && topics.size() > 2) {
        return Response.status(Response.Status.BAD_REQUEST)
                       .entity("Només es permeten un màxim de dos tòpics.")
                       .build();
    }


    String query = "SELECT a FROM Article a WHERE 1=1";
    if (topics != null && !topics.isEmpty()) {
        query += " AND :topics MEMBER OF a.topics";
    }
    if (author != null && !author.isEmpty()) {
        query += " AND a.author.username = :author";
    }
    query += " ORDER BY a.views DESC"; // Ordenar por número de visualizaciones


    TypedQuery<Article> queryExec = em.createQuery(query, Article.class);
    if (topics != null && !topics.isEmpty()) {
        queryExec.setParameter("topics", topics);
    }
    if (author != null && !author.isEmpty()) {
        queryExec.setParameter("author", author);
    }


    List<Article> articles = queryExec.getResultList();
    if (articles.isEmpty()) {
        return Response.status(Response.Status.NOT_FOUND)
                       .entity("No hi ha articles d'aquest autor/amb aquest tòpics.")
                       .build();
    }

    List<Map<String, Object>> result = articles.stream().map(article -> {
        Map<String, Object> map = new HashMap<>();
        map.put("titol", article.getTitle());
        map.put("descripcio", article.getContent());
        map.put("nomAut", article.getAuthor().getUsername());
        map.put("dataPubli", article.getPublicationDate());
        map.put("nViews", article.getViews());
        map.put("topics", article.getTopics());
        return map;
    }).collect(Collectors.toList());

    return Response.ok(result).build();
}



    @Override
    protected EntityManager getEntityManager() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
