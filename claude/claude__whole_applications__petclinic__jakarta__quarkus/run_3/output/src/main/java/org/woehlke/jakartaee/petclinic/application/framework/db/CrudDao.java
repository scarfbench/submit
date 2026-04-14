package org.woehlke.jakartaee.petclinic.application.framework.db;

import org.woehlke.jakartaee.petclinic.application.framework.EntityBase;

import java.io.Serializable;
import java.util.List;

/**
 * @param <T>
 */
public interface CrudDao<T extends EntityBase> {

    long serialVersionUID = 5140497751059102450L;

    List<T> getAll();

    T findById(long id);

    T addNew(T entity);

    T update(T entity);

    void delete(long id);

}
