package edu.umass.ckc.wo.beans;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created with IntelliJ IDEA.
 * User: marshall
 * Date: 5/28/14
 * Time: 1:10 PM
 * To change this template use File | Settings | File Templates.
 */
@javax.persistence.Table(name = "commoncorestandards", schema = "", catalog = "wayangoutpostdb")
@Entity
public class CommoncorestandardsEntity {
    private String id;
    private String description;

    @javax.persistence.Column(name = "ID")
    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @javax.persistence.Column(name = "Description")
    @Basic
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommoncorestandardsEntity that = (CommoncorestandardsEntity) o;

        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }
}
