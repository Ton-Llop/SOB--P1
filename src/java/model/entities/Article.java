package model.entities;

import jakarta.persistence.Column;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.ElementCollection;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.JoinColumn;

@Entity
@XmlRootElement
public class Article implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "Article_Gen", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Article_Gen")
    private Long id;

    private String title; // Títol de l'article
    private String content; // Contingut complet de l'article
   @Column(columnDefinition = "BOOLEAN")
    private boolean isPrivate;

    private LocalDateTime publicationDate; // Data de publicació
    private int views; // Nombre de visualitzacions

    @ManyToOne
    @JoinColumn(name = "author", nullable = false) // Personaliza el nombre de la columna si es necesario
    private Usuari author;


    @ElementCollection
    private List<String> topics; // Llista de tòpics associats a l'article

    private String image; // URL de la imatge associada a l'article
    
    // Getters i Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
    public boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }


    public LocalDateTime getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(LocalDateTime publicationDate) {
        this.publicationDate = publicationDate;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }
    
    @JsonbTransient
    public Usuari getAuthor() {
        return author;
    }

    public void setAuthor(Usuari author) {
        this.author = author;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    // hashCode, equals i toString
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Article)) {
            return false;
        }
        Article other = (Article) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Article[ title=" + title + ", author=" + (author != null ? author.getUsername() : "unknown") + " ]";
    }
}
