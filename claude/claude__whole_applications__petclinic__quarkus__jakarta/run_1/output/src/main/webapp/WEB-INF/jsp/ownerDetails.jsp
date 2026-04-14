<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="header.jsp"/>
<h2>Owner Information</h2>

    <table class="table table-striped">
      <tr>
        <th>Name</th>
        <td><b><c:out value="${owner.firstName}"/> <c:out value="${owner.lastName}"/></b></td>
      </tr>
      <tr>
        <th>Address</th>
        <td><c:out value="${owner.address}"/></td>
      </tr>
      <tr>
        <th>City</th>
        <td><c:out value="${owner.city}"/></td>
      </tr>
      <tr>
        <th>Telephone</th>
        <td><c:out value="${owner.telephone}"/></td>
      </tr>
    </table>

    <a href="${pageContext.request.contextPath}/app/owners/${owner.id}/edit" class="btn btn-primary">Edit Owner</a>
    <a href="${pageContext.request.contextPath}/app/owners/${owner.id}/pets/new" class="btn btn-primary">Add New Pet</a>

    <br /><br /><br />
    <c:if test="${not empty owner.pets}">
    <h2>Pets and Visits</h2>

    <table class="table table-striped">
      <c:forEach var="pet" items="${owner.pets}">
      <tr>
        <td valign="top">
          <dl class="dl-horizontal">
            <dt>Name</dt>
            <dd><c:out value="${pet.name}"/></dd>
            <dt>Birthdate</dt>
            <dd><c:out value="${pet.birthDate}"/></dd>
            <dt>Type</dt>
            <dd><c:out value="${pet.type.name}"/></dd>
          </dl>
        </td>
        <td valign="top">
          <table class="table-condensed">
            <thead>
              <tr>
                <th>Visit Date</th>
                <th>Description</th>
              </tr>
              <tr>
                <td><a href="${pageContext.request.contextPath}/app/owners/${owner.id}/pets/${pet.id}/edit">Edit Pet</a></td>
                <td><a href="${pageContext.request.contextPath}/app/owners/${owner.id}/pets/${pet.id}/visits/new">Add Visit</a></td>
              </tr>
            </thead>
            <c:forEach var="visit" items="${pet.sortedVisits}">
            <tr>
              <td><c:out value="${visit.date}"/></td>
              <td><c:out value="${visit.description}"/></td>
            </tr>
            </c:forEach>
          </table>
        </td>
      </tr>
      </c:forEach>
    </table>
    </c:if>
<jsp:include page="footer.jsp"/>
