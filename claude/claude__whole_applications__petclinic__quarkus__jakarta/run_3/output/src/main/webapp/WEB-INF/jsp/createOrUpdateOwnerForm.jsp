<%@ include file="header.jsp" %>
<h2>Owner</h2>
<form class="form-horizontal" id="add-owner-form" method="post">
    <div class="form-group has-feedback">

        <div class="form-group <c:if test='${not empty errors["firstName"]}'>has-error</c:if>">
            <label class="col-sm-2 control-label">Name</label>
            <div class="col-sm-10">
                <div>
                    <input class="form-control" type="text" id="firstName" name="firstName" value="${fn:escapeXml(owner.firstName)}" />
                </div>
                <c:if test="${not empty errors['firstName']}">
                    <span class="help-inline">${fn:escapeXml(errors['firstName'])}</span>
                </c:if>
            </div>
        </div>

        <div class="form-group <c:if test='${not empty errors["lastName"]}'>has-error</c:if>">
            <label class="col-sm-2 control-label">Last Name</label>
            <div class="col-sm-10">
                <div>
                    <input class="form-control" type="text" id="lastName" name="lastName" value="${fn:escapeXml(owner.lastName)}" />
                </div>
                <c:if test="${not empty errors['lastName']}">
                    <span class="help-inline">${fn:escapeXml(errors['lastName'])}</span>
                </c:if>
            </div>
        </div>

        <div class="form-group <c:if test='${not empty errors["address"]}'>has-error</c:if>">
            <label class="col-sm-2 control-label">Address</label>
            <div class="col-sm-10">
                <div>
                    <input class="form-control" type="text" id="address" name="address" value="${fn:escapeXml(owner.address)}" />
                </div>
                <c:if test="${not empty errors['address']}">
                    <span class="help-inline">${fn:escapeXml(errors['address'])}</span>
                </c:if>
            </div>
        </div>

        <div class="form-group <c:if test='${not empty errors["city"]}'>has-error</c:if>">
            <label class="col-sm-2 control-label">City</label>
            <div class="col-sm-10">
                <div>
                    <input class="form-control" type="text" id="city" name="city" value="${fn:escapeXml(owner.city)}" />
                </div>
                <c:if test="${not empty errors['city']}">
                    <span class="help-inline">${fn:escapeXml(errors['city'])}</span>
                </c:if>
            </div>
        </div>

        <div class="form-group <c:if test='${not empty errors["telephone"]}'>has-error</c:if>">
            <label class="col-sm-2 control-label">Telephone</label>
            <div class="col-sm-10">
                <div>
                    <input class="form-control" type="text" id="telephone" name="telephone" value="${fn:escapeXml(owner.telephone)}" />
                </div>
                <c:if test="${not empty errors['telephone']}">
                    <span class="help-inline">${fn:escapeXml(errors['telephone'])}</span>
                </c:if>
            </div>
        </div>

    </div>
    <div class="form-group">
        <div class="col-sm-offset-2 col-sm-10">
            <button class="btn btn-primary" type="submit">
                <c:choose>
                    <c:when test="${not empty owner}">Edit Owner</c:when>
                    <c:otherwise>Add Owner</c:otherwise>
                </c:choose>
            </button>
        </div>
    </div>
</form>
<%@ include file="footer.jsp" %>
