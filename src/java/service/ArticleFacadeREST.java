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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import java.time.LocalDateTime;
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
   

    @Override
    protected EntityManager getEntityManager() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
   
    @GET
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public Response getArticles(@QueryParam("topic") List<String> topics, @QueryParam("author") String author){
 
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
    
    
    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getArticleId(@PathParam("id")long id, @Context SecurityContext securityContext){
        Article article = super.find(id);
        if (article == null){
            return Response.status(Response.Status.NOT_FOUND).entity("L'article no existeix").build();
        }
        //Comprovar si l'article es privat
        if (article.isPrivate()){
            //Comprovar que l'usuari estigui registrat
        String usuariAutentificat = securityContext.getUserPrincipal().getName();
        if (usuariAutentificat == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("Has d'estar autentificat per fer aquesta acció!")
                .build();
    }
            article.setViews(article.getViews() + 1);
            super.edit(article); //Persistir els canvis en la bd
            return Response.ok().entity(article).build();
        }
        article.setViews(article.getViews() + 1);
        super.edit(article); //Persistir els canvis en la bd
        return Response.ok().entity(article).build();
        }
    
    @DELETE
    @Path("/{id}")
    @Secured
    public Response deleteArticle(@PathParam("id") Long id, @Context SecurityContext securityContext) {
    // 1. Recuperar l'article de la base de dades
    Article article = em.find(Article.class, id);
    
    if (article == null) {
        // Art existent
        return Response.status(Response.Status.NOT_FOUND)
                       .entity("Article amb ID " + id + " no trobat.")
                       .build();
    }
    
    //  Comprovar que autentificacio
    String usuariAutentificat = securityContext.getUserPrincipal().getName();
    if (usuariAutentificat == null) {
        return Response.status(Response.Status.UNAUTHORIZED)
                       .entity("Has d'estar autentificat per fer aquesta acció!")
                       .build();
    }

    // Que es autor
    if (!article.getAuthor().getUsername().equals(usuariAutentificat)) {
        return Response.status(Response.Status.FORBIDDEN)
                       .entity("No ets l'autor d'aquest article!")
                       .build();
    }
   
    em.getTransaction().begin();
    em.remove(article);
    em.getTransaction().commit();
    // Si surt be retornem buit
    return Response.status(Response.Status.NO_CONTENT).build();
}
   @POST
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Secured
public Response addArticle(Article newArticle, @Context SecurityContext securityContext) {
    // Validar que l'usuari està autentificat
    String username = securityContext.getUserPrincipal().getName();
    if (username == null || username.isEmpty()) {
        return Response.status(Response.Status.UNAUTHORIZED)
                       .entity("Usuari no autentificat.")
                       .build();
    }

    // Validar que l'usuari existeix
    User author = em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

    if (author == null) {
        return Response.status(Response.Status.BAD_REQUEST)
                       .entity("Usuari no trobat.")
                       .build();
    }

    // Validar els tòpics proporcionats
    if (newArticle.getTopics() == null || newArticle.getTopics().isEmpty()) {
        return Response.status(Response.Status.BAD_REQUEST)
                       .entity("L'article ha de tenir almenys un tòpic.")
                       .build();
    }

    List<String> validTopics = em.createQuery("SELECT t.name FROM Topic t", String.class)
                                 .getResultList();

    for (String topic : newArticle.getTopics()) {
        if (!validTopics.contains(topic)) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Tòpic invàlid: " + topic)
                           .build();
        }
    }

    // Assignar les propietats automàtiques de l'article
    newArticle.setAuthor(author);
    newArticle.setPublicationDate(LocalDateTime.now());
    newArticle.setViews(0); // Inicialitzar visualitzacions a 0

    // Persistir l'article
    em.persist(newArticle);

    // Retornar resposta amb codi 201 Created i l'identificador de l'article
    return Response.status(Response.Status.CREATED)
                   .entity(newArticle.getId())
                   .build();
}
 
}
