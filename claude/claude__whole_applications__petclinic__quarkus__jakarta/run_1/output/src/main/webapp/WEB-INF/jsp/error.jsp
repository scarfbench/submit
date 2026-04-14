<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="header.jsp"/>
  <h2>Welcome</h2>
  <div class="row">
      <div class="col-md-12">
        <img class="img-responsive" src="${pageContext.request.contextPath}/images/pets.png"/>
        <h2>Error</h2>
        <p>Something happened...</p>
        <c:if test="${not empty message}">
          <p><c:out value="${message}"/></p>
        </c:if>
      </div>
  </div>
<jsp:include page="footer.jsp"/>
