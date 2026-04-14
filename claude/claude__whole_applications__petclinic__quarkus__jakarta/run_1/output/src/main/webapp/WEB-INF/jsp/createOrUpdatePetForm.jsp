<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="header.jsp"/>
<h2>
    <c:if test="${empty pet}">New </c:if>
    Pet
</h2>
<form class="form-horizontal" method="post">
    <input type="hidden" name="id" value="" />
    <div class="form-group has-feedback">
      <div class="form-group">
        <label class="col-sm-2 control-label">Owner</label>
        <div class="col-sm-10">
          <span><c:out value="${owner.firstName}"/></span>
        </div>
      </div>

      <div class="form-group <c:if test='${not empty errors["name"]}'>has-error</c:if>">
        <label class="col-sm-2 control-label">Name</label>
        <div class="col-sm-10">
            <div>
                <input class="form-control" type="text" id="name" name="name" value="<c:out value='${pet.name}' default=''/>"/>
            </div>
            <c:if test="${not empty errors['name']}">
                <span class="help-inline"><c:out value="${errors['name']}"/></span>
            </c:if>
        </div>
      </div>

      <div class="form-group <c:if test='${not empty errors["birthDate"]}'>has-error</c:if>">
        <label class="col-sm-2 control-label">Birthdate</label>
        <div class="col-sm-10">
            <div>
                <input class="form-control" type="text" placeholder="YYYY-MM-DD" title="Enter a date in this format: YYYY-MM-DD" id="birthDate" name="birthDate" value="<c:out value='${pet.birthDate}' default=''/>"/>
            </div>
            <c:if test="${not empty errors['birthDate']}">
                <span class="help-inline"><c:out value="${errors['birthDate']}"/></span>
            </c:if>
        </div>
      </div>

      <div class="form-group">
        <label class="col-sm-2 control-label">Type</label>
        <div class="col-sm-10">
          <select id="type" name="type">
            <c:forEach var="petType" items="${petTypes}">
            <option value="${petType.name}" <c:if test="${not empty pet and not empty pet.type and petType.name == pet.type.name}">selected</c:if>><c:out value="${petType.name}"/></option>
            </c:forEach>
          </select>
        </div>
      </div>

    </div>
    <div class="form-group">
      <div class="col-sm-offset-2 col-sm-10">
        <button class="btn btn-primary" type="submit">
          <c:choose>
            <c:when test="${not empty pet}">Edit Pet</c:when>
            <c:otherwise>Add Pet</c:otherwise>
          </c:choose>
        </button>
      </div>
    </div>
</form>
<jsp:include page="footer.jsp"/>
