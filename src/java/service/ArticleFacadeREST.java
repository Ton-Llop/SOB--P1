/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import authn.Credentials;
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
import model.entities.Usuari;
import authn.Secured;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.SecurityContext;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.stream.Collectors;


@Stateless
@Path("/article")
public class ArticleFacadeREST extends AbstractFacade<Article> {

    @PersistenceContext(unitName = "Homework1PU")
    private EntityManager em;

    public ArticleFacadeREST() {
        super(Article.class);
    }
   

    @Override
    protected EntityManager getEntityManager() {
        return em;
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
        map.put("id", article.getId()); // Agregar el ID
        map.put("titol", article.getTitle());
        map.put("descripcio", article.getContent());
        map.put("nomAut", article.getAuthor().getUsername());
        map.put("dataPubli", article.getPublicationDate());
        map.put("nViews", article.getViews());
        map.put("topics", article.getTopics());
        map.put("imatge", article.getImage());
        map.put("isPrivate",article.getIsPrivate());

        return map;
    }).collect(Collectors.toList());

    return Response.ok(result).build();
    }
    
    
@GET
@Path("/{id}")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public Response obtenirArticle(@PathParam("id") Long id, @Context HttpHeaders headers) {
    try {
        System.out.println("Buscando artículo con ID: " + id);

        // Recuperar el artículo con el autor
        Article article = em.createQuery("SELECT a FROM Article a JOIN FETCH a.author WHERE a.id = :id", Article.class)
                            .setParameter("id", id)
                            .getSingleResult();

        if (article == null) {
            System.out.println("Artículo no encontrado.");
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("No hi ha aquest article encara")
                           .build();
        }

        // Incrementar las visualizaciones del artículo
        article.setViews(article.getViews() + 1);
        em.merge(article);

        // Construir la respuesta manualmente
        Map<String, Object> result = new HashMap<>();
        result.put("id", article.getId());
        result.put("titol", article.getTitle());
        result.put("descripcio", article.getContent());
        result.put("nomAut", article.getAuthor().getUsername());
        result.put("dataPubli", article.getPublicationDate());
        result.put("nViews", article.getViews());
        result.put("topics", article.getTopics());
        result.put("imatge",article.getImage());
        result.put("isPrivate",article.getIsPrivate());

        return Response.ok(result).build();

    } catch (Exception e) {
        e.printStackTrace();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .entity("Error al processar la sol·licitud: " + e.getMessage())
                       .build();
    }
}









    
    @DELETE
    @Path("/{id}")
    // Decisió de disseny treiem el @secured i ho comprovem manualment.
    public Response deleteArticle(@PathParam("id") Long id, @Context HttpHeaders headers) {
    //Recuperar l'article de la base de dades
    Article article = em.find(Article.class, id);

    if (article == null) {
        // Art no existeix
        return Response.status(Response.Status.NOT_FOUND)
                       .entity("Article amb ID " + id + " no trobat.")
                       .build();
    }

    //Comprobar autentificació
    if (!validarRegistrat(headers)) {
        return Response.status(Response.Status.UNAUTHORIZED)
                       .entity("Has d'estar autentificat per fer aquesta acció!")
                       .build();
    }

    //Extreure nom user 
    String usuariAutentificat = extractUsername(headers);
    
    if (usuariAutentificat == null) {
        return Response.status(Response.Status.UNAUTHORIZED)
                       .entity("No es pot obtenir el nom d'usuari!")
                       .build();
    }

    //Comprobar user  es  autor del art
    if (!article.getAuthor().getUsername().equals(usuariAutentificat)) {
        return Response.status(Response.Status.FORBIDDEN)
                       .entity("No ets l'autor d'aquest article!")
                       .build();
    }

    // Eliminar art
    em.remove(article);

    //Retornar respuesta con código 204 No Content
    return Response.status(Response.Status.NO_CONTENT).build();
}

