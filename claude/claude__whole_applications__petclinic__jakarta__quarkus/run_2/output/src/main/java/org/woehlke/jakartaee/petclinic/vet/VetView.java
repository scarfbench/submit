package org.woehlke.jakartaee.petclinic.vet;

import org.woehlke.jakartaee.petclinic.application.framework.views.CrudView;
import org.woehlke.jakartaee.petclinic.application.framework.views.LanguageChangeableView;
import org.woehlke.jakartaee.petclinic.application.framework.views.SearchableView;
import org.woehlke.jakartaee.petclinic.application.framework.views.CrudView2Model;
import org.woehlke.jakartaee.petclinic.specialty.Specialty;

import java.io.Serializable;
import java.util.List;


/**
 * Simplified for Quarkus migration - PrimeFaces removed
 */
public interface VetView extends CrudView<Vet>, CrudView2Model {

    String JSF_PAGE = "veterinarian.jsf";

    Specialty findSpecialtyByName(String name);

    // PrimeFaces DualListModel removed for Quarkus migration
    List<Specialty> getSpecialtiesPickListSource();
    void setSpecialtiesPickListSource(List<Specialty> specialtiesPickListSource);

    List<Specialty> getSpecialtiesPickListTarget();
    void setSpecialtiesPickListTarget(List<Specialty> specialtiesPickListTarget);

    long serialVersionUID = -4141782100256382881L;
}
