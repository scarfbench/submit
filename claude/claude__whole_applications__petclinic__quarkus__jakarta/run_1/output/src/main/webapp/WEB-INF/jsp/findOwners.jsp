<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="header.jsp"/>
  <h2>Find Owners</h2>

  <form action="${pageContext.request.contextPath}/app/owners" method="get"
    class="form-horizontal" id="search-owner-form">
    <div class="form-group">
      <div class="control-group" id="lastNameGroup">
        <label class="col-sm-2 control-label">Last Name</label>
        <div class="col-sm-10">
          <input type="text" class="form-control" id="lastName" name="lastName" size="30"
            maxlength="80" />
            <c:if test="${not empty errors}">
            <span class="help-inline">
                <div>
                    <c:forEach var="err" items="${errors}">
                        <p><c:out value="${err}"/></p>
                    </c:forEach>
                </div>
            </span>
            </c:if>
        </div>
      </div>
    </div>
    <div class="form-group">
      <div class="col-sm-offset-2 col-sm-10">
        <button type="submit" class="btn btn-primary">Find Owner</button>
      </div>
    </div>

    <a class="btn btn-primary" href="${pageContext.request.contextPath}/app/owners/new">Add Owner</a>

  </form>
<jsp:include page="footer.jsp"/>
