<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="spring.examples.tutorial.counter.service.CounterService" %>
<%@ page import="jakarta.inject.Inject" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Counter - A singleton session bean example.</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/default.css" />
</head>
<body>
    <jsp:useBean id="counterService" class="spring.examples.tutorial.counter.service.CounterService" scope="application"/>
    <h1>This page has been accessed <%= counterService.getHits() %> time(s).</h1>
    <p>Hooray!</p>
</body>
</html>
