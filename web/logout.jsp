<%-------------------------------------------------------------------%>
<%-- Copyright 2013 Code Strategies                                --%>
<%-- This code may be freely used and distributed in any project.  --%>
<%-- However, please do not remove this credit if you publish this --%>
<%-- code in paper or electronic form, such as on a web site.      --%>
<%-------------------------------------------------------------------%>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta charset="utf-8"><meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	
	<meta name="description" content="">
	<meta name="author" content="JSI">
	<link rel="shortcut icon" href="img/favicon.ico" type="image/x-icon">
	<link rel="icon" href="img/favicon.ico" type="image/x-icon">

    <title>Log Out</title>
		
    <!-- Bootstrap Core CSS -->
    <link href="css/bootstrap.min.css" rel="stylesheet">
    <!-- Custom Fonts -->
    <link rel="stylesheet" href="css/font-awesome.min.css">
    <link href="css/googlefonts.css" rel="stylesheet" type="text/css">

	<style>.container input { margin: 5px 0; }</style>
</head>

<body>

<div class="alert alert-info" style="width: 300px; margin: 50px;">

<%@ page session="true"%>
User '<%=request.getRemoteUser()%>' has been logged out.
<% session.invalidate(); %>
<br/><br/>
<a href="ui/admin/index.html">Click here to log in</a>

</div>

</body>
</html>