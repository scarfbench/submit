<%@ include file="header.jsp" %>
<h2>Owner Information</h2>

<table class="table table-striped">
  <tr>
    <th>Name</th>
    <td><b>${fn:escapeXml(owner.firstName)} ${fn:escapeXml(owner.lastName)}</b></td>
  </tr>
  <tr>
    <th>Address</th>
    <td>${fn:escapeXml(owner.address)}</td>
  </tr>
  <tr>
    <th>City</th>
    <td>${fn:escapeXml(owner.city)}</td>
  </tr>
  <tr>
    <th>Telephone</th>
    <td>${fn:escapeXml(owner.telephone)}</td>
  </tr>
</table>

<a href="${pageContext.request.contextPath}/owners/${owner.id}/edit" class="btn btn-primary">Edit Owner</a>
<a href="${pageContext.request.contextPath}/owners/${owner.id}/pets/new" class="btn btn-primary">Add New Pet</a>

<br />
<br />
<br />
<c:if test="${not empty owner.pets}">
<h2>Pets and Visits</h2>

<table class="table table-striped">
  <c:forEach var="pet" items="${owner.pets}">
  <tr>
    <td valign="top">
      <dl class="dl-horizontal">
        <dt>Name</dt>
        <dd>${fn:escapeXml(pet.name)}</dd>
        <dt>Birthdate</dt>
        <dd>${pet.formattedBirthDate}</dd>
        <dt>Type</dt>
        <dd>${fn:escapeXml(pet.type.name)}</dd>
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
            <td><a href="${pageContext.request.contextPath}/owners/${owner.id}/pets/${pet.id}/edit">Edit Pet</a></td>
            <td><a href="${pageContext.request.contextPath}/owners/${owner.id}/pets/${pet.id}/visits/new">Add Visit</a></td>
          </tr>
        </thead>
        <c:forEach var="visit" items="${pet.sortedVisits}">
        <tr>
          <td>${visit.formattedDate}</td>
          <td>${fn:escapeXml(visit.description)}</td>
        </tr>
        </c:forEach>
      </table>
    </td>
  </tr>
  </c:forEach>
</table>
</c:if>
<%@ include file="footer.jsp" %>
