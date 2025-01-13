/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import authn.Credentials;
import authn.Secured;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import model.entities.Article;
import model.entities.Usuari;


    @Stateless
    @Path("/user")
    public class UserFacadeREST extends AbstractFacade<Usuari> {

    @PersistenceContext(unitName = "Homework1PU")
    private EntityManager em;

    public UserFacadeREST() {
        super(Usuari.class);
    }

    @Override
    protected EntityManager getEntityManager() {
    return em;
    }


    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getCustomers() {

    String query = "SELECT u FROM Usuari u";
    List<Usuari> users = em.createQuery(query, Usuari.class).getResultList();

   
    List<Object> responseList = new ArrayList<>();
    // recorrer users
    for (Usuari u : users) {
        
        Map<String, Object> userMap = new LinkedHashMap<>();
        userMap.put("id", u.getId());
        userMap.put("username", u.getUsername());
        userMap.put("email", u.getEmail());
        userMap.put("nom", u.getNom());

        // Verificar si el usuario es autor de artículos
        if (u.getArticles() != null && !u.getArticles().isEmpty()) {
            userMap.put("isAuthor", true);
            Article lastArticle = u.getArticles().get(u.getArticles().size() - 1);
            // Añadir enlace HATEOAS al último artículo
            Map<String, String> links = new LinkedHashMap<>();
            links.put("article", "/article/" + lastArticle.getId());
            userMap.put("links", links);
        } else {
            userMap.put("isAuthor", false);
        }

        // Añadir el usuario procesado a la respuesta final
        responseList.add(userMap);
    }

    // Devolver la respuesta como JSON
    return Response.status(Response.Status.OK)
                   .entity(responseList)
                   .build();
    }

    
    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response find(@PathParam("id") long id) {
        Usuari usuari = super.find(id);
        if (usuari == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("L'usuari no existeix")
                    .build();
        }

        // Excluir la contrasenya
        usuari.setPassword(null);
        return Response.status(Response.Status.OK).entity(usuari).build();
    }
    
    @PUT
    @Path("/{id}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateCustomer(@PathParam("id") Long id, Usuari UserCanviat, @Context HttpHeaders headers) {
        Usuari UserExistent = em.find(Usuari.class, id);
        
        if (!validarRegistrat(headers)) {
        return Response.status(Response.Status.UNAUTHORIZED)
                       .entity("Has d'estar autentificat per fer aquesta acció!")
                       .build();
    }
        String username = extractUsername(headers);
        if (username == null || username.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Credencials invàlides o falta l'encapçalament Authorization.").build();
        }
        if (UserExistent == null) {
            // Si no existeix ,retornar un error 404
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("L'usuari no existeix")
                           .build();
        }
        // Actualitza el username i email passats
        if (UserCanviat.getUsername() != null) {
            UserExistent.setUsername(UserCanviat.getUsername());
        }
        if (UserCanviat.getEmail() != null) {
            UserExistent.setEmail(UserCanviat.getEmail());
        }
        if (UserCanviat.getNom() != null) {
            UserExistent.setNom(UserCanviat.getNom());
        }
        // Guardar els canvis
        em.merge(UserExistent);
        UserExistent.setPassword(null);
        return Response.status(Response.Status.OK)
                       .entity(UserExistent)
                       .build();
    }
private boolean validarRegistrat(HttpHeaders headers) {
    List<String> headersAuth = headers.getRequestHeader(HttpHeaders.AUTHORIZATION);

    if (headersAuth == null || headersAuth.isEmpty()) {
        return false;
    } else {
        try {
            // Decodificar y extraer usuario y contraseña
            String auth = headersAuth.get(0).replace("Basic ", "");
            String decode = new String(Base64.getDecoder().decode(auth), StandardCharsets.UTF_8);
            StringTokenizer tokenizer = new StringTokenizer(decode, ":");
            String username = tokenizer.nextToken();
            String password = tokenizer.nextToken();

            // Validar credenciales contra la base de datos
            TypedQuery<Credentials> query = em.createNamedQuery("Credentials.findUser", Credentials.class);
            Credentials c = query.setParameter("username", username).getSingleResult();

            // Comprobar si las credenciales son válidas
            return c.getPassword().equals(password);
        } catch (Exception e) {
            return false;
        }
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
@GET
@Path("/LoginVerification")
@Produces(MediaType.APPLICATION_JSON)
public Response verifyLogin(@Context HttpHeaders headers) {
    if (validarRegistrat(headers)) {
        String username = extractUsername(headers);
        return Response.ok("Usuario autenticado: " + username).build();
    } else {
        return Response.status(Response.Status.UNAUTHORIZED).entity("Credenciales inválidas").build();
    }
}

}
