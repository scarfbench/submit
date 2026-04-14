<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="header.jsp"/>
<h2>Owners</h2>

<table id="owners" class="table table-striped">
    <thead>
        <tr>
            <th style="width: 150px;">Name</th>
            <th style="width: 200px;">Address</th>
            <th>City</th>
            <th style="width: 120px">Telephone</th>
            <th>Pets</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="owner" items="${owners}">
        <tr>
            <td>
                <a href="${pageContext.request.contextPath}/app/owners/${owner.id}"><c:out value="${owner.firstName}"/> <c:out value="${owner.lastName}"/></a>
            </td>
            <td><c:out value="${owner.address}"/></td>
            <td><c:out value="${owner.city}"/></td>
            <td><c:out value="${owner.telephone}"/></td>
            <td>
                <c:forEach var="pet" items="${owner.pets}">
                    <span><c:out value="${pet.name}"/></span>
                </c:forEach>
            </td>
        </tr>
        </c:forEach>
    </tbody>
</table>
<jsp:include page="footer.jsp"/>
