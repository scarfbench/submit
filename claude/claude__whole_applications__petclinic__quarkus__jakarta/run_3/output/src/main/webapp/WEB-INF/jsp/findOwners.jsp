<%@ include file="header.jsp" %>
<h2>Find Owners</h2>

<form action="${pageContext.request.contextPath}/owners" method="get"
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
                      <p>${fn:escapeXml(err)}</p>
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

  <a class="btn btn-primary" href="${pageContext.request.contextPath}/owners/new">Add Owner</a>

</form>

<%@ include file="footer.jsp" %>
