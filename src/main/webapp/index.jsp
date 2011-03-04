<html>
<head>
<script type="text/javascript">
	var rpxJsHost = (("https:" == document.location.protocol) ? "https://"
			: "http://static.");
	document
			.write(unescape("%3Cscript src='"
					+ rpxJsHost
					+ "rpxnow.com/js/lib/rpx.js' type='text/javascript'%3E%3C/script%3E"));
</script>
<script type="text/javascript">
	RPXNOW.overlay = true;
	RPXNOW.language_preference = 'en';
</script>
</head>
<body>
<a class="rpxnow" onclick="return false;"
href="https://hello-janrain.rpxnow.com/openid/v2/signin?token_url=http%3A%2F%2Flocalhost%3A8080%2Fauth"> Sign In </a><br>
<%if(request.getParameter("error") != null){%>
<%=request.getParameter("error")%>
<%}%>
</body>
</html>