@POST
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
// Decisió de disseny treiem el @secured i ho comprovem manualment.
public Response crearArticle(Article e, @Context HttpHeaders headers) {
    try {
        
        // Validar autentificacio
        if (!validarRegistrat(headers)) {
        return Response.status(Response.Status.UNAUTHORIZED)
                       .entity("Has d'estar autentificat per fer aquesta acció!")
                       .build();
    }
        String username = extractUsername(headers);
        if (username == null || username.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Credencials invàlides o falta l'encapçalament Authorization.").build();
        }

        // 2. Verificar que el usuario existe en la base de datos
        Usuari autorBD;
        try {
            String queryAutor = "SELECT u FROM Usuari u WHERE u.username = :username";
            autorBD = em.createQuery(queryAutor, Usuari.class)
                        .setParameter("username", username)
                        .getSingleResult();
        } catch (NoResultException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity("Usuari no trobat").build();
        }

        // 3. Validar los tópicos proporcionados
        List<String> llistaTopics = e.getTopics();
        if (llistaTopics == null || llistaTopics.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Cal proporcionar almenys un tòpic").build();
        }

        String existQuery = "SELECT t.name FROM Topic t WHERE t.name IN :noms";
        List<String> resultatNoms = em.createQuery(existQuery, String.class)
                                     .setParameter("noms", llistaTopics)
                                     .getResultList();

        if (llistaTopics.size() != resultatNoms.size()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Un o més tòpics no són vàlids").build();
        }
        
        e.setAuthor(autorBD);
        e.setPublicationDate(LocalDateTime.now());
        e.setViews(0);

        em.persist(e);
        
        // 5. Retornar respuesta exitosa
        return Response.status(Response.Status.CREATED)
               .entity(e.getId())
               .build();

    } catch (Exception ex) {
        ex.printStackTrace(); // Para depuración en los logs
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error processant l'article.").build();
    }
}


private boolean validarRegistrat(HttpHeaders headers) {
    List<String> headersAuth = headers.getRequestHeader(HttpHeaders.AUTHORIZATION);

    if (headersAuth == null || headersAuth.isEmpty()) {
        System.out.println("Encabezado Authorization no encontrado.");
        return false;
    }

    try {
        // Decodificar y extraer usuario y contraseña
        String auth = headersAuth.get(0).replace("Basic ", "");
        String decode = new String(Base64.getDecoder().decode(auth), StandardCharsets.UTF_8);

        System.out.println("Credenciales decodificadas: " + decode);

        StringTokenizer tokenizer = new StringTokenizer(decode, ":");
        String username = tokenizer.nextToken();
        String password = tokenizer.nextToken();

        System.out.println("Username: " + username);
        System.out.println("Password: " + password);

        // Validar credenciales contra la base de datos
        TypedQuery<Credentials> query = em.createNamedQuery("Credentials.findUser", Credentials.class);
        Credentials c = query.setParameter("username", username).getSingleResult();

        // Comprobar si las credenciales son válidas
        boolean isValid = c.getPassword().equals(password);
        System.out.println("Validación de credenciales: " + isValid);
        return isValid;

    } catch (Exception e) {
        System.err.println("Error validando las credenciales: " + e.getMessage());
        return false;
    }
}

private String extractUsername(HttpHeaders headers) {
    try {
        // Obtener el encabezado Authorization
        List<String> headersAuth = headers.getRequestHeader(HttpHeaders.AUTHORIZATION);
        
        // Validar que el encabezado esté presente
        if (headersAuth == null || headersAuth.isEmpty()) {
            System.out.println("Encabezado Authorization no encontrado.");
            return null;
        }

        // Obtener el valor del encabezado y decodificarlo
        String auth = headersAuth.get(0).replace("Basic ", "");
        String decode = new String(Base64.getDecoder().decode(auth), StandardCharsets.UTF_8);

        // Extraer el username (antes de ':')
        String username = decode.split(":")[0];
        return username;

    } catch (Exception e) {
        // Registrar el error y retornar null
        System.out.println("Error extrayendo el nombre de usuario: " + e.getMessage());
        return null;
    }
}

}
