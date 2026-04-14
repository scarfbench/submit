<%@ include file="header.jsp" %>
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
        <td>${fn:escapeXml(pet.name)}</td>
        <td>${pet.formattedBirthDate}</td>
        <td>${fn:escapeXml(pet.type.name)}</td>
        <td>${fn:escapeXml(pet.owner.firstName)} ${fn:escapeXml(pet.owner.lastName)}</td>
    </tr>
</table>

<form class="form-horizontal" method="post">
    <div class="form-group has-feedback">

        <div class="form-group <c:if test='${not empty errors["date"]}'>has-error</c:if>">
            <label class="col-sm-2 control-label">Date</label>
            <div class="col-sm-10">
                <div>
                    <input class="form-control" type="text" placeholder="YYYY-MM-DD"
                        id="date" name="date" value="${visit.formattedDate}"/>
                </div>
                <c:if test="${not empty errors['date']}">
                    <span class="help-inline">${fn:escapeXml(errors['date'])}</span>
                </c:if>
            </div>
        </div>

        <div class="form-group <c:if test='${not empty errors["description"]}'>has-error</c:if>">
            <label class="col-sm-2 control-label">Description</label>
            <div class="col-sm-10">
                <div>
                    <input class="form-control" type="text" id="description" name="description" value="${fn:escapeXml(visit.description)}" />
                </div>
                <c:if test="${not empty errors['description']}">
                    <span class="help-inline">${fn:escapeXml(errors['description'])}</span>
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
        <td>${v.formattedDate}</td>
        <td>${fn:escapeXml(v.description)}</td>
    </tr>
    </c:forEach>
    </c:if>
</table>
<%@ include file="footer.jsp" %>
