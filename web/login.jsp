<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<title>Log In</title>
</head>
<body>
<form method="POST" action="<%=response.encodeURL("j_security_check")%>">
<table>
<tr>
<td>Username:</td>
<td><input type="text" name="j_username"></td>
</tr>
<tr>
<td>Password:</td>
<td><input type="password" name="j_password"></td>
</tr>
</table>
<input type="submit" value="Log In"></form>
</body>
</html>