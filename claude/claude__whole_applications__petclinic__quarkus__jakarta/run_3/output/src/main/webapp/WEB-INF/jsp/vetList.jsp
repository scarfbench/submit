<%@ include file="header.jsp" %>
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
            <td>${fn:escapeXml(vet.firstName)} ${fn:escapeXml(vet.lastName)}</td>
            <td>
                <c:forEach var="s" items="${vet.specialties}">
                <span>
                    ${fn:escapeXml(s.name)}&nbsp;
                </span>
                </c:forEach>
                <c:if test="${vet.nrOfSpecialties == 0}">
                <span>None</span>
                </c:if>
            </td>
        </tr>
        </c:forEach>
    </tbody>
</table>
<%@ include file="footer.jsp" %>
