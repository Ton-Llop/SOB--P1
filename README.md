# SOB P1
 
# Enunciat
## Haurem de suportar les funcionalitats següents:

## Definirem una API REST comuna per tothom i que els grups hauran d’implementar a les seves pràctiques.

## Cal implementar els serveis REST perquè treballin i retornin JSON. Eines que us poden ser d'interès: Gson, JAXB (exemple d'ús)

## Fer servir versionat per la sostenibilitat de l’API.
Pels articles:

GET /rest/api/v1/article?topic=${topic}&author=${author}

Per defecte, proporciona un llistat JSON amb tots els articles ordenats per popularitat de forma descendent. La informació dels articles és la que es correspon a la Figura 1.

Els paràmetres "topic" i "author" són opcionals.

Si s'especifica el paràmetre "topic", només s'inclouran en el llistat els articles del tipus ${topic}. Es podrà especificar un màxim de dos topics en l'atribut topic, i per tant la part de consulta de l'URL podria ser del tipus: topic=${topic1}&topic=${topic2}&author=${author}

Si s'especifica el paràmetre "author", només s'inclouran en el llistat els articles de l'autor ${author}.

El filtratge s'ha de fer mitjançant una consulta a la base de dades. No s'acceptarà com a vàlid retornar el llistat de tots els articles i filtrar-los de forma programàtica amb Java.
GET /rest/api/v1/article/${id}

Retorna l'article sencer a partir de l'identificador de l'article, és a dir, tota la informació indicada en la Figura 2. 

Si l'article és privat, només es podrà retornar si l'usuari està registrat.

Aquesta operació implica augmentar el nombre de visualitzacions de l'article en +1. 

DELETE /rest/api/v1/article/${id}
Opcional! Esborra l'article amb identificador ${id} del sistema.

Per aquesta operació, cal que l'usuari sigui l'autor de l'article i estigui autentificat.

POST/rest/api/v1/article

Permetre afegir un article nou a partir de les dades proporcionades en format JSON/XML.

La data de la publicació es determinarà de forma automàtica i s'haurà de validar que els tòpics escollits siguin vàlids i que l'usuari existeixi. 

Cal retornar el codi HTTP 201 Created en cas afirmatiu juntament amb l'identificador de l'article.

Per aquesta operació, cal que l'usuari estigui autentificat.

Pels usuaris:

GET /rest/api/v1/customer

 Llistat JSON dels usuaris.

Si l'usuari és autor d'un article, cal indicar que és autor i retornar l'enllaç a l'últim article que ha publicat per suportar el principi de HATEOAS. Per exemple, en JSON:
                 "links": {
                      "article": "/article/12345"
                 }
Aquesta crida no pot retornar informació confidencial, p. ex., la  contrasenya dels usuaris.

GET /rest/api/v1/customer/${id}

Retorna la informació de l'usuari amb identificador ${id}. 

Aquesta crida no pot retornar informació confidencial, p. ex., la  contrasenya d'aquest usuari.

PUT /rest/api/v1/customer/${id}

Opcional! Modifica les dades del client amb identificador ${id} al sistema amb les dades JSON/XML proporcionades.

Per aquesta operació, cal que el client estigui autentificat.
