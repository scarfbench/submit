<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="header.jsp"/>
<h2>Owner</h2>
<form class="form-horizontal" id="add-owner-form" method="post">
    <div class="form-group has-feedback">

        <div class="form-group <c:if test='${not empty errors["firstName"]}'>has-error</c:if>">
            <label class="col-sm-2 control-label">Name</label>
            <div class="col-sm-10">
                <div>
                    <input class="form-control" type="text" id="firstName" name="firstName" value="<c:out value='${owner.firstName}' default=''/>" />
                </div>
                <c:if test="${not empty errors['firstName']}">
                    <span class="help-inline"><c:out value="${errors['firstName']}"/></span>
                </c:if>
            </div>
        </div>

        <div class="form-group <c:if test='${not empty errors["lastName"]}'>has-error</c:if>">
            <label class="col-sm-2 control-label">Last Name</label>
            <div class="col-sm-10">
                <div>
                    <input class="form-control" type="text" id="lastName" name="lastName" value="<c:out value='${owner.lastName}' default=''/>" />
                </div>
                <c:if test="${not empty errors['lastName']}">
                    <span class="help-inline"><c:out value="${errors['lastName']}"/></span>
                </c:if>
            </div>
        </div>

        <div class="form-group <c:if test='${not empty errors["address"]}'>has-error</c:if>">
            <label class="col-sm-2 control-label">Address</label>
            <div class="col-sm-10">
                <div>
                    <input class="form-control" type="text" id="address" name="address" value="<c:out value='${owner.address}' default=''/>" />
                </div>
                <c:if test="${not empty errors['address']}">
                    <span class="help-inline"><c:out value="${errors['address']}"/></span>
                </c:if>
            </div>
        </div>

        <div class="form-group <c:if test='${not empty errors["city"]}'>has-error</c:if>">
            <label class="col-sm-2 control-label">City</label>
            <div class="col-sm-10">
                <div>
                    <input class="form-control" type="text" id="city" name="city" value="<c:out value='${owner.city}' default=''/>" />
                </div>
                <c:if test="${not empty errors['city']}">
                    <span class="help-inline"><c:out value="${errors['city']}"/></span>
                </c:if>
            </div>
        </div>

        <div class="form-group <c:if test='${not empty errors["telephone"]}'>has-error</c:if>">
            <label class="col-sm-2 control-label">Telephone</label>
            <div class="col-sm-10">
                <div>
                    <input class="form-control" type="text" id="telephone" name="telephone" value="<c:out value='${owner.telephone}' default=''/>" />
                </div>
                <c:if test="${not empty errors['telephone']}">
                    <span class="help-inline"><c:out value="${errors['telephone']}"/></span>
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
<jsp:include page="footer.jsp"/>
