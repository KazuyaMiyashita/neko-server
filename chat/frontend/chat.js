console.log("chat.js");

var isLogin = false;
var serverAddr = "http://localhost:2200";

var loginOrCreateUserDom = null
var chatDom = null

window.onload = function() {
  loginOrCreateUserDom = document.getElementById("loginOrCreateUser");
  chatDom = document.getElementById("chat");
  checkCurrentSession();
}

function changeIsLogin(_isLogin) {
  isLogin = _isLogin;
  loginOrCreateUserDom.style.display = isLogin ? "none" : "block";
  chatDom.style.display = isLogin ? "block" : "none";
  if (isLogin) {
    console.log("ログインしている");
  } else {
    console.log("ログインしていない");
  }
}

function checkCurrentSession() {
  console.log(document.cookie);
  var xhr = new XMLHttpRequest();
  xhr.onreadystatechange = function() {
    switch (xhr.readyState) {
      case 0:
        break;
      case 1:
        break;
      case 2:
        break;
      case 3:
        break;
      case 4:
        var _isLogin = xhr.status == 200
        changeIsLogin(_isLogin);
        if (xhr.status == 500) {
          alert("something wrong!");
        }
    }
  }
  xhr.open("GET", serverAddr + "/auth/session", false);
  xhr.withCredentials = true;
  xhr.send();
  xhr.abort();
}

function createUser() {
  var json = null;
  try {
    json = JSON.stringify({
      screenName: document.createUserForm.screenName.value,
      loginName: document.createUserForm.loginName.value,
      password: document.createUserForm.password.value
    });
  } catch {
    console.log("データが揃ってないよん");
    return false;
  }

  var xhr = new XMLHttpRequest();
  xhr.onreadystatechange = function() {
    switch (xhr.readyState) {
      case 0:
        break;
      case 1:
        break;
      case 2:
        break;
      case 3:
        break;
      case 4:
        // ログイン処理
        console.log(xhr.repsonse);
        if (xhr.status == 500) {
          alert("something wrong!");
        }
    }
  }
  xhr.open("POST", serverAddr + "/users", false);
  xhr.send(json);
  xhr.abort();

  return false;
}

function login() {
  var json = null;
  try {
    json = JSON.stringify({
      loginName: document.loginForm.loginName.value,
      password: document.loginForm.password.value
    });
  } catch {
    console.log("データが揃ってないよん");
    return false;
  }

  console.log(json);

  var xhr = new XMLHttpRequest();
  xhr.onreadystatechange = function() {
    switch (xhr.readyState) {
      case 0:
        break;
      case 1:
        break;
      case 2:
        break;
      case 3:
        break;
      case 4:
        var _isLogin = xhr.status == 200
        changeIsLogin(_isLogin);
        if (xhr.status == 500) {
          alert("something wrong!");
        }
    }
  }
  xhr.open("POST", serverAddr + "/auth/login", false);
  xhr.send(json);
  xhr.abort();

  return false;
}

function logout() {
  var xhr = new XMLHttpRequest();
  xhr.onreadystatechange = function() {
    switch (xhr.readyState) {
      case 0:
        break;
      case 1:
        break;
      case 2:
        break;
      case 3:
        break;
      case 4:
        changeIsLogin(false);
        if (xhr.status == 500) {
          alert("something wrong!");
        }
    }
  }
  xhr.open("POST", serverAddr + "/auth/logout", false);
  xhr.send();
  xhr.abort();

  return false;
}
