<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="header.jsp"/>
<h2>Veterinarians</h2>

<table id="vets" class="table table-striped">
    <thead>
        <tr>
            <th>Name</th>
            <th>Specialties</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="vet" items="${vets}">
        <tr>
            <td><c:out value="${vet.firstName}"/> <c:out value="${vet.lastName}"/></td>
            <td>
                <c:forEach var="s" items="${vet.specialties}">
                <span><c:out value="${s.name}"/>&nbsp;</span>
                </c:forEach>
                <c:if test="${vet.nrOfSpecialties == 0}"><span>None</span></c:if>
            </td>
        </tr>
        </c:forEach>
    </tbody>
</table>
<jsp:include page="footer.jsp"/>
