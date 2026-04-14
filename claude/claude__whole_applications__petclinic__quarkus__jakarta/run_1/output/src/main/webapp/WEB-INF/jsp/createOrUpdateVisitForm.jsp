<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="header.jsp"/>
<h2>
    <c:if test="${empty visit}">New </c:if>
    Visit
</h2>

<b>Pet</b>
<table class="table table-striped">
    <thead>
        <tr>
            <th>Name</th>
            <th>Birthdate</th>
            <th>Type</th>
            <th>Owner</th>
        </tr>
    </thead>
    <tr>
        <td><c:out value="${pet.name}"/></td>
        <td><c:out value="${pet.birthDate}"/></td>
        <td><c:out value="${pet.type.name}"/></td>
        <td><c:out value="${pet.owner.firstName}"/> <c:out value="${pet.owner.lastName}"/></td>
    </tr>
</table>

<form class="form-horizontal" method="post">
    <div class="form-group has-feedback">

        <div class="form-group <c:if test='${not empty errors["date"]}'>has-error</c:if>">
            <label class="col-sm-2 control-label">Date</label>
            <div class="col-sm-10">
                <div>
                    <input class="form-control" type="text" placeholder="YYYY-MM-DD"
                        id="date" name="date" value="<c:out value='${visit.date}' default=''/>"/>
                </div>
                <c:if test="${not empty errors['date']}">
                    <span class="help-inline"><c:out value="${errors['date']}"/></span>
                </c:if>
            </div>
        </div>

        <div class="form-group <c:if test='${not empty errors["description"]}'>has-error</c:if>">
            <label class="col-sm-2 control-label">Description</label>
            <div class="col-sm-10">
                <div>
                    <input class="form-control" type="text" id="description" name="description" value="<c:out value='${visit.description}' default=''/>" />
                </div>
                <c:if test="${not empty errors['description']}">
                    <span class="help-inline"><c:out value="${errors['description']}"/></span>
                </c:if>
            </div>
        </div>

    </div>

    <div class="form-group">
        <div class="col-sm-offset-2 col-sm-10">
            <input type="hidden" name="petId" value="1" />
            <button class="btn btn-primary" type="submit">Add Visit</button>
        </div>
    </div>
</form>

<br />
<b>Previous Visits</b>
<table class="table table-striped">
    <tr>
        <th>Date</th>
        <th>Description</th>
    </tr>
    <c:if test="${not empty visit}">
    <c:forEach var="v" items="${pet.sortedVisits}">
    <tr>
        <td><c:out value="${v.date}"/></td>
        <td><c:out value="${v.description}"/></td>
    </tr>
    </c:forEach>
    </c:if>
</table>
<jsp:include page="footer.jsp"/>
