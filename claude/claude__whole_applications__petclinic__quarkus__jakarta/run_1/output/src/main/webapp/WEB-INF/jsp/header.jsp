<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>PetClinic :: a Jakarta EE Framework demonstration</title>
  <link rel="shortcut icon" type="image/x-icon" href="${pageContext.request.contextPath}/images/favicon.ico">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/webjars/font-awesome/4.7.0/css/font-awesome.min.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/petclinic.css" />
</head>
<body>
  <nav class="navbar navbar-expand-lg navbar-dark" role="navigation">
    <div class="container">
      <div class="navbar-header">
        <a class="navbar-brand" href="${pageContext.request.contextPath}/app/"><span></span></a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#main-navbar">
          <span class="navbar-toggler-icon"></span>
        </button>
      </div>
      <div class="collapse navbar-collapse" id="main-navbar" style>
        <ul class="nav navbar-nav me-auto">
          <li class="active">
            <a href="${pageContext.request.contextPath}/app/" title="home page">
              <span class="fa fa-home" aria-hidden="true"></span>
              <span>Home</span>
            </a>
          </li>
          <li>
            <a href="${pageContext.request.contextPath}/app/owners/find" title="find owners">
              <span class="fa fa-search" aria-hidden="true"></span>
              <span>Find Owners</span>
            </a>
          </li>
          <li>
            <a href="${pageContext.request.contextPath}/app/vets.html" title="veterinarians">
              <span class="fa fa-th-list" aria-hidden="true"></span>
              <span>Veterinarians</span>
            </a>
          </li>
          <li>
            <a href="${pageContext.request.contextPath}/app/oups" title="trigger a RuntimeException to see how it is handled">
              <span class="fa exclamation-triangle" aria-hidden="true"></span>
              <span>Error</span>
            </a>
          </li>
        </ul>
      </div>
    </div>
  </nav>
  <div class="container-fluid">
    <div class="container xd-container">
