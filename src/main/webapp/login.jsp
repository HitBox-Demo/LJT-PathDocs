<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1,viewport-fit=cover">
    <meta name="theme-color" content="#0297F1">
    <title>Sign In - LJT RouteFlow</title>
    <link rel="manifest" href="${ctx}/manifest.json">
    <link rel="icon" href="${ctx}/assets/icons/favicon-32.png">
    <link rel="stylesheet" href="${ctx}/assets/css/app.css">
</head>
<body class="login-body" data-context="${ctx}">
<div class="login-grid">
    <main class="login-panel">
        <section class="login-card" aria-labelledby="login-title">
            <div class="brand brand-login">
                <span class="brand-mark" aria-hidden="true">LJT</span>
                <div><strong>LJT RouteFlow</strong><small>Document Management System</small></div>
            </div>
            <h1 id="login-title">Sign in to your account</h1>
            <p class="muted">Capture, approve and route office documents securely.</p>

            <c:if test="${param.expired == '1'}"><div class="alert alert-warning">Your session expired. Please sign in again.</div></c:if>
            <c:if test="${param.logout == '1'}"><div class="alert alert-success">You have been signed out.</div></c:if>
            <c:if test="${not empty error}"><div class="alert alert-error"><c:out value="${error}"/></div></c:if>

            <form method="post" action="${ctx}/login" class="stack-form">
                <label>Username
                    <input name="username" autocomplete="username" required autofocus placeholder="e.g. clerk">
                </label>
                <label>Password
                    <input type="password" name="password" autocomplete="current-password" required placeholder="Enter password">
                </label>
                <button class="btn btn-primary btn-block" type="submit">Sign In</button>
            </form>

            <c:if test="${demoMode}">
                <details class="demo-box" open>
                    <summary>Demo accounts</summary>
                    <div class="demo-grid">
                        <button type="button" data-demo-user="clerk" data-demo-password="Clerk@123">Clerk/Admin</button>
                        <button type="button" data-demo-user="boss" data-demo-password="Boss@123">Boss</button>
                        <button type="button" data-demo-user="it.user" data-demo-password="Dept@123">IT User</button>
                    </div>
                    <small>Demo records reset when Tomcat restarts.</small>
                </details>
            </c:if>
            <p class="login-foot">Forgot password? Contact the System Administrator.</p>
        </section>
    </main>
    <aside class="login-info">
        <div>
            <h2>From hardcopy to the right department</h2>
            <article><span>1</span><div><strong>Capture and digitise</strong><p>Choose document images from your device or upload an existing PDF.</p></div></article>
            <article><span>2</span><div><strong>Boss approval checkpoint</strong><p>Approve, reject or redirect before routing.</p></div></article>
            <article><span>3</span><div><strong>Department repository</strong><p>IT, Finance and Management see only authorised records.</p></div></article>
        </div>
    </aside>
</div>
<script src="${ctx}/assets/js/app.js" defer></script>
</body>
</html>
