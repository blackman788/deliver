<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="stylesheet" href="${request.contextPath}/static/css/style.css" />
    <title>响应式登录页面</title>
</head>
<body>
<div class="container">
    <div class="forms-container">
        <div class="signin-signup">
            <form id="loginForm" action="#" onsubmit="performLogin(event)" class="sign-in-form">
                <h2 class="title">登录</h2>
                <div class="input-field">
                    <i class="fas fa-user"></i>
                    <input type="text" name="username" placeholder="用户名" value="user" maxlength="50" >
                </div>
                <div class="input-field">
                    <i class="fas fa-lock"></i>
                    <input type="password" name="password" class="form-control" placeholder="密码" value="123456" maxlength="50" >
                </div>
                <input type="submit" value="立即登录" class="btn solid" />
            </form>
            <form action="${request.contextPath}/register" class="sign-up-form">
                <h2 class="title">注册</h2>
                <div class="input-field">
                    <i class="fas fa-user"></i>
                    <input type="text" name="username2r" placeholder="用户名"  maxlength="50" >
                </div>
                <div class="input-field">
                    <i class="fas fa-envelope"></i>
                    <input type="password" name="password2r" class="form-control" placeholder="密码" maxlength="50" >
                </div>
                <input type="submit" class="btn" value="立即注册" />
            </form>
        </div>
    </div>

    <div class="panels-container">
        <div class="panel left-panel">
            <div class="content">
                <h3>加入我们</h3>
                <p>
                    加入我们，成为本站的一份子。
                </p>
                <button class="btn transparent" id="sign-up-btn">
                    去注册
                </button>
            </div>
            <img src="${request.contextPath}/static/img/log.svg" class="image" alt="" />
        </div>
        <div class="panel right-panel">
            <div class="content">
                <h3>已有帐号？</h3>
                <p>
                    立即登录已有帐号，享受独家权益。
                </p>
                <button class="btn transparent" id="sign-in-btn">
                    去登录
                </button>
            </div>
            <img src="${request.contextPath}/static/img/register.svg" class="image" alt="" />
        </div>
    </div>
</div>

<script>
function performLogin(event) {
    event.preventDefault();
    var form = document.getElementById('loginForm');
    var username = form.username.value;
    var password = form.password.value;

    fetch('${request.contextPath}/app/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username: username, password: password })
    })
    .then(response => response.json())
    .then(data => {
        if (data.code === 200) {
            // 处理成功登录后的逻辑
            window.location.href = data.data.redirectUrl;
        } else {
            // 处理登录失败后的逻辑
            alert('登录失败: ' + data.msg);
        }
    })
    .catch(error => {
        console.error('Error:', error);
    });
}
</script>
<script src="${request.contextPath}/static/js/app.js"></script>
</body>
</html>