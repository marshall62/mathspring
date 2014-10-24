package edu.umass.ckc.wo.beans;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 6/9/14
 * Time: 2:17 PM
 * To change this template use File | Settings | File Templates.
 */
@javax.persistence.Table(name = "problemgroup", schema = "", catalog = "wayangoutpostdb")
@Entity
public class TopicEntity {
    private int id;
    private String intro;
    private String summary;
    private String description;
    private String type;
    private byte active;
    private byte isCcMapped;

    @javax.persistence.Column(name = "id")
    @Id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @javax.persistence.Column(name = "intro")
    @Basic
    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    @javax.persistence.Column(name = "summary")
    @Basic
    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @javax.persistence.Column(name = "description")
    @Basic
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @javax.persistence.Column(name = "type")
    @Basic
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @javax.persistence.Column(name = "active")
    @Basic
    public byte getActive() {
        return active;
    }

    public void setActive(byte active) {
        this.active = active;
    }

    @javax.persistence.Column(name = "isCCMapped")
    @Basic
    public byte getCcMapped() {
        return isCcMapped;
    }

    public void setCcMapped(byte ccMapped) {
        isCcMapped = ccMapped;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TopicEntity that = (TopicEntity) o;

        if (active != that.active) return false;
        if (id != that.id) return false;
        if (isCcMapped != that.isCcMapped) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (intro != null ? !intro.equals(that.intro) : that.intro != null) return false;
        if (summary != null ? !summary.equals(that.summary) : that.summary != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (intro != null ? intro.hashCode() : 0);
        result = 31 * result + (summary != null ? summary.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (int) active;
        result = 31 * result + (int) isCcMapped;
        return result;
    }
}
