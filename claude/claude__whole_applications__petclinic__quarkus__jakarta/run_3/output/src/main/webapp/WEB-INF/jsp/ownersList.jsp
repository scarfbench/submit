<%@ include file="header.jsp" %>
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
                <a href="${pageContext.request.contextPath}/owners/${owner.id}">${fn:escapeXml(owner.firstName)} ${fn:escapeXml(owner.lastName)}</a>
            </td>
            <td>${fn:escapeXml(owner.address)}</td>
            <td>${fn:escapeXml(owner.city)}</td>
            <td>${fn:escapeXml(owner.telephone)}</td>
            <td>
                <c:forEach var="pet" items="${owner.pets}">
                    <span>${fn:escapeXml(pet.name)}</span>
                </c:forEach>
            </td>
        </tr>
        </c:forEach>
    </tbody>
</table>
<%@ include file="footer.jsp" %>
